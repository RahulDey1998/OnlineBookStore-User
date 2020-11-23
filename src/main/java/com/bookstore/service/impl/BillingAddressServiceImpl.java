package com.bookstore.service.impl;

import com.bookstore.domain.BillingAddress;
import com.bookstore.domain.ShippingAddress;
import com.bookstore.domain.UserBilling;
import com.bookstore.service.BillingAddressService;

import org.springframework.stereotype.Service;

@Service
public class BillingAddressServiceImpl implements BillingAddressService {

	@Override
	public BillingAddress setByUserBilling(UserBilling userBilling, BillingAddress billingAddress) {
		
		billingAddress.setBillingAddressName(userBilling.getUserBillingName());
		billingAddress.setBillingAddressCity(userBilling.getUserBillingCity());
		billingAddress.setBillingAddressState(userBilling.getUserBillingState());
		billingAddress.setBillingAddressCountry(userBilling.getUserBillingCountry());
		billingAddress.setBillingAddressStreet1(userBilling.getUserBillingStreet1());
		billingAddress.setBillingAddressStreet2(userBilling.getUserBillingStreet2());
		billingAddress.setBillingAddressZipcode(userBilling.getUserBillingZipCode());
		
		return billingAddress;
	}

	@Override
	public BillingAddress setByShippingAddress(BillingAddress billingAddress, ShippingAddress shippingAddress) {

		billingAddress.setBillingAddressName(shippingAddress.getShippingAddressName());
		billingAddress.setBillingAddressCity(shippingAddress.getShippingAddressCity());
		billingAddress.setBillingAddressState(shippingAddress.getShippingAddressState());
		billingAddress.setBillingAddressCountry(shippingAddress.getShippingAddressCountry());
		billingAddress.setBillingAddressStreet1(shippingAddress.getShippingAddressStreet1());
		billingAddress.setBillingAddressStreet2(shippingAddress.getShippingAddressStreet2());
		billingAddress.setBillingAddressZipcode(shippingAddress.getShippingAddressZipcode());

		return billingAddress;
	}

}
