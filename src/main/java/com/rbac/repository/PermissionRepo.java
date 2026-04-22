package com.rbac.repository;

import com.rbac.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepo extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);
    boolean existsByName(String name);

}
