package com.bookstore.service;

import java.util.List;

import com.bookstore.domain.Book;


public interface BookService {
	
	List<Book> findAllBooks();

	Book findOne(Long id);

	List<Book> findByCategory(String category);

	List<Book> blurySearch(String keyword);

}
