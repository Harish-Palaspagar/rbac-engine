package com.rbac.repository;

import com.rbac.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepo extends JpaRepository<RolePermission, Long> {

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    Optional<RolePermission> findByRoleIdAndPermissionId(Long roleId, Long permissionId);

    List<RolePermission> findByRoleId(Long roleId);

    @Query("SELECT rp.permission.name FROM RolePermission rp WHERE rp.role.id IN :roleIds")
    List<String> findPermissionNamesByRoleIds(@Param("roleIds") List<Long> roleIds);

}
