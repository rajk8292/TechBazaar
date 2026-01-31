package com.app.TechBazaar.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.TechBazaar.DTO.UserDTO;
import com.app.TechBazaar.Repository.UserRepository;
import com.app.TechBazaar.Service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
	
	@Autowired
	private UserService userservice;
	
	@Autowired
	private UserRepository userRepo;
	
	@GetMapping("/")
	public String ShowIndex()
	{
		return "index";
	}
	@GetMapping("/Login")
	public String ShowLogin()
	{
		return "Login";
	}
	@GetMapping("/Orders")
	public String ShowOrders()
	{
		return "Orders";
	}
	
	@GetMapping("/Products")
	public String ShowProduct()
	{
		return "Products";
	}
	
	@GetMapping("/Services")
	public String ShowServices()
	{
		return "Services";
	}
	@GetMapping("/ContactUs")
	public String ShowContactUs()
	{
		return "ContactUs";
	}
	@GetMapping("/AboutUs")
	public String ShowAboutUs()
	{
		return "AboutUs";
	}
    @GetMapping("/Register")
	public String ShowRegister(Model model)
	{
    	model.addAttribute("dto", new UserDTO());
		return "Register";
	}
    
    @PostMapping("/Register")
    public String Register(@ModelAttribute("dto") UserDTO dto, HttpSession session)
    {
    	try {
    		userservice.saveUserBuyer(dto);
    		session.setAttribute("email", dto.getEmail());
    		return "redirect:/verify-otp";
    	} catch(Exception e) {
    		return "redirect:Register";
    	}
    }
    
    @GetMapping("/verify-otp")
    public String ShowVerifyOTP(HttpSession session)
    {
    	if(session.getAttribute("email") == null) {
    		return "redirect:Register";
    	}
    	return "VerifyOTP";
    }
    @PostMapping("/verify-otp")
    public String VerifyRegisterOTP(@RequestParam("otp") String otp, HttpSession session, RedirectAttributes attributes)
    {
    	try {
    		String email = (String) session.getAttribute("email");
    		if(!userservice.verifyOTP(email, otp)) {
    			attributes.addFlashAttribute("msg", "Invalid or Expired OTP");
    			return "redirect:/verify-otp";
    		}
    		session.removeAttribute("email");
    		attributes.addFlashAttribute("msg", "OTP Verification Successfull, Registration Completed");
    		return "redirect:/Login";
    	} catch(Exception e) {
    		attributes.addFlashAttribute("msg", e.getMessage());
    		return "redirect:/verify-otp";
    	}
    }

}
