package com.bookstore.Controller;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.bookstore.domain.Book;
import com.bookstore.domain.CartItem;
import com.bookstore.domain.Order;
import com.bookstore.domain.User;
import com.bookstore.domain.UserBilling;
import com.bookstore.domain.UserPayment;
import com.bookstore.domain.UserShipping;
import com.bookstore.domain.security.PasswordResetToken;
import com.bookstore.domain.security.Role;
import com.bookstore.domain.security.UserRole;
import com.bookstore.service.BookService;
import com.bookstore.service.CartItemService;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserPaymentService;
import com.bookstore.service.UserService;
import com.bookstore.service.UserShippingService;
import com.bookstore.service.impl.UserSecurityService;
import com.bookstore.utility.IndiaConstants;
import com.bookstore.utility.MailConstructor;
import com.bookstore.utility.SecurityUtility;

@Controller
public class HomeController {
	
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private UserService userService;
	
	@Autowired 
	private UserDetailsService userDetailsService;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private BookService bookService;
	
	@Autowired
	private UserShippingService userShippingService;
	
	@Autowired
	private MailConstructor mailConstructor;
	
	@Autowired
	private UserPaymentService userPaymentService;
	
	@Autowired
	private UserSecurityService userSecurityService;
	
	@Autowired
	private CartItemService cartItemService;
	
	@Autowired
	private OrderService orderService;
	
	@RequestMapping("/")
	public String index()
	{
		return "index";
	}
	
	@RequestMapping("/myAccount")
	public String myAccount()
	{
		return "myAccount";
	}
	
	@RequestMapping("/login")
	public String login(Model model)
	{
		model.addAttribute("classActiveLogin", true);
		return "myAccount";
	}
	
	@RequestMapping("/forgetPassword")
	public String forgetPassword(
			HttpServletRequest request,
			@ModelAttribute("email") String email,
			Model model)
	{
		
		model.addAttribute("classActiveForgetPassword", true);
		User user = userService.findByEmail(email);
		
		if(user == null)
		{
			model.addAttribute("emailNotExist" , true);
			
			return "myAccount";
		}
		
		String password = SecurityUtility.randomPassword();
		String encryptedPassword = SecurityUtility.passwordEncoder().encode(password);
	
	    user.setPassword(encryptedPassword);
	    
	    Role role = new Role();
	    role.setRoleId(1);
	    role.setName("ROLE_USER");
	    
	    Set<UserRole> userRoles = new HashSet<>();
	    userRoles.add(new UserRole(user, role));
	    
	    userService.save(user);
	    
	    String token = UUID.randomUUID().toString();
	    userService.createPasswordResetTokenForUser(user, token);
	    
	    String appUrl = "http://"+request.getServerName()+":"+ request.getServerPort()
	                   + request.getContextPath();
	    
	    SimpleMailMessage newEmail = mailConstructor.constructorResestTokenEmail(appUrl, request.getLocale(), token, user, password);
	    
	    mailSender.send(newEmail);
	    
	    model.addAttribute("forgetPasswordEmailSent" , true);
	    
	    
		return "myAccount";
	}
	
	@RequestMapping("/myProfile")
	public String myProfile(Model model, Principal principal)
	{
		User user = userService.findByUsername(principal.getName());
		
		model.addAttribute("user", user);
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("orderList" , user.getOrderList());
		
		UserShipping userShipping = new UserShipping();
		model.addAttribute("userShipping", userShipping);
		
		model.addAttribute("listOfCreditCards" , true);
		model.addAttribute("listOfShippingAddresses", true);
		model.addAttribute("listOfCreditCards", true);
	
	
		
		List<String> stateList = IndiaConstants.listOfIndiaStateName;
		Collections.sort(stateList);
		
		model.addAttribute("stateList" , stateList);
		model.addAttribute("classActiveEdit" , true);
		
		return "myProfile";
				
	}
	
