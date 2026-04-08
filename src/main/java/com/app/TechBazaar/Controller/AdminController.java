package com.app.TechBazaar.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.misc.TestRig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.TechBazaar.DTO.ProductCategoryDTO;
import com.app.TechBazaar.Model.Enquiry;
import com.app.TechBazaar.Model.Feedback;
import com.app.TechBazaar.Model.Orders;
import com.app.TechBazaar.Model.Orders.OrderStatus;
import com.app.TechBazaar.Model.ProductCategory;
import com.app.TechBazaar.Model.Users;
import com.app.TechBazaar.Model.Users.UserRole;
import com.app.TechBazaar.Model.Users.UserStatus;
import com.app.TechBazaar.Repository.EnquiryRepository;
import com.app.TechBazaar.Repository.FeedbackRepository;
import com.app.TechBazaar.Repository.OrderRepository;
import com.app.TechBazaar.Repository.ProductCategoryRepository;
import com.app.TechBazaar.Repository.ProductRepository;
import com.app.TechBazaar.Repository.UserRepository;
import com.app.TechBazaar.Service.EnquiryService;
import com.app.TechBazaar.Service.ProductCategoryService;
import com.app.TechBazaar.Service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Admin")
public class AdminController {
	
	@Autowired
	private HttpSession session;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private ProductCategoryRepository productCategoryRepo;
	
	@Autowired
	private ProductCategoryService productcategoryService;
	
	@Autowired
	private UserService userService;
	@Autowired
	private EnquiryRepository enquiryRepo;
	
	@Autowired
	private EnquiryService enquiryService;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private OrderRepository orderRepo;
	
	@Autowired
	private FeedbackRepository feedbackRepo;
	
	@GetMapping("Dashboard")
	public String ShowDashboard(Model model) 
	{
		if(session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/Login";
		}
		model.addAttribute("sellerCount", userRepo.countByUserRoleAndUserStatusNot(UserRole.SELLER,UserStatus.DELETED));
		model.addAttribute("buyerCount", userRepo.countByUserRoleAndUserStatusNot(UserRole.BUYER,UserStatus.DELETED));
		model.addAttribute("productCount", productRepo.count());
		model.addAttribute("orderCount", orderRepo.count());
		model.addAttribute("categoryCount", productCategoryRepo.count());
		model.addAttribute("enquiryCount", enquiryRepo.count());
		model.addAttribute("cancelledOrders", orderRepo.countByOrderStatus(OrderStatus.CANCELLED));
		model.addAttribute("confirmedOrders", orderRepo.countByOrderStatus(OrderStatus.CONFIRMED));
		model.addAttribute("deliveredOrders", orderRepo.countByOrderStatus(OrderStatus.DELIVERED));
		
		List<Enquiry> recentEnquiries = enquiryRepo.findTop5ByOrderByEnquiryDateDesc();
		model.addAttribute("recentEnquiries", recentEnquiries);
		
		//Monthly Order Stats count for Chart
		List<Object[]> stats = orderRepo.getMonthlyOrderStats();
		
		Map<Integer, Long> monthCountMap = new HashMap<>();
		
		for(Object[] row : stats) {
			int monthNumber = ((Integer) row[0]).intValue();
			long count = ((Long) row[1]).longValue();
			monthCountMap.put(monthNumber, count);
			
		}
		List<String> orderMonths = new ArrayList<>();
		List<Long> orderCounts = new ArrayList<>();
		
		String monthNames[] = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
		
		for(int i=1;i<monthNames.length;i++)
		{
			orderMonths.add(monthNames[i-1]);
			orderCounts.add(monthCountMap.getOrDefault(i, 0L));
			
		}
		model.addAttribute("orderMonths",orderMonths);
		model.addAttribute("orderCounts", orderCounts);
		return "Admin/Dashboard";
	}
	
	
	@GetMapping("/ManageSellers")
	public String ShowManageSellers(Model model)
	{
		List<Users> sellers = userRepo.findAllByUserRoleAndIsVerifiedAndUserStatusNot(UserRole.SELLER, true,UserStatus.DELETED);
		model.addAttribute("sellers", sellers);
		return "Admin/ManageSellers";
	}
	@GetMapping("UpdateUserStatus/{id}")
	public String UpdateUserStatus(@PathVariable long id,HttpServletRequest request, RedirectAttributes attributes)
	{
		try {
			userService.updateUserStatus(id);
		} catch(Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
			
		}
		String referer = request.getHeader("Referer");
		return "redirect:"+referer;
	}
	
	@GetMapping("/ManageUsers")
	public String ManageUser(Model model)
	{
		List<Users> users = userRepo.findAllByUserRoleAndIsVerifiedAndUserStatusNot(UserRole.BUYER, true,UserStatus.DELETED);
		model.addAttribute("users", users);
		return "Admin/ManageUsers";
	}
	
