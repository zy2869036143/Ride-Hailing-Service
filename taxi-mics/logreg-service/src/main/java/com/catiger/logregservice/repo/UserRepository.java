package com.catiger.logregservice.repo;

import com.catiger.logregservice.dao.UserDao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserDao, String> {
    @Override
    Optional<UserDao> findById(String s);

}
