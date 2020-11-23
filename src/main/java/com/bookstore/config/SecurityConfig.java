package com.bookstore.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.bookstore.utility.SecurityUtility;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter{
    
	@Autowired
	private Environment env;
	
	@Autowired
	private UserDetailsService userSecurityService; //spring will call this service to get the user information 
	
	private BCryptPasswordEncoder passwordEncoder()
	{
		return SecurityUtility.passwordEncoder();
	}

	private static final String[] PUBLIC_MATCHERS = {
			"/css/**",
			"/js/**",
			"/image/**",
			"/forgetPassword",
			"/newUser",
			"/login",
			"/",
			"/fonts/**",
			"/bookshelf",
			"/bookDetails",
			"/hours",
			"/faq",
			"/searchByCategory",
			"/searchBook"
	};

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http
		   .authorizeRequests()
//		   .antMatchers("/**")
		   .antMatchers(PUBLIC_MATCHERS)
		   .permitAll().anyRequest().authenticated();
		
		http 
		    .csrf().disable().cors().disable()
		    .formLogin().failureUrl("/login?error").defaultSuccessUrl("/")
		    .loginPage("/login").permitAll()
		    .and()
		    .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
		    .logoutSuccessUrl("/?logout").deleteCookies("remember-me").permitAll()
		    .and()
		    .rememberMe();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		
		auth.userDetailsService(userSecurityService).passwordEncoder(passwordEncoder());
	}
	
	
	
	
	
}
