package com.ead.authuser.repositories;

import com.ead.authuser.models.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleModel, UUID>, JpaSpecificationExecutor<RoleModel> {

}
