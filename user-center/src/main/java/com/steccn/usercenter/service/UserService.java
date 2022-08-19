package com.steccn.usercenter.service;

import com.steccn.usercenter.dto.RoleDTO;
import com.steccn.usercenter.dto.UserDTO;
import com.steccn.usercenter.domain.SysUser;
import com.steccn.usercenter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/8
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    public Mono<SysUser> getUserById(Long userId) {
        return userRepository.getUserById(userId);
    }

    public Mono<SysUser> getUserByName(String username) {
        return userRepository.getUserByName(username);
    }

    public Mono<UserDTO> getUserInfoByUserId(Long userId) {
        return getUserById(userId)
                .flatMap(user -> roleService.getRolesByUserId(user.getId())
                        .flatMap(role -> permissionService.getPermissionsByRoleId(role.getId())
                                .collectList()
                                .map(permissions -> new RoleDTO(role, permissions)))
                        .collectList()
                        .map(roleDtoList -> new UserDTO(user, roleDtoList)));
    }

    public Mono<UserDTO> getUserInfoByUsername(String username) {
        return getUserByName(username)
                .flatMap(user -> roleService.getRolesByUserId(user.getId())
                        .flatMap(role -> permissionService.getPermissionsByRoleId(role.getId())
                                .collectList()
                                .map(permissions -> new RoleDTO(role, permissions)))
                        .collectList()
                        .map(roleDtoList -> new UserDTO(user, roleDtoList)));
    }

}
