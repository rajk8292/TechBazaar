package com.app.TechBazaar.Controller;


import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.app.TechBazaar.DTO.EnquiryDTO;
import com.app.TechBazaar.DTO.FeedbackDTO;
import com.app.TechBazaar.DTO.SavedAddressDTO;
import com.app.TechBazaar.DTO.UserDTO;
import com.app.TechBazaar.Model.CartItem;
import com.app.TechBazaar.Model.Feedback;
import com.app.TechBazaar.Model.Orders;
import com.app.TechBazaar.Model.ProductCategory;
import com.app.TechBazaar.Model.Products;
import com.app.TechBazaar.Model.SavedAddress;
import com.app.TechBazaar.Model.Users;
import com.app.TechBazaar.Model.Users.UserRole;
import com.app.TechBazaar.Model.Users.UserStatus;
import com.app.TechBazaar.Repository.CartItemRepository;
import com.app.TechBazaar.Repository.EnquiryRepository;
import com.app.TechBazaar.Repository.FeedbackRepository;
import com.app.TechBazaar.Repository.OrderRepository;
import com.app.TechBazaar.Repository.ProductCategoryRepository;
import com.app.TechBazaar.Repository.ProductRepository;
import com.app.TechBazaar.Repository.SavedAddressRepository;
import com.app.TechBazaar.Repository.UserRepository;
import com.app.TechBazaar.Service.CartItemService;
import com.app.TechBazaar.Service.EnquiryService;
import com.app.TechBazaar.Service.FeedbackService;
import com.app.TechBazaar.Service.InvoiceService;
import com.app.TechBazaar.Service.OrderService;
import com.app.TechBazaar.Service.ProductService;
import com.app.TechBazaar.Service.SavedAddressService;
import com.app.TechBazaar.Service.UserService;
import com.razorpay.RazorpayException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Controller
public class MainController {
	
	@Autowired
	private UserService userservice;
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private EnquiryService enquiryService;
	
	@Autowired
	private EnquiryRepository enquiryRepo;
	
	@Autowired
	private ProductCategoryRepository categoryRepo;
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private HttpSession session;
	
	@Autowired
	private CartItemService cartItemService;
	
	@Autowired
	private CartItemRepository cartItemRepo;
	
	@Autowired
	private SavedAddressRepository savedAddressRepo;
	
	@Autowired
	private SavedAddressService savedAddressService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private OrderRepository orderRepo;
	
	@Autowired
	private FeedbackRepository feedbackRepo;
	
	@Autowired
	private FeedbackService feedbackService;
	
	@Autowired
	private ProductService productService;
	
	@Autowired
	private InvoiceService invoiceService;
	
	@Controller
	public class HomeController {

	    

	    @GetMapping("/")
	    public String index(Model model) {

	        List<Products> topProducts = productService.getTop6Products();
	        model.addAttribute("products", topProducts);
	        return "index";
	    }
	}
	
	@GetMapping("/Login")
	public String ShowLogin(Model model)
	{
		model.addAttribute("dto", new UserDTO());
		return "Login";
	}
	
	@PostMapping("/Login")
	public String UserLogin(@ModelAttribute("dto") UserDTO dto, RedirectAttributes attributes, HttpSession session)
	{
		try {
			if(!userRepo.existsByEmailAndIsVerified(dto.getEmail(), true)) {
				attributes.addFlashAttribute("msg","User not found");
				return "redirect:/Login";
			}
			
		
			Users user = userRepo.findByEmail(dto.getEmail());
			if(!user.getPassword().equals(dto.getPassword())) {
				attributes.addFlashAttribute("msg", "Invalid user id or Password");
				return "redirect:/Login";
			}
			if(user.getUserStatus().equals(UserStatus.UNBLOCKED)) {
				if(user.getUserRole().equals(UserRole.ADMIN)) {
					session.setAttribute("loggedInAdmin", user);
					return "redirect:/Admin/Dashboard";
				} else if(user.getUserRole().equals(UserRole.SELLER)) {
					session.setAttribute("loggedInSeller", user);
					return "redirect:/Seller/Dashboard";
				} else if(user.getUserRole().equals(UserRole.BUYER)) {
					cartItemService.mergeGuestCartToUser(session, user);
					session.setAttribute("loggedInUser", user);
					return "redirect:/";
				}
			} else {
				attributes.addFlashAttribute("msg", "Login Disabled, Please contact Administration");
			}
			
		} catch(Exception e) {
			attributes.addFlashAttribute("msg",e.getMessage());
		}
		return "redirect:/Login";
	}
	
	
	@GetMapping("/Orders")
	public String ShowOrders(Model model)
	{
		if(session.getAttribute("loggedInUser")==null) {
			return "redirect:Login";
		}
		Users user = (Users) session.getAttribute("loggedInUser");
		List<Orders> orders = orderRepo.findAllByUser(user);
		model.addAttribute("orders", orders.reversed());
		return "Orders";
	}
	@GetMapping("/{productName}/details/{id}")
	public String showOrderDetails(@PathVariable("productName") String productName, @PathVariable("id") long id,
			Model model) {

		if (session.getAttribute("loggedInUser")==null) {
			return"redirect:/Login";
		}
		System.err.println("Product Name: " + productName);
		System.err.println("Order Id: " + id);

		Orders order = orderRepo.findById(id).orElse(null);

		if (order == null) {
			return "redirect:/Order";
		}

		model.addAttribute("order", order);

		return "ViewOrder";
	}

