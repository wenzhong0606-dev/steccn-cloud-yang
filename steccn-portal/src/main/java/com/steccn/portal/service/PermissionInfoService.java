package com.steccn.portal.service;

import com.steccn.portal.domain.PermissionInfo;
import com.steccn.portal.dto.PermissionInfoDTO;
import com.steccn.portal.repository.PermissonInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @desc 菜单权限业务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionInfoService {

    private final PermissonInfoRepository permissonInfoRepository;

    public Mono<PermissionInfoDTO> getPermissionByCode(String code) {
        //1.获取所有的菜单
        Flux<PermissionInfo> all = permissonInfoRepository.findAll();
        //2.根据code过滤菜单
        Flux<PermissionInfo> filter = all.filter(v -> code.equals(v.getCode()));
        //3.获取单个元素
        Mono<PermissionInfo> single = filter.single();
        //4.拷贝属性，返回
        Mono<PermissionInfoDTO> resMono = single.map(v -> {
            PermissionInfoDTO dto = new PermissionInfoDTO();
            BeanUtils.copyProperties(v, dto);
            return dto;
        });
        return resMono;
    }
}
