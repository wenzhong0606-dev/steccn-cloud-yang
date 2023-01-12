package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.pms.domain.PmsCompleteDelivery;
import com.steccn.pms.domain.PmsProduceOrderCraft;
import com.steccn.pms.domain.PmsWorkOrderBom;
import com.steccn.pms.dto.PmsCompleteDeliveryDTO;
import com.steccn.pms.dto.PmsDayReportDTO;
import com.steccn.pms.service.*;
import com.steccn.pms.mapper.PmsCompleteDeliveryMapper;
import com.steccn.pms.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
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
    @Autowired
    private PmsWorkOrderBomService pmsWorkOrderBomService;


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
            Map<Long, String> dictData = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(ids), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取仓库货架信息
            Map<Integer, String> officeMapData = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(office), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(unitIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //查询机构数据
            Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            page.getRecords().forEach(pmsCompleteDeliveryVO -> {
                pmsCompleteDeliveryVO.setUnit(dictMap.get(pmsCompleteDeliveryVO.getUnitId()));
                pmsCompleteDeliveryVO.setWarehouseName(officeMap.get(pmsCompleteDeliveryVO.getWarehouseId()));
            });
        }
        return page;
    }

    @Override
    public Map<Long, Map<Long, Map<Long, ProduceOrderProcessNumVO>>> processNumConsumeMap(Set<Long> produceOrderIdSet) {
        //定义返回结构 key-订单id value【key-工序id value【key-物料id，value-数据对象】】
        Map<Long, Map<Long, Map<Long, ProduceOrderProcessNumVO>>> resMap = Maps.newHashMap();
        //如果生产订单不为空
        if (CollUtil.isNotEmpty(produceOrderIdSet)) {
            //查询分组数据
            List<ProduceOrderProcessNumVO> dataList = this.baseMapper.processNumConsumeMap(produceOrderIdSet);
            //分组数据不为空
            if (CollUtil.isNotEmpty(dataList)) {
                List<Long> craftProcessIdList = dataList.stream().map(ProduceOrderProcessNumVO::getProcessId).collect(Collectors.toList());
                Map<Long, PmsProduceOrderCraftVO> craftMap = pmsProduceOrderCraftService.listById(craftProcessIdList);

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
                            //基于生产订单的bom去查询计划数量
                            PmsWorkOrderBom one = pmsWorkOrderBomService.getOne(new LambdaQueryWrapper<PmsWorkOrderBom>()
                                    .eq(PmsWorkOrderBom::getWorkProduceId, produceOrderEntry.getKey())
                                    .eq(Objects.nonNull(craftMap.get(processEntry.getKey())), PmsWorkOrderBom::getProcessId, craftMap.get(processEntry.getKey()).getProcessId())
                                    .eq(PmsWorkOrderBom::getModuleId, materialVO.getMaterialId()), false);
                            if (Objects.nonNull(one) && Objects.nonNull(one.getQuantity())) {
                                materialVO.setAvaQuantity(one.getQuantity());
                            } else {
                                materialVO.setAvaQuantity(BigDecimal.ZERO);
                            }
                        }
                        processResMap.put(processEntry.getKey(), materialResMap);
                    }
                    resMap.put(produceOrderEntry.getKey(), processResMap);
                }
            }
        }
        return resMap;
    }

    @Override
    public List<SubstrateIngredientVO> subIngredientReport(PmsDayReportDTO dto) {
        List<SubstrateIngredientVO> resList = Lists.newArrayList();

        List<SubstrateIngredientConsumeVO> dataList = this.baseMapper.subIngredientReport(dto.getProductLineIdList(), dto.getClassGroupIdList(), dto.getDate());
        if (CollUtil.isNotEmpty(dataList)) {
            dataList.forEach(ele -> {
                SubstrateIngredientVO vo = new SubstrateIngredientVO();
                BeanUtils.copyProperties(ele, vo);
                vo.setOutKg(ele.getQuantity());
                vo.setOutTon(BigDecimalUtil.divide(ele.getQuantity(), BigDecimal.valueOf(1000)));
                if (StrUtil.isNotBlank(ele.getProduceOrderIdStr())) {
                    String[] split = ele.getProduceOrderIdStr().split(",");
                    Set<Long> produceOrderIdSet = Sets.newHashSet();
                    for (String str : split) {
                        try {
                            produceOrderIdSet.add(Long.parseLong(str));
                        } catch (Exception e) {
                            log.error(e.getLocalizedMessage());
                        }
                    }
                    vo.setProduceOrderIdSet(produceOrderIdSet);
                }
                resList.add(vo);
            });
        }
        return resList;
    }
}




