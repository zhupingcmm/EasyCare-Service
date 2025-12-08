package com.hr.maternity.repository;

import com.hr.maternity.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenRepository extends JpaRepository<Token, UUID> {

    /**
     * 根据用户ID查找未撤销的令牌
     */
    List<Token> findByUserIdAndRevokedFalse(String userId);

    /**
     * 根据访问令牌查找
     */
    Optional<Token> findByOpAccTokenAndRevokedFalse(String opAccToken);

    /**
     * 根据刷新令牌查找
     */
    Optional<Token> findByOpRefTokenAndRevokedFalse(String opRefToken);

    /**
     * 撤销用户的所有令牌
     */
    @Modifying
    @Query("UPDATE Token t SET t.revoked = true, t.updatedAt = :updatedAt WHERE t.userId = :userId AND t.revoked = false")
    void revokeAllUserTokens(@Param("userId") String userId, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 清理过期的令牌
     */
    @Modifying
    @Query("DELETE FROM Token t WHERE t.expTime < :currentTime")
    void deleteExpiredTokens(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查找即将过期的令牌
     */
    @Query("SELECT t FROM Token t WHERE t.expTime BETWEEN :startTime AND :endTime AND t.revoked = false")
    List<Token> findTokensExpiringBetween(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
}
