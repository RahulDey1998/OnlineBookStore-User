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
import com.bookstore.domain.User;
import com.bookstore.service.BookService;
import com.bookstore.service.UserService;

@Controller
public class SearchController {
	
	private static final String RequestType = null;

	@Autowired
	private UserService userService;
	
	@Autowired
	private BookService bookService;
	
	@RequestMapping("/searchByCategory")
	public String searchByCategory(
			@RequestParam("category") String category,
			Model model, Principal prinicpal)
	{
		if(prinicpal != null)
		{
			String username = prinicpal.getName();
			User user = userService.findByUsername(username);
			model.addAttribute("user", user);
			
		}
		
		String classActiveCategory = "active"+category;
		classActiveCategory.replaceAll("\\s+", "");
		classActiveCategory.replaceAll("&", "");
		model.addAttribute(classActiveCategory, true);
		
		List<Book> bookList = bookService.findByCategory(category);
		
		if(bookList.isEmpty())
		{
			model.addAttribute("emptyList", true);
			return "bookShelf";
		}
		
		model.addAttribute("bookList", bookList);
		
		return "bookShelf";
		
	}
	
	@RequestMapping( value = "/searchBook" , method=RequestMethod.POST)
	public String searchBook(
			@ModelAttribute("keyword") String keyword,
			Principal principal, Model model)
	{
		if(principal != null)
		{
			String username = principal.getName();
			User user = userService.findByUsername(username);
		}
		
		List<Book> bookList = bookService.blurySearch(keyword);
	
	
		if(bookList.isEmpty())
		{
			model.addAttribute("emptyList", true);
			return "bookShelf";
		}
		
		model.addAttribute("bookList", bookList);
		
		return "bookShelf";
	}

}
