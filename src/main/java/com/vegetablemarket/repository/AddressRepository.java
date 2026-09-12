package com.vegetablemarket.repository;

import com.vegetablemarket.entity.Address;
import com.vegetablemarket.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUser(User user);

    List<Address> findByUserOrderByDefaultAddressDescIdDesc(User user);
}
