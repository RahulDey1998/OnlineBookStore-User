package com.bookstore.Controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.bookstore.domain.Book;
import com.bookstore.domain.CartItem;
import com.bookstore.domain.ShoppingCart;
import com.bookstore.domain.User;
import com.bookstore.service.BookService;
import com.bookstore.service.CartItemService;
import com.bookstore.service.ShoppingCartService;
import com.bookstore.service.UserService;
import com.bookstore.service.UserShippingService;

@Controller
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
	
	@Autowired
	private UserService userService;

	@Autowired 
    private ShoppingCartService shoppingCartService;
	
	@Autowired 
	private CartItemService cartItemService;
	
	@Autowired 
	private BookService bookService;
	
	
	
	@RequestMapping("/cart")
	public String shoppingCart(Model model, Principal principal)
	{
		User user = userService.findByUsername(principal.getName());
		ShoppingCart shoppingCart = user.getShoppingCart();
		
		List<CartItem> cartItemList = cartItemService.findByShopping(shoppingCart);
	
	    shoppingCartService.updateShoppingCart(shoppingCart);
	    
	    model.addAttribute("cartItemList" , cartItemList);
	    model.addAttribute("shoppingCart" , shoppingCart);
	    
	    return "shoppingCart";
	}
	
	@RequestMapping(value = "/addItem", method = RequestMethod.POST)
	public String addItem(
			@ModelAttribute("book") Book book,
			@ModelAttribute("qty") String qty,
			Model model, Principal principal)
	{
		User user = userService.findByUsername(principal.getName());
		book = bookService.findOne(book.getId());
		
		if(Integer.parseInt(qty) > book.getInStockNumber())
		{
			model.addAttribute("notEnoughStock" , true);
			return "forward:/bookDetails?id="+book.getId();
		}
		
		CartItem cartItem = cartItemService.addBookToCartItem(book, user, Integer.parseInt(qty));
		model.addAttribute("addBookSuccess", true);
		
		return "forward:/bookDetails?id="+book.getId();
	}
	
	@RequestMapping(value = "/updateCartItem", method = RequestMethod.POST)
	public String updateCartItem(
			@ModelAttribute("qty") int qty,
			@ModelAttribute("id") Long id,
			Model model)
	{
		CartItem cartItem = cartItemService.findById(id);
	
		if(cartItem.getBook().getInStockNumber() < qty )
		{
			model.addAttribute("notEnoughStock" , true);
			return "forward:/shoppingCart/cart";
		}
		
		cartItem.setQty(qty);
		cartItemService.updateCartItem(cartItem);
		model.addAttribute("updateQtySuccess", true);
		
		return "forward:/shoppingCart/cart";
	}
	
	@RequestMapping("/removeItem")
	public String removeItem(
			@RequestParam("id") Long cartItemId,
			Principal prinipal)
	{
		cartItemService.deleteById(cartItemId);
		return "forward:/shoppingCart/cart";
	}	
	
	
}
 