package com.pam.pam_backend.exception;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(ApiException.class) public ResponseEntity<?> api(ApiException e){return error(e.getStatus(),e.getMessage());}
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<?> validation(MethodArgumentNotValidException e){
 return error(400,e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).sorted().findFirst().orElse("Invalid request"));}
 @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class}) public ResponseEntity<?> malformed(Exception e){return error(400,"Invalid request body or parameter");}
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<?> conflict(Exception e){return error(409,"A record with these details already exists or is referenced");}
 @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class) public ResponseEntity<?> missing(Exception e){return error(404,"Resource not found");}
 @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class) public ResponseEntity<?> method(Exception e){return error(405,"Method not allowed");}
 @ExceptionHandler(Exception.class) public ResponseEntity<?> unexpected(Exception e){
 org.slf4j.LoggerFactory.getLogger(getClass()).error("SafeAccess request failed: {}",e.getClass().getSimpleName());
 return error(500,"The request could not be completed");}
 private ResponseEntity<?> error(int status,String message){return ResponseEntity.status(status).body(Map.of("success",false,"message",message));}
}
