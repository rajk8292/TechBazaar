package com.app.TechBazaar.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.app.TechBazaar.DTO.SavedAddressDTO;
import com.app.TechBazaar.Model.SavedAddress;
import com.app.TechBazaar.Model.Users;
import com.app.TechBazaar.Repository.SavedAddressRepository;

@Service
public class SavedAddressService {

    @Autowired
    private SavedAddressRepository addressRepo;

    // ===== Save New Address =====
    public void saveNewAddress(SavedAddressDTO dto, Users user) {
        SavedAddress newAddress = new SavedAddress();
        newAddress.setName(dto.getName());
        newAddress.setContactNo(dto.getContactNo());
        newAddress.setPincode(dto.getPincode());
        newAddress.setLocality(dto.getLocality());
        newAddress.setAddress(dto.getAddress());
        newAddress.setCityDistrict(dto.getCityDistrict());
        newAddress.setState(dto.getState());
        newAddress.setLandmark(dto.getLandmark());
        newAddress.setAltContactNo(dto.getAltContactNo());
        newAddress.setAddressType(dto.getAddressType());
        newAddress.setUser(user);
        newAddress.setAddedDate(LocalDateTime.now());

        addressRepo.save(newAddress);
        changeAddress(user, newAddress.getId());
    }

    // ===== Change Active Address =====
    public void changeAddress(Users user, long id) {
        List<SavedAddress> addresses = addressRepo.findAllByUser(user);
        for (SavedAddress add : addresses) {
            if (add.getId() == id)
                add.setActive(true);
            else
                add.setActive(false);

            addressRepo.save(add);
        }
    }

    // ===== Fetch Address by ID (for Edit) =====
    public SavedAddress getAddressById(Long id) {
        return addressRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found with ID: " + id));
    }

    // ===== Update Existing Address =====
    public void updateAddress(SavedAddressDTO dto) {
        SavedAddress existing = addressRepo.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        existing.setName(dto.getName());
        existing.setContactNo(dto.getContactNo());
        existing.setPincode(dto.getPincode());
        existing.setLocality(dto.getLocality());
        existing.setAddress(dto.getAddress());       // ← Ensure non-null
        existing.setCityDistrict(dto.getCityDistrict());
        existing.setState(dto.getState());
        existing.setLandmark(dto.getLandmark());
        existing.setAltContactNo(dto.getAltContactNo());
        existing.setAddressType(dto.getAddressType());

        addressRepo.save(existing);
    }
    public void deleteAddress(Long id) {
        if(addressRepo.existsById(id)) {
            addressRepo.deleteById(id);
        } else {
            throw new RuntimeException("Address not found with id: " + id);
        }
    }
    }

