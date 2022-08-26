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

import java.util.List;
import java.util.Optional;

/**
 * @desc 菜单权限业务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionInfoService {

    private final PermissonInfoRepository permissonInfoRepository;

    public Mono<Optional<Object>> getPermissionByCode(String code) {
        return permissonInfoRepository.findAll().filter(v ->code.equals(v.getCode()))
                .collectList().flatMap(pl -> Mono.just(Optional.ofNullable(translateDTO(pl))));
    }

    private PermissionInfoDTO translateDTO(List pl){
        if(pl.size() == 0)
            return null;
        PermissionInfo permissionInfo = (PermissionInfo) pl.get(0);
        PermissionInfoDTO dto = new PermissionInfoDTO();
        BeanUtils.copyProperties(permissionInfo, dto);
        return dto;
    }
}
