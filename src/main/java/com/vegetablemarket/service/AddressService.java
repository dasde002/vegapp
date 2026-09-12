package com.vegetablemarket.service;

import com.vegetablemarket.dto.AddressRequest;
import com.vegetablemarket.entity.Address;
import com.vegetablemarket.entity.User;
import com.vegetablemarket.repository.AddressRepository;
import com.vegetablemarket.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;


    // CREATE ADDRESS
    @Transactional
    public Address addAddress(
            String email,
            AddressRequest request) {

        User user = getUser(email);

        validateRequest(request);

        // If this is the first address, make it default
        List<Address> existingAddresses =
                addressRepository.findByUser(user);

        boolean makeDefault =
                existingAddresses.isEmpty()
                        || Boolean.TRUE.equals(
                                request.getDefaultAddress());

        if (makeDefault) {
            clearDefaultAddress(user);
        }

        Address address = new Address();

        address.setUser(user);
        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setLandmark(request.getLandmark());
        address.setDefaultAddress(makeDefault);

        return addressRepository.save(address);
    }


    // GET ALL ADDRESSES
    public List<Address> getAddresses(String email) {

        User user = getUser(email);

        return addressRepository
                .findByUserOrderByDefaultAddressDescIdDesc(user);
    }


    // GET SINGLE ADDRESS
    public Address getAddress(
            String email,
            Long addressId) {

        User user = getUser(email);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        new RuntimeException("Address not found"));

        checkOwnership(address, user);

        return address;
    }


    // UPDATE ADDRESS
    @Transactional
    public Address updateAddress(
            String email,
            Long addressId,
            AddressRequest request) {

        User user = getUser(email);

        validateRequest(request);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        new RuntimeException("Address not found"));

        checkOwnership(address, user);

        if (Boolean.TRUE.equals(request.getDefaultAddress())) {
            clearDefaultAddress(user);
            address.setDefaultAddress(true);
        }

        address.setFullName(request.getFullName());
        address.setPhone(request.getPhone());
        address.setAddressLine(request.getAddressLine());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setLandmark(request.getLandmark());

        return addressRepository.save(address);
    }


    // DELETE ADDRESS
    @Transactional
    public void deleteAddress(
            String email,
            Long addressId) {

        User user = getUser(email);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        new RuntimeException("Address not found"));

        checkOwnership(address, user);

        boolean wasDefault =
                Boolean.TRUE.equals(address.getDefaultAddress());

        addressRepository.delete(address);

        // If default address was deleted,
        // make another address default.
        if (wasDefault) {

            List<Address> addresses =
                    addressRepository.findByUserOrderByDefaultAddressDescIdDesc(
                            user);

            if (!addresses.isEmpty()) {
                Address newDefault = addresses.get(0);
                newDefault.setDefaultAddress(true);
                addressRepository.save(newDefault);
            }
        }
    }


    // SET DEFAULT ADDRESS
    @Transactional
    public Address setDefaultAddress(
            String email,
            Long addressId) {

        User user = getUser(email);

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() ->
                        new RuntimeException("Address not found"));

        checkOwnership(address, user);

        clearDefaultAddress(user);

        address.setDefaultAddress(true);

        return addressRepository.save(address);
    }


    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));
    }


    private void checkOwnership(
            Address address,
            User user) {

        if (!address.getUser().getId().equals(user.getId())) {

            throw new RuntimeException(
                    "You are not authorized to access this address");
        }
    }


    private void clearDefaultAddress(User user) {

        List<Address> addresses =
                addressRepository.findByUser(user);

        for (Address address : addresses) {

            address.setDefaultAddress(false);
            addressRepository.save(address);
        }
    }


    private void validateRequest(AddressRequest request) {

        if (request.getFullName() == null
                || request.getFullName().isBlank()) {

            throw new RuntimeException(
                    "Full name is required");
        }

        if (request.getPhone() == null
                || request.getPhone().isBlank()) {

            throw new RuntimeException(
                    "Phone is required");
        }

        if (request.getAddressLine() == null
                || request.getAddressLine().isBlank()) {

            throw new RuntimeException(
                    "Address line is required");
        }

        if (request.getCity() == null
                || request.getCity().isBlank()) {

            throw new RuntimeException(
                    "City is required");
        }

        if (request.getState() == null
                || request.getState().isBlank()) {

            throw new RuntimeException(
                    "State is required");
        }

        if (request.getPostalCode() == null
                || request.getPostalCode().isBlank()) {

            throw new RuntimeException(
                    "Postal code is required");
        }
    }
}

