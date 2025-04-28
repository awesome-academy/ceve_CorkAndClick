package com.sun.wineshop.repository;

import com.sun.wineshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository  extends JpaRepository<User, Long> {

    Boolean existsUserByUsername(String username);

    Optional<User> findUserByUsername(String username);
}