	@RequestMapping(value = "/updateUserInfo", method = RequestMethod.POST)
	public String updateUserInfo(
			@ModelAttribute("user") User user,
			@ModelAttribute("newPassword") String newPass,
			Model model, Principal prinipal
			) throws Exception
	{
		User currentUser = userService.findById(user.getId());
		
		if(currentUser == null)
		{
			throw new Exception("User not found");
		}
		
		//Check email already exist	
		if(userService.findByEmail(user.getEmail()) != null && userService.findByEmail(user.getEmail()).getId() != currentUser.getId() )
		{
			model.addAttribute("emailExists", true);
			return "myProfile";
		}
		
		//Check username already exists
		if(userService.findByUsername(user.getUsername()) != null && userService.findByUsername(user.getUsername()).getId() != currentUser.getId() )
		{
			model.addAttribute("usernameExists", true);
			return "myProfile";
		}
		
		
		//update password
		if(newPass != null && !newPass.isEmpty() && !newPass.equals(""))
		{
			BCryptPasswordEncoder passwordEncoder = SecurityUtility.passwordEncoder();
			String dbPassword = currentUser.getPassword();
			if(passwordEncoder.matches(user.getPassword(), dbPassword))
			{
				currentUser.setPassword(passwordEncoder.encode(newPass));
			}
			else 
			{
				model.addAttribute("incorrectPassword", true);
				
				return "myProfile";
			}
		}
		
		currentUser.setUsername(user.getUsername());
		currentUser.setFirstName(user.getFirstName());
		currentUser.setLastName(user.getLastName());
		currentUser.setEmail(user.getEmail());
		
		userService.save(currentUser);
		
		model.addAttribute("updateSuccess", true);
		model.addAttribute("user", user);
		model.addAttribute("classActiveEdit", true);
		
		model.addAttribute("listOfShippingAddresses", true);
		model.addAttribute("listOfCreditCards", true);
		
		UserDetails userDetails = userSecurityService.loadUserByUsername(currentUser.getUsername());
		
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, 
				userDetails.getPassword(), userDetails.getAuthorities());
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		return "myProfile";
	}
	
	@RequestMapping("/hours")
	public String hours()
	{
		return "hours";
	}
	
	@RequestMapping("/faq")
	public String faq()
	{
		return "faq";
	}
	
	@RequestMapping("/listOfCreditCards")
	public String listOfCreditCards(
			Model model, Principal principal,
			HttpServletRequest request)
	{
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("orderList" , user.getOrderList());
		
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveBilling", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		return "myProfile";
		
	}
	
	@RequestMapping("/addNewCreditCard")
	public String addNewCreditCard(Model model, Principal principal,
			HttpServletRequest request)
	{
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		
		model.addAttribute("addNewCreditCard", true);
		model.addAttribute("classActiveBilling" ,true);
		model.addAttribute("listOfShippingAddresses", true);
		
		UserBilling userBilling = new UserBilling();
		UserPayment userPayment = new UserPayment();
		
		model.addAttribute("userBilling" , userBilling);
		model.addAttribute("userPayment", userPayment); 
		
		List<String> stateList = IndiaConstants.listOfIndiaStateName.stream().sorted()
				.collect(Collectors.toList());
		model.addAttribute("stateList" , stateList);
		
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("orderList" , user.getOrderList());
		
		return "myProfile";
		
	}
	
	@RequestMapping(value="/addNewCreditCard", method = RequestMethod.POST)
	public String addNewCreditCard(@ModelAttribute("userPayment") UserPayment userPayment,
			@ModelAttribute("userBilling") UserBilling userBilling, Principal principal,
			HttpServletRequest request, Model model)
	{
		
		User user = userService.findByUsername(principal.getName());
		userService.updateUserBilling(userPayment, userBilling, user);
		
		model.addAttribute("user" , user);
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());

		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveBilling", true);
		model.addAttribute("listOfShippingAddresses", true);
		model.addAttribute("orderList" , user.getOrderList());
		return "myProfile";
	}
	
	@RequestMapping("/removeCreditCard")
	public String deleteCreditCard(
			@ModelAttribute("id") Long creditCardId,
		    Model model, Principal principal)
	{
		User user = userService.findByUsername(principal.getName());
		UserPayment userPayment = userPaymentService.findById(creditCardId);
		
		if(user.getUsername() != userPayment.getUser().getUsername())
		{
			return "badRequest";
		}
		
		userPaymentService.removeById(creditCardId);
		
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveBilling", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("user" , user);
		model.addAttribute("orderList" , user.getOrderList());
		
		return "myProfile";
	}
	
	@RequestMapping("/updateCreditCard")
	public String updateCreditCard(
			@ModelAttribute("id") Long creditCardId,
			Principal principal, Model model
			)
	{
		User user = userService.findByUsername(principal.getName());
		UserPayment userPayment = userPaymentService.findById(creditCardId);
		UserBilling userBilling = userPayment.getUserBilling();
		
		if(user.getId() != userPayment.getUser().getId()) //For security reason as Payment id is unique
		{
			return "badRequest";
		}
		
		model.addAttribute("userPayment" , userPayment);
		model.addAttribute("userBilling" , userBilling);
		model.addAttribute("user" , user); 
		
		List<String> stateList = IndiaConstants.listOfIndiaStateName.stream().sorted()
				.collect(Collectors.toList());
		model.addAttribute("stateList" , stateList);
		
		model.addAttribute("addNewCreditCard", true);
		model.addAttribute("classActiveBilling" ,true);
		model.addAttribute("listOfShippingAddresses", true);
		
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("orderList" , user.getOrderList());
		
		return "myProfile";
	}
	
	@RequestMapping(value =  "/setDefaultShippingAddress", method = RequestMethod.POST)
	public String setDefaultShippingAddress(Principal principal, Model model,
			@ModelAttribute("defaultShippingAddressId") Long id)
	{
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		
		userService.setUserDefaultShipping(id, user);
		
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveShipping", true);
		model.addAttribute("listOfShippingAddresses", true);

		
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("orderList" , user.getOrderList());
		return "myProfile";
	}
	
	@RequestMapping(value =  "/setDefaultPayment", method = RequestMethod.POST)
	public String setDefaultPayment(Principal principal, Model model,
			@ModelAttribute("defaultUserPaymentId") Long id)
	{
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		
		userService.setDefaultPayment(id, user);
		
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveBilling", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("orderList" , user.getOrderList());
		
		return "myProfile";
	}
	
	@RequestMapping("/addNewShippingAddress")
	public String addNewShippingAddress(
			 Model model, Principal principal,
			HttpServletRequest request)
	{
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		
		model.addAttribute("addNewShippingAddress", true);
		model.addAttribute("classActiveShipping" ,true);
		model.addAttribute("listOfCreditCards", true);
		
		UserShipping userShipping = new UserShipping();
		
		model.addAttribute("userShipping" , userShipping);
		
		List<String> stateList = IndiaConstants.listOfIndiaStateName.stream().sorted()
				.collect(Collectors.toList());
		model.addAttribute("stateList" , stateList);
		
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("orderList" , user.getOrderList());
		
		return "myProfile";
		
	}
	
	@RequestMapping("/removeUserShipping")
	public String removeUserShipping(
			@ModelAttribute("id") Long id, Principal principal, Model model,
			HttpServletRequest request)
	{
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		
		UserShipping userShipping = userShippingService.findById(id);
		
		if(user.getId() != userShipping.getUser().getId())
		{
			return "badRequest";
		}
		
		userShippingService.deleteById(id);
		
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveShipping", true);
		model.addAttribute("listOfShippingAddresses", true);
		
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("user" , user);
		model.addAttribute("orderList" , user.getOrderList());
		
		return "myProfile";
		
	}
	
	@RequestMapping("/updateUserShipping")
	public String updateUserShipping(
			@ModelAttribute("id") Long id,
			Model model, Principal principal,
			HttpServletRequest request)
	{
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		
		UserShipping userShipping = userShippingService.findById(id);
		
		if(user.getId() != userShipping.getUser().getId())
		{
			return "badRequest";
		}
		
		model.addAttribute("addNewShippingAddress", true);
		model.addAttribute("classActiveShipping" ,true);
		
		model.addAttribute("userShipping" , userShipping);
		
		List<String> stateList = IndiaConstants.listOfIndiaStateName.stream().sorted()
				.collect(Collectors.toList());
		model.addAttribute("stateList" , stateList);
		
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("orderList" , user.getOrderList());
		
		return "myProfile";
		
	}
	
	
	@RequestMapping(value="/addNewShippingAddress", method = RequestMethod.POST)
	public String addNewShippingAddressPost(
			@ModelAttribute("userShipping") UserShipping userShipping, Model model, Principal principal,
			HttpServletRequest request)
	{
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		
		userService.updateUserShipping(userShipping, user);
		
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveShipping", true);
		model.addAttribute("listOfShippingAddresses", true);

		
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("orderList" , user.getOrderList());
		
		return "myProfile";
		
	}
	
	@RequestMapping("/listOfShippingAddresses")
	public String listOfShippingAddresses(
			Model model, Principal principal,
			HttpServletRequest request)
	{
		User user = userService.findByUsername(principal.getName());
		model.addAttribute("user", user);
		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("orderList" , user.getOrderList());
		
		model.addAttribute("listOfCreditCards", true);
		model.addAttribute("classActiveShipping", true);
		model.addAttribute("listOfShippingAddresses", true);
		model.addAttribute("orderList" , user.getOrderList());
		
		return "myProfile";
		
	}
	
	
	
	@RequestMapping(value="/newUser", method = RequestMethod.POST)
	public String newUserPost(
			HttpServletRequest request,
			@ModelAttribute("email") String userEmail,
			@ModelAttribute("username") String userName,
			Model model) throws Exception
	{
		model.addAttribute("classActiveNewAccount" , true);
		model.addAttribute("email" , userEmail);
		model.addAttribute("username" , userName);
		
		if(userService.findByUsername(userName) != null)
		{
			model.addAttribute("usernameExist" , true);
			
			return "myAccount";
		}
		
		if(userService.findByEmail(userEmail) != null)
		{
			model.addAttribute("emailExist" , true);
			
			return "myAccount";
		}
		
		User user = new User();
		
		user.setUsername(userName);
		user.setEmail(userEmail);
		
		String password = SecurityUtility.randomPassword();
		String encryptedPassword = SecurityUtility.passwordEncoder().encode(password);
	
	    user.setPassword(encryptedPassword);
	    
	    Role role = new Role();
	    role.setRoleId(1);
	    role.setName("ROLE_USER");
	    
	    Set<UserRole> userRoles = new HashSet<>();
	    userRoles.add(new UserRole(user, role));
	    
	    userService.createUser(user, userRoles);
	    
	    String token = UUID.randomUUID().toString();
	    userService.createPasswordResetTokenForUser(user, token);
	    
	    String appUrl = "http://"+request.getServerName()+":"+ request.getServerPort()
	                   + request.getContextPath();
	    
	    SimpleMailMessage email = mailConstructor.constructorResestTokenEmail(appUrl, request.getLocale(), token, user, password);
	    
	    mailSender.send(email);
	    
	    model.addAttribute("emailSent" , true);

	    return "myAccount";
	}
	
	@RequestMapping("/newUser")
	public String newUser(
			Locale locale ,
			@RequestParam("token") String token,
			Model model)
	{
		PasswordResetToken passwordResetToken = userService.getPasswordResetToken(token);

		if(passwordResetToken == null)
		{
			String message = "Invalid Token";
			model.addAttribute("message", message);
			return "redirect:/badRequest";
		}
		
		User user = passwordResetToken.getUser();
		String username = user.getUsername();
		
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
		
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, 
				userDetails.getPassword(), userDetails.getAuthorities());
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		model.addAttribute("user", user);
		
		logger.info("----------> {}", user);
		
		model.addAttribute("classActiveEdit", true);
		model.addAttribute("orderList" , user.getOrderList());
		return "myProfile";
	}
	
	@RequestMapping("/bookshelf")
	public String bookshelf(Model model)
	{
		List<Book> bookList = bookService.findAllBooks();
		
		
		model.addAttribute("emptyList", (bookList == null) ? true :false);
		
		model.addAttribute("bookList", bookList);
		model.addAttribute("activeAll", true);
		
		return "bookshelf";
	}
	
	@RequestMapping("/bookDetails")
	public String bookDetails(
			@RequestParam("id") Long id,
			Model model, Principal principal)
	{
		if(principal != null)
		{
			String username = principal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user", user);
		}
		Book book = bookService.findOne(id);
		
		List<Integer> qtylist = Arrays.asList(1,2,3,4,5,6,7,8,9);
		int qty = 1;
		
		model.addAttribute("qtyList" , qtylist);
		model.addAttribute("qty" , qty);
		model.addAttribute("book" , book);
		
		return "bookDetails";
	}
	
	@RequestMapping("/orderDetails")
	public String orderDetails(
			@RequestParam("id") Long orderId,
			Principal prinipal, Model model)
	{
		User user = userService.findByUsername(prinipal.getName());
		Order order = orderService.findById(orderId);
		
		if(order.getUser().getId() != user.getId())
		{
			return "badRequestPage ";
		}
		
		List<CartItem> cartItemList = cartItemService.findByOrder(order);
		model.addAttribute("cartItemList", cartItemList);
		model.addAttribute("order", order);
		model.addAttribute("user", user);


		model.addAttribute("userPaymentList" , user.getUserPaymentList());
		model.addAttribute("userShippingList" , user.getUserShippingList());
		model.addAttribute("orderList", user.getOrderList());
		
		UserShipping userShipping = new UserShipping();
		model.addAttribute("userShipping" , userShipping);
		
		List<String> stateList = IndiaConstants.listOfIndiaStateName.stream().sorted()
				.collect(Collectors.toList());
		model.addAttribute("stateList" , stateList);
		
		
	    model.addAttribute("classActiveOrder", true);
	    model.addAttribute("displayOrderDetails", true);
	    model.addAttribute("addNewShippingAddress", true);

		return "myProfile";
	}
	
	
}
