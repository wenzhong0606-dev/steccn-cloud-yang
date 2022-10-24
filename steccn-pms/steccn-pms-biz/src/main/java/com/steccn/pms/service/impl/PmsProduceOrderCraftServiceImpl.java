package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.pms.domain.PmsProduceOrderCraft;
import com.steccn.pms.domain.PmsWorkOrderCraftStation;
import com.steccn.pms.dto.PmsProduceOrderCraftDTO;
import com.steccn.pms.dto.PmsWorkOrderCraftStationDTO;
import com.steccn.pms.service.PmsProduceOrderCraftService;
import com.steccn.pms.mapper.PmsProduceOrderCraftMapper;
import com.steccn.pms.service.PmsWorkOrderCraftStationService;
import com.steccn.pms.vo.PmsProduceOrderCraftVO;
import com.steccn.pms.vo.PmsWorkOrderCraftStationVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private final RemoteDictFeignApi remoteDictFeignApi;

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

    /**
     * 根据生产订单id查询工序信息
     *
     * @param id 生产订单id
     */
    @Override
    public List<PmsProduceOrderCraftVO> getCrafts(Long id) {
        //1.定义查询条件
        LambdaQueryWrapper<PmsProduceOrderCraft> eq = new LambdaQueryWrapper<PmsProduceOrderCraft>().eq(PmsProduceOrderCraft::getProduceOrderId, id);
        //2.获取数据
        List<PmsProduceOrderCraft> list = list(eq);
        if (CollUtil.isEmpty(list)) {
            return null;
        }
        //3.处理数据
        List<PmsProduceOrderCraftVO> craftVOList = Lists.newArrayList();
        //3.1处理单位数据
        Set<Long> dictIds = Sets.newHashSet();
        list.forEach(pmsProduceOrderCraft -> {
            //查询工位信息
            PmsProduceOrderCraftVO pmsProduceOrderCraftVO = new PmsProduceOrderCraftVO();
            BeanUtil.copyProperties(pmsProduceOrderCraft, pmsProduceOrderCraftVO);
            dictIds.add(pmsProduceOrderCraftVO.getTypeId());
            dictIds.add(pmsProduceOrderCraftVO.getUnitId());
            List<PmsWorkOrderCraftStation> stations = pmsWorkOrderCraftStationService.list(new LambdaQueryWrapper<PmsWorkOrderCraftStation>()
                    .eq(PmsWorkOrderCraftStation::getProduceOrderCraftId, pmsProduceOrderCraft.getId()));
            if (CollUtil.isNotEmpty(stations)) {
                pmsProduceOrderCraftVO.setStationList(BeanUtil.copyToList(stations, PmsWorkOrderCraftStationVO.class));
            }
            craftVOList.add(pmsProduceOrderCraftVO);
        });
        //3.2 获取字典数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
        if (CollUtil.isNotEmpty(craftVOList)) {
            craftVOList.forEach(pmsProduceOrderCraft -> {
                pmsProduceOrderCraft.setUnit(dictMap.get(pmsProduceOrderCraft.getUnitId()));
                pmsProduceOrderCraft.setType(dictMap.get(pmsProduceOrderCraft.getTypeId()));
            });
        }
        return craftVOList;
    }

    /**
     * 查询工序信息
     *
     * @param processIds
     * @return
     */
    @Override
    public Map<Long, PmsProduceOrderCraftVO> listById(List<Long> processIds) {
        Map<Long, PmsProduceOrderCraftVO> resMap = Maps.newHashMap();
        List<PmsProduceOrderCraft> pmsProduceOrderCrafts = Lists.newArrayList();
        if (CollUtil.isNotEmpty(processIds)) {
            pmsProduceOrderCrafts = listByIds(processIds);
        }
        List<PmsProduceOrderCraftVO> craftVOList = BeanUtil.copyToList(pmsProduceOrderCrafts, PmsProduceOrderCraftVO.class);
        craftVOList.forEach(pmsProduceOrderCraft -> {
            resMap.put(pmsProduceOrderCraft.getId(), pmsProduceOrderCraft);
        });
        return resMap;

    }

    @Override
    public Boolean delete(Integer id) {
        return removeById(id);
    }


}




