package com.app.TechBazaar.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import com.app.TechBazaar.Model.CartItem;
import com.app.TechBazaar.Model.Products;
import com.app.TechBazaar.Model.Users;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	CartItem findByUserAndProduct(Users user, Products product);
    
	
	int countByUser(Users user);

	
   
	int countBySessionId(String id);

	CartItem findBySessionIdAndProduct(String id, Products product);

	List<CartItem> findAllBySessionId(String sessionId);


	List<CartItem> findAllByUser(Users user);


	void deleteByUserAndProduct(Users user, Products product);

	
}
