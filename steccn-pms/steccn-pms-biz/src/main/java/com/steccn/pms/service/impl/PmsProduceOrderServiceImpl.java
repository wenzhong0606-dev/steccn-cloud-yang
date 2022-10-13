package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.*;
import com.steccn.pms.mapper.PmsProduceOrderMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.prefs.BackingStoreException;

/**
 * @author yequan
 * @description 针对表【pms_produce_order(生产订单)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
@RequiredArgsConstructor
public class PmsProduceOrderServiceImpl extends ServiceImpl<PmsProduceOrderMapper, PmsProduceOrder>
        implements PmsProduceOrderService {

    private final PmsProduceOrderCraftService pmsProduceOrderCraftService;
    private final PmsWorkOrderBomService pmsWorkOrderBomService;
    private final PmsWorkOrderCraftStationService pmsWorkOrderCraftStationService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final PmsWorkOrderBatchService pmsWorkOrderBatchService;
    private final PmsCompleteStorageService pmsCompleteStorageService;

    /**
     * 新增生产订单信息
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public Boolean add(PmsProduceOrderDTO dto) {
        //1.校验数据是否存在
        if (Objects.isNull(dto)) {
            throw new BusinessException("订单信息为空");
        }
        //2.定义数据模型
        PmsProduceOrder model = new PmsProduceOrder();
        BeanUtil.copyProperties(dto, model);
        //3.给当前订单生成编码
        model.setCode(PmsCodeUtil.getProduceOrderCode(PmsCodeUtil.PREFIX_PMS_PRODUCE_ORDER, PmsCodeUtil.NUM_BIT_4, currentMaxCode()));
        //4.保存订单信息
        //4.1.保存基础信息
        save(model);
        //4.2保存工序信息
        if (CollUtil.isNotEmpty(dto.getProcessList())) {
            pmsProduceOrderCraftService.saveCrafts(dto.getProcessList(), model.getId());
        }
        //4.3保存物料信息
        if (CollUtil.isNotEmpty(dto.getMaterialList())) {
            pmsWorkOrderBomService.saveMaterial(dto.getMaterialList(), model.getId());
        }
        return true;
    }

    @Override
    public Page<PmsProduceOrderVO> pageList(PmsProduceOrderPageDTO dto) {
        //1.将日期拼接最大时间和最小时间
        //计划开始时间-开始
        if (Objects.nonNull(dto.getPlanStartDateStart())) {
            dto.setPlanStartTimeStart(LocalDateTime.of(dto.getPlanStartDateStart(), LocalTime.MIN));
        }
        //计划开始时间-结束
        if (Objects.nonNull(dto.getPlanStartDateEnd())) {
            dto.setPlanStartTimeEnd(LocalDateTime.of(dto.getPlanStartDateEnd(), LocalTime.MAX));
        }
        //计划结束时间-开始
        if (Objects.nonNull(dto.getPlanEndDateStart())) {
            dto.setPlanEndTimeStart(LocalDateTime.of(dto.getPlanEndDateStart(), LocalTime.MIN));
        }
        //计划结束时间-结束
        if (Objects.nonNull(dto.getPlanEndDateEnd())) {
            dto.setPlanEndTimeEnd(LocalDateTime.of(dto.getPlanEndDateEnd(), LocalTime.MAX));
        }
        //实际开始时间-开始
        if (Objects.nonNull(dto.getActualStartDateStart())) {
            dto.setActualStartTimeStart(LocalDateTime.of(dto.getActualStartDateStart(), LocalTime.MIN));
        }
        //实际开始时间-结束
        if (Objects.nonNull(dto.getActualStartDateEnd())) {
            dto.setActualStartTimeEnd(LocalDateTime.of(dto.getActualStartDateEnd(), LocalTime.MAX));
        }
        //实际结束时间-开始
        if (Objects.nonNull(dto.getActualEndDateStart())) {
            dto.setActualEndTimeStart(LocalDateTime.of(dto.getActualEndDateStart(), LocalTime.MIN));
        }
        //实际结束时间-结束
        if (Objects.nonNull(dto.getActualEndDateEnd())) {
            dto.setActualEndTimeEnd(LocalDateTime.of(dto.getActualEndDateEnd(), LocalTime.MAX));
        }
        //2.自定义sql查询订单信息列表
        Page<PmsProduceOrderVO> page = this.baseMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
        //3.处理数据
        page.getRecords().forEach(
                pmsProduceOrderVO -> {
                    //获取工序信息
                    pmsProduceOrderVO.setProcessList(pmsProduceOrderCraftService.getCrafts(pmsProduceOrderVO.getId()));
                    //获取物料信息
                    pmsProduceOrderVO.setMaterialList(pmsWorkOrderBomService.getMaterialList(pmsProduceOrderVO.getId()));
                }
        );
        return page;
    }

    /**
     * 编辑物料清单
     *
     * @return
     */
    @Override
    public Boolean edit(PmsProduceOrderDTO dto) {
        if (Objects.nonNull(dto)) {
            PmsProduceOrder pmsProduceOrder = new PmsProduceOrder();
            BeanUtil.copyProperties(dto, pmsProduceOrder);
            updateById(pmsProduceOrder);
            // 获取工序信息
            if (CollUtil.isNotEmpty(dto.getProcessList())) {
                dto.getProcessList().forEach(
                        pmsProduceOrderCraftDTO -> {
                            PmsProduceOrderCraft craft = new PmsProduceOrderCraft();
                            BeanUtil.copyProperties(pmsProduceOrderCraftDTO, craft);
                            craft.setProduceOrderId(dto.getId());
                            if (Objects.nonNull(craft.getId())) {
                                //修改工序信息
                                pmsProduceOrderCraftService.updateById(craft);
                                //处理工位数据
                                if (CollUtil.isNotEmpty(pmsProduceOrderCraftDTO.getStationList())) {
                                    pmsProduceOrderCraftDTO.getStationList().forEach(station -> {
                                        PmsWorkOrderCraftStation pmsWorkOrderCraftStation = new PmsWorkOrderCraftStation();
                                        BeanUtil.copyProperties(station, pmsWorkOrderCraftStation);
                                        pmsWorkOrderCraftStation.setProduceOrderCraftId(craft.getId());
                                        if (Objects.nonNull(pmsWorkOrderCraftStation.getId())) {
                                            pmsWorkOrderCraftStationService.updateById(pmsWorkOrderCraftStation);
                                        } else {
                                            pmsWorkOrderCraftStationService.save(pmsWorkOrderCraftStation);
                                        }
                                    });
                                }
                            } else {
                                //新增工序信息
                                craft.setProduceOrderId(dto.getId());
                                pmsProduceOrderCraftService.save(craft);
                                //处理工位数据
                                if (CollUtil.isNotEmpty(pmsProduceOrderCraftDTO.getStationList())) {
                                    List<PmsWorkOrderCraftStation> stations = Lists.newArrayList();
                                    pmsProduceOrderCraftDTO.getStationList().forEach(station -> {
                                        PmsWorkOrderCraftStation pmsWorkOrderCraftStation = new PmsWorkOrderCraftStation();
                                        pmsWorkOrderCraftStation.setId(craft.getId());
                                        BeanUtil.copyProperties(station, pmsWorkOrderCraftStation);
                                        stations.add(pmsWorkOrderCraftStation);
                                    });
                                    //保存工位数据
                                    pmsWorkOrderCraftStationService.saveBatch(stations);
                                }
                            }
                        }
                );
            }
            //获取物料信息
            List<PmsWorkOrderBom> bomList = Lists.newArrayList();
            if (CollUtil.isNotEmpty(dto.getMaterialList())) {
                dto.getMaterialList().forEach(material -> {
                    PmsWorkOrderBom pmsWorkOrderBom = new PmsWorkOrderBom();
                    BeanUtil.copyProperties(material, pmsWorkOrderBom);
                    pmsWorkOrderBom.setWorkProduceId(dto.getId());
                    if (Objects.nonNull(material.getId())) {
                        pmsWorkOrderBomService.updateById(pmsWorkOrderBom);
                    } else {
                        bomList.add(pmsWorkOrderBom);
                    }
                });
            }
            pmsWorkOrderBomService.saveBatch(bomList);
        }
        return true;
    }

    /**
     * 获取订单批次信息
     *
     * @param dto
     * @return
     */
    @Override
    public Page<PmsWorkOrderBatchVO> batchPageList(PmsProduceOrderBatchDTO dto) {
        //1.定义查询条件
        LambdaQueryWrapper<PmsWorkOrderBatch> eq = new LambdaQueryWrapper<PmsWorkOrderBatch>().eq(PmsWorkOrderBatch::getProduceOrderId, dto.getProduceOrderId());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = pmsWorkOrderBatchService.page(page, eq);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<PmsWorkOrderBatchVO> res = BeanUtil.copyToList(resPage.getRecords(), PmsWorkOrderBatchVO.class);
        //6.处理数据
        Set<Long> dictIds = Sets.newHashSet();
        res.forEach(pmsWorkOrderBatchVO -> {
            dictIds.add(pmsWorkOrderBatchVO.getUnitId());
        });
        //查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
        res.forEach(pmsWorkOrderBatchVO -> pmsWorkOrderBatchVO.setUnit(dictMap.get(pmsWorkOrderBatchVO.getUnitId())));
        return page.setRecords(res);
    }

    @Override
    public ReportWorkOrderVO reportWorkOrder(Integer productLineId, Integer stationId) {
        //1.根据产线查询订单
        PmsProduceOrder order = getOne(new LambdaQueryWrapper<PmsProduceOrder>()
                .eq(PmsProduceOrder::getProductionLineId, productLineId)
                .orderByDesc(PmsProduceOrder::getCreateTime)
                .last(MpConstant.STR_LIMIT_1));
        Assert.notNull(order, "该产线暂无订单");
        return reportWorkOrder(order, stationId);
    }

    @Override
    public ReportWorkOrderVO reportWorkOrderId(Long orderId, Integer stationId) {
        PmsProduceOrder order = getById(orderId);
        Assert.notNull(order, "该产线暂无订单");
        return reportWorkOrder(order, stationId);
    }

    /**
     * 订单接收
     *
     * @param dto
     * @return
     */
    @Override
    public Page<PmsProduceOrderVO> acceptPage(ProduceOrderAcceptPageDTO dto) {
        //1.自定义sql查询订单接收数据
        Page<PmsProduceOrderVO> page = this.baseMapper.acceptPage(new Page(dto.getCurrent(), dto.getSize()), dto);
        //2.处理订单数据
        Set<Long> dictIds = Sets.newHashSet();
        page.getRecords().forEach(pmsProduceOrderVO -> {
            dictIds.add(pmsProduceOrderVO.getUnitId());
            dictIds.add(pmsProduceOrderVO.getTypeId());
        });
        //3.查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
        //4.处理返回数据
        page.getRecords().forEach(pmsProduceOrderVO -> {
            pmsProduceOrderVO.setUnit(dictMap.get(pmsProduceOrderVO.getUnitId()));
            pmsProduceOrderVO.setType(dictMap.get(pmsProduceOrderVO.getTypeId()));
        });
        return page;
    }

    /**
     * 订单下达
     *
     * @param orderId
     * @return
     */
    @Override
    public Boolean placeAnOrder(Long orderId) {
        //1.根据订单id查询订单
        PmsProduceOrder order = getOne(new LambdaQueryWrapper<PmsProduceOrder>().eq(PmsProduceOrder::getId, orderId));
        Assert.notNull(order, "该订单不存在");
        //2.校验所有工序是否已分配工位
        //2.1 获取订单工序数据
        List<PmsProduceOrderCraftVO> crafts = pmsProduceOrderCraftService.getCrafts(orderId);
        if (CollUtil.isEmpty(crafts)) {
            new BackingStoreException("该订单不存在工序数据，请检查");
        }
        crafts.forEach(craft -> {
            if (CollUtil.isEmpty(craft.getStationList())) {
                new BackingStoreException("工序未分配工位，请分配工位");
            }
        });
        //3.校验所有组件是否已分配工序
        //3.1 获取组件数据
        List<PmsWorkOrderBomVO> materialList = pmsWorkOrderBomService.getMaterialList(orderId);
        if (CollUtil.isEmpty(materialList)) {
            new BackingStoreException("该订单不存在组件,请检查");
        }
        materialList.forEach(material -> {
            Assert.notNull(material.getProcessId(), "组件未分配工序，请检查");
        });
        //4.修改订单状态为已下达
        order.setState(PmManagerConstant.STATE_ONE);
        updateById(order);
        return Boolean.TRUE;
    }

    @Override
    public Page<PmsProduceOrderVO> reportWorkOrderPage(ReportWorkOrderPageDTO dto) {
        Page<PmsProduceOrderVO> page = this.baseMapper.reportWorkOrderPage(new Page<>(dto.getCurrent(), dto.getSize()), dto);
        if (CollUtil.isNotEmpty(page.getRecords())) {
            List<PmsProduceOrderVO> records = page.getRecords();
            Set<Long> orderSet = Sets.newHashSet();
            Set<Long> dictSet = Sets.newHashSet();
            for(PmsProduceOrderVO produceOrderVO : records){
                orderSet.add(produceOrderVO.getId());
                dictSet.add(produceOrderVO.getUnitId());
            }
            orderSet = records.stream().map(PmsProduceOrderVO::getId).collect(Collectors.toSet());
            Map<Long, PmsCompleteStorageNumDTO> orderMap = pmsCompleteStorageService.orderCompleteNum(orderSet);
            if(CollUtil.isNotEmpty(orderMap)){
                Set<Long> orderDictSet = orderMap.values().stream().map(PmsCompleteStorageNumDTO::getUnitId).collect(Collectors.toSet());
                dictSet.addAll(orderDictSet);
            }
            //获取数据字典数据
            Map<Long, String> dictChMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictSet)).get();

            PmsCompleteStorageNumDTO defaultPmsCompleteStorageNumDTO = new PmsCompleteStorageNumDTO();
            for (PmsProduceOrderVO order : records) {
                PmsCompleteStorageNumDTO numDto = orderMap.getOrDefault(order.getId(), defaultPmsCompleteStorageNumDTO);
                order.setCompleteQuantity(numDto.getQuantity());
                order.setCompleteUnitId(numDto.getUnitId());
                order.setCompleteUnitName(dictChMap.get(numDto.getUnitId()));
                order.setUnit(dictChMap.get(order.getUnitId()));
            }
        }
        return page;
    }

    /**
     * 报工订单批次详情
     *
     * @param order     订单信息
     * @param stationId 工位id
     * @return 批次详情
     */
    private ReportWorkOrderVO reportWorkOrder(PmsProduceOrder order, Integer stationId) {
        //1.根据订单查询当前批次
        PmsWorkOrderBatch currentBatch = pmsWorkOrderBatchService.getOne(new LambdaQueryWrapper<PmsWorkOrderBatch>()
                .eq(PmsWorkOrderBatch::getProduceOrderId, order.getId())
                .eq(PmsWorkOrderBatch::getCurrentBatchFlag, MpConstant.YES));
        Assert.notNull(currentBatch, "订单" + order.getCode() + "无当前批次");
        //2.查询工位
        PmsWorkOrderCraftStation station = pmsWorkOrderCraftStationService.getOne(new LambdaQueryWrapper<PmsWorkOrderCraftStation>()
                .eq(PmsWorkOrderCraftStation::getExecuteStationId, stationId)
                .last(MpConstant.STR_LIMIT_1));
        Assert.notNull(station, "该工位暂无工序配置");
        //3.查询工序
        PmsProduceOrderCraft craft = pmsProduceOrderCraftService.getById(station.getProduceOrderCraftId());
        Assert.notNull(craft, "工序不存在");
        //4.定义返回数据
        ReportWorkOrderVO vo = new ReportWorkOrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setProduceOrderId(order.getId());
        vo.setProduceOrderCode(order.getCode());
        vo.setBatch(currentBatch.getId());
        vo.setBatchNo(currentBatch.getCode());
        vo.setProcessId(craft.getId());
        vo.setProcessName(craft.getName());
        vo.setStationId(station.getExecuteStationId());
        vo.setStationName(station.getExecuteStationName());
        return vo;
    }


    /**
     * 当前最大的code值
     *
     * @return 数值类型，可能为null
     */
    private Long currentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        PmsProduceOrder maxCodeModel = getOne(new LambdaQueryWrapper<PmsProduceOrder>().orderByDesc(PmsProduceOrder::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(PmsCodeUtil.PREFIX_PMS_PRODUCE_ORDER.length() + PmsCodeUtil.NUM_BIT_8);
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }
}




