package com.app.TechBazaar.API;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.app.TechBazaar.Model.Users;

@Service
public class SendEmailService {
	
	@Autowired
	private JavaMailSender mailSender;
	
	public void sendRegistrationOTP(Users user, String otp)
	{
		String subject ="Registration OTP from TechBazzar";
		
		String message ="Hello,"+user.getName()+"\n Your Registration OTP is "+otp+"\n Enter your OTP to complete your Registration,\n OTP will be expired in next 5 minutes";
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setSubject(subject);
		mailMessage.setText(message);
		mailMessage.setTo(user.getEmail());
		mailSender.send(mailMessage);
	}

}
