package com.pam.pam_backend.service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import com.pam.pam_backend.entity.*;
import com.pam.pam_backend.exception.ApiException;
import com.pam.pam_backend.repository.*;
import com.pam.pam_backend.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
@Service
public class MfaService {
 private final MfaCodeRepository codes; private final UserRepository users; private final JwtService jwt;
 private final PasswordEncoder encoder; private final OtpDeliveryService delivery; private final AuditLogService audit;
 private final SecureRandom random=new SecureRandom();
 public MfaService(MfaCodeRepository codes,UserRepository users,JwtService jwt,PasswordEncoder encoder,OtpDeliveryService delivery,AuditLogService audit){
  this.codes=codes;this.users=users;this.jwt=jwt;this.encoder=encoder;this.delivery=delivery;this.audit=audit;
 }
 @Transactional public Map<String,Object> begin(Long userId){return issue(active(userId));}
 private User active(Long id){
  User user=users.findLockedById(id).orElseThrow(()->new ApiException(400,"Invalid MFA challenge"));
  if(!"ACTIVE".equals(user.getStatus()))throw new ApiException(401,"Account is inactive");return user;
 }
 private Map<String,Object> issue(User user){
  codes.findByUserIdAndUsedFalse(user.getId()).forEach(c->c.setUsed(true));
  String otp=String.format(java.util.Locale.ROOT,"%06d",random.nextInt(1000000));
  MfaCode code=new MfaCode();code.setUserId(user.getId());code.setCode(encoder.encode(otp));
  code.setExpiresAt(LocalDateTime.now().plusMinutes(5));code.setUsed(false);code.setAttemptCount(0);
  codes.saveAndFlush(code);delivery.deliver(user.getEmail(),otp);
  audit.record(user.getId(),"MFA_ISSUED","AUTH","SUCCESS");
  return Map.of("success",true,"message","Enter your verification code","mfaRequired",true,
   "challengeToken",jwt.challenge(user,code.getId()),"expiresIn",300);
 }
 private MfaCode challenge(User user,String token){
  var claims=jwt.parse(token,"mfa");
  if(!user.getId().equals(claims.get("userId",Long.class)) || !user.getEmail().equals(claims.getSubject()))
   throw new ApiException(400,"Invalid MFA challenge");
  MfaCode code=codes.findById(claims.get("codeId",Long.class)).orElseThrow(()->new ApiException(400,"Invalid MFA challenge"));
  if(!code.getUserId().equals(user.getId()) || Boolean.TRUE.equals(code.getUsed()))throw new ApiException(400,"Code has already been used or replaced");
  if(!code.getExpiresAt().isAfter(LocalDateTime.now()))throw new ApiException(400,"Code has expired");
  if(code.getAttemptCount()>=5)throw new ApiException(400,"Too many incorrect codes; sign in again");
  return code;
 }
 private User challengeUser(String email,String token){
  try{
   var claims=jwt.parse(token,"mfa");User user=active(claims.get("userId",Long.class));
   if(!user.getEmail().equalsIgnoreCase(email.trim()))throw new ApiException(400,"Invalid MFA challenge");
   return user;
  }catch(ApiException e){if(e.getStatus()==401)throw new ApiException(400,"Invalid or expired MFA challenge");throw e;}
 }
 @Transactional public Map<String,Object> resend(String email,String token){
  User user=challengeUser(email,token);MfaCode code=challenge(user,token);
  if(code.getExpiresAt().minusMinutes(5).plusSeconds(30).isAfter(LocalDateTime.now()))
   throw new ApiException(429,"Wait 30 seconds before requesting another code");
  return issue(user);
 }
 @Transactional(noRollbackFor=ApiException.class)
 public Map<String,Object> verify(String email,String otp,String token){
  User user=challengeUser(email,token);
  try{
   MfaCode code=challenge(user,token);
   if(!encoder.matches(otp,code.getCode())){
    code.setAttemptCount(code.getAttemptCount()+1);codes.saveAndFlush(code);
    throw new ApiException(400,"Invalid verification code");
   }
   code.setUsed(true);codes.saveAndFlush(code);
   audit.record(user.getId(),"MFA_VERIFIED","AUTH","SUCCESS");
   return Map.of("success",true,"message","Authentication successful","token",jwt.generateToken(user),"expiresIn",jwt.expiresIn());
  }catch(ApiException e){audit.record(user.getId(),"MFA_VERIFY","AUTH","DENIED");throw e;}
 }
}
