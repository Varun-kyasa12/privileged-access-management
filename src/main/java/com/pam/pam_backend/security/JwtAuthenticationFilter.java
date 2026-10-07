package com.pam.pam_backend.security;
import java.io.IOException;
import java.util.List;
import com.pam.pam_backend.repository.UserRepository;
import com.pam.pam_backend.exception.ApiException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
public class JwtAuthenticationFilter extends OncePerRequestFilter {
 private final JwtService jwt; private final UserRepository users;
 public JwtAuthenticationFilter(JwtService jwt,UserRepository users){this.jwt=jwt;this.users=users;}
 @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)throws ServletException,IOException{
  String header=request.getHeader("Authorization");
  if(header!=null){
   try{
    if(!header.startsWith("Bearer "))throw new ApiException(401,"Invalid token");
    var claims=jwt.parse(header.substring(7),"access");
    var user=users.findById(claims.get("userId",Long.class)).orElseThrow(()->new ApiException(401,"Invalid token"));
    if(!"ACTIVE".equals(user.getStatus()) || !user.getEmail().equals(claims.getSubject()) || !Boolean.TRUE.equals(claims.get("mfa",Boolean.class)))
     throw new ApiException(401,"Invalid token");
    var context=SecurityContextHolder.createEmptyContext();
    context.setAuthentication(new UsernamePasswordAuthenticationToken(user,null,List.of()));
    SecurityContextHolder.setContext(context);
   }catch(ApiException e){
    SecurityContextHolder.clearContext();response.setStatus(401);response.setContentType("application/json");
    response.getWriter().write("{\"success\":false,\"message\":\"Invalid or expired token\"}");return;
   }
  }
  chain.doFilter(request,response);
 }
}
