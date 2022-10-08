package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmsProduceOrderCraft;
import com.steccn.pms.domain.PmsWorkOrderCraftStation;
import com.steccn.pms.dto.PmsProduceOrderCraftDTO;
import com.steccn.pms.dto.PmsWorkOrderCraftStationDTO;
import com.steccn.pms.service.PmsProduceOrderCraftService;
import com.steccn.pms.mapper.PmsProduceOrderCraftMapper;
import com.steccn.pms.service.PmsWorkOrderCraftStationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yequan
 * @description 针对表【pms_produce_order_craft(生产订单-工艺信息)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
@RequiredArgsConstructor
public class PmsProduceOrderCraftServiceImpl extends ServiceImpl<PmsProduceOrderCraftMapper, PmsProduceOrderCraft>
        implements PmsProduceOrderCraftService {

    private final PmsWorkOrderCraftStationService pmsWorkOrderCraftStationService;

    /**
     * 保存生产订单-工序信息
     *
     * @param processList
     * @param id          生产订单id
     */
    @Override
    public void saveCrafts(List<PmsProduceOrderCraftDTO> processList, Long id) {
        processList.forEach(craft -> {
            //1.处理工序信息数据
            PmsProduceOrderCraft model = new PmsProduceOrderCraft();
            BeanUtil.copyProperties(craft, model);
            model.setProduceOrderId(id);
            //2.保存工序数据
            save(model);
            //3.保存工位信息
            if (CollUtil.isNotEmpty(craft.getStationList())) {
                List<PmsWorkOrderCraftStationDTO> stationList = craft.getStationList();
                stationList.forEach(station -> {
                    PmsWorkOrderCraftStation pmsWorkOrderCraftStation = new PmsWorkOrderCraftStation();
                    BeanUtil.copyProperties(station, pmsWorkOrderCraftStation);
                    pmsWorkOrderCraftStation.setProduceOrderCraftId(model.getId());
                    pmsWorkOrderCraftStationService.save(pmsWorkOrderCraftStation);
                });
            }
        });
    }
}




