package com.rbac.repository;

import com.rbac.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepo extends JpaRepository<UserRole, Long> {

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    @Query("SELECT ur.role.id FROM UserRole ur WHERE ur.user.id = :userId")
    List<Long> findRoleIdsByUserId(@Param("userId") Long userId);

}