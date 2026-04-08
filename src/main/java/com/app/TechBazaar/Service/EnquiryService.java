package com.app.TechBazaar.Service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.TechBazaar.DTO.EnquiryDTO;
import com.app.TechBazaar.Model.Enquiry;
import com.app.TechBazaar.Repository.EnquiryRepository;

@Service
public class EnquiryService {
	
	@Autowired
	private EnquiryRepository enquiryRepo;
	
	public void saveEnquiry(EnquiryDTO dto)
	{
		Enquiry enq = new Enquiry();
		enq.setName(dto.getName());
		enq.setContactNo(dto.getContactNo());
		enq.setEmail(dto.getEmail());
		enq.setMessage(dto.getMessage());
		enq.setTitle(dto.getTitle());
		enq.setEnquiryDate(LocalDateTime.now());
		
		enquiryRepo.save(enq);
		
	}

}
