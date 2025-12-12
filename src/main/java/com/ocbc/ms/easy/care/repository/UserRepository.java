package com.ocbc.ms.easy.care.repository;

import com.ocbc.ms.easy.care.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 根据LAN账号查找用户
     */
    Optional<User> findByLanIdAndIsActiveTrue(String lanId);

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUserNameAndIsActiveTrue(String userName);

    /**
     * 根据规范化用户名查找用户
     */
    Optional<User> findByNormalizedUserNameAndIsActiveTrue(String normalizedUserName);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);

    /**
     * 检查LAN账号是否存在
     */
    boolean existsByLanIdAndIsActiveTrue(String lanId);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUserNameAndIsActiveTrue(String userName);

    /**
     * 查询用户及其角色信息
     */
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.userRoles ur " +
           "LEFT JOIN FETCH ur.role r " +
           "WHERE u.lanId = :lanId AND u.isActive = true")
    Optional<User> findByLanIdWithRoles(@Param("lanId") String lanId);
}
