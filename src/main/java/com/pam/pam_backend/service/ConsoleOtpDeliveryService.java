package com.pam.pam_backend.service;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.pam.pam_backend.exception.ApiException;
@Service
public class ConsoleOtpDeliveryService implements OtpDeliveryService {
 private final boolean enabled;
 public ConsoleOtpDeliveryService(@Value("${mfa.console-enabled:false}") boolean enabled){this.enabled=enabled;}
 public void deliver(String email,String otp){
  if(!enabled)throw new ApiException(503,"OTP delivery is not configured");
  org.slf4j.LoggerFactory.getLogger(getClass()).info("SafeAccess DEMO OTP for {}: {}",email,otp);
 }
}
