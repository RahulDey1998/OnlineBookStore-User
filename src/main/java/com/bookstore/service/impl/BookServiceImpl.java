package com.bookstore.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bookstore.domain.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.BookService;

@Service
public class BookServiceImpl implements BookService{

	@Autowired
	private BookRepository bookRepository;
	
	@Override
	public List<Book> findAllBooks() {
		
		return (List<Book>) bookRepository.findAll();
	}

	@Override
	public Book findOne(Long id) {
		
		 Optional<Book> book = bookRepository.findById(id);
		 
		 if(book.isPresent())
		 {
			 return book.get();
		 }
		 else 
		 {
			 throw new RuntimeException("Book not found with id : -" + id);
		 }
	}

	@Override
	public List<Book> findByCategory(String category) {
		
		List<Book> bookList =  bookRepository.findByCategory(category);
		
		List<Book> activeBookList = bookList.stream().filter((book) -> book.isActive()).collect(Collectors.toList());
	
	    return activeBookList;
	}

	@Override
	public List<Book> blurySearch(String keyword) {
		
		List<Book> bookList = bookRepository.findByTitleContaining(keyword);
		
		List<Book> activeBookList = bookList.stream().filter((book) -> book.isActive()).collect(Collectors.toList());
	
	    return activeBookList;
		
	}

}
