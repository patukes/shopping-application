package com.patukes.Repository;

import com.patukes.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmailAndPasswordAndUsertype(String email, String password, String usertype);
}
