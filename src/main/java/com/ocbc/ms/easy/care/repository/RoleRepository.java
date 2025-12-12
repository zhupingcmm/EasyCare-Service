package com.ocbc.ms.easy.care.repository;

import com.ocbc.ms.easy.care.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    /**
     * 根据角色名称查找角色
     */
    Optional<Role> findByName(String name);

    /**
     * 根据规范化角色名称查找角色
     */
    Optional<Role> findByNormalizedName(String normalizedName);

    /**
     * 检查角色名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查规范化角色名称是否存在
     */
    boolean existsByNormalizedName(String normalizedName);
}
