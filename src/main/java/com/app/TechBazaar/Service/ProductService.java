package com.app.TechBazaar.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.TechBazaar.DTO.ProductDTO;
import com.app.TechBazaar.Model.Products;
import com.app.TechBazaar.Model.Users;
import com.app.TechBazaar.Repository.ProductCategoryRepository;
import com.app.TechBazaar.Repository.ProductRepository;
import com.app.TechBazaar.Model.Products.ProductStatus;

@Service
public class ProductService {
	
	@Autowired
	private ProductRepository productRepo;
	
	@Autowired
	private ProductCategoryRepository productCategoryRepo;
	
	public void saveProduct(ProductDTO productDto, MultipartFile[] images, Users user)
	{
		try {
			if(images.length < 1) {
				throw new RuntimeException("At least upload 1 image");
			}
			if(images.length > 5) {
				throw new RuntimeException("You can upload maximum 5 image");
			}
			if(productDto.getQuantityAvailable()<3) {
				throw new RuntimeException("Low Available Quantity, you can't list your product");
			}
			
			String uploadDir = "public/ProductImages/";
			File folder = new File(uploadDir);
			
			if(!folder.exists()) {
				folder.mkdirs();
			}
			List<String> productImagesNames = new ArrayList<>();
			for(MultipartFile image : images)
			{
				String storageFileName = UUID.randomUUID()+"_"+image.getOriginalFilename();
				Path uploadPath = Paths.get(uploadDir, storageFileName);
				Files.copy(image.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);
				productImagesNames.add(storageFileName);
			}
			//Save products into model
			Products product = new Products();
			product.setProductName(productDto.getProductName());
			product.setProductDescription(productDto.getProductDescription());
			product.setBrandName(productDto.getBrandName());
			product.setPricePerUnit(productDto.getPricePerUnit());
			product.setDiscount(productDto.getDiscount());
			product.setFinalPrice(productDto.getFinalPrice());
			product.setQuantityAvailable(productDto.getQuantityAvailable());
			product.setVisibility(true);
			
			product.setCategory(productDto.getCategory());
			
			product.setCancellationAllowed(productDto.isCancellationAllowed());
			product.setCodAvailable(productDto.isCodAvailable());
			product.setShippingType(productDto.getShippingType());
			product.setShippingCharge(productDto.getShippingCharge());
			product.setMinDeliveryDays(productDto.getMinDeliveryDays());
			product.setMaxDeliveryDays(productDto.getMaxDeliveryDays());
			product.setReturnAvailable(productDto.isReturnAvailable());
			product.setReturnCondition(productDto.getReturnCondition());
			product.setReturnDays(productDto.getReturnDays());
			product.setWarranty(productDto.isWarranty());
			product.setWarrantyDuration(productDto.getWarrantyDuration());
			product.setWarrantyTerms(productDto.getWarrantyTerms());
			product.setWarrantyUnit(productDto.getWarrantyUnit());
			product.setStatus(ProductStatus.AVAILABLE);
			product.setCreatedAt(LocalDateTime.now());
			product.setUpdatedAt(LocalDateTime.now());
			product.setSeller(user);
			product.setProductImages(productImagesNames);
			
			productRepo.save(product);
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public void ProductVisibility(long id)
	{
		Products product = productRepo.findById(id).orElseThrow(()-> new RuntimeException("products Not Found"));
		if (product.isVisibility()) {
			
			product.setVisibility(false);	
		}
		else {
			product.setVisibility(true);
		}
		productRepo.save(product);
	}
	
	public void setProductVisibility(Users user, boolean visibility)
	{
		List<Products> products= productRepo.findAllBySeller(user);
		for(Products product: products)
		{
			product.setVisibility(visibility);
			productRepo.save(product);
		}
	}
	
	// Existing update method
	public void updateEditProduct(Products product,
            MultipartFile[] productImagesFiles) {

Products existingProduct = productRepo.findById(product.getId())
.orElseThrow(() -> new RuntimeException("Product not found"));

String uploadDir = "public/ProductImages/";

try {

// 🔹 IMAGE UPDATE (only if new uploaded)
if (productImagesFiles != null &&
productImagesFiles.length > 0 &&
!productImagesFiles[0].isEmpty()) {

// delete old images
if (existingProduct.getProductImages() != null) {
for (String img : existingProduct.getProductImages()) {
  Path filePath = Paths.get(uploadDir + img);
  Files.deleteIfExists(filePath);
}
existingProduct.getProductImages().clear();
}

// save new images
for (MultipartFile image : productImagesFiles) {
if (!image.isEmpty()) {

  String fileName = UUID.randomUUID()
          + "_" + image.getOriginalFilename();

  Files.copy(image.getInputStream(),
          Paths.get(uploadDir + fileName),
          StandardCopyOption.REPLACE_EXISTING);

  existingProduct.getProductImages().add(fileName);
}
}
}

// 🔹 SAFE FIELD UPDATE (no null overwrite)
existingProduct.setProductName(product.getProductName());
existingProduct.setProductDescription(product.getProductDescription());
existingProduct.setBrandName(product.getBrandName());
existingProduct.setPricePerUnit(product.getPricePerUnit());
existingProduct.setDiscount(product.getDiscount());


double finalPrice =
product.getPricePerUnit()
      - (product.getPricePerUnit()
      * product.getDiscount() / 100);

existingProduct.setFinalPrice(finalPrice);

existingProduct.setQuantityAvailable(product.getQuantityAvailable());

existingProduct.setCodAvailable(product.isCodAvailable());
existingProduct.setReturnAvailable(product.isReturnAvailable());
existingProduct.setWarranty(product.isWarranty());

existingProduct.setUpdatedAt(LocalDateTime.now());

productRepo.save(existingProduct);

} catch (IOException e) {
throw new RuntimeException("Error updating product: " + e.getMessage());
}
}
	public List<Products> getTop6Products() {
	    return productRepo.findTop6ByOrderByIdDesc();
	}
}
