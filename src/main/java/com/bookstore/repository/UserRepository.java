package com.bookstore.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.bookstore.domain.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long>{

	  User findByusername(String userName);

	  User findByEmail(String email);
	  
	  
}
