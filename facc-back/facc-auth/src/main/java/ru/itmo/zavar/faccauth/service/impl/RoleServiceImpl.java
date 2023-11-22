package ru.itmo.zavar.faccauth.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.faccauth.entity.RoleEntity;
import ru.itmo.zavar.faccauth.repo.RoleRepository;
import ru.itmo.zavar.faccauth.service.RoleService;
import ru.itmo.zavar.faccauth.util.RoleConstants;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    @Override
    public Optional<RoleEntity> findByName(String name) {
        return roleRepository.findByName(name);
    }

    @Override
    public Optional<RoleEntity> getAdminRole() {
        return findByName(RoleConstants.ADMIN);
    }

    @Override
    public Optional<RoleEntity> getUserRole() {
        return findByName(RoleConstants.USER);
    }

    @Override
    public RoleEntity saveRole(RoleEntity roleEntity) {
        return roleRepository.save(roleEntity);
    }
}