	@GetMapping("/cancel/order/{id}")
	public String cancelOrder(@PathVariable("id") long id , HttpServletRequest request)
	{
			try {
				orderService.cancelOrder(id);
			} catch (RazorpayException e) {
				
				e.printStackTrace();
			}
		      String referer=  request.getHeader("Referer"); 
				return "redirect:"+referer;
	}
	
	@GetMapping("/return/request/{id}")
	public String ReturnRequest(@PathVariable("id") long id,HttpServletRequest request)
	{ 
		orderService.returnRequest(id);
		return "redirect:"+request.getHeader("Referer");
	}
	
	
	@GetMapping({"/Products","Products/{selectedCategory}"})
	public String ShowProduct(@PathVariable(value="selectedCategory", required = false) String selectedCategory, Model model)
	{
		List<ProductCategory> categories = categoryRepo.findAll();
		model.addAttribute("categories", categories);
		model.addAttribute("selectedCategory", selectedCategory);
		
	
		
		if(selectedCategory!=null) {
			ProductCategory category = categoryRepo.findByCategoryName(selectedCategory);
			List<Products> products = productRepo.findAllByCategoryAndVisibility(category,true);
			model.addAttribute("products", products);
		}
		else {
			List<Products> products = productRepo.findByVisibility(true);
			model.addAttribute("products", products);
		}
		
		return "Products";
	}
	
	@GetMapping("/ViewProduct/{SelectedProduct}/{id}")
	public String ShowViewProduct(@PathVariable("id") long id, Model model)
	{
		Products product = productRepo.findById(id).orElseThrow(()-> new RuntimeException("Products not found"));
		model.addAttribute("product", product);
		return "ViewProduct";
	}
	@PostMapping("/product/addToCart/{id}")
	@ResponseBody
	public Map<String, Object> AddToCart(@PathVariable("id") long id, RedirectAttributes attributes)
	{
		try {
			
			Users user = (Users) session.getAttribute("loggedInUser");
			int count = cartItemService.addToCart(id, session, user);
			
			return Map.of(
					"message","Product successfully added into cart",
					"count", count
					);
		} catch(Exception e) {
			return Map.of("message", e.getMessage());
		}
		
	}
	
	@GetMapping("/ViewCart")
	public String ShowViewCart(Model model)
	{
		List<CartItem> cartItems = cartItemService.getCartItems(session);
		
        double totalPrice = cartItems.stream()
        		.mapToDouble(cartItem ->
        		cartItem.getQuantity()*cartItem.getProduct().getPricePerUnit()).sum();
        double finalPrice = cartItems.stream()
        		.mapToDouble(cartItem ->
        		cartItem.getQuantity()*cartItem.getProduct().getFinalPrice()
        		).sum();
        double shippingCharge = cartItems.stream()
        		.mapToDouble(cartItem -> cartItem.getProduct().getShippingCharge()).sum();
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("finalPrice", finalPrice );
		model.addAttribute("shippingCharge", shippingCharge);
		model.addAttribute("cartItems",cartItems);
		
		
		Users user = (Users) session.getAttribute("loggedInUser");
		List<SavedAddress> addresses = savedAddressRepo.findAllByUser(user);
		model.addAttribute("addresses",addresses);
		SavedAddress address = savedAddressRepo.findByUserAndActive(user,true);
		model.addAttribute("address",address);
		return "ViewCart";
	}
	
	@GetMapping("/RemovecartItem/{id}")
	public String RemovecartItem(@PathVariable("id") long cartItemId)
	{
		cartItemRepo.deleteById(cartItemId);
		return "redirect:/ViewCart";
	}
	
