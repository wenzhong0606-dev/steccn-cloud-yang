package com.steccn.usercenter.service;

import cn.hutool.core.util.IdUtil;
import com.steccn.usercenter.domain.SysUser;
import com.steccn.usercenter.dto.RoleDTO;
import com.steccn.usercenter.dto.UserDTO;
import com.steccn.usercenter.dto.UserSaveDTO;
import com.steccn.usercenter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

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

    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

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

    /**
     * 保存用户信息
     *
     * @param userSaveDTO 用户保存的dto
     * @return
     */
    public Mono<SysUser> saveUserInfo(UserSaveDTO userSaveDTO) {
        Mono<SysUser> userByName = userRepository.getUserByName(userSaveDTO.getUsername());
        Mono<SysUser> resSysUser = userByName.hasElement().flatMap(v -> {
            if (Objects.isNull(v) || !v) {
                log.info("v-> {}", v);
                SysUser sysUser = new SysUser();
                BeanUtils.copyProperties(userSaveDTO, sysUser);
                sysUser.setPassword(bCryptPasswordEncoder.encode(sysUser.getPassword()));
                Mono<SysUser> save = userRepository.save(sysUser);
                return save;
            }
            return userByName;
        });
        return resSysUser;
    }

}
