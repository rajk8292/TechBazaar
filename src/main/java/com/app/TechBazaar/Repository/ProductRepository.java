package com.app.TechBazaar.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.app.TechBazaar.Model.ProductCategory;
import com.app.TechBazaar.Model.Products;
import com.app.TechBazaar.Model.Users;
import com.app.TechBazaar.Model.Users.UserStatus;

@Repository
public interface ProductRepository extends JpaRepository<Products, Long> {

    List<Products> findAllBySeller(Users seller);

    List<Products> findByVisibility(boolean visibility);

    List<Products> findAllByCategoryAndVisibility(ProductCategory category, boolean visibility);

    long countBySeller(Users seller);

    @Query("SELECT COALESCE(SUM(p.finalPrice * p.quantityAvailable), 0) " +
           "FROM Products p WHERE p.seller = :seller")
    double getTotalInStockRevenueBySeller(@Param("seller") Users seller);

	List<Products> findAllBySellerId(long id);

	List<Products> findAllBySeller_Id(long id);

	List<Products> findTop6ByOrderByIdDesc();

	

	
}
	



	

	



	



	



	



	

	


