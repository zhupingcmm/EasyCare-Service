package com.hr.maternity.repository;

import com.hr.maternity.entity.UserRole;
import com.hr.maternity.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    /**
     * 根据用户ID查找用户角色关系
     */
    List<UserRole> findByUserId(String userId);

    /**
     * 根据角色ID查找用户角色关系
     */
    List<UserRole> findByRoleId(Integer roleId);

    /**
     * 查询用户的角色列表（带角色详情）
     */
    @Query("SELECT ur FROM UserRole ur " +
           "JOIN FETCH ur.role r " +
           "WHERE ur.userId = :userId")
    List<UserRole> findByUserIdWithRole(@Param("userId") String userId);

    /**
     * 删除用户的所有角色
     */
    void deleteByUserId(String userId);

    /**
     * 删除角色的所有用户关系
     */
    void deleteByRoleId(Integer roleId);

    /**
     * 检查用户是否拥有指定角色
     */
    boolean existsByUserIdAndRoleId(String userId, Integer roleId);
}