	@GetMapping("/ViewOrder")
	public String showViewOrder(Model model)
	{
		if (session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/Login";
		}
		List<Orders> orders=orderRepo.findAll();
		model.addAttribute("orders",orders.reversed());
		
		
		
		return "Admin/ViewOrder";
	}
	
	@GetMapping("/AddCategory")
	public String ShowAddCategory(Model model) {
	    if (session.getAttribute("loggedInAdmin") == null) {
	        return "redirect:/Login";
	    }
	    List<ProductCategory> categories = productCategoryRepo.findAll();
	    model.addAttribute("categories", categories);
	    model.addAttribute("dto", new ProductCategory());
	    return "Admin/AddCategory";
	}

	@PostMapping("/AddCategory")
	public String AddCategory(@ModelAttribute("dto") ProductCategoryDTO dto, RedirectAttributes attributes)
	{
		try {
			
			productcategoryService.saveProductCategory(dto);
			attributes.addFlashAttribute("msg",dto.getCategoryName()+"category Successfully saved");
			
		} catch(Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/Admin/AddCategory";
	}
	
	@GetMapping("/CategoryStatus/{id}")
	public String CategoryStatus(@PathVariable long id)
	{
		productcategoryService.updateCategoryStatus(id);
		return "redirect:/Admin/AddCategory";
	}
	
	
	
	@GetMapping("/Enquiry")
	public String Enquiry(Model model, RedirectAttributes attributes)
	{
		if (session.getAttribute("loggedInAdmin") == null) {
	        return "redirect:/Login";
	    }
		List<Enquiry> enquiry = enquiryRepo.findAll();
	    model.addAttribute("enquiry", enquiry);
	    model.addAttribute("dto", new Enquiry());
		return "Admin/Enquiry";
	}
	
	@GetMapping("/UpdateProfilePic")
	public  String ShowUpdateProfilePic()
	{
		if(session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/Login";
		}
		return "Admin/UpdateProfilePic";
	}
	@PostMapping("/UpdateProfilePic")
	public String updateUpdateProfilePic(@RequestParam("profilePic") MultipartFile profilePic, RedirectAttributes attributes)
	{
		try {
			Users admin = (Users) session.getAttribute("loggedInAdmin");
			userService.updateProfilePic(admin, profilePic);
			attributes.addFlashAttribute("msg","profile pic updated successfully");
		} catch(Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
		}
		return "redirect:/Admin/UpdateProfilePic";
	}
	
	
	@GetMapping("/ChangePassword")
	public String ShowChangePassword()
	{
		if(session.getAttribute("loggedInAdmin")==null) {
			return "redirect:/Login";
		}
		return "Admin/ChangePassword";
	}
	
	@PostMapping("/ChangePassword")
	public String ChangePassword(HttpServletRequest request, RedirectAttributes attributes)
	{
		try {
			String  newPassword = request.getParameter("newPass");
			String oldPassword = request.getParameter("oldPass");
			String confirmPassword = request.getParameter("confirmPass");
			
			Users admin = (Users) session.getAttribute("loggedInAdmin");
			
			userService.changePassword(admin, oldPassword, newPassword, confirmPassword);
		   attributes.addFlashAttribute("msg", "Password Successfully Changed");

			return "redirect:/Login";
		} catch(Exception e) {
			attributes.addFlashAttribute("msg", e.getMessage());
			return "redirect:/Admin/ChangePassword";
		}
	}
	@GetMapping("/DeleteUser/{id}")
	public String DeleteUser(@PathVariable long id, HttpServletRequest request, RedirectAttributes attributes )
	{
		try {
			userService.deleteUser(id);
		} catch(Exception e) {
		attributes.addFlashAttribute("msg", e.getMessage());
			
		}
		String referer = request.getHeader("Referer");
		return "redirect:"+referer;
	}
	
	
	@GetMapping("/Feedbacks")
    public String showAllFeedbacks(Model model, HttpSession session) {

        if (session.getAttribute("loggedInAdmin") == null) {
            return "redirect:/Login";
        }

        List<Feedback> feedbackList = feedbackRepo.findAll();

        model.addAttribute("feedbackList", feedbackList);
        
        

        return "Admin/Feedbacks";
    }
	@GetMapping("/deleteFeedback/{id}")
	public String deleteFeedback(@PathVariable Long id,
	                             HttpSession session,
	                             RedirectAttributes attributes) {

	    if (session.getAttribute("loggedInAdmin") == null) {
	        return "redirect:/Login";
	    }

	    feedbackRepo.deleteById(id);

	    attributes.addFlashAttribute("success",
	            "Feedback deleted successfully!");

	    return "redirect:/Admin/Feedbacks";
	}

}
