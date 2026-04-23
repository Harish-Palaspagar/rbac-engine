package com.rbac.repository;

import com.rbac.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepo extends JpaRepository<Role, Long> {

    boolean existsByName(String name);

}
