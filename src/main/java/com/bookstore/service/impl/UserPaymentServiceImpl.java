package com.bookstore.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookstore.domain.UserPayment;
import com.bookstore.repository.UserPaymentRepository;
import com.bookstore.service.UserPaymentService;

@Service
public class UserPaymentServiceImpl implements UserPaymentService{

	@Autowired
	private UserPaymentRepository userPaymentRepository;
	
	@Override
	public UserPayment findById(Long creditCardId) {
		
		 Optional<UserPayment> userPayment = userPaymentRepository.findById(creditCardId);
		 
		 if(userPayment.isPresent())
			 return userPayment.get();
		 else
			 throw new RuntimeException("User Payment not found");
	}

	@Override
	public void removeById(Long creditCardId) {
		userPaymentRepository.deleteById(creditCardId);
		
	}

	
}
