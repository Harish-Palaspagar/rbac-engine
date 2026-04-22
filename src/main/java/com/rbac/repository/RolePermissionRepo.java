package com.rbac.repository;

import com.rbac.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePermissionRepo extends JpaRepository<RolePermission, Long> {

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    @Query("SELECT rp.permission.name FROM RolePermission rp WHERE rp.role.id IN :roleIds")
    List<String> findPermissionNamesByRoleIds(@Param("roleIds") List<Long> roleIds);

}
