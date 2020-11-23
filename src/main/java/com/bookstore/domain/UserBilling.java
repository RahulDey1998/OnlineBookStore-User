package com.bookstore.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class UserBilling {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long billingId;
	private String userBillingName;
	private String userBillingStreet1;
	private String userBillingStreet2;
	private String userBillingState;
	private String userBillingCity;
	private String userBillingCountry;
	private String userBillingZipCode;
	
	@OneToOne(cascade = CascadeType.ALL)
//	@JoinColumn(name="payment_id")
	private UserPayment userPayment;

	

	
	public Long getBillingId() {
		return billingId;
	}

	public void setBillingId(Long billingId) {
		this.billingId = billingId;
	}

	public UserPayment getUserPayment() {
		return userPayment;
	}

	public void setUserPayment(UserPayment userPayment) {
		this.userPayment = userPayment;
	}

	public String getUserBillingName() {
		return userBillingName;
	}

	public void setUserBillingName(String userBillingName) {
		this.userBillingName = userBillingName;
	}

	public String getUserBillingStreet1() {
		return userBillingStreet1;
	}

	public void setUserBillingStreet1(String userBillingStreet1) {
		this.userBillingStreet1 = userBillingStreet1;
	}

	public String getUserBillingStreet2() {
		return userBillingStreet2;
	}

	public void setUserBillingStreet2(String userBillingStreet2) {
		this.userBillingStreet2 = userBillingStreet2;
	}

	public String getUserBillingState() {
		return userBillingState;
	}

	public void setUserBillingState(String userBillingState) {
		this.userBillingState = userBillingState;
	}

	public String getUserBillingCountry() {
		return userBillingCountry;
	}

	public void setUserBillingCountry(String userBillingCountry) {
		this.userBillingCountry = userBillingCountry;
	}

	public String getUserBillingZipCode() {
		return userBillingZipCode;
	}

	public void setUserBillingZipCode(String userBillingZipCode) {
		this.userBillingZipCode = userBillingZipCode;
	}

	public UserPayment getUserPayments() {
		return userPayment;
	}

	public void setUserPayments(UserPayment userPayments) {
		this.userPayment = userPayments;
	}

	public String getUserBillingCity() {
		return userBillingCity;
	}

	public void setUserBillingCity(String userBillingCity) {
		this.userBillingCity = userBillingCity;
	}
	
	
	
}
