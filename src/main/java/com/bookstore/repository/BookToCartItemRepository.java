package com.bookstore.repository;



import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.bookstore.domain.BookToCartItem;

@Repository
@Transactional
public interface BookToCartItemRepository extends CrudRepository<BookToCartItem, Long>{

}
