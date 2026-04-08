package com.app.TechBazaar.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.app.TechBazaar.API.SendEmailService;
import com.app.TechBazaar.DTO.EnquiryDTO;
import com.app.TechBazaar.DTO.UserDTO;
import com.app.TechBazaar.Model.Enquiry;
import com.app.TechBazaar.Model.Users;
import com.app.TechBazaar.Model.Users.LoginStatus;
import com.app.TechBazaar.Model.Users.UserRole;
import com.app.TechBazaar.Model.Users.UserStatus;
import com.app.TechBazaar.Repository.UserRepository;

@Service
public class UserService {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private SendEmailService emailService;
	
	private final String uploadDir = "public/uploads/";
	
	public String generateOTP() {
		return String.valueOf(100000 + new Random().nextInt(900000));
	}
	
	public void saveUserBuyer(UserDTO dto)
	{
		try {
			Users buyer = new Users();
			
			//Data from DTO
			buyer.setName(dto.getName());
			buyer.setEmail(dto.getEmail());
			buyer.setContactNo(dto.getContactNo());
			buyer.setGender(dto.getGender());
			buyer.setPassword(dto.getPassword());
			
			buyer.setLoginStatus(LoginStatus.INACTIVE);
			buyer.setUserRole(UserRole.BUYER);
			buyer.setUserStatus(UserStatus.UNBLOCKED);
			buyer.setRegDate(LocalDateTime.now());
			
			//otp verification and authentication
			String otp = generateOTP();
			buyer.setOtp(otp);
			buyer.setExpiryTime(LocalDateTime.now().plusMinutes(5));
			buyer.setVerified(false);
			
			userRepo.save(buyer);
			emailService.sendRegistrationOTP(buyer, otp);
			System.err.println(otp+"OTP for email "+buyer.getEmail());
		} catch(Exception e) {
			System.err.println("Error from service : "+e.getMessage());
			throw new RuntimeException("Something went wrong, please try again later");
		}
	}
	
