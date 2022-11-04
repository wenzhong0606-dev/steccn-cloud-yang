package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.pms.domain.PmBillOfMaterial;
import com.steccn.pms.domain.PmMaterial;
import com.steccn.pms.domain.PmProcessStation;
import com.steccn.pms.dto.PmBillOfMaterialDTO;
import com.steccn.pms.mapper.PmBillOfMaterialMapper;
import com.steccn.pms.service.PmMaterialService;
import com.steccn.pms.mapper.PmMaterialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* @author yequan
* @description 针对表【pm_material(物料清单-物料表)】的数据库操作Service实现
* @createDate 2022-09-02 17:01:20
*/
@Service
@RequiredArgsConstructor
public class PmMaterialServiceImpl extends ServiceImpl<PmMaterialMapper, PmMaterial>
    implements PmMaterialService{

    private final PmMaterialMapper pmMaterialMapper;

    @Override
    public Boolean saveMaterial(PmBillOfMaterialDTO dto) {
        List<PmMaterial> processStationList = new ArrayList<>();
        //保存可执行工位数据
        dto.getList().forEach(material -> {
            PmMaterial model = new PmMaterial();
            BeanUtil.copyProperties(material, model);
            model.setCreateBy(SecurityUtils.getUser().getUserId());
            model.setCreateName(SecurityUtils.getUser().getName());
            model.setBomId(dto.getId());
            processStationList.add(model);
        });
        return saveBatch(processStationList);
    }

    @Override
    public void deleteByMaterialId(Long id) {
        pmMaterialMapper.deleteByMaterialId(id);
    }

    @Override
    public Map<String, Long> selectIdByCode(List<String> codes) {
        Map<String, Long> resMap = Maps.newHashMap();

        LambdaQueryWrapper<PmMaterial> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(PmMaterial::getCode,codes);
        List<PmMaterial> pmMaterialList = list(queryWrapper);
        for (PmMaterial pmMaterial : pmMaterialList) {
            resMap.put(pmMaterial.getCode(), pmMaterial.getId());
        }
        return resMap;
    }
}




