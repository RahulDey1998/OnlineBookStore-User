package com.bookstore.Controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.bookstore.domain.BillingAddress;
import com.bookstore.domain.CartItem;
import com.bookstore.domain.Order;
import com.bookstore.domain.Payment;
import com.bookstore.domain.ShippingAddress;
import com.bookstore.domain.ShoppingCart;
import com.bookstore.domain.User;
import com.bookstore.domain.UserBilling;
import com.bookstore.domain.UserPayment;
import com.bookstore.domain.UserShipping;
import com.bookstore.service.BillingAddressService;
import com.bookstore.service.CartItemService;
import com.bookstore.service.OrderService;
import com.bookstore.service.PaymentService;
import com.bookstore.service.ShippingAddressService;
import com.bookstore.service.ShoppingCartService;
import com.bookstore.service.UserPaymentService;
import com.bookstore.service.UserService;
import com.bookstore.service.UserShippingService;
import com.bookstore.utility.IndiaConstants;
import com.bookstore.utility.MailConstructor;

@Controller
public class CheckOutController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CartItemService cartItemService;
	
	@Autowired
	private ShippingAddressService shippingAddressService;
	
	@Autowired
	private PaymentService paymentService;
	
	@Autowired
	private BillingAddressService billingAddressService;
	
	@Autowired
	private UserShippingService userShippingService;
	
	@Autowired
	private UserPaymentService userPaymentService;
	
	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired 
	private OrderService orderService;
	
	@Autowired
	private ShoppingCartService shoppingCartService;
	
	@Autowired
	private MailConstructor mailConstructor;
	
	private ShippingAddress shippingAddress = new ShippingAddress();
	private BillingAddress billingAddress = new BillingAddress();
	private Payment payment = new Payment();

	@RequestMapping("/checkout")
	public String chechout(
			@RequestParam("id") Long cartId,
			@RequestParam(value="missingRequireField", required = false) boolean missingRequiredField,
			Model model, Principal principal)
	{
		User user = userService.findByUsername(principal.getName());
		
		if(cartId != user.getShoppingCart().getId())
		{
			return "badRequestPage";
		}
		
		List<CartItem> cartItemList = cartItemService.findByShopping(user.getShoppingCart());
		
		if(cartItemList.size() == 0)
		{
			model.addAttribute("emptyCart", true);
			return "forward:/shoppingCart/cart";
		}
		
		for(CartItem cartItem : cartItemList)
		{
			if(cartItem.getBook().getInStockNumber() < cartItem.getQty())
			{
				model.addAttribute("notEnoughtStock", true);
				return "forward:/shoppingCart/cart";
			}
		}
		
		List<UserShipping> userShippingList = user.getUserShippingList();
		List<UserPayment> userPaymentList = user.getUserPaymentList();
		
		model.addAttribute("userShippingList", userShippingList);
		model.addAttribute("userPaymentList", userPaymentList);
		
		if(userPaymentList.size() == 0)
		{
			model.addAttribute("emptyPaymentList", true);
		}
		else
		{
			model.addAttribute("emptyPaymentList", false);
		}
		
		if(userShippingList.size() == 0)
		{
			model.addAttribute("emptyShippingList", true);
		}
		else
		{
			model.addAttribute("emptyShippingList", false);
		}
		
		ShoppingCart shoppingCart = user.getShoppingCart();
		
		for(UserShipping userShipping : userShippingList)
		{
			if(userShipping.isUserShippingDefault())
			{
				shippingAddressService.setByUserShipping(userShipping, shippingAddress);
			}
		}
		
		for(UserPayment userPayment : userPaymentList)
		{
			if(userPayment.isDefaultPayment())
			{
				paymentService.setByUserPayment(userPayment, payment);
				billingAddressService.setByUserBilling(userPayment.getUserBilling(), billingAddress);
			}
		}
		
		model.addAttribute("shippingAddress", shippingAddress);
		model.addAttribute("billingAddress", billingAddress);
		model.addAttribute("payment", payment);
		model.addAttribute("cartItemList", cartItemList);
		model.addAttribute("shoppingCart", user.getShoppingCart());
	
		List<String> stateList = IndiaConstants.listOfIndiaStateName;
		Collections.sort(stateList);
		
		model.addAttribute("stateList" , stateList);
	   
		model.addAttribute("classActiveShipping", true);
		
		if(missingRequiredField)
		{
			model.addAttribute("missingRequiredField", true);
		}
	
		return "checkout";
	}
	
	@RequestMapping(value = "/checkout", method = RequestMethod.POST)
	public String checkOutPost(
			@ModelAttribute("shippingAddress") ShippingAddress shippingAddress,
			@ModelAttribute("billingAddress") BillingAddress billingAddress,
			@ModelAttribute("payment") Payment payment,
			@RequestParam(value = "billingSameAsShipping", required = false) String billingSameAsShipping,
			@ModelAttribute("shippingMethod") String shippingMethod,
			Principal principal, Model model)
	{
		User user = userService.findByUsername(principal.getName());
		ShoppingCart shoppingCart = user.getShoppingCart();

		List<CartItem> cartItemList = cartItemService.findByShopping(shoppingCart);
	    model.addAttribute("cartItemList", cartItemList);
		
		if(billingSameAsShipping != null)
		{
			billingAddressService.setByShippingAddress(billingAddress, shippingAddress);
		}		

		if (shippingAddress.getShippingAddressStreet1().isEmpty() || shippingAddress.getShippingAddressCity().isEmpty()
				|| shippingAddress.getShippingAddressState().isEmpty()
				|| shippingAddress.getShippingAddressName().isEmpty()
				|| shippingAddress.getShippingAddressZipcode().isEmpty() || payment.getCardNumber().isEmpty()
				|| payment.getCvv() == 0 || billingAddress.getBillingAddressStreet1().isEmpty()
				|| billingAddress.getBillingAddressCity().isEmpty() || billingAddress.getBillingAddressState().isEmpty()
				|| billingAddress.getBillingAddressName().isEmpty()
				|| billingAddress.getBillingAddressZipcode().isEmpty())
			return "redirect:/checkout?id=" + shoppingCart.getId() + "&missingRequiredField=true";
		
		Order order = orderService.createOrder(shoppingCart, shippingAddress, billingAddress, payment, user, shippingMethod);
		
		mailSender.send(mailConstructor.constructOrderConfirmationEmail(user, order, cartItemList, Locale.ENGLISH));
		
		shoppingCartService.clearShoppingCart(shoppingCart);
		
		LocalDate today = LocalDate.now();
		LocalDate estimatedDeliveryDate;
		
		if(shippingMethod.equals("groundShipping"))
		{
			estimatedDeliveryDate = today.plusDays(5);
		}
		else
		{
			estimatedDeliveryDate = today.plusDays(2);
		}
		
		model.addAttribute( "estimatedDeliveryDate", estimatedDeliveryDate);
		
		return "orderSubmittedPage";
		
	}
	
	
	@RequestMapping("/setShippingAddress")
	public String setShippingAddress(
			@RequestParam("userShippingId") Long userShippingId,
			Model model, Principal principal)
	
	{
		User user = userService.findByUsername(principal.getName());
		UserShipping userShipping = userShippingService.findById(userShippingId);
		
		if(userShipping.getUser().getId() != user.getId())
		{
			return "badRequestPage";
		}
		
		shippingAddressService.setByUserShipping(userShipping, shippingAddress);
		model.addAttribute("shippingAddress", shippingAddress);
		
		List<CartItem> cartItemList = cartItemService.findByShopping(user.getShoppingCart());
		
		List<UserShipping> userShippingList = user.getUserShippingList();
		List<UserPayment> userPaymentList = user.getUserPaymentList();
		
		if(userPaymentList.size() == 0)
		{
			model.addAttribute("emptyPaymentList", true);
		}
		else
		{
			model.addAttribute("emptyPaymentList", false);
		}
		
		model.addAttribute("emptyShippingList", false);

		model.addAttribute("userShippingList", userShippingList);
		model.addAttribute("userPaymentList", userPaymentList);
		
		model.addAttribute("billingAddress", billingAddress);
		model.addAttribute("payment", payment);
		model.addAttribute("cartItemList", cartItemList);
		model.addAttribute("shoppingCart", user.getShoppingCart());
		
		List<String> stateList = IndiaConstants.listOfIndiaStateName;
		Collections.sort(stateList);
		
		model.addAttribute("stateList" , stateList);
	   
		model.addAttribute("classActiveShipping", true);
		

		
		return "checkout";
	}
	
	@RequestMapping("/setPaymentMethod")
	public String setPaymentMethod(
			@RequestParam("userPaymentId") Long userPaymentId,
			Model model, Principal principal)
	{
		User user = userService.findByUsername(principal.getName());
		UserPayment userPayment = userPaymentService.findById(userPaymentId);
		UserBilling userBilling = userPayment.getUserBilling();
		
		if(userPayment.getUser().getId() != user.getId())
		{
			return "badRequest";
		}
		
		List<CartItem> cartItemList = cartItemService.findByShopping(user.getShoppingCart());
		
		List<UserShipping> userShippingList = user.getUserShippingList();
		List<UserPayment> userPaymentList = user.getUserPaymentList();
		
		paymentService.setByUserPayment(userPayment, payment);
		
        billingAddressService.setByUserBilling(userBilling, billingAddress);
		
	
		model.addAttribute("emptyPaymentList", false);
		
		
		if(userShippingList.size() == 0)
		{
			model.addAttribute("emptyShippingList", true);
		}
		else
		{
			model.addAttribute("emptyShippingList", false);
		}

		


		model.addAttribute("userShippingList", userShippingList);
		model.addAttribute("userPaymentList", userPaymentList);
		model.addAttribute("billingAddress", billingAddress);
		model.addAttribute("shippingAddress", shippingAddress);
		model.addAttribute("payment", payment);
		model.addAttribute("cartItemList", cartItemList);
		model.addAttribute("shoppingCart", user.getShoppingCart());
		
		List<String> stateList = IndiaConstants.listOfIndiaStateName;
		Collections.sort(stateList);
		
		model.addAttribute("stateList" , stateList);
	   
		model.addAttribute("classActivePayment", true);

		return "checkout";
	}
}
