package com.pam.pam_backend.service;
/** Delivery boundary: replace the console adapter with an email adapter for deployment. */
public interface OtpDeliveryService { void deliver(String email,String otp); }
