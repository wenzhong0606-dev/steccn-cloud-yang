package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsMaterialRequisitionInfo;
import com.steccn.pms.domain.PmsProduceOrderCraft;
import com.steccn.pms.dto.MaterialRequisitionDTO;
import com.steccn.pms.service.PmsMaterialRequisitionInfoService;
import com.steccn.pms.mapper.PmsMaterialRequisitionInfoMapper;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_material_requisition_info(生产领用单详情)】的数据库操作Service实现
 * @createDate 2022-11-02 10:10:08
 */
@Service
public class PmsMaterialRequisitionInfoServiceImpl extends ServiceImpl<PmsMaterialRequisitionInfoMapper, PmsMaterialRequisitionInfo>
        implements PmsMaterialRequisitionInfoService {

    @Override
    public Boolean editList(MaterialRequisitionDTO materialRequisitionDTO) {
        //1.定义修改的数据集合和新增的数据集合
        List<PmsMaterialRequisitionInfo> list = Lists.newArrayList();
        Set<Long> idSet = Sets.newHashSet();
        //2.处理数据
        materialRequisitionDTO.getList().forEach(materialRequisitionInfoDTO -> {
            PmsMaterialRequisitionInfo materialRequisitionInfo = new PmsMaterialRequisitionInfo();
            BeanUtil.copyProperties(materialRequisitionInfoDTO, materialRequisitionInfo);
            materialRequisitionInfo.setMaterialRequisitionId(materialRequisitionDTO.getId());
            if (Objects.nonNull(materialRequisitionInfo.getId())) {
                list.add(materialRequisitionInfo);
                idSet.add(materialRequisitionInfo.getId());
            } else {
                list.add(materialRequisitionInfo);
            }
        });
        //3.删除其他多余的数据
        remove(new LambdaQueryWrapper<PmsMaterialRequisitionInfo>()
                .eq(PmsMaterialRequisitionInfo::getMaterialRequisitionId, materialRequisitionDTO.getId())
                .notIn(CollUtil.isNotEmpty(idSet),PmsMaterialRequisitionInfo::getId, idSet));
        //4.批量修改新增
        return saveOrUpdateBatch(list);
    }
}




