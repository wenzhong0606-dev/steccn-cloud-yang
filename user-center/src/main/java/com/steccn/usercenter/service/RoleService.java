package com.steccn.usercenter.service;

import com.steccn.usercenter.domain.Role;
import com.steccn.usercenter.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/15
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {
    @Autowired
    private final RoleRepository roleRepository;

    public Mono<Role> getRoleById(Integer roleId){
        return roleRepository.getRoleById(roleId);
    }

    public Flux<Role> getRolesByUserId(Long UserId){
        return roleRepository.getRolesByUserId(UserId);
    }
}
