package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.feign.RemoteSpareUnitConvertFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.*;
import com.steccn.pms.mapper.PmsProduceOrderMapper;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.*;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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
    private final PmsBarcodePrintService pmsBarcodePrintService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final PmCraftRouteService pmCraftRouteService;
    private final PmsProducePlanService pmsProducePlanService;
    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;
    private final RedissonClient redissonClient;
    private final PmsReportWorkMapper pmsReportWorkMapper;
    private final RemoteSpareFeignApi remoteSpareFeignApi;

    /**
     * 新增生产订单信息
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public Boolean add(PmsProduceOrderDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_PMS_PRODUCE_ORDER);
        lock.lock();
        try {
            //1.校验数据是否存在
            Assert.isTrue(Objects.nonNull(dto), "订单信息为空");
            //2.定义数据模型
            PmsProduceOrder model = new PmsProduceOrder();
            BeanUtil.copyProperties(dto, model);
            //3.给当前订单生成编码
            model.setCode(PmsCodeUtil.getProduceOrderCode(PmsCodeUtil.PREFIX_PMS_PRODUCE_ORDER, PmsCodeUtil.NUM_BIT_4, currentMaxCode()));
            //4.保存订单信息
            //4.1.保存基础信息
            //获取货架信息
            if (Objects.nonNull(model.getDefaultShelfId())) {
                SysOfficeVO sysOfficeVO = FeignOptional.of(remoteOfficeFeign.parent(model.getDefaultShelfId())).get();
                model.setDefaultWarehouseId(sysOfficeVO.getId());
            }
            save(model);
            //4.2保存工序信息
            if (CollUtil.isNotEmpty(dto.getProcessList())) {
                pmsProduceOrderCraftService.saveCrafts(dto.getProcessList(), model.getId(), model.getProductionLineId());
            }
            //4.3保存物料信息
            if (CollUtil.isNotEmpty(dto.getMaterialList())) {
                pmsWorkOrderBomService.saveMaterial(dto.getMaterialList(), model.getId());
            }
            //判断是否是计划转生产订单
            if (PmManagerConstant.OrderStateEnum.ZERO.getCode().equals(dto.getPlanFlag())) {
                try {
                    BeanUtil.copyProperties(model, dto);
                    pmsProducePlanService.turnProduceOrder(dto);
                } catch (Exception e) {
                }
            }
        } finally {
            lock.unlock();
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
        Set<Long> dictIds = Sets.newHashSet();
        Set<Integer> officeIds = Sets.newHashSet();
        Set<Long> craftIds = Sets.newHashSet();
        Set<Long> orderSet = Sets.newHashSet();
        Set<Long> craftSet = Sets.newHashSet();
        page.getRecords().forEach(
                pmsProduceOrderVO -> {
//                    //获取工序信息
                    craftSet.add(pmsProduceOrderVO.getId());
                    craftIds.add(pmsProduceOrderVO.getCraftId());
                    dictIds.add(pmsProduceOrderVO.getTypeId());
                    dictIds.add(pmsProduceOrderVO.getUnitId());
                    officeIds.add(pmsProduceOrderVO.getProductionLineId());
                    orderSet.add(pmsProduceOrderVO.getId());
                }
        );
        Map<Long, List<PmsProduceOrderCraftVO>> craftMap = pmsProduceOrderCraftService.crafts(craftSet);
        //获取物料信息
        Map<Long, List<PmsWorkOrderBomVO>> materials = pmsWorkOrderBomService.materials(craftSet);
        List<PmsProduceOrderCraftVO> craftVOList = Lists.newArrayList();
        List<PmsWorkOrderBomVO> bomVOList = Lists.newArrayList();
        //3.0 补充处理完工信息
        Map<Long, PmsCompleteStorageNumDTO> map = pmsBarcodePrintService.orderSum(orderSet);
        page.getRecords().forEach(
                pmsProduceOrderVO -> {
                    List<PmsProduceOrderCraftVO> orderCraftVOList = craftMap.getOrDefault(pmsProduceOrderVO.getId(), craftVOList);
                    List<PmsWorkOrderBomVO> orderBomVOList = materials.getOrDefault(pmsProduceOrderVO.getId(), bomVOList);
                    PmsCompleteStorageNumDTO storageNumDTO = map.get(pmsProduceOrderVO.getId());
                    pmsProduceOrderVO.setProcessList(orderCraftVOList);
                    pmsProduceOrderVO.setMaterialList(orderBomVOList);
                    if (Objects.nonNull(storageNumDTO)) {
                        pmsProduceOrderVO.setCompleteQuantity(storageNumDTO.getQuantity());
                        pmsProduceOrderVO.setCompleteUnitId(storageNumDTO.getUnitId());
                        dictIds.add(storageNumDTO.getUnitId());
                    }
                }
        );
        //3.1查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
        //3.2查询机构数据
        Map<Integer, String> officeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(officeIds)).get();
        //3.3查询工艺信息
        Map<Long, String> versionMap = pmCraftRouteService.versionById(craftIds);
        //4.处理列表单位以及产线数据
        page.getRecords().forEach(
                pmsProduceOrderVO -> {
                    //4.1 处理类型数据
                    pmsProduceOrderVO.setType(dictMap.get(pmsProduceOrderVO.getTypeId()));
                    //4.2 处理产线数据
                    pmsProduceOrderVO.setProductionLineName(officeMap.get(pmsProduceOrderVO.getProductionLineId()));
                    //4.3 处理单位数据
                    pmsProduceOrderVO.setUnit(dictMap.get(pmsProduceOrderVO.getUnitId()));
                    pmsProduceOrderVO.setMinPackageUnit(dictMap.get(pmsProduceOrderVO.getMinPackageUnitId()));
                    pmsProduceOrderVO.setCompleteUnitName(dictMap.get(pmsProduceOrderVO.getCompleteUnitId()));
                    //4.4 处理工艺版本数据
                    pmsProduceOrderVO.setCraftVersion(versionMap.get(pmsProduceOrderVO.getCraftId()));
                    //4.4 处理完工数据
                    pmsProduceOrderVO.setCompleteUnitName(dictMap.get(pmsProduceOrderVO.getCompleteUnitId()));
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
            //获取货架信息
            if (Objects.nonNull(pmsProduceOrder.getDefaultShelfId())) {
                SysOfficeVO sysOfficeVO = FeignOptional.of(remoteOfficeFeign.parent(pmsProduceOrder.getDefaultShelfId())).get();
                pmsProduceOrder.setDefaultShelfId(sysOfficeVO.getId());
            }
            updateById(pmsProduceOrder);
            // 获取工序信息
            if (CollUtil.isNotEmpty(dto.getProcessList())) {
                Set<Long> ids = Sets.newHashSet();
                dto.getProcessList().forEach(
                        pmsProduceOrderCraftDTO -> {
                            PmsProduceOrderCraft craft = new PmsProduceOrderCraft();
                            BeanUtil.copyProperties(pmsProduceOrderCraftDTO, craft);
                            craft.setProduceOrderId(dto.getId());
                            if (Objects.nonNull(craft.getId())) {
                                //修改工序信息
                                ids.add(craft.getId());
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
                                        pmsWorkOrderCraftStation.setProduceOrderCraftId(craft.getId());
                                        BeanUtil.copyProperties(station, pmsWorkOrderCraftStation);
                                        stations.add(pmsWorkOrderCraftStation);
                                    });
                                    //保存工位数据
                                    pmsWorkOrderCraftStationService.saveBatch(stations);
                                }
                            }
                        }
                );
                //删除工序信息
                pmsProduceOrderCraftService.deleteCrafts(ids, dto.getId());
            }
            //获取物料信息
            List<PmsWorkOrderBom> bomList = Lists.newArrayList();
            if (CollUtil.isNotEmpty(dto.getMaterialList())) {
                Set<Long> idSet = Sets.newHashSet();
                dto.getMaterialList().forEach(material -> {
                    PmsWorkOrderBom pmsWorkOrderBom = new PmsWorkOrderBom();
                    BeanUtil.copyProperties(material, pmsWorkOrderBom);
                    pmsWorkOrderBom.setWorkProduceId(dto.getId());
                    if (Objects.nonNull(material.getId())) {
                        idSet.add(material.getId());
                        pmsWorkOrderBomService.updateById(pmsWorkOrderBom);
                    } else {
                        bomList.add(pmsWorkOrderBom);
                    }
                });
                //删除物料信息
                pmsWorkOrderBomService.deleteBom(idSet, dto.getId());
                pmsWorkOrderBomService.saveBatch(bomList);
            }
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
        Set<String> batchSet = Sets.newHashSet();
        res.forEach(pmsWorkOrderBatchVO -> {
            batchSet.add(pmsWorkOrderBatchVO.getCode());
        });
        Map<String, PmsCompleteStorageNumDTO> numMap = pmsBarcodePrintService.batchSum(batchSet);
        Set<Long> dictIds = numMap.values().stream().map(PmsCompleteStorageNumDTO::getUnitId).collect(Collectors.toSet());
        //查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
        PmsCompleteStorageNumDTO defaultPmsCompleteStorageNumDTO = new PmsCompleteStorageNumDTO();
        res.forEach(pmsWorkOrderBatchVO -> {
            PmsCompleteStorageNumDTO pmsNumDto = numMap.getOrDefault(pmsWorkOrderBatchVO.getCode(), defaultPmsCompleteStorageNumDTO);
            pmsWorkOrderBatchVO.setUnitId(pmsNumDto.getUnitId());
            pmsWorkOrderBatchVO.setQuantity(pmsNumDto.getQuantity());
            pmsWorkOrderBatchVO.setUnit(dictMap.get(pmsWorkOrderBatchVO.getUnitId()));
        });
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
        //处理时间数据
        if (Objects.nonNull(dto.getStartTime())) {
            dto.setActualStartTime(LocalDateTime.of(dto.getStartTime(), LocalTime.MIN));
        }
        //计划开始时间-结束
        if (Objects.nonNull(dto.getEndTime())) {
            dto.setActualEndTime(LocalDateTime.of(dto.getEndTime(), LocalTime.MAX));
        }
        //1.自定义sql查询订单接收数据
        Page<PmsProduceOrderVO> page = this.baseMapper.acceptPage(new Page(dto.getCurrent(), dto.getSize()), dto);
        //2.处理订单数据
        Set<Long> dictIds = Sets.newHashSet();
        Set<Long> orderIds = Sets.newHashSet();
        page.getRecords().forEach(pmsProduceOrderVO -> {
            dictIds.add(pmsProduceOrderVO.getUnitId());
            dictIds.add(pmsProduceOrderVO.getTypeId());
            orderIds.add(pmsProduceOrderVO.getId());

        });
        //3.查询数据
        //3.1查询完工数据
        Map<Long, PmsCompleteStorageNumDTO> map = pmsBarcodePrintService.orderSum(orderIds);
        //3.2获取完工数据单位
        map.forEach((key, pmsCompleteStorageNum) -> {
            dictIds.add(pmsCompleteStorageNum.getUnitId());
        });
        //3.3查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictIds)).get();
        //4.处理返回数据
        page.getRecords().forEach(pmsProduceOrderVO -> {
            PmsCompleteStorageNumDTO storageNumDTO = map.get(pmsProduceOrderVO.getId());
            if (Objects.nonNull(storageNumDTO)) {
                pmsProduceOrderVO.setCompleteQuantity(storageNumDTO.getQuantity());
                pmsProduceOrderVO.setCompleteUnitId(storageNumDTO.getUnitId());
                pmsProduceOrderVO.setCompleteUnitName(dictMap.get(storageNumDTO.getUnitId()));
            }
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
        //1.查询订单
        PmsProduceOrder order = selectOrder(orderId);
        //2.校验所有工序是否已分配工位
        //2.1 获取订单工序数据
        List<PmsProduceOrderCraftVO> crafts = pmsProduceOrderCraftService.getCrafts(orderId);
        Assert.isFalse(CollUtil.isEmpty(crafts), "该订单不存在工序数据，请检查");
        crafts.forEach(craft -> {
            Assert.isFalse(CollUtil.isEmpty(craft.getStationList()), "工序未分配工位，请检查");
        });
        //3.校验所有组件是否已分配工序
        //3.1 获取组件数据
        List<PmsWorkOrderBomVO> materialList = pmsWorkOrderBomService.getMaterialList(orderId);
//        Assert.isFalse(CollUtil.isEmpty(materialList), "该订单不存在组件,请检查");
        if (CollUtil.isNotEmpty(materialList)) {
            materialList.forEach(material -> {
                Assert.notNull(material.getProcessId(), "组件未分配工序，请检查");
            });
        }
        //4.校验订单状态
        Assert.checkBetween(order.getState(), 0, 0, "仅已创建订单可下达");
        //5.修改订单状态为已下达
        order.setState(PmManagerConstant.OrderStateEnum.ONE.getCode());
        return updateById(order);
    }

    @Override
    public Page<PmsProduceOrderVO> reportWorkOrderPage(ReportWorkOrderPageDTO dto) {
        Page<PmsProduceOrderVO> page = this.baseMapper.reportWorkOrderPage(new Page<>(dto.getCurrent(), dto.getSize()), dto);
        if (CollUtil.isNotEmpty(page.getRecords())) {
            List<PmsProduceOrderVO> records = page.getRecords();
            Set<Long> orderSet = Sets.newHashSet();
            Set<Long> dictSet = Sets.newHashSet();
            for (PmsProduceOrderVO produceOrderVO : records) {
                orderSet.add(produceOrderVO.getId());
                dictSet.add(produceOrderVO.getUnitId());
            }
            orderSet = records.stream().map(PmsProduceOrderVO::getId).collect(Collectors.toSet());
            Map<Long, PmsCompleteStorageNumDTO> orderMap = pmsBarcodePrintService.orderSum(orderSet);
            if (CollUtil.isNotEmpty(orderMap)) {
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
     * 订单关闭
     *
     * @param orderId
     * @return
     */
    @Override
    public Boolean close(Long orderId) {
        //1.查询订单
        PmsProduceOrder order = selectOrder(orderId);
        //2.校验状态
        Assert.checkBetween(order.getState(), 0, 0, "仅已创建状态订单可关闭");
        order.setState(PmManagerConstant.OrderStateEnum.SEVEN.getCode());
        return updateById(order);
    }

    /**
     * 取消下达
     *
     * @param orderId
     * @return
     */
    @Override
    public Boolean cancelTheOrder(Long orderId) {
        //1.查询订单
        PmsProduceOrder order = selectOrder(orderId);
        //2.校验状态
        Assert.checkBetween(order.getState(), 1, 1, "仅已下达状态订单可取消下达");
        //3.修改状态为已创建
        order.setState(PmManagerConstant.OrderStateEnum.ZERO.getCode());
        return updateById(order);
    }

    /**
     * 确认
     *
     * @param orderId
     * @return
     */
    @Override
    public Boolean affirm(Long orderId) {
        //1.查询订单
        PmsProduceOrder order = selectOrder(orderId);
        //2.判断订单状态
        Assert.isTrue(PmManagerConstant.OrderStateEnum.THREE.getCode().equals(order.getState())
                || PmManagerConstant.OrderStateEnum.FIVE.getCode().equals(order.getState()), "当前订单状态不可确认");
        //3.记录批次时间
        //3.1 获取批次信息
        PmsWorkOrderBatch batch = pmsWorkOrderBatchService.getByOrderId(orderId);
        if (PmManagerConstant.OrderStateEnum.THREE.getCode().equals(order.getState())) {
            Assert.notNull(batch, "当前订单不存在批次信息，请检查");
            //3.2批次结束时间
            batch.setEndTime(LocalDateTime.now());
            batch.setCurrentBatchFlag(PmsCodeUtil.DEFAULT_PADDING);
            pmsWorkOrderBatchService.updateById(batch);
        } else {
            //完成 确认状态
            if (Objects.nonNull(batch)) {
                batch.setEndTime(LocalDateTime.now());
                batch.setCurrentBatchFlag(PmsCodeUtil.DEFAULT_PADDING);
                pmsWorkOrderBatchService.updateById(batch);
            }
        }
        //4.修改订单状态
        if (PmManagerConstant.OrderStateEnum.THREE.getCode().equals(order.getState())) {
            order.setState(PmManagerConstant.OrderStateEnum.FOUR.getCode());
        }
        if (PmManagerConstant.OrderStateEnum.FIVE.getCode().equals(order.getState())) {
            order.setState(PmManagerConstant.OrderStateEnum.SIX.getCode());
            //确认状态时修改订单完成时间
            order.setActualEndTime(LocalDateTime.now());
        }
        return updateById(order);
    }

    @Override
    public Boolean execute(Long orderId) {
        //1.查询订单信息
        PmsProduceOrder order = selectOrder(orderId);
        //2.校验当前产线是否在其他执行中的订单
        //2.1查询当前产线的执行订单数量
        PmsProduceOrderVO pmsProduceOrderVO = listByProduceLineId(order.getProductionLineId());
        if (Objects.nonNull(pmsProduceOrderVO)) {
            throw new BusinessException("其他订单(" + pmsProduceOrderVO.getCode() + ")正在执行中");
        }
        //2.2 校验状态（仅已创建可下达）
        Assert.isTrue(order.getState().equals(PmManagerConstant.OrderStateEnum.ONE.getCode()) || order.getState().equals(PmManagerConstant.OrderStateEnum.FOUR.getCode()), "仅已下达状态或暂停中可执行");
        //3.生成新的批次，并翻转订单状态为执行中
        ProduceOrderBatchDto produceOrderBatchDto = new ProduceOrderBatchDto();
        produceOrderBatchDto.setStartTime(LocalDateTime.now());
        produceOrderBatchDto.setProduceOrderId(orderId);
        produceOrderBatchDto.setMinPackQuantity(order.getMinPackageQuantity());
        produceOrderBatchDto.setUnitId(order.getUnitId());
        pmsWorkOrderBatchService.add(produceOrderBatchDto);
        //4.修改状态
        order.setState(PmManagerConstant.OrderStateEnum.TWO.getCode());
        //5.修改执行时间
        order.setActualStartTime(LocalDateTime.now());
        return updateById(order);
    }

    /**
     * 根据产线id查询当前正在执行订单
     *
     * @param produceLineId
     * @return
     */
    @Override
    public PmsProduceOrderVO listByProduceLineId(Integer produceLineId) {
        //定义返回对象
        PmsProduceOrderVO pmsProduceOrderVO = null;
        //查询产线正在执行的订单
        PmsProduceOrder order = getOne(new LambdaQueryWrapper<PmsProduceOrder>()
                .eq(PmsProduceOrder::getProductionLineId, produceLineId)
                .eq(PmsProduceOrder::getState, PmManagerConstant.OrderStateEnum.TWO.getCode())
                .last(MpConstant.STR_LIMIT_1)
        );
        if (Objects.nonNull(order)) {
            pmsProduceOrderVO = new PmsProduceOrderVO();
            BeanUtil.copyProperties(order, pmsProduceOrderVO);
            PmsWorkOrderBatch pmsWorkOrderBatch = pmsWorkOrderBatchService.getOne(new LambdaQueryWrapper<PmsWorkOrderBatch>()
                    .eq(PmsWorkOrderBatch::getProduceOrderId, order.getId())
                    .eq(PmsWorkOrderBatch::getCurrentBatchFlag, MpConstant.YES)
                    .last(MpConstant.STR_LIMIT_1));
            String productionLineName = FeignOptional.of(remoteOfficeFeign.getNameById(order.getProductionLineId())).get();
            Map<Long, String> dictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(Sets.newHashSet(order.getUnitId(), order.getMinPackageUnitId()))).get();
            if (Objects.nonNull(pmsWorkOrderBatch)) {
                pmsProduceOrderVO.setBatch(pmsWorkOrderBatch.getId());
                pmsProduceOrderVO.setBatchNo(pmsWorkOrderBatch.getCode());
                pmsProduceOrderVO.setProductionLineName(productionLineName);
                pmsProduceOrderVO.setUnit(dictMap.get(order.getUnitId()));
                pmsProduceOrderVO.setMinPackageUnit(dictMap.get(order.getMinPackageUnitId()));
            }
        }
        return pmsProduceOrderVO;
    }

    /**
     * 订单暂停
     *
     * @param orderId
     * @return
     */
    @Override
    public Boolean stop(Long orderId) {
        //1.查询订单
        PmsProduceOrder order = selectOrder(orderId);
        //2.判断状态
        Assert.isFalse(PmManagerConstant.OrderStateEnum.TWO.getCode().equals(order.getState()), "当前状态不可暂停");
        //3.修改状态
        order.setState(PmManagerConstant.OrderStateEnum.THREE.getCode());
        return updateById(order);
    }

    @Override
    public Boolean complete(Long orderId) {
        //1.查询订单
        PmsProduceOrder order = selectOrder(orderId);
        //2.判断状态
        Assert.isFalse(PmManagerConstant.OrderStateEnum.TWO.getCode().equals(order.getState()), "当前状态不可完成");
        //3.状态完成
        order.setState(PmManagerConstant.OrderStateEnum.FIVE.getCode());
        return updateById(order);
    }

    @Override
    public Map<Long, BaseSelectVO<Long>> listUnit(Set<Long> orderSet) {
        Map<Long, BaseSelectVO<Long>> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(orderSet)) {
            List<PmsProduceOrder> pmsProduceOrders = listByIds(orderSet);
            if (CollUtil.isNotEmpty(pmsProduceOrders)) {
                Set<Long> unitSet = pmsProduceOrders.stream().map(PmsProduceOrder::getUnitId).collect(Collectors.toSet());
                Map<Long, String> unitDictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(unitSet)).get();
                for (PmsProduceOrder pmsProduceOrder : pmsProduceOrders) {
                    BaseSelectVO<Long> baseVo = new BaseSelectVO<>();
                    baseVo.setId(pmsProduceOrder.getUnitId());
                    baseVo.setName(unitDictMap.get(baseVo.getId()));
                    resMap.put(pmsProduceOrder.getId(), baseVo);
                }
            }
        }
        return resMap;
    }

    @Override
    public Map<Integer, PmsProduceOrderDTO> listByLineIds(Set<Integer> ids) {
        Map<Integer, PmsProduceOrderDTO> map = Maps.newHashMap();
        if (CollUtil.isEmpty(ids)) {
            return map;
        }
        List<PmsProduceOrder> list = list(new LambdaQueryWrapper<PmsProduceOrder>().in(PmsProduceOrder::getProductionLineId, ids).eq(PmsProduceOrder::getState, PmManagerConstant.OrderStateEnum.TWO.getCode()));
        if (CollUtil.isNotEmpty(list)) {
            List<PmsProduceOrderDTO> pmsProduceOrderDTOS = BeanUtil.copyToList(list, PmsProduceOrderDTO.class);
            pmsProduceOrderDTOS.forEach(pmsProduceOrderDTO -> {
                map.put(pmsProduceOrderDTO.getProductionLineId(), pmsProduceOrderDTO);
            });
        }
        return map;
    }

    @Override
    public Page<ProduceOrderTrackReportVO> trackReportList(ProduceOrderTrackReportDTO dto) {
        if (CollectionUtils.isEmpty(dto.getProductLineList())) {
            Page<ProduceOrderTrackReportVO> page = new Page<>();
            page.setSize(dto.getSize());
            page.setCurrent(dto.getCurrent());
            return page;
        }
        Map<Integer, SysOfficeVO> productLineMap = remoteOfficeFeign.listById(new HashSet<>(dto.getProductLineList())).getData();
        Page<ProduceOrderTrackReportVO> produceOrderTrackReportVOPage = this.baseMapper.trackReportPage(new Page(dto.getCurrent(), dto.getSize()), dto.getProductLineList());
        Set<Long> materialIdSet = produceOrderTrackReportVOPage.getRecords().stream().map(ProduceOrderTrackReportVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, List<SysSpareUnitConversionVO>> map = remoteSpareUnitConvertFeignApi.convert(materialIdSet).getData();
        SysDictVO sysDictVO = remoteDictFeignApi.getByCode("m²", "measure_unit").getData();

        produceOrderTrackReportVOPage.getRecords().forEach(produceOrderTrackReportVO -> {
            BigDecimal conversionQuantity = null;
            BigDecimal quantity = null;
            if (CollectionUtils.isNotEmpty(map.get(produceOrderTrackReportVO.getMaterialId()))) {
                Optional<SysSpareUnitConversionVO> optional = map.get(produceOrderTrackReportVO.getMaterialId())
                        .stream().filter(sysSpareUnitConversionVO -> sysDictVO.getId().equals(sysSpareUnitConversionVO.getConversionUnitId())).findAny();

                if (optional.isPresent()) {
                    conversionQuantity = optional.get().getConversionQuantity();
                    quantity = optional.get().getQuantity();
                }
            }
            BigDecimal planQuantity = produceOrderTrackReportVO.getPlanQuantity();
            BigDecimal completeQuantity = produceOrderTrackReportVO.getCompleteQuantity();
            BigDecimal shortfallQuantity = null;
            String productionLineName = Objects.nonNull(productLineMap.get(produceOrderTrackReportVO.getProductionLineId())) ? productLineMap.get(produceOrderTrackReportVO.getProductionLineId()).getOfficeName() : null;
            produceOrderTrackReportVO.setProductionLineName(productionLineName);
            if (Objects.isNull(completeQuantity)) {
                produceOrderTrackReportVO.setShortfallQuantity(planQuantity);
            } else {
                shortfallQuantity = planQuantity.subtract(completeQuantity);
                produceOrderTrackReportVO.setShortfallQuantity(shortfallQuantity);
            }

            if (Objects.nonNull(conversionQuantity) && Objects.nonNull(quantity)) {
                if (Objects.nonNull(planQuantity)) {
                    produceOrderTrackReportVO.setPlanQuantitySq(BigDecimalUtil.divide(planQuantity, quantity).multiply(conversionQuantity));
                }
                if (Objects.nonNull(completeQuantity)) {
                    produceOrderTrackReportVO.setCompleteQuantitySq(BigDecimalUtil.divide(completeQuantity, quantity).multiply(conversionQuantity));
                }
                if (Objects.nonNull(shortfallQuantity)) {
                    produceOrderTrackReportVO.setShortfallQuantitySq(BigDecimalUtil.divide(shortfallQuantity, quantity).multiply(conversionQuantity));
                }
            }
        });
        return produceOrderTrackReportVOPage;
    }

    /**
     * 根据生产订单号来查询当前正在执行中的记录
     *
     * @param code
     * @return 生产订单信息
     */
    @Override
    public PmsProduceOrderVO code(String code) {
        //1.根据生产订单号来查询当前正在执行中的记录
        PmsProduceOrder order = getOne(new LambdaQueryWrapper<PmsProduceOrder>().eq(PmsProduceOrder::getCode, code).eq(PmsProduceOrder::getState, PmManagerConstant.OrderStateEnum.TWO.getCode()));
        //2.校验数据
        Assert.notNull(order, "该订单下没有正在执行的检验任务");
        //3.复制数据传入VO
        PmsProduceOrderVO pmsProduceOrderVO = new PmsProduceOrderVO();
        BeanUtils.copyProperties(order, pmsProduceOrderVO);

        return pmsProduceOrderVO;

    }

    @Override
    public List<PmsProduceOrderVO> executionOfOrder(Set<Integer> ids) {
        //查询产线正在执行的订单
        List<PmsProduceOrder> orderList = list(new LambdaQueryWrapper<PmsProduceOrder>()
                .in(PmsProduceOrder::getProductionLineId, ids)
                .notIn(PmsProduceOrder::getState, PmManagerConstant.OrderStateEnum.ZERO.getCode(),
                        PmManagerConstant.OrderStateEnum.SIX.getCode(),
                        PmManagerConstant.OrderStateEnum.SEVEN.getCode())
        );
        return BeanUtil.copyToList(orderList, PmsProduceOrderVO.class);
    }

    @Override
    public Map<Long, PmsProduceOrder> orderList(List<Long> orderIds) {
        Map<Long, PmsProduceOrder> map = Maps.newHashMap();
        List<PmsProduceOrder> list = list(new LambdaQueryWrapper<PmsProduceOrder>().in(PmsProduceOrder::getId, orderIds));
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(order ->
                    map.put(order.getId(), order)
            );
        }
        return map;
    }

    @Override
    public Map<Long, PmsProduceOrderVO> listByMaterialIds(Set<Long> materialIds) {
        Map<Long, PmsProduceOrderVO> map = Maps.newHashMap();
        if (CollUtil.isEmpty(materialIds)) {
            return map;
        }
        List<PmsProduceOrder> list = list(new LambdaQueryWrapper<PmsProduceOrder>()
                .in(PmsProduceOrder::getMaterialId, materialIds)
                .isNotNull(PmsProduceOrder::getActualEndTime));
        if (CollUtil.isNotEmpty(list)) {
            Map<Long, List<PmsProduceOrder>> collect = list.stream().collect(Collectors.groupingBy(PmsProduceOrder::getMaterialId));
            collect.forEach((key, value) -> {
                Collections.sort(value, Comparator.nullsFirst(Comparator.comparing(PmsProduceOrder::getActualEndTime).reversed()));
                PmsProduceOrderVO vo = new PmsProduceOrderVO();
                BeanUtils.copyProperties(value.get(0), vo);
                map.put(key, vo);
            });
        }
        return map;
    }

    @Override
    public WorkOrderVO getMaterialCodeByClassTime(LocalDateTime startTime, LocalDateTime endTime) {
        WorkOrderVO workOrderVO = this.baseMapper.selectByClassTime(startTime, endTime);
        long second = ChronoUnit.SECONDS.between(workOrderVO.getEndTime(), endTime);
        if (second < 60) {
            return workOrderVO;
        }
        return null;
    }

    @Override
    public List<DwCurrentReportWorkDTO> currentDateReportWork(LocalDate currentDate) {
        //计算整个班次日期的时间段
        LocalDateTime start = LocalDateTime.of(currentDate, LocalTime.of(8, 0, 0));
        LocalDateTime end = LocalDateTime.of(currentDate.plusDays(1), LocalTime.of(8, 0, 0));
        //查询这段时间内的所有报工记录
        List<DwCurrentReportWorkDTO> resList = pmsReportWorkMapper.currentDateReportWork(start, end);
        if (CollUtil.isNotEmpty(resList)) {
            Set<Long> classIdSet = resList.stream().map(v -> v.getClassesId()).collect(Collectors.toSet());
            Map<Long, SysDictVO> classMap = FeignOptional.of(remoteDictFeignApi.listById(Lists.newArrayList(classIdSet))).get();
            Set<Long> materialSet = resList.stream().map(v -> v.getMaterialId()).collect(Collectors.toSet());
            Map<Long, EqpSpareVO> materialMap = FeignOptional.of(remoteSpareFeignApi.listById(Lists.newArrayList(materialSet))).get();
            EqpSpareVO defaultEqpSpareVO = new EqpSpareVO();

            Set<Integer> productLineIdSet = resList.stream().map(v -> v.getProductLineId()).collect(Collectors.toSet());
            Map<Integer, SysOfficeVO> parentMap = FeignOptional.of(remoteOfficeFeign.parentInfo(new ArrayList<>(productLineIdSet))).get();
            Set<Integer> collect = parentMap.values().stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
            Map<Integer, SysOfficeVO> grandPaMap = FeignOptional.of(remoteOfficeFeign.parentInfo(new ArrayList<>(collect))).get();
            SysOfficeVO defaultSysOfficeVO = new SysOfficeVO();
            resList.forEach(ele -> {
                BigDecimal thick = materialMap.getOrDefault(ele.getMaterialId(), defaultEqpSpareVO).getThickQuantity();
                ele.setThick(thick);
                ele.setClassDate(currentDate);
                //获取车间和制造部
                ele.setWorkshopId(parentMap.getOrDefault(ele.getProductLineId(), defaultSysOfficeVO).getId());
                ele.setDepartmentId(grandPaMap.getOrDefault(ele.getWorkshopId(), defaultSysOfficeVO).getId());
                SysDictVO sysDictVO = classMap.get(ele.getClassesId());
                if (Objects.nonNull(sysDictVO)) {
                    if (sysDictVO.getName().contains("早")) {
                        ele.setClassId(0);
                    } else {
                        ele.setClassId(1);
                    }
                }
            });
        }
        return Objects.isNull(resList) ? Lists.newArrayList() : resList;
    }

    /**
     * 查询订单
     */
    private PmsProduceOrder selectOrder(Long orderId) {
        //1.查询订单
        PmsProduceOrder order = getOne(new LambdaQueryWrapper<PmsProduceOrder>().eq(PmsProduceOrder::getId, orderId));
        //2.校验数据
        Assert.notNull(order, "该订单不存在");
        return order;
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
                .orderByDesc(PmsWorkOrderBatch::getCode)
                .last(MpConstant.STR_LIMIT_1));
        Assert.notNull(currentBatch, "订单" + order.getCode() + "无当前批次");

        //查询订单的工序craft
        List<PmsProduceOrderCraft> craftList = pmsProduceOrderCraftService.list(new LambdaQueryWrapper<PmsProduceOrderCraft>()
                .eq(PmsProduceOrderCraft::getProduceOrderId, order.getId())
                .select(PmsProduceOrderCraft::getId));
        Assert.notEmpty(craftList, "该订单暂未配置工序信息");
        Set<Long> craftIdSet = craftList.stream().map(PmsProduceOrderCraft::getId).collect(Collectors.toSet());
        //2.查询工位
        PmsWorkOrderCraftStation station = pmsWorkOrderCraftStationService.getOne(new LambdaQueryWrapper<PmsWorkOrderCraftStation>()
                .eq(PmsWorkOrderCraftStation::getExecuteStationId, stationId)
                .in(PmsWorkOrderCraftStation::getProduceOrderCraftId, craftIdSet)
                .last(MpConstant.STR_LIMIT_1));
        Assert.notNull(station, "该工位暂无工序配置");
        //3.查询工序
        PmsProduceOrderCraft craft = pmsProduceOrderCraftService.getById(station.getProduceOrderCraftId());
        Assert.notNull(craft, "工序不存在");
        //4.定义返回数据
        ReportWorkOrderVO vo = new ReportWorkOrderVO();
        BeanUtils.copyProperties(order, vo);

        vo.setUnitId(order.getUnitId());
        Map<Long, String> unitMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(Sets.newHashSet(order.getUnitId()))).get();
        vo.setUnitName(unitMap.get(order.getUnitId()));

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
        LocalDateTime max = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        PmsProduceOrder maxCodeModel = getOne(new LambdaQueryWrapper<PmsProduceOrder>()
                .between(PmsProduceOrder::getCreateTime, min, max)
                .orderByDesc(PmsProduceOrder::getCode)
                .last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(PmsCodeUtil.PREFIX_PMS_PRODUCE_ORDER.length() + PmsCodeUtil.NUM_BIT_8 + PmsCodeUtil.NUM_BIT_2);
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




