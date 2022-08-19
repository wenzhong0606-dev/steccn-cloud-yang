package com.steccn.usercenter.service;

import com.steccn.usercenter.domain.Permission;
import com.steccn.usercenter.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/15
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    @Autowired
    private final PermissionRepository permissionRepository;


    public Flux<Permission> getPermissionsByRoleId(Integer roleId) {
        return permissionRepository.getPermissionsByRoleId(roleId);
    }
}
