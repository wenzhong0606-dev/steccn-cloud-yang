package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
}




