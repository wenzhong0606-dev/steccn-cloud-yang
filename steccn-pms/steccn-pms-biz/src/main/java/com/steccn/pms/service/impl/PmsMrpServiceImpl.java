package com.steccn.pms.service.impl;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.pms.domain.PmsMrp;
import com.steccn.pms.domain.PmsMrpInfo;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.dto.MesMaterialInventoryDTO;
import com.steccn.pms.dto.PmsMrpPageDTO;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.service.PmsMrpInfoService;
import com.steccn.pms.service.PmsMrpService;
import com.steccn.pms.mapper.PmsMrpMapper;
import com.steccn.pms.vo.PmsMrpVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.compress.utils.Sets;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_mrp(物料需求计划)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@Service
@RequiredArgsConstructor
public class PmsMrpServiceImpl extends ServiceImpl<PmsMrpMapper, PmsMrp>
        implements PmsMrpService {
    private final RemoteMessFeignApi remoteMessFeignApi;
    private final PmsMrpInfoService pmsMrpInfoService;

    @Override
    public Page<PmsMrpVO> pageList(PmsMrpPageDTO dto) {
        return null;
    }

    @Override
    public Boolean add(List<PmsMrp> list) {
        //1.保存主数据
        saveBatch(list);
        //2.处理从数据
        List<PmsMrpInfo> pmsMrpInfos = Lists.newArrayList();
        Set<Long> materialIds = Sets.newHashSet();
        list.forEach(pmsMrp -> {
            materialIds.add(pmsMrp.getMaterialId());

        });
        //3.获取物料的可用库存数量
        Map<Long, MesMaterialInventoryDTO> materialInventoryMap = remoteMessFeignApi.materialInventory(materialIds);
        MesMaterialInventoryDTO mesMaterialInventoryDTO = new MesMaterialInventoryDTO();
        list.forEach(pmsMrp -> {
            PmsMrpInfo pmsMrpInfo = new PmsMrpInfo();
            MesMaterialInventoryDTO inventoryDTO = materialInventoryMap.getOrDefault(pmsMrp.getMaterialId(), mesMaterialInventoryDTO);
            pmsMrpInfo.setAvailableQuantity(inventoryDTO.getAvailableQuantity());
            pmsMrpInfo.setMrpId(pmsMrp.getId());
            pmsMrpInfos.add(pmsMrpInfo);
        });
        return pmsMrpInfoService.saveBatch(pmsMrpInfos);
    }
}




