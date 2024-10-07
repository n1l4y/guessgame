package com.controller;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dto.EmailDto;
import com.dto.LoginDto;
import com.dto.ResetPassDto;
import com.entity.UserEntity;
import com.repository.UserRepository;
import com.service.MailService;
import com.service.OtpService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
public class SessionController {
	
	@Autowired
	PasswordEncoder encoder;
	
	@Autowired
	UserRepository repository;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	OtpService otpService;
	
	@GetMapping("/signup")
	public String signup() {
		return "Signup";
	}
	
	@PostMapping("/signup")
	public String signup(@Validated UserEntity user, BindingResult result, Model model) {
		
		if(result.hasErrors()) {
			model.addAttribute("error", result);
			return "Signup";
		}
		
		String plainPassword = user.getPassword();
		user.setPassword(encoder.encode(plainPassword));
		user.setRole("USER");
		user.setCredits(500);
		
		repository.save(user);
		
		model.addAttribute("message", "User has signed up successfully, please sign in...");
		
		return "Signin";
	}
	
	@GetMapping("/signin")
	public String signin() {
		return "Signin";
	}
	
	@PostMapping("/signin")
	public String signin(@Validated LoginDto login, BindingResult result,Model model) {
		if(result.hasErrors()) {
			model.addAttribute("validationError", result);
			return "Signin";
		}
		
		Optional<UserEntity> searched = repository.findByEmail(login.getEmail());
		if(searched.isEmpty()) {
			
			model.addAttribute("error", "Email is not registered");
			return "Signin";
		}
		
		UserEntity user = searched.get();
		
		if(!encoder.matches(login.getPassword(), user.getPassword())) {
			model.addAttribute("error", "Incorrect password");
			return "Signin";
		}
		
		model.addAttribute("user", user);
		
		if(user.getRole().equalsIgnoreCase("USER")) {
			return "Home";
		} 
		else {
			return "AdminDashboard";
		}
	}
	
	@GetMapping("forgot")
	public String forgotPassword() {
		return "ResetPassword";
	}
	
	@PostMapping("reset")
	public String resetPassword(@Validated EmailDto email, BindingResult result, Model model, HttpServletResponse response) {
		if(result.hasErrors()) {
			model.addAttribute("error", result.getFieldError("email").getDefaultMessage());
			return "ResetPassword";
		}
		Optional<UserEntity> search = repository.findByEmail(email.getEmail());
		if(search.isEmpty()) {
			model.addAttribute("error", "Email is not registered");
			return "ResetPassword";
		}
		UserEntity user = search.get();
		String otp = otpService.generateOtp(6);
		
		mailService.sendMessage(user.getEmail(), otp);
		user.setOtp(otp);		
		repository.save(user);
		Cookie cookie = new Cookie("email", user.getEmail());
		response.addCookie(cookie);
//		model.addAttribute("email", user.getEmail());
		return "redirect:/newpassword";
	}
	@GetMapping("newpassword")
	public String newPassword() {
		return "NewPassword";
	}
	
	@PostMapping("updatepassword")
	public String updatePassword(@Validated ResetPassDto updatedPassword, BindingResult result, Model model) {
		
		System.out.println("email from jsp..." + updatedPassword.getEmail());
		if(result.hasErrors()) {
			model.addAttribute("error", result.getFieldError("password1").getDefaultMessage());
			return "NewPassword";
		}
		
		
		if(!updatedPassword.getPassword1().equals(updatedPassword.getPassword2())) {
			model.addAttribute("passError", "Password do not match!");
			return "NewPassword";
		}
		
		System.out.println("Optional..." + updatedPassword.getEmail());
		Optional<UserEntity> search = repository.findByEmail(updatedPassword.getEmail());
		
		UserEntity user = search.get();
		System.out.println("Email..." + user.getEmail());
		
		if(!user.getOtp().equals(updatedPassword.getOtp())) {
			model.addAttribute("otpError", "Invalid OTP");
			return "NewPassword";
		}
		
		user.setPassword(encoder.encode(updatedPassword.getPassword1()));
		model.addAttribute("message", "Password has been updated!");
		return "Signin";
	}
}
