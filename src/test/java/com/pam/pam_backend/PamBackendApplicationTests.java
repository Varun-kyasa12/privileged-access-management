package com.pam.pam_backend;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import com.fasterxml.jackson.databind.*;
import com.pam.pam_backend.entity.*;
import com.pam.pam_backend.repository.*;
import com.pam.pam_backend.security.JwtService;
import com.pam.pam_backend.service.*;
import com.pam.pam_backend.exception.ApiException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.*;
import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts="/test-data.sql",executionPhase=Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PamBackendApplicationTests {
 static final String PASSWORD="Test-only-password!27";
 static final String SECRET=UUID.randomUUID().toString()+UUID.randomUUID();
 @DynamicPropertySource static void secrets(DynamicPropertyRegistry registry){registry.add("jwt.secret",()->SECRET);}
 @Autowired MockMvc mvc;
 @Autowired UserRepository users;
 @Autowired UserRoleRepository userRoles;
 @Autowired MfaCodeRepository codes;
 @Autowired AuditLogRepository audits;
 @Autowired PasswordEncoder encoder;
 @Autowired JwtService jwt;
 @Autowired MfaService mfa;
 @Autowired JdbcTemplate jdbc;
 @MockitoBean OtpDeliveryService delivery;
 @MockitoBean RateLimitService limiter;
 final ObjectMapper json=new ObjectMapper();

 User user(String email,long...roles){
  User u=new User();u.setName("Test User");u.setEmail(email);u.setPasswordHash(encoder.encode(PASSWORD));
  u.setStatus("ACTIVE");u.setCreatedAt(LocalDateTime.now());users.saveAndFlush(u);
  for(long role:roles)userRoles.saveAndFlush(new UserRole(new UserRoleId(u.getId(),role)));
  return u;
 }
 JsonNode response(MvcResult result)throws Exception{return json.readTree(result.getResponse().getContentAsString());}
 MvcResult request(String method,String path,Object body,String token,int status)throws Exception{
  var builder=switch(method){case "POST"->post(path);case "PUT"->put(path);case "DELETE"->delete(path);default->get(path);};
  if(body!=null)builder.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
  if(token!=null)builder.header("Authorization","Bearer "+token);
  MvcResult r=mvc.perform(builder).andReturn();
  assertEquals(status,r.getResponse().getStatus(),method+" "+path+": "+r.getResponse().getContentAsString());
  return r;
 }
 record Challenge(String email,String token,String code){}
 Challenge login(User u)throws Exception{
  reset(delivery);
  JsonNode r=response(request("POST","/auth/login",Map.of("email",u.getEmail(),"password",PASSWORD),null,200));
  assertTrue(r.get("mfaRequired").asBoolean());assertFalse(r.has("token"));
  ArgumentCaptor<String> captor=ArgumentCaptor.forClass(String.class);
  verify(delivery).deliver(eq(u.getEmail()),captor.capture());
  assertTrue(captor.getValue().matches("[0-9]{6}"));
  return new Challenge(u.getEmail(),r.get("challengeToken").asText(),captor.getValue());
 }
 Map<String,String> verification(Challenge c,String code){return Map.of("email",c.email(),"otp",code,"challengeToken",c.token());}
 @Test void contextAndHealth()throws Exception{request("GET","/hello",null,null,200);}
 @Test void registrationAssignsEmployeeAndHidesPassword()throws Exception{
  var body=Map.of("name","New User","email","new@example.test","password",PASSWORD);
  String result=request("POST","/auth/register",body,null,201).getResponse().getContentAsString();
  assertFalse(result.contains("password"));User u=users.findByEmail("new@example.test").orElseThrow();
  assertTrue(encoder.matches(PASSWORD,u.getPasswordHash()));assertEquals(1,userRoles.findByIdUserId(u.getId()).size());
  request("POST","/auth/register",body,null,409);
 }
 @Test void registrationRejectsInvalidAndMultibytePasswords()throws Exception{
  request("POST","/auth/register",Map.of("name","","email","invalid","password","short"),null,400);
  request("POST","/auth/register",Map.of("name","Test","email","test@example.test","password","界".repeat(30)),null,400);
 }
 @Test void badCredentialsAndUnknownUsers()throws Exception{
  User u=user("employee@example.test",1);
  request("POST","/auth/login",Map.of("email",u.getEmail(),"password","wrong-password"),null,401);
  request("POST","/auth/login",Map.of("email","absent@example.test","password",PASSWORD),null,401);
  assertTrue(audits.findAll().stream().anyMatch(a->a.getStatus().equals("DENIED")));
 }
 @Test void finalJwtOnlyAfterMfaAndOtpSingleUse()throws Exception{
  User u=user("employee@example.test",1);Challenge c=login(u);
  request("GET","/hr",null,c.token(),401);
  var row=codes.findTopByUserIdAndUsedFalseOrderByIdDesc(u.getId()).orElseThrow();
  assertNotEquals(c.code(),row.getCode());assertTrue(encoder.matches(c.code(),row.getCode()));
  JsonNode r=response(request("POST","/auth/verify-otp",verification(c,c.code()),null,200));
  String token=r.get("token").asText();request("GET","/auth/me",null,token,200);
  assertTrue(codes.findById(row.getId()).orElseThrow().getUsed());
  request("POST","/auth/verify-otp",verification(c,c.code()),null,400);
 }
 @Test void wrongCodesHavePersistedAttemptLimit()throws Exception{
  User u=user("employee@example.test",1);Challenge c=login(u);String wrong=c.code().equals("000000")?"000001":"000000";
  for(int i=0;i<5;i++)request("POST","/auth/verify-otp",verification(c,wrong),null,400);
  assertEquals(5,codes.findTopByUserIdAndUsedFalseOrderByIdDesc(u.getId()).orElseThrow().getAttemptCount());
  request("POST","/auth/verify-otp",verification(c,c.code()),null,400);
 }
 @Test void expiredCodeRejected()throws Exception{
  User u=user("employee@example.test",1);Challenge c=login(u);
  var row=codes.findTopByUserIdAndUsedFalseOrderByIdDesc(u.getId()).orElseThrow();row.setExpiresAt(LocalDateTime.now().minusSeconds(1));codes.saveAndFlush(row);
  request("POST","/auth/verify-otp",verification(c,c.code()),null,400);
 }
 @Test void challengeRequiredAndBoundToPasswordUser()throws Exception{
  User u=user("employee@example.test",1);Challenge c=login(u);
  request("POST","/auth/send-otp",Map.of("email",u.getEmail()),null,400);
  request("POST","/auth/verify-otp",Map.of("email",u.getEmail(),"otp",c.code()),null,400);
  request("POST","/auth/verify-otp",Map.of("email","absent@example.test","otp",c.code(),"challengeToken",c.token()),null,400);
 }
 @Test void resendThrottledAndOldChallengeReplaced()throws Exception{
  User u=user("employee@example.test",1);Challenge c=login(u);
  request("POST","/auth/send-otp",Map.of("email",u.getEmail(),"challengeToken",c.token()),null,429);
  var row=codes.findTopByUserIdAndUsedFalseOrderByIdDesc(u.getId()).orElseThrow();row.setExpiresAt(LocalDateTime.now().plusMinutes(4));codes.saveAndFlush(row);
  request("POST","/auth/send-otp",Map.of("email",u.getEmail(),"challengeToken",c.token()),null,200);
  request("POST","/auth/verify-otp",verification(c,c.code()),null,400);
 }
 @Test void newLoginReplacesOldCode()throws Exception{
  User u=user("employee@example.test",1);Challenge old=login(u);login(u);
  request("POST","/auth/verify-otp",verification(old,old.code()),null,400);
 }
 @Test void concurrentVerificationIssuesOnlyOneAccessToken()throws Exception{
  User u=user("employee@example.test",1);Challenge c=login(u);
  ExecutorService pool=Executors.newFixedThreadPool(2);
  try{
   Callable<Boolean> call=()->{try{mfa.verify(c.email(),c.code(),c.token());return true;}catch(ApiException e){return false;}};
   List<Future<Boolean>> results=pool.invokeAll(List.of(call,call));
   int count=0;for(var r:results)if(r.get(15,TimeUnit.SECONDS))count++;
   assertEquals(1,count);
  }finally{pool.shutdownNow();}
 }
 @Test void missingInvalidAndExpiredJwt()throws Exception{
  User u=user("employee@example.test",1);
  request("GET","/hr",null,null,401);request("GET","/hr",null,"invalid",401);
  String expired=Jwts.builder().issuer("safeaccess").subject(u.getEmail()).claim("userId",u.getId()).claim("type","access").claim("mfa",true)
    .expiration(Date.from(Instant.now().minusSeconds(1))).signWith(Keys.hmacShaKeyFor(SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8))).compact();
  request("GET","/hr",null,expired,401);
 }
 @Test void inactiveAccountRejectedImmediately()throws Exception{
  User u=user("employee@example.test",1);String token=jwt.generateToken(u);u.setStatus("INACTIVE");users.saveAndFlush(u);
  request("GET","/hr",null,token,401);request("POST","/auth/login",Map.of("email",u.getEmail(),"password",PASSWORD),null,401);
 }
 @ParameterizedTest @CsvSource({"1,/hr,200,403","1,/attendance,200,403","1,/inventory,200,403",
 "2,/hr,200,200","2,/attendance,200,403","2,/inventory,403,403",
 "3,/hr,200,403","3,/attendance,200,403","3,/inventory,200,403",
 "4,/hr,200,200","4,/attendance,200,200","4,/inventory,200,200"})
 void permissionMatrix(long role,String path,int readStatus,int writeStatus)throws Exception{
  String token=jwt.generateToken(user("role@example.test",role));
  request("GET",path,null,token,readStatus);request("POST",path,Map.of(),token,writeStatus);
  assertTrue(audits.count()>=2);
 }
 @Test void multipleRolesUnionAndProfileApplications()throws Exception{
  User u=user("multi@example.test",1,2);String token=jwt.generateToken(u);
  request("POST","/hr",Map.of(),token,200);request("GET","/inventory",null,token,200);
  JsonNode profile=response(request("GET","/auth/me",null,token,200)).get("data");
  assertEquals(2,profile.get("roles").size());assertEquals(3,profile.get("applications").size());
 }
 @Test void managerCannotReadAdminOrAudit()throws Exception{
  String token=jwt.generateToken(user("manager@example.test",3));
  for(String path:List.of("/admin/users","/admin/roles","/roles","/permissions","/audit"))request("GET",path,null,token,403);
  assertTrue(audits.findAll().stream().anyMatch(a->a.getStatus().equals("DENIED")));
 }
 @Test void adminUserCrudAndSoftDelete()throws Exception{
  User admin=user("admin@example.test",4);String token=jwt.generateToken(admin);
  var body=Map.of("name","Managed User","email","managed@example.test","password",PASSWORD,"roleIds",List.of(1));
  JsonNode created=response(request("POST","/admin/users",body,token,201)).get("data");long id=created.get("id").asLong();
  assertFalse(created.has("passwordHash"));
  request("PUT","/admin/users/"+id,Map.of("name","Updated User","email","managed@example.test","status","ACTIVE","roleIds",List.of(2,3)),token,200);
  assertEquals(2,userRoles.findByIdUserId(id).size());
  request("DELETE","/admin/users/"+id,null,token,200);assertEquals("INACTIVE",users.findById(id).orElseThrow().getStatus());
  request("DELETE","/admin/users/"+admin.getId(),null,token,400);
  request("GET","/admin/users",null,token,200);
  request("PUT","/admin/users/999999",Map.of("name","Missing","email","missing@example.test","status","ACTIVE","roleIds",List.of(1)),token,404);
 }
 @Test void adminRolesCatalogAndAudit()throws Exception{
  jdbc.execute("ALTER TABLE roles ALTER COLUMN id RESTART WITH 100");
  String token=jwt.generateToken(user("admin@example.test",4));
  JsonNode role=response(request("POST","/admin/roles",Map.of("name","REVIEWER","permissionIds",List.of(3)),token,201)).get("data");
  request("PUT","/admin/roles/"+role.get("id").asLong(),Map.of("name","REVIEWER","permissionIds",List.of(3,5)),token,200);
  request("PUT","/admin/roles/4",Map.of("name","ADMIN","permissionIds",List.of()),token,400);
  request("GET","/roles",null,token,200);request("GET","/permissions",null,token,200);
  JsonNode result=response(request("GET","/audit",null,token,200)).get("data");
  assertTrue(result.get("totalElements").asLong()>0);request("GET","/audit?size=101",null,token,400);
 }
 @Test void successAndDenialBothInAudit()throws Exception{
  String token=jwt.generateToken(user("employee@example.test",1));request("GET","/hr",null,token,200);request("POST","/hr",Map.of(),token,403);
  assertTrue(audits.findAll().stream().anyMatch(a->a.getStatus().equals("SUCCESS")&&a.getResource().equals("HR")));
  assertTrue(audits.findAll().stream().anyMatch(a->a.getStatus().equals("DENIED")&&a.getResource().equals("HR")));
 }
 @Test void limiterBoundsAttempts(){
  RateLimitService service=new RateLimitService();for(int i=0;i<10;i++)service.check("test");
  assertEquals(429,assertThrows(ApiException.class,()->service.check("test")).getStatus());
 }
}