package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.pms.domain.PmsWorkOrderBom;
import com.steccn.pms.dto.PmsWorkOrderBomDTO;
import com.steccn.pms.service.PmsWorkOrderBomService;
import com.steccn.pms.mapper.PmsWorkOrderBomMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【pms_work_order_bom(生产工单-物料清单信息)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsWorkOrderBomServiceImpl extends ServiceImpl<PmsWorkOrderBomMapper, PmsWorkOrderBom>
        implements PmsWorkOrderBomService {

    /**
     * 保存物料信息
     * @param materialList
     * @param id
     */
    @Override
    public void saveMaterial(List<PmsWorkOrderBomDTO> materialList, Long id) {
        //定义数据集合
        List<PmsWorkOrderBom> bomList = Lists.newArrayList();
        //处理数据
        materialList.forEach(material -> {
            PmsWorkOrderBom pmsWorkOrderBom = new PmsWorkOrderBom();
            BeanUtil.copyProperties(material, pmsWorkOrderBom);
            pmsWorkOrderBom.setWorkProduceId(id);
            bomList.add(pmsWorkOrderBom);
        });
        //批量保存
        saveBatch(bomList);
    }
}




