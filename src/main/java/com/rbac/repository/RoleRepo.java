package com.rbac.repository;

import com.rbac.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepo extends JpaRepository<Role, Long> {

    boolean existsByName(String name);

}
