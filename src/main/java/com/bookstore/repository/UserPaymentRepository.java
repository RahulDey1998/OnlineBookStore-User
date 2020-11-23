package com.bookstore.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bookstore.domain.UserPayment;

@Repository
public interface UserPaymentRepository extends CrudRepository<UserPayment, Long> {

	 Optional<UserPayment> findById(Long creditCardId) ;


}
