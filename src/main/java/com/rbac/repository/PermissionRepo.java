package com.rbac.repository;

import com.rbac.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PermissionRepo extends JpaRepository<Permission, Long> {

    boolean existsByName(String name);

}
