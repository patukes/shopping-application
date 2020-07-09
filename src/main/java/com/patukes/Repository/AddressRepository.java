package com.patukes.Repository;

import com.patukes.model.Address;
import com.patukes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;



@Repository
@Transactional
public interface AddressRepository extends JpaRepository<Address, Long> {

    Address findByUser(User user);
}