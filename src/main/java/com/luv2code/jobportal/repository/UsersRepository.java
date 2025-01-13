package com.luv2code.jobportal.repository;

import com.luv2code.jobportal.entitiy.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<Users, Integer> {
}
