package com.hr.maternity.repository;

import com.hr.maternity.entity.Nonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NonceRepository extends JpaRepository<Nonce, String> {

    Optional<Nonce> findByNonceValueAndUserId(String nonceValue, String userId);

    Optional<Nonce> findByNonceValue(String nonceValue);

    boolean existsByNonceValueAndUserId(String nonceValue, String userId);

    @Modifying
    @Query("DELETE FROM Nonce n WHERE n.expiresAt < :now")
    int deleteExpiredNonces(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Nonce n WHERE n.used = true AND n.usedAt < :threshold")
    int deleteUsedNonces(@Param("threshold") LocalDateTime threshold);
}
