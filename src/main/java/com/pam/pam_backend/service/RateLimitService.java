package com.pam.pam_backend.service;
import java.util.*;
import org.springframework.stereotype.Service;
import com.pam.pam_backend.exception.ApiException;
@Service
public class RateLimitService {
 private final Map<String,Window> windows=new LinkedHashMap<>();
 private record Window(long start,int count){}
 public synchronized void check(String key){
  long now=System.currentTimeMillis();windows.entrySet().removeIf(e->now-e.getValue().start()>300000);
  Window w=windows.get(key);
  if(w!=null && w.count()>=10)throw new ApiException(429,"Too many attempts; try again in five minutes");
  if(w==null && windows.size()>=10000)throw new ApiException(429,"Authentication is busy; try again later");
  windows.put(key,new Window(w==null?now:w.start(),w==null?1:w.count()+1));
 }
}
