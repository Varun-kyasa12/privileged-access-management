package com.pam.pam_backend.security;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import javax.crypto.SecretKey;
import com.pam.pam_backend.entity.User;
import com.pam.pam_backend.exception.ApiException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
@Service
public class JwtService {
 private final SecretKey key; private final long expiration;
 public JwtService(@Value("${jwt.secret}") String secret,@Value("${jwt.expiration:3600000}") long expiration){
  key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));this.expiration=expiration;
 }
 public String generateToken(User user){return builder(user,"access",expiration).claim("mfa",true).compact();}
 public String challenge(User user,Long codeId){return builder(user,"mfa",300000).claim("codeId",codeId).compact();}
 private JwtBuilder builder(User user,String type,long ttl){
  Instant now=Instant.now();
  return Jwts.builder().issuer("safeaccess").subject(user.getEmail()).claim("userId",user.getId())
   .claim("name",user.getName()).claim("type",type).id(UUID.randomUUID().toString())
   .issuedAt(Date.from(now)).expiration(Date.from(now.plusMillis(ttl))).signWith(key);
 }
 public Claims parse(String token,String type){
  try{
   Claims claims=Jwts.parser().verifyWith(key).requireIssuer("safeaccess").require("type",type).build().parseSignedClaims(token).getPayload();
   if(claims.getExpiration()==null || claims.get("userId",Long.class)==null) throw new ApiException(401,"Invalid or expired token");
   return claims;
  }catch(JwtException|IllegalArgumentException e){throw new ApiException(401,"Invalid or expired token");}
 }
 public long expiresIn(){return expiration/1000;}
}
