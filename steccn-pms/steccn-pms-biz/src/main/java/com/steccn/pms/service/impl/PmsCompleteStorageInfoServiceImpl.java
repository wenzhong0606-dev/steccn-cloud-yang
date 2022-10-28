package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.pms.domain.PmsBarcodePrint;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.dto.PmsCompleteStorageNumDTO;
import com.steccn.pms.service.PmsCompleteStorageInfoService;
import com.steccn.pms.mapper.PmsCompleteStorageInfoMapper;
import com.steccn.pms.vo.PmsCompleteStorageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_complete_storage_info(完工入库-详情)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsCompleteStorageInfoServiceImpl extends ServiceImpl<PmsCompleteStorageInfoMapper, PmsCompleteStorageInfo>
        implements PmsCompleteStorageInfoService {

    @Override
    public Boolean existCaseNo(Set<String> caseNoSet) {
        long count = count(new LambdaQueryWrapper<PmsCompleteStorageInfo>()
                .in(PmsCompleteStorageInfo::getCaseNo, caseNoSet));
        return count != 0;
    }

    @Override
    public Page<PmsCompleteStorageVO> pageByOrderId(PmsCompleteStorageDTO dto) {
        return this.baseMapper.pageByOrderId(new Page(dto.getCurrent(), dto.getSize()), dto.getProduceOrderId());
    }

    @Override
    public Map<Long, PmsCompleteStorageNumDTO> numByOrderId(Set<Long> orderSet) {
        Map<Long, PmsCompleteStorageNumDTO> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(orderSet)) {
            List<PmsCompleteStorageInfo> list = list(new QueryWrapper<PmsCompleteStorageInfo>()
                    .select("produce_order_id,sum(quantity) as quantity ,unit_id")
                    .lambda().in(PmsCompleteStorageInfo::getProduceOrderId, orderSet).groupBy(PmsCompleteStorageInfo::getProduceOrderId));
            if (CollUtil.isNotEmpty(list)) {
                list.forEach( pmsCompleteStorageInfo -> {
                    PmsCompleteStorageNumDTO storageNumDTO = new PmsCompleteStorageNumDTO();
                    storageNumDTO.setProduceOrderId(pmsCompleteStorageInfo.getProduceOrderId());
                    storageNumDTO.setQuantity(pmsCompleteStorageInfo.getQuantity());
                    storageNumDTO.setUnitId(pmsCompleteStorageInfo.getUnitId());
                    resMap.put(pmsCompleteStorageInfo.getProduceOrderId(), storageNumDTO);
                });
            }
        }
        return resMap;
    }


    @Override
    public List<String> getCaseNo(Long id) {
        List<PmsCompleteStorageInfo> list = list(new LambdaQueryWrapper<PmsCompleteStorageInfo>().eq(PmsCompleteStorageInfo::getCompleteStorageId, id));
            List<String> caseNoList = list.stream().map(PmsCompleteStorageInfo::getCaseNo).collect(Collectors.toList());
        if (CollUtil.isEmpty(list)){
           throw new BusinessException("未找到完工入库-详情");
       }
        return caseNoList;
    }
}




