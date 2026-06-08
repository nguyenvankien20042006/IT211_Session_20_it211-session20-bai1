package com.example.bai1.repository;

import com.example.bai1.model.entity.Token;
import com.example.bai1.model.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);

    @Query("""
            update Token t set t.expired = true, t.revoked = true where t.user = :user
            """)
    void logout(Employee user);
}