	@PostMapping("/UpdateQuantity/{id}")
	@ResponseBody
	public Map<String, Object> UpdateItemQuantity(@PathVariable("id") long cartid, @RequestParam("quantity") int quantity)
	{
		//service se method call hoga
		try {
			cartItemService.updateQuantity(cartid, quantity);
			List<CartItem> cartItems = cartItemService.getCartItems(session);
			 double totalPrice = cartItems.stream()
		        		.mapToDouble(cartItem ->
		        		cartItem.getQuantity()*cartItem.getProduct().getPricePerUnit()).sum();
		        double finalPrice = cartItems.stream()
		        		.mapToDouble(cartItem ->
		        		cartItem.getQuantity()*cartItem.getProduct().getFinalPrice()
		        		).sum();
			        double shippingCharge = cartItems.stream()
		        				.mapToDouble(cartItem -> cartItem.getProduct().getShippingCharge()).sum();
		        return Map.of(
		        		"totalPrice", totalPrice,
		        		"finalPrice", finalPrice,
		        		"shippingCharge", shippingCharge
		        		);
		} catch(Exception e) {
			return Map.of(
					"err", e.getMessage(),
					"quantity", quantity
					);
		}
	}
	
	@PostMapping("/ChangeAddress")
	public String ChangeAddress(@RequestParam("addressId") long addressId,HttpServletRequest request)
	{
		Users user= (Users) session.getAttribute("loggedInUser");
		savedAddressService.changeAddress(user, addressId);
		String referer = request.getHeader("Referer");

	    if (referer != null) {
	        return "redirect:" + referer;
	    } else {
	        return "redirect:/";
	    }
	}
	
	
	@GetMapping("/EditAddress/{id}")
	public String showEditAddress(@PathVariable Long id, Model model) {

	     SavedAddress savedAddress = savedAddressService.getAddressById(id);
	    model.addAttribute("address", savedAddress);

	    return "User/EditAddress";
	}
	@PostMapping("/EditAddress")
	public String updateAddress(@ModelAttribute SavedAddressDTO dto,
	                            RedirectAttributes redirectAttributes) {

	    savedAddressService.updateAddress(dto);
	    redirectAttributes.addFlashAttribute("msg", "Address Updated Successfully!");

	    return "redirect:/User/ManageAddress";
	}
	