	public void saveSeller(UserDTO dto)
	{
		try {
			Users seller = new Users();
			String storageFileName = UUID.randomUUID() +"_"+ dto.getProfilePic().getOriginalFilename();
			
			Path uploadPath = Paths.get(uploadDir);
			
			if(!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			
			try(InputStream inputStream = dto.getProfilePic().getInputStream())
			{
				Files.copy(inputStream, Paths.get(uploadDir+storageFileName), StandardCopyOption.REPLACE_EXISTING);
			}
			//Data from DTO
		    seller.setEmail(dto.getEmail());
			seller.setName(dto.getName());
			seller.setContactNo(dto.getContactNo());
			seller.setGender(dto.getGender());
			seller.setPassword(dto.getPassword());
			seller.setAadharNo(dto.getAadharNo());
			seller.setGstNo(dto.getGstNo());
			seller.setPanCard(dto.getPanCard());
			seller.setAddress(dto.getAddress());
			seller.setProfilePic(storageFileName);
			
			
			
			seller.setLoginStatus(LoginStatus.INACTIVE);
			seller.setUserRole(UserRole.SELLER);
			seller.setUserStatus(UserStatus.UNBLOCKED);
			seller.setRegDate(LocalDateTime.now());
			
			//otp verification and authentication
			String otp = generateOTP();
			seller.setOtp(otp);
			seller.setExpiryTime(LocalDateTime.now().plusMinutes(5));
			seller.setVerified(false);
			
			userRepo.save(seller);
			emailService.sendRegistrationOTP(seller, otp);
			System.err.println(otp+"OTP for email "+seller.getEmail());
		} catch(Exception e) {
			System.err.println("Error from service : "+e.getMessage());
			throw new RuntimeException("Something went wrong, please try again later");
		}
	}
	
	public boolean verifyOTP(String email, String otp) throws Exception
	{
		Users user = userRepo.findByEmail(email);
		if(user.getOtp().equals(otp) && LocalDateTime.now().isBefore(user.getExpiryTime())) {
			user.setVerified(true);
			userRepo.save(user);
			return true;
		}
		return false;
	}
	
	public void ResendOTP(String email)
	{
		try {
			String otp = generateOTP();
			Users user =userRepo.findByEmail(email);
			user.setExpiryTime(LocalDateTime.now().plusMinutes(5));
			user.setOtp(otp);
			emailService.sendRegistrationOTP(user, otp);
			userRepo.save(user);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public void changePassword(Users user, String oldPass, String newPass, String confirmPass)
	{
		try {
			if(!newPass.equals(confirmPass)) {
				throw new RuntimeException("New Password and confirm Password are not Same");
			}
			if(newPass.equals(user.getPassword())) {
				throw new RuntimeException("New Password can't be same as Old Password");
			}
			if(!oldPass.equals(user.getPassword())) {
				throw new RuntimeException("Invalid Old Password");
			}
			user.setPassword(confirmPass);
			userRepo.save(user);
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	public void updateUserStatus(long id)
	{
		Users user = userRepo.findById(id).orElseThrow(()-> new RuntimeException("User not found"));
		if(user.getUserStatus().equals(UserStatus.BLOCKED)) {
			user.setUserStatus(UserStatus.UNBLOCKED);
			//update product
		}
		else if(user.getUserStatus().equals(UserStatus.UNBLOCKED)) {
			user.setUserStatus(UserStatus.BLOCKED);
			//update product
		}
		userRepo.save(user);
	}
	
	public void deleteUser(long id)
	{
		Users user = userRepo.findById(id).orElseThrow(()-> new RuntimeException("User not found"));
		if(user.getUserRole().equals(UserRole.SELLER)) {
			user.setUserStatus(UserStatus.DELETED);
			//Deactive product Also
		} else {
			user.setUserStatus(UserStatus.DELETED);
		}
		userRepo.save(user);
	}
	
	public void updateProfilePic(Users user, MultipartFile profilePic) throws IOException
	{
		String storageFileName = UUID.randomUUID()+"_"+profilePic.getOriginalFilename();
		String uploadDir = "public/ProfilePic/";
		Path uploadPath = Paths.get(uploadDir);
		
		if(!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}
		if(Paths.get(uploadDir+user.getProfilePic())!=null) {
			Files.deleteIfExists(Paths.get(uploadDir+user.getProfilePic()));
		}
		
		Files.copy(profilePic.getInputStream(), Paths.get(uploadDir+storageFileName), StandardCopyOption.REPLACE_EXISTING);
		
		user.setProfilePic(storageFileName);
		userRepo.save(user);
	}
	
	public void updateEditProfile(Users seller, MultipartFile profileImage) throws IOException
	{
		Users existingUsers = userRepo.findById(seller.getId()).orElseThrow();
		
		//Delete Existing profile pic from source
		if(existingUsers.getProfilePic()!=null && !existingUsers.getProfilePic().isEmpty()) 
		{
			Path filePath = Paths.get(uploadDir+existingUsers.getProfilePic());
			Files.deleteIfExists(filePath);
		}
		
		String storageFileName  = UUID.randomUUID()+"_"+profileImage.getOriginalFilename();
		Files.copy(profileImage.getInputStream(), Paths.get(uploadDir+storageFileName), StandardCopyOption.REPLACE_EXISTING);
		
		existingUsers.setName(seller.getName());
		existingUsers.setContactNo(seller.getContactNo());
		existingUsers.setEmail(seller.getEmail());
		existingUsers.setAddress(seller.getAddress());
		existingUsers.setProfilePic(storageFileName);
		
		userRepo.save(existingUsers);
		
	}
	public void updateProfile(Users user,UserDTO dto) 
	{
		user.setName(dto.getName());
		user.setAadharNo(dto.getAadharNo());
		user.setContactNo(dto.getContactNo());
		user.setPanCard(dto.getPanCard());
		user.setGstNo(dto.getGstNo());
		user.setAddress(dto.getAddress());
		userRepo.save(user);
	}
	
	public void editprofile(Users user,UserDTO dto) 
	{
		user.setName(dto.getName());
		user.setContactNo(dto.getContactNo());
		user.setGender(dto.getGender());
		userRepo.save(user);
	}

}
