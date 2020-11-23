package com.bookstore.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookstore.domain.ShoppingCart;
import com.bookstore.domain.User;
import com.bookstore.domain.UserBilling;
import com.bookstore.domain.UserPayment;
import com.bookstore.domain.UserShipping;
import com.bookstore.domain.security.PasswordResetToken;
import com.bookstore.domain.security.UserRole;
import com.bookstore.repository.PasswordResetTokenRepository;
import com.bookstore.repository.RoleRepository;
import com.bookstore.repository.UserPaymentRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.repository.UserShippingRepository;
import com.bookstore.service.UserService;
import com.bookstore.service.UserShippingService;


@Service
public class UserServiceImpl implements UserService {
	
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(UserService.class);

	@Autowired
	private PasswordResetTokenRepository passwordResetTokenRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private UserShippingRepository userShippingRepository;
	
	@Autowired
	private UserPaymentRepository userPaymentRepository;
	
	@Override
	public PasswordResetToken getPasswordResetToken(String token) {
		
		return passwordResetTokenRepository.findByToken(token);
	}

	@Override
	public void createPasswordResetTokenForUser(final User user, 
			final String token) {
		
		final PasswordResetToken myToken = new PasswordResetToken(token, user);
		
		passwordResetTokenRepository.save(myToken);
	}

	@Override
	public User findByUsername(String username) {
		
		return userRepository.findByusername(username);
	}

	@Override
	public User findByEmail(String email) {

        return userRepository.findByEmail(email);
	}

	@Override
//	@Transactional
	public User createUser(User user, Set<UserRole> userRoles) throws Exception {
		
		User localUser = userRepository.findByusername(user.getUsername());
		
		if(localUser != null)
		{
			logger.info("User {} Already Exist!!" , user.getUsername());
			
		}
		else 
		{
			for(UserRole role : userRoles)
			{
				roleRepository.save(role.getRole());
			}
			
			user.getUserRoles().addAll(userRoles);
			
			ShoppingCart shoppingCart = new ShoppingCart();
			shoppingCart.setUser(user);
			user.setShoppingCart(shoppingCart);
			
			user.setUserShippingList(new ArrayList<UserShipping>());
			user.setUserPaymentList(new ArrayList<UserPayment>());
			
			localUser = userRepository.save(user);
		}
		
		return localUser;
	}

	@Override
	public User save(User user) {
		return userRepository.save(user);
		
	}

	@Override
	public void updateUserBilling(UserPayment userPayment, UserBilling userBilling, User user) {
		
		userPayment.setUser(user);
		userPayment.setUserBilling(userBilling);
		userPayment.setDefaultPayment(true);
		userBilling.setUserPayments(userPayment);
		user.getUserPaymentList().add(userPayment);
		save(user);
	}

	@Override
	public void updateUserShipping(UserShipping userShipping, User user) {
		
		userShipping.setUser(user);
		userShipping.setUserShippingDefault(true);
		user.getUserShippingList().add(userShipping);
		save(user);
		
	}

	@Override
	public void setUserDefaultShipping(Long id, User user) {
		List<UserShipping> shippingList = user.getUserShippingList();
		for(UserShipping userShipping : shippingList)
		{
			if(userShipping.getId() == id)
			{
				userShipping.setUserShippingDefault(true);
				userShippingRepository.save(userShipping);
			}
			else
			{
				userShipping.setUserShippingDefault(false);
				userShippingRepository.save(userShipping);
			}
		}
		
	}
	
	@Override
	public void setDefaultPayment(Long id, User user) {
		List<UserPayment> paymentList = user.getUserPaymentList();
		for(UserPayment userPayment : paymentList)
		{
			if(userPayment.getId() == id)
			{
				userPayment.setDefaultPayment(true);
				userPaymentRepository.save(userPayment);
			}
			else
			{
				userPayment.setDefaultPayment(false);
				userPaymentRepository.save(userPayment);
			}
		}
		
	}

	@Override
	public User findById(Long id) {
		
		return userRepository.findById(id).orElseThrow();
	}

}
