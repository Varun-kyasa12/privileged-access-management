package com.pam.pam_backend.config;
import java.util.List;
import com.pam.pam_backend.repository.UserRepository;
import com.pam.pam_backend.security.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
@Configuration
public class SecurityConfig {
 @Bean public org.springframework.security.core.userdetails.UserDetailsService userDetailsService(){return username->{throw new org.springframework.security.core.userdetails.UsernameNotFoundException("Use SafeAccess MFA authentication");};}
 @Bean public PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
 @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http,JwtService jwt,UserRepository users)throws Exception{
  http.csrf(c->c.disable()).cors(c->{}).formLogin(c->c.disable()).httpBasic(c->c.disable())
   .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(a->a.requestMatchers(HttpMethod.POST,"/auth/register","/auth/login","/auth/send-otp","/auth/verify-otp").permitAll()
     .requestMatchers(HttpMethod.GET,"/hello").permitAll().anyRequest().authenticated())
   .exceptionHandling(e->e.authenticationEntryPoint((q,r,x)->{r.setStatus(401);r.setContentType("application/json");r.getWriter().write("{\"success\":false,\"message\":\"Authentication required\"}");})
     .accessDeniedHandler((q,r,x)->{r.setStatus(403);r.setContentType("application/json");r.getWriter().write("{\"success\":false,\"message\":\"Access denied\"}");}))
   .addFilterBefore(new JwtAuthenticationFilter(jwt,users),UsernamePasswordAuthenticationFilter.class);
  return http.build();
 }
 @Bean public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors-origins:http://localhost:5173}") String origins){
  CorsConfiguration c=new CorsConfiguration();c.setAllowedOrigins(List.of(origins.split(",")));
  c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type"));
  c.setAllowCredentials(false);var source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/**",c);return source;
 }
}
