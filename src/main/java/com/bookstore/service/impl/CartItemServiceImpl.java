package com.bookstore.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookstore.domain.Book;
import com.bookstore.domain.BookToCartItem;
import com.bookstore.domain.CartItem;
import com.bookstore.domain.Order;
import com.bookstore.domain.ShoppingCart;
import com.bookstore.domain.User;
import com.bookstore.repository.BookToCartItemRepository;
import com.bookstore.repository.CartItemRepository;
import com.bookstore.service.CartItemService;

@Service
public class CartItemServiceImpl implements CartItemService{

	@Autowired
	private CartItemRepository cartItemRepository;
	
	@Autowired
	private BookToCartItemRepository bookToCartItemRepository;	
	
	@Override
	public List<CartItem> findByShopping(ShoppingCart shoppingCart) {

		return cartItemRepository.findByShoppingCart(shoppingCart);
		
	}

	@Override
	public CartItem updateCartItem(CartItem cartItem) {
		
		BigDecimal bigDecimal = new BigDecimal(cartItem.getBook().getOurPrice()).multiply(new BigDecimal(cartItem.getQty()));
		
		bigDecimal.setScale(2, RoundingMode.HALF_EVEN);
		cartItem.setSubTotal(bigDecimal);
		
		cartItemRepository.save(cartItem);
		
		return cartItem;
		
	}


	@Override
	public CartItem addBookToCartItem(Book book, User user, int qty) {
		
		List<CartItem> cartItemList = findByShopping(user.getShoppingCart());
		
		for(CartItem cartItem : cartItemList)
		{
			if(cartItem.getBook().getId() == book.getId())
			{
				cartItem.setQty(cartItem.getQty() + qty);
				cartItem.setSubTotal(new BigDecimal(cartItem.getQty()).multiply(new BigDecimal(book.getOurPrice())));
			    cartItemRepository.save(cartItem);	
			    return cartItem;
			}
		}
		
		CartItem cartItem = new CartItem();
		cartItem.setShoppingCart(user.getShoppingCart());
		cartItem.setBook(book);
		cartItem.setQty(qty);
		cartItem.setSubTotal((new BigDecimal(cartItem.getQty()).multiply(new BigDecimal(book.getOurPrice()))));
	    cartItemRepository.save(cartItem);
	    
	    BookToCartItem bookToCartItem = new BookToCartItem();
	    bookToCartItem.setBook(book);
	    bookToCartItem.setCartItem(cartItem);
	    bookToCartItemRepository.save(bookToCartItem);
	    
	    return cartItem;
	}

	@Override
	public CartItem findById(Long id) {

        return cartItemRepository.findById(id).orElseThrow();
	}

	@Override
	public void deleteById(Long cartItemId) {


		cartItemRepository.deleteById(cartItemId);
		
	}

	@Override
	public CartItem save(CartItem cartItem) {
		
		return cartItemRepository.save(cartItem);
	}

	@Override
	public List<CartItem> findByOrder(Order order) {
		
		return cartItemRepository.findByOrder(order);
	}

}