	@GetMapping("/DeleteAddress/{id}")
    public String deleteAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        savedAddressService.deleteAddress(id);
        redirectAttributes.addFlashAttribute("msg", "Address deleted successfully!");
        return "redirect:/"; // redirect to user address list or profile page
    }
	
	//checkout, Buy Now and Payment Start from Here
	@GetMapping("/checkout")
	public String ShowCheckout(@RequestParam(required = false) Long productId, @RequestParam(value="qty",required = false, defaultValue = "1") Integer quantity, Model model, RedirectAttributes attributes)
	{
		if(session.getAttribute("loggedInUser")==null) {
			attributes.addFlashAttribute("msg","Please Login first");
			return "redirect:/Login";
		}
		Users user = (Users) session.getAttribute("loggedInUser");
		double totalPrice = 0;
		double finalPrice = 0;
		double shippingCharge = 0;
		boolean cod=true;
		
		if(productId!=null)
		{
			Products buyNowProduct = productRepo.findById(productId).orElse(null);
			totalPrice = buyNowProduct.getPricePerUnit() * quantity;
			finalPrice = buyNowProduct.getFinalPrice() * quantity;
			shippingCharge = buyNowProduct.getShippingCharge();
			model.addAttribute("buyNowProduct", buyNowProduct);
			model.addAttribute("quantity", quantity);
			model.addAttribute("isBuyNow", true);
			model.addAttribute("cod",buyNowProduct.isCodAvailable());
			
			
		} else {
			List<CartItem> cartItems = cartItemRepo.findAllByUser(user);
	   		
	           totalPrice = cartItems.stream()
	   				.mapToDouble(cartItem -> 
	   						cartItem.getQuantity()*cartItem.getProduct().getPricePerUnit()
	   						).sum();
	   		
	   		finalPrice = cartItems.stream()
	   				.mapToDouble(cartItem ->
	   				cartItem.getQuantity()*cartItem.getProduct().getFinalPrice()
	   						).sum();
	   		shippingCharge = cartItems.stream()
	   				.mapToDouble(cartItem -> cartItem.getProduct().getShippingCharge()).sum();
	   		for(CartItem item : cartItems)
	   		{
	   			if(!item.getProduct().isCodAvailable())
	   			{
	   				cod = false;
	   				break;
	   			}
	   		}
	   		model.addAttribute("cod", cod);
	   		model.addAttribute("cartItems", cartItems);
	   		model.addAttribute("isBuyNow", false);
			
		}
        
		model.addAttribute("totalPrice", totalPrice);
		model.addAttribute("finalPrice", finalPrice);
		model.addAttribute("shippingCharge", shippingCharge);
		
		
		List<SavedAddress> addresses = savedAddressRepo.findAllByUser(user);
		model.addAttribute("addresses",addresses);
		
		SavedAddress address = savedAddressRepo.findByUserAndActive(user,true);
		model.addAttribute("address",address);

		
		 return "checkout";
	}
	@PostMapping("/place-order")
	@ResponseBody
	public ResponseEntity<?> placeOrder(@RequestParam String  paymentMethod, @RequestParam(required = false) Long productId, @RequestParam(value = "qty",required = false) Integer quantity)
	{
		try {
			Users user = (Users) session.getAttribute("loggedInUser");
			return ResponseEntity.ok(orderService.checkout(user, productId, quantity, paymentMethod));
		} catch(Exception e) {
			return ResponseEntity.ok(
					Map.of("message",e.getMessage(),
							"status", "FAILED"
				));
			
		}
		
	}
	
	@PostMapping("/verify-payment")
	@ResponseBody
	public ResponseEntity<String> verifyPayment(@RequestBody Map<String, String> paylod)
	{
		try {
			orderService.verifyPayment(
					paylod.get("razorpay_signature"), 
					paylod.get("razorpay_order_id"),
					paylod.get("razorpay_payment_id")
				);
			return ResponseEntity.ok("Payment Verified");
		} catch(Exception e) {
			return ResponseEntity.ok(e.getMessage());
		}
		
	}
	
	
	@GetMapping("/Services")
	public String ShowServices()
	{
		return "Services";
	}
	@GetMapping("/ContactUs")
	public String ShowContactUs(Model model)
	{
		model.addAttribute("dto", new EnquiryDTO());
		return "ContactUs";
	}
	
	@PostMapping("/ContactUs")
    public String ShowContactUs(@ModelAttribute("dto") EnquiryDTO dto, HttpSession session, RedirectAttributes attributes)
    {
		enquiryService.saveEnquiry(dto);
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
    public String Register(@ModelAttribute("dto") UserDTO dto, HttpSession session, RedirectAttributes attributes)
    {
    	try {
    		if(userRepo.existsByEmailAndIsVerified(dto.getEmail(), true)) {
    			attributes.addFlashAttribute("msg", "User Already Exists");
    		}
    		userservice.saveUserBuyer(dto);
    		session.setAttribute("email", dto.getEmail());
    		return "redirect:/verify-otp";
    	} catch(Exception e) {
    		attributes.addFlashAttribute("msg",e.getMessage());
    		return "redirect:Register";
    	}
    }
    
    @GetMapping("/SellerRegistration")
    public String ShowSellerRegister(Model model)
    {
    	model.addAttribute("dto", new UserDTO());
    	return "/SellerRegistration";
    }
    @PostMapping("/SellerRegistration")
    public String SellerRegistration(@ModelAttribute("dto") UserDTO dto, HttpSession session, RedirectAttributes attributes)
    {
    	try {
    		if(userRepo.existsByEmailAndIsVerified(dto.getEmail(), true)) {
    			attributes.addFlashAttribute("msg", "User Already Exists");
    		}
    		userservice.saveSeller(dto);
    		session.setAttribute("email", dto.getEmail());
    		return "redirect:/verify-otp";
    	} catch(Exception e) {
    		attributes.addFlashAttribute("msg",e.getMessage());
    		return "redirect:SellerRegistration";
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
    		//attributes.addFlashAttribute("msg", "OTP Verification Successfull, Registration Completed");
    		return "redirect:/Login";
    	} catch(Exception e) {
    		attributes.addFlashAttribute("msg", e.getMessage());
    		return "redirect:/verify-otp";
    	}
    }
    
    @GetMapping("/ResendOTP")
    public String ResendOTP(HttpSession session, RedirectAttributes attributes)
    {
    	try {
    		String email = (String) session.getAttribute("email");
    		userservice.ResendOTP(email);
    	} catch(Exception e) {
    		attributes.addFlashAttribute("msg",e.getMessage());
    		
    	}
    	return "redirect:/verify-otp";
    }
    
 // Feedback page open karne ke liye
    @GetMapping("/Feedback/{orderId}")
    public String showFeedbackPage(@PathVariable Long orderId, Model model) {

        Orders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        model.addAttribute("order", order);

        return "Feedback";
    }


    // Feedback submit karne ke liye
    @PostMapping("/Feedback")
    public String submitFeedback(
            @ModelAttribute FeedbackDTO dto,
            @RequestParam("orderId") long orderId) {

        Users user = (Users) session.getAttribute("loggedInUser");

        feedbackService.saveFeedback(dto, orderId, user);

        return "redirect:/Orders";
    }
    
    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        return "redirect:/";
    }
    
    
    @GetMapping("/viewProduct/{id}")
    public String viewProduct(@PathVariable Long id, Model model) {

        Products product = productRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product Not Found"));

        model.addAttribute("product", product);

        return "ViewProduct";
    }

    @GetMapping("/invoice/{orderId}")
    public ResponseEntity<InputStreamResource> downloadInvoice(
            @PathVariable Long orderId) {

        Orders order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        ByteArrayInputStream pdf = invoiceService.generateInvoice(order);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=TechBazaar-Invoice.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }
}
