package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.platform.domain.SysCustomerMaterial;
import com.steccn.platform.domain.SysCustomerMaterialInfo;
import com.steccn.platform.dto.SysCustomerMaterialPageDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.mapper.SysCustomerMaterialMapper;
import com.steccn.platform.service.SysCustomerMaterialInfoService;
import com.steccn.platform.service.SysCustomerMaterialService;
import com.steccn.platform.vo.SysCustomerMaterialSubVO;
import com.steccn.platform.vo.SysCustomerMaterialVO;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【sys_customer_material(客户物料表)】的数据库操作Service实现
 * @createDate 2023-03-03 10:04:02
 */
@Service
@RequiredArgsConstructor
public class SysCustomerMaterialServiceImpl extends ServiceImpl<SysCustomerMaterialMapper, SysCustomerMaterial>
        implements SysCustomerMaterialService {

    private final SysCustomerMaterialInfoService sysCustomerMaterialInfoService;
    private final RemoteDictFeignApi remoteDictFeignApi;

    @Override
    public Page<SysCustomerMaterialVO> pageList(SysCustomerMaterialPageDTO dto) {
        return this.baseMapper.pageList(new Page<>(dto.getCurrent(), dto.getSize()), dto);
    }

    @Override
    public SysCustomerMaterialVO info(Long id) {
        SysCustomerMaterial sysCustomerMaterial = getById(id);
        Assert.notNull(sysCustomerMaterial, "客户物料不存在");
        List<SysCustomerMaterialInfo> detailList = sysCustomerMaterialInfoService.list(new LambdaQueryWrapper<SysCustomerMaterialInfo>()
                .eq(SysCustomerMaterialInfo::getCmId, id));
        Assert.notEmpty(detailList, "客户物料明细不存在");
        SysCustomerMaterialVO resVo = BeanUtil.copyProperties(sysCustomerMaterial, SysCustomerMaterialVO.class);
        //获取所有unit的数据字典值
        Set<Long> dictIdSet = detailList.stream().map(SysCustomerMaterialInfo::getUnit).collect(Collectors.toSet());
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<SysCustomerMaterialSubVO> subVOList = Lists.newArrayList();
        detailList.forEach(ele -> {
            SysCustomerMaterialSubVO subVO = BeanUtil.copyProperties(ele, SysCustomerMaterialSubVO.class);
            subVO.setUnitName(dictMap.getOrDefault(subVO.getUnit(), Strings.EMPTY));
            subVOList.add(subVO);
        });
        resVo.setSubVOList(subVOList);
        return resVo;
    }
}




