package com.jwt.springBoot.JWTEx.repository;

import com.jwt.springBoot.JWTEx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsername(String userName);

    boolean existsByUsernameIgnoreCase(String userName);

    boolean existsByEmailIgnoreCase(String email);

    void deleteByUsername(String userName);
}
