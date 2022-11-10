package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.pms.domain.PmsCompleteDelivery;
import com.steccn.pms.domain.PmsCompleteDeliveryInfo;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.service.*;
import com.steccn.pms.mapper.PmsCompleteDeliveryMapper;
import com.steccn.pms.vo.PmsCompleteDeliveryVO;
import com.steccn.pms.vo.PmsProduceOrderCraftVO;
import com.steccn.pms.vo.ProduceOrderProcessNumVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_complete_delivery(生产订单-出库)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsCompleteDeliveryServiceImpl extends ServiceImpl<PmsCompleteDeliveryMapper, PmsCompleteDelivery>
        implements PmsCompleteDeliveryService {


    @Autowired
    private PmsProduceOrderCraftService pmsProduceOrderCraftService;
    @Autowired
    private PmsCompleteDeliveryInfoService pmsCompleteDeliveryInfoService;
    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private RemoteSpareFeignApi remoteSpareFeignApi;


    @Override
    public Page<PmsCompleteDeliveryVO> pageList(PmsCompleteDeliveryDTO dto) {
        //1.自定义Mapper查询投料信息
        Page<PmsCompleteDeliveryVO> page = this.baseMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
        return page;
    }

    @Override
    public Page<PmsCompleteDeliveryVO> completeDeliveryPageList(PmsCompleteDeliveryDTO dto) {
        //1.查询详情信息
        Page<PmsCompleteDeliveryVO> page = pmsCompleteDeliveryInfoService.listByOrderId(dto);
        //2.处理数据
        List<Long> materialIds = Lists.newArrayList();
        Set<Integer> office = Sets.newHashSet();
        Set<Long> ids = Sets.newHashSet();
        List<Long> processIds = Lists.newArrayList();
        List<PmsCompleteDeliveryVO> records = page.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            records.forEach(pmsCompleteDeliveryVO -> {
                materialIds.add(pmsCompleteDeliveryVO.getMaterialId());
                ids.add(pmsCompleteDeliveryVO.getUnitId());
                ids.add(pmsCompleteDeliveryVO.getTypeId());
                if (Objects.nonNull(pmsCompleteDeliveryVO.getWarehouseId())) {
                    office.add(pmsCompleteDeliveryVO.getWarehouseId());
                }
                if (Objects.nonNull(pmsCompleteDeliveryVO.getShelfId())) {
                    office.add(pmsCompleteDeliveryVO.getShelfId());
                }
                processIds.add(pmsCompleteDeliveryVO.getProcessId());
            });
            //获取单位数据
            Map<Long, String> dictData = FeignOptional.of(remoteDictFeignApi.listByIdCh(ids)).get();
            //获取仓库货架信息
            Map<Integer, String> officeMapData = FeignOptional.of(remoteOfficeFeign.listByIdCh(office)).get();
            //获取工序信息
            Map<Long, PmsProduceOrderCraftVO> processMap = pmsProduceOrderCraftService.listById(processIds);
            PmsProduceOrderCraftVO defaultCraftVO = new PmsProduceOrderCraftVO();
            if (CollUtil.isNotEmpty(records)) {
                records.forEach(
                        pmsCompleteDeliveryVO -> {
                            PmsProduceOrderCraftVO pmsProduceOrderCraftVO = processMap.getOrDefault(pmsCompleteDeliveryVO.getProcessId(), defaultCraftVO);
                            //处理单位数据
                            pmsCompleteDeliveryVO.setUnit(dictData.get(pmsCompleteDeliveryVO.getUnitId()));
                            pmsCompleteDeliveryVO.setTypeName(dictData.get(pmsCompleteDeliveryVO.getTypeId()));
                            //处理工序数据
                            pmsCompleteDeliveryVO.setProcessName(pmsProduceOrderCraftVO.getName());
                            //处理仓库货架信息
                            pmsCompleteDeliveryVO.setWarehouseName(officeMapData.get(pmsCompleteDeliveryVO.getWarehouseId()));
                            pmsCompleteDeliveryVO.setShelfName(officeMapData.get(pmsCompleteDeliveryVO.getShelfId()));
                        }
                );
            }
        }
        return page;
    }

    @Override
    public Page<PmsCompleteDeliveryVO> feedPageList(PmsCompleteDeliveryDTO dto) {
        //1.查询投料信息
        Page<PmsCompleteDeliveryVO> page = pageList(dto);
        //2.处理数据
        Set<Long> unitIds = Sets.newHashSet();
        Set<Integer> officeIds = Sets.newHashSet();
        if (CollUtil.isNotEmpty(page.getRecords())) {
            page.getRecords().forEach(pmsCompleteDeliveryVO -> {
                unitIds.add(pmsCompleteDeliveryVO.getUnitId());
                officeIds.add(pmsCompleteDeliveryVO.getShelfId());
                officeIds.add(pmsCompleteDeliveryVO.getWarehouseId());
            });
            //查询字典数据
            Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(unitIds)).get();
            //查询机构数据
            Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIds)).get();
            page.getRecords().forEach(pmsCompleteDeliveryVO -> {
                pmsCompleteDeliveryVO.setUnit(dictMap.get(pmsCompleteDeliveryVO.getUnitId()));
                pmsCompleteDeliveryVO.setWarehouseName(officeMap.get(pmsCompleteDeliveryVO.getWarehouseId()));
            });
        }
        return page;
    }

    @Override
    public Map<Long, Map<Long, Map<Long, ProduceOrderProcessNumVO>>> processNumConsumeMap(Set<Long> produceOrderIdSet) {
        //定义返回结构
        Map<Long, Map<Long, Map<Long, ProduceOrderProcessNumVO>>> resMap = Maps.newHashMap();
        //如果生产订单不为空
        if (CollUtil.isNotEmpty(produceOrderIdSet)) {
            //查询分组数据
            List<ProduceOrderProcessNumVO> dataList = this.baseMapper.processNumConsumeMap(produceOrderIdSet);
            //分组数据不为空
            if (CollUtil.isNotEmpty(dataList)) {
                //根据生产订单id分组  key-生产订单id value-分组数据
                Map<Long, List<ProduceOrderProcessNumVO>> produceOrderMap = dataList.stream().collect(Collectors.groupingBy(ProduceOrderProcessNumVO::getProduceOrderId));
                for (Map.Entry<Long, List<ProduceOrderProcessNumVO>> produceOrderEntry : produceOrderMap.entrySet()) {
                    //根据工序分组  key-工序id value-工序map
                    Map<Long, List<ProduceOrderProcessNumVO>> processMap = produceOrderEntry.getValue().stream().collect(Collectors.groupingBy(ProduceOrderProcessNumVO::getProcessId));
                    Map<Long, Map<Long, ProduceOrderProcessNumVO>> processResMap = Maps.newHashMap();
                    for (Map.Entry<Long, List<ProduceOrderProcessNumVO>> processEntry : processMap.entrySet()) {
                        Map<Long, ProduceOrderProcessNumVO> materialResMap = Maps.newHashMap();
                        for (ProduceOrderProcessNumVO materialVO : processEntry.getValue()) {
                            materialResMap.put(materialVO.getMaterialId(), materialVO);
                        }
                        processResMap.put(processEntry.getKey(), materialResMap);
                    }
                    resMap.put(produceOrderEntry.getKey(), processResMap);
                }
            }
        }
        return resMap;
    }
}




