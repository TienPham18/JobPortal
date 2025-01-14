package com.luv2code.jobportal.repository;

import com.luv2code.jobportal.entitiy.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Integer> {

    Optional<Users> findByEmail(String email); // SELECT * FROM users WHERE email = ?;

    /*
    Spring Data JPA:
    1. Parses the method name (findByEmail).
    2. Understands that it corresponds to a query on the email field of the Users entity.
    3. Automatically generates a query like: sql
    SELECT * FROM users WHERE email = ?;
     */
}
