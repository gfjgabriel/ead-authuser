package com.ead.authuser.services.impl;

import com.ead.authuser.repositories.RoleRepository;
import com.ead.authuser.services.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository repository;
}
