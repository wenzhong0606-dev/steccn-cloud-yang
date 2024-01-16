package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
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
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.*;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.QtTaskClassCalculate;
import com.steccn.common.vo.R;
import com.steccn.data.dto.PmReportWorkPreloadDTO;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.dto.OaGroupCountDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.feign.*;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.*;
import com.steccn.pms.mapper.PmsProduceOrderMapper;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.mapper.PmsWorkOrderBatchMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.pms.vo.*;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtDefectReportDTO;
import com.steccn.qms.excel.strategy.HeadContentStyleStrategy;
import com.steccn.qms.excel.strategy.RowHeightStyleStrategy;
import com.steccn.qms.feign.RemoteCheckTaskDetailFeignApi;
import com.steccn.qms.feign.RemoteItemFeignApi;
import com.steccn.qms.vo.CoatingFilmRowVO;
import com.steccn.qms.vo.CoatingFilmVO;
import com.steccn.qms.vo.QtCheckTaskDetailReportVO;
import com.steccn.qms.vo.QtDefectiveItemVO;
import com.steccn.wms.feign.RemoteWmsCustomerBarcodeFeignApi;
import com.steccn.wms.feign.RemoteWmsStorageReceiptFeignApi;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import feign.Feign;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yequan
 * @description 针对表【pms_produce_order(生产订单)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PmsProduceOrderServiceImpl extends ServiceImpl<PmsProduceOrderMapper, PmsProduceOrder> implements PmsProduceOrderService {

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
    private final RemoteSysOfficeCollectFeignApi remoteSysOfficeCollectFeignApi;
    private final RemoteDataPlatformFeignApi remoteDataPlatformFeignApi;
    private final OaInfoService oaInfoService;
    private final RemoteCheckTaskDetailFeignApi remoteCheckTaskDetailFeignApi;
    private final RemoteItemFeignApi remoteItemFeignApi;
    private final RemoteWmsStorageReceiptFeignApi remoteWmsStorageReceiptFeignApi;
    private final PmsWorkOrderBatchMapper pmsWorkOrderBatchMapper;

    @Autowired
    @Lazy
    private PmsReportWorkService pmsReportWorkService;
    private final RemoteWmsCustomerBarcodeFeignApi remoteWmsCustomerBarcodeFeignApi;


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
                SysOfficeVO sysOfficeVO = FeignOptional.of(() -> remoteOfficeFeign.parent(model.getDefaultShelfId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
        //开始时间-结束时间
        if (Objects.nonNull(dto.getStartDate()) && Objects.nonNull(dto.getEndDate())) {
            dto.setStartTime(LocalDateTime.of(dto.getStartDate(), LocalTime.MIN));
            dto.setEndTime(LocalDateTime.of(dto.getEndDate(), LocalTime.MAX));
        }
        //2.自定义sql查询订单信息列表
        Page<PmsProduceOrderVO> page = this.baseMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
        //3.处理数据
        Set<Long> dictIds = Sets.newHashSet();
        Set<Integer> officeIds = Sets.newHashSet();
        Set<Long> craftIds = Sets.newHashSet();
        Set<Long> orderSet = Sets.newHashSet();
        Set<Long> craftSet = Sets.newHashSet();
        page.getRecords().forEach(pmsProduceOrderVO -> {
//                    //获取工序信息
            craftSet.add(pmsProduceOrderVO.getId());
            craftIds.add(pmsProduceOrderVO.getCraftId());
            dictIds.add(pmsProduceOrderVO.getTypeId());
            dictIds.add(pmsProduceOrderVO.getUnitId());
            officeIds.add(pmsProduceOrderVO.getProductionLineId());
            officeIds.add(pmsProduceOrderVO.getCustomerId());
            orderSet.add(pmsProduceOrderVO.getId());
        });
        Map<Long, List<PmsProduceOrderCraftVO>> craftMap = pmsProduceOrderCraftService.crafts(craftSet);
        //获取物料信息
        Map<Long, List<PmsWorkOrderBomVO>> materials = pmsWorkOrderBomService.materials(craftSet);
        List<PmsProduceOrderCraftVO> craftVOList = Lists.newArrayList();
        List<PmsWorkOrderBomVO> bomVOList = Lists.newArrayList();
        //3.0 补充处理完工信息
        Map<Long, PmsCompleteStorageNumDTO> map = pmsBarcodePrintService.orderSum(orderSet);
        page.getRecords().forEach(pmsProduceOrderVO -> {
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
        });
        //3.1查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //3.2查询机构数据
        Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //3.3查询工艺信息
        Map<Long, String> versionMap = pmCraftRouteService.versionById(craftIds);
        //4.处理列表单位以及产线数据
        page.getRecords().forEach(pmsProduceOrderVO -> {
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
            //客户名称
            pmsProduceOrderVO.setCustomerName(officeMap.get(pmsProduceOrderVO.getCustomerId()));
        });
        return page;
    }

    @Override
    public PmsProduceOrderVO info(Long id) {
        //1、获取一条详情
        PmsProduceOrderVO pmsProduceOrderVO = new PmsProduceOrderVO();
        PmsProduceOrder pmsProduceOrder = getById(id);
        BeanUtil.copyProperties(pmsProduceOrder,pmsProduceOrderVO);
        //3.处理数据
        Set<Long> dictIds = Sets.newHashSet(Sets.newHashSet(pmsProduceOrderVO.getTypeId(),pmsProduceOrderVO.getUnitId()));
        Set<Integer> officeIds = Sets.newHashSet(pmsProduceOrderVO.getProductionLineId(),pmsProduceOrderVO.getCustomerId());
        Set<Long> craftIds = Sets.newHashSet(pmsProduceOrderVO.getCraftId());
        Set<Long> orderSet = Sets.newHashSet(pmsProduceOrderVO.getId());
        Set<Long> craftSet = Sets.newHashSet(pmsProduceOrderVO.getId());
        Map<Long, List<PmsProduceOrderCraftVO>> craftMap = pmsProduceOrderCraftService.crafts(craftSet);
        //获取物料信息
        Map<Long, List<PmsWorkOrderBomVO>> materials = pmsWorkOrderBomService.materials(craftSet);
        List<PmsProduceOrderCraftVO> craftVOList = Lists.newArrayList();
        List<PmsWorkOrderBomVO> bomVOList = Lists.newArrayList();
        //4、 补充处理完工信息
        Map<Long, PmsCompleteStorageNumDTO> map = pmsBarcodePrintService.orderSum(orderSet);
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
        //4.1查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //4.2查询机构数据
        Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //4.3查询工艺信息
        Map<Long, String> versionMap = pmCraftRouteService.versionById(craftIds);
        //5.处理列表单位以及产线数据
        //5.1 处理类型数据
        pmsProduceOrderVO.setType(dictMap.get(pmsProduceOrderVO.getTypeId()));
        //5.2 处理产线数据
        pmsProduceOrderVO.setProductionLineName(officeMap.get(pmsProduceOrderVO.getProductionLineId()));
        //5.3 处理单位数据
        pmsProduceOrderVO.setUnit(dictMap.get(pmsProduceOrderVO.getUnitId()));
        pmsProduceOrderVO.setMinPackageUnit(dictMap.get(pmsProduceOrderVO.getMinPackageUnitId()));
        pmsProduceOrderVO.setCompleteUnitName(dictMap.get(pmsProduceOrderVO.getCompleteUnitId()));
        //5.4 处理工艺版本数据
        pmsProduceOrderVO.setCraftVersion(versionMap.get(pmsProduceOrderVO.getCraftId()));
        //5.4 处理完工数据
        pmsProduceOrderVO.setCompleteUnitName(dictMap.get(pmsProduceOrderVO.getCompleteUnitId()));
        //客户名称
        pmsProduceOrderVO.setCustomerName(officeMap.get(pmsProduceOrderVO.getCustomerId()));

        return pmsProduceOrderVO;
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
                SysOfficeVO sysOfficeVO = FeignOptional.of(() -> remoteOfficeFeign.parent(pmsProduceOrder.getDefaultShelfId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                pmsProduceOrder.setDefaultShelfId(sysOfficeVO.getId());
            }
            updateById(pmsProduceOrder);
            // 获取工序信息
            if (CollUtil.isNotEmpty(dto.getProcessList())) {
                Set<Long> ids = Sets.newHashSet();
                dto.getProcessList().forEach(pmsProduceOrderCraftDTO -> {
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
                });
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
            } else {
                pmsWorkOrderBomService.remove(new LambdaQueryWrapper<PmsWorkOrderBom>().eq(PmsWorkOrderBom::getWorkProduceId, dto.getId()));
            }
        }
        return true;
    }

    @Override
    public Boolean editPlanTime(PmsProduceOrderDTO dto) {
        Assert.notNull(dto.getId(),"id不能为空");
        Assert.notNull(dto.getPlanStartTime(),"计划开始时间不能为空");
        Assert.notNull(dto.getPlanEndTime(),"计划结束时间不能为空");
        //1、先根据Id查询
        PmsProduceOrder produceOrder = getById(dto.getId());
        //只有已创建和已下达状态才可以修改
        if(produceOrder.getState().equals(OaConstant.NO)){
            PmsProduceOrder model = new PmsProduceOrder();
            model.setId(dto.getId());
            model.setPlanStartTime(dto.getPlanStartTime());
            model.setPlanEndTime(dto.getPlanEndTime());
            updateById(model);
        }else if(produceOrder.getState().equals(OaConstant.NUM_1)){
            //如果是已下达，则取消下达
            cancelTheOrder(dto.getId());
            //修改时间
            PmsProduceOrder model = new PmsProduceOrder();
            model.setId(dto.getId());
            model.setPlanStartTime(dto.getPlanStartTime());
            model.setPlanEndTime(dto.getPlanEndTime());
            updateById(model);
        }else {
            throw new BusinessException("执行中、已完成的订单无法修改计划时间");
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
        //查询生产订单id
        PmsProduceOrder pmsProduceOrder = getById(dto.getProduceOrderId());
        Assert.notNull(pmsProduceOrder, "生产订单不存在");
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
        Set<Long> dictIds = Sets.newHashSet();
        res.forEach(pmsWorkOrderBatchVO -> {
            batchSet.add(pmsWorkOrderBatchVO.getCode());
            dictIds.add(pmsWorkOrderBatchVO.getUnitId());
        });
        Map<String, PmsCompleteStorageNumDTO> numMap = pmsBarcodePrintService.batchSum(batchSet);
        //查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        PmsCompleteStorageNumDTO defaultPmsCompleteStorageNumDTO = new PmsCompleteStorageNumDTO();
        res.forEach(pmsWorkOrderBatchVO -> {
            PmsCompleteStorageNumDTO pmsNumDto = numMap.getOrDefault(pmsWorkOrderBatchVO.getCode(), defaultPmsCompleteStorageNumDTO);
            pmsWorkOrderBatchVO.setQuantity(pmsNumDto.getQuantity());
            pmsWorkOrderBatchVO.setUnit(dictMap.get(pmsWorkOrderBatchVO.getUnitId()));
            pmsWorkOrderBatchVO.setProduceOrderId(pmsProduceOrder.getId());
            pmsWorkOrderBatchVO.setMaterialId(pmsProduceOrder.getMaterialId());
            pmsWorkOrderBatchVO.setProductLineId(pmsProduceOrder.getProductionLineId());
            pmsWorkOrderBatchVO.setProduceOrderCode(pmsProduceOrder.getCode());
        });
        return page.setRecords(res);
    }

    @Override
    public PmsProduceOrderGanttChartVO produceOrderGanttChart(PmsProduceOrderGanttChartDTO dto) {
        List<SysOfficeVO> sysOfficeVOS = Lists.newArrayList();
        if(dto.getType().equals(OaConstant.NO)){
            //基片
            SysOfficeDTO officeDTO = new SysOfficeDTO();
            officeDTO.setOfficeType("9");
            officeDTO.setOfficeName(PmManagerConstant.UROPATAGIA);
            List<SysOfficeVO> sysOfficeVOTmp1 = FeignOptional.of(() -> remoteOfficeFeign.getByTypeAndLine(officeDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            sysOfficeVOS.addAll(sysOfficeVOTmp1.stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList()));
            //深加工
            officeDTO.setOfficeName(PmManagerConstant.DEEP_PROCESSING);
            List<SysOfficeVO> sysOfficeVOTmp2 = FeignOptional.of(() -> remoteOfficeFeign.getByTypeAndLine(officeDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            sysOfficeVOS.addAll(sysOfficeVOTmp2.stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList()));
        }else if(dto.getType().equals(OaConstant.YES)){
            //深加工
            SysOfficeDTO officeDTO = new SysOfficeDTO();
            officeDTO.setOfficeType("9");
            officeDTO.setOfficeName(PmManagerConstant.DEEP_PROCESSING);
            List<SysOfficeVO> sysOfficeVOTmp = FeignOptional.of(() -> remoteOfficeFeign.getByTypeAndLine(officeDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            sysOfficeVOS.addAll(sysOfficeVOTmp.stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList()));
        }else if(dto.getType().equals(OaConstant.NUM_2)){
            //基片
            SysOfficeDTO officeDTO = new SysOfficeDTO();
            officeDTO.setOfficeType("9");
            officeDTO.setOfficeName(PmManagerConstant.UROPATAGIA);
            List<SysOfficeVO> sysOfficeVOTmp = FeignOptional.of(() -> remoteOfficeFeign.getByTypeAndLine(officeDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            sysOfficeVOS.addAll(sysOfficeVOTmp.stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList()));
        }
        //拿到所有的产线id
        List<Integer> officeIds = sysOfficeVOS.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        //查询对应的批次信息
        List<PmsDefectDayOrderBasicDTO> pmsDefectDayOrderBasicDTOS = pmsWorkOrderBatchMapper.produceOrderGanttChart(officeIds, dto.getStartTime().atTime(LocalTime.MIN), dto.getEndTime().atTime(LocalTime.MAX));
        //开始组数据
        PmsProduceOrderGanttChartVO result = new PmsProduceOrderGanttChartVO();
        //行
        List<PmsProduceOrderGanttRowsVO> rows = Lists.newArrayList();
        //过滤掉车间
        sysOfficeVOS = sysOfficeVOS.stream().filter(v -> !v.getOfficeName().contains("车间")).collect(Collectors.toList());
        sysOfficeVOS.forEach(tmp->{
            rows.add(new PmsProduceOrderGanttRowsVO(PmManagerConstant.GANTT_PREFIX+tmp.getId(), tmp.getOfficeName()));
        });
        result.setRows(rows);
        //信息
        //1、执行中的批次信息
        LocalDateTime now = LocalDateTime.now();
        List<PmsProduceOrderGanttInfoVO> infos = Lists.newArrayList();
        if(CollUtil.isNotEmpty(pmsDefectDayOrderBasicDTOS)){
            for(PmsDefectDayOrderBasicDTO tmp:pmsDefectDayOrderBasicDTOS){
                if(tmp.getProductLineId().equals(170)
                        ||tmp.getProductLineId().equals(171)
                        ||tmp.getProductLineId().equals(172)
                        ||tmp.getProductLineId().equals(173)){
                    continue;
                }
                PmsProduceOrderGanttInfoVO info = new PmsProduceOrderGanttInfoVO();
                info.setId(tmp.getProduceOrderId());
                info.setLabel(tmp.getProduceOrderCode());
                info.setRowId(PmManagerConstant.GANTT_PREFIX+tmp.getProductLineId());
                info.setStatus(tmp.getState());
                //若结束时间为空则取当前时间
                if(Objects.isNull(tmp.getEndTime())){
                    if(Objects.nonNull(tmp.getPlanEndTime())){
                        tmp.setEndTime(tmp.getPlanEndTime());
                    }else {
                        tmp.setEndTime(now);
                    }
                }
                PmsProduceOrderGanttTimeVO timeStamp = new PmsProduceOrderGanttTimeVO(tmp.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),tmp.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                info.setTime(timeStamp);
                infos.add(info);
            }
        }
        //2、找到计划中的订单(已创建，已下达)
        List<PmsProduceOrder> orderList = list(new LambdaQueryWrapper<PmsProduceOrder>()
                .and(lq->lq.between(PmsProduceOrder::getPlanStartTime, dto.getStartTime(), dto.getEndTime()).or().between(PmsProduceOrder::getPlanEndTime, dto.getStartTime(), dto.getEndTime()))
                .and(lq->lq.eq(PmsProduceOrder::getState, OaConstant.NO).or().eq(PmsProduceOrder::getState, OaConstant.YES))
                .in(PmsProduceOrder::getProductionLineId, officeIds));
        if(CollUtil.isNotEmpty(orderList)){
            for(PmsProduceOrder tmp:orderList){
                if(tmp.getProductionLineId().equals(170)
                        ||tmp.getProductionLineId().equals(171)
                        ||tmp.getProductionLineId().equals(172)
                        ||tmp.getProductionLineId().equals(173)){
                    continue;
                }
                PmsProduceOrderGanttInfoVO info = new PmsProduceOrderGanttInfoVO();
                info.setId(tmp.getId());
                info.setLabel(tmp.getCode());
                info.setRowId(PmManagerConstant.GANTT_PREFIX+tmp.getProductionLineId());
                info.setStatus(tmp.getState());
                PmsProduceOrderGanttTimeVO timeStamp = new PmsProduceOrderGanttTimeVO(tmp.getPlanStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),tmp.getPlanEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                info.setTime(timeStamp);
                infos.add(info);
            }
        }

        result.setInfos(infos);
        return result;
    }

    @Override
    public ReportWorkOrderVO reportWorkOrder(Integer productLineId, Integer stationId) {
        //1.根据产线查询订单
        PmsProduceOrder order = getOne(new LambdaQueryWrapper<PmsProduceOrder>().eq(PmsProduceOrder::getProductionLineId, productLineId).eq(PmsProduceOrder::getState, 2).orderByDesc(PmsProduceOrder::getCreateTime).last(MpConstant.STR_LIMIT_1));
        Assert.notNull(order, "该产线暂无执行中的订单，请选择其他订单");
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
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
            Map<Long, String> dictChMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

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
        Assert.isTrue(PmManagerConstant.OrderStateEnum.THREE.getCode().equals(order.getState()) || PmManagerConstant.OrderStateEnum.FIVE.getCode().equals(order.getState()), "当前订单状态不可确认");
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
    @Transactional(rollbackFor = Exception.class)
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
        PmsProduceOrder order = getOne(new LambdaQueryWrapper<PmsProduceOrder>().eq(PmsProduceOrder::getProductionLineId, produceLineId).eq(PmsProduceOrder::getState, PmManagerConstant.OrderStateEnum.TWO.getCode()).last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(order)) {
            pmsProduceOrderVO = new PmsProduceOrderVO();
            BeanUtil.copyProperties(order, pmsProduceOrderVO);
            PmsWorkOrderBatch pmsWorkOrderBatch = pmsWorkOrderBatchService.getOne(new LambdaQueryWrapper<PmsWorkOrderBatch>().eq(PmsWorkOrderBatch::getProduceOrderId, order.getId()).eq(PmsWorkOrderBatch::getCurrentBatchFlag, MpConstant.YES).last(MpConstant.STR_LIMIT_1));
            String productionLineName = FeignOptional.of(() -> remoteOfficeFeign.getNameById(order.getProductionLineId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(Sets.newHashSet(order.getUnitId(), order.getMinPackageUnitId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
        Assert.isTrue(PmManagerConstant.OrderStateEnum.TWO.getCode().equals(order.getState()), "当前状态不可暂停");
        //3.修改状态
        order.setState(PmManagerConstant.OrderStateEnum.THREE.getCode());
        updateById(order);
        return affirm(orderId);
    }

    @Override
    public Boolean complete(Long orderId) {
        //1.查询订单
        PmsProduceOrder order = selectOrder(orderId);
        //2.判断状态
        Assert.isTrue(order.getState().equals(PmManagerConstant.OrderStateEnum.ONE.getCode()) || PmManagerConstant.OrderStateEnum.TWO.getCode().equals(order.getState()) || order.getState().equals(PmManagerConstant.OrderStateEnum.FOUR.getCode()), "当前状态不可完成");
        //3.状态完成
        order.setState(PmManagerConstant.OrderStateEnum.FIVE.getCode());
        updateById(order);
        return affirm(orderId);
    }

    @Override
    public Map<Long, BaseSelectVO<Long>> listUnit(Set<Long> orderSet) {
        Map<Long, BaseSelectVO<Long>> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(orderSet)) {
            List<PmsProduceOrder> pmsProduceOrders = listByIds(orderSet);
            if (CollUtil.isNotEmpty(pmsProduceOrders)) {
                Set<Long> unitSet = pmsProduceOrders.stream().map(PmsProduceOrder::getUnitId).collect(Collectors.toSet());
                Map<Long, String> unitDictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(unitSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
                Optional<SysSpareUnitConversionVO> optional = map.get(produceOrderTrackReportVO.getMaterialId()).stream().filter(sysSpareUnitConversionVO -> sysDictVO.getId().equals(sysSpareUnitConversionVO.getConversionUnitId())).findAny();

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
    public PmsProduceOrderVO allCode(String code) {
        //1.根据生产订单号来查询当前正在执行中的记录
        PmsProduceOrder order = getOne(new LambdaQueryWrapper<PmsProduceOrder>().eq(PmsProduceOrder::getCode, code));
        //2.校验数据
        Assert.notNull(order, "没有对应code的订单");
        //3.复制数据传入VO
        PmsProduceOrderVO pmsProduceOrderVO = new PmsProduceOrderVO();
        BeanUtils.copyProperties(order, pmsProduceOrderVO);
        return pmsProduceOrderVO;
    }

    @Override
    public List<PmsProduceOrderVO> executionOfOrder(Set<Integer> ids) {
        //查询产线正在执行的订单
        List<PmsProduceOrder> orderList = list(new LambdaQueryWrapper<PmsProduceOrder>().in(PmsProduceOrder::getProductionLineId, ids).notIn(PmsProduceOrder::getState, PmManagerConstant.OrderStateEnum.ZERO.getCode(), PmManagerConstant.OrderStateEnum.SIX.getCode(), PmManagerConstant.OrderStateEnum.SEVEN.getCode()));
        return BeanUtil.copyToList(orderList, PmsProduceOrderVO.class);
    }

    @Override
    public Map<Long, PmsProduceOrder> orderList(List<Long> orderIds) {
        Map<Long, PmsProduceOrder> map = Maps.newHashMap();
        List<PmsProduceOrder> list = list(new LambdaQueryWrapper<PmsProduceOrder>().in(PmsProduceOrder::getId, orderIds));
        if (CollUtil.isNotEmpty(list)) {
            list.forEach(order -> map.put(order.getId(), order));
        }
        return map;
    }

    @Override
    public Map<Long, PmsProduceOrderVO> listByMaterialIds(Set<Long> materialIds) {
        Map<Long, PmsProduceOrderVO> map = Maps.newHashMap();
        if (CollUtil.isEmpty(materialIds)) {
            return map;
        }
        List<PmsProduceOrder> list = list(new LambdaQueryWrapper<PmsProduceOrder>().in(PmsProduceOrder::getMaterialId, materialIds).isNotNull(PmsProduceOrder::getActualEndTime));
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
            Map<Long, SysDictVO> classMap = FeignOptional.of(() -> remoteDictFeignApi.listById(Lists.newArrayList(classIdSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Set<Long> materialSet = resList.stream().map(v -> v.getMaterialId()).collect(Collectors.toSet());
            Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(Lists.newArrayList(materialSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            EqpSpareVO defaultEqpSpareVO = new EqpSpareVO();

            Set<Integer> productLineIdSet = resList.stream().map(v -> v.getProductLineId()).collect(Collectors.toSet());
            Map<Integer, SysOfficeVO> parentMap = FeignOptional.of(() -> remoteOfficeFeign.parentInfo(new ArrayList<>(productLineIdSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Set<Integer> collect = parentMap.values().stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
            Map<Integer, SysOfficeVO> grandPaMap = FeignOptional.of(() -> remoteOfficeFeign.parentInfo(new ArrayList<>(collect)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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

    @Override
    public List<DwProduceOrderDTO> currentProduceOrder(LocalDate currentDate) {
        //定义返回集合
        List<DwProduceOrderDTO> resList = Lists.newArrayList();
        //获取班次日期-早班和晚班
        LocalDateTime start = LocalDateUtil.getMorning(currentDate);
        LocalDateTime end = start.plusDays(1);
        //过滤当前日期已完成报工的订单
        List<PmsReportWork> rwOrderList = pmsReportWorkService.list(new LambdaQueryWrapper<PmsReportWork>().eq(PmsReportWork::getClassDate, currentDate)
                .select(PmsReportWork::getProduceOrderId));
        if (CollUtil.isNotEmpty(rwOrderList)) {
            Set<Long> rwOrderIdSet = rwOrderList.stream().map(PmsReportWork::getProduceOrderId).collect(Collectors.toSet());
            //根据订单查询批次
            List<PmsWorkOrderBatch> batchList = pmsWorkOrderBatchService.list(new LambdaQueryWrapper<PmsWorkOrderBatch>().in(PmsWorkOrderBatch::getProduceOrderId, rwOrderIdSet));
            if (CollUtil.isNotEmpty(batchList)) {
                //根据生产订单获取
                List<PmsProduceOrder> pmsProduceOrders = listByIds(rwOrderIdSet);
                Map<Long, PmsProduceOrder> orderMap = Maps.newHashMap();
                Set<Long> materialIdSet = Sets.newHashSet();
                pmsProduceOrders.forEach(ele -> {
                    materialIdSet.add(ele.getMaterialId());
                    orderMap.put(ele.getId(), ele);
                });
                //获取物料的信息
                Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(Lists.newArrayList(materialIdSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                for (PmsWorkOrderBatch batch : batchList) {
                    //过滤时间-批次的开始时间为空，或者批次的结束开始
                    if(Objects.isNull(batch.getStartTime()) || LocalDateUtil.gt(batch.getStartTime(),end)){
                        continue;
                    }
                    if(Objects.nonNull(batch.getEndTime()) && LocalDateUtil.lt(batch.getEndTime(),start)){
                        continue;
                    }
                    //1.如果开始时间和结束时间都存在，结束时间大于start，开始时间小于end,共计有六种情况
                    //①结束时间小于end，开始时间小于start        ==> start,结束时间
                    //②结束时间小于end，开始时间等于start        ==> start,结束时间
                    //③结束时间小于end，开始时间大于start        ==> 开始时间,结束时间
                    //④结束时间大于等于end，开始时间小于start     ==> start,end
                    //⑤结束时间大于等于end，开始时间等于start     ==> start,end
                    //⑥结束时间大于等于end，开始时间大于start     ==> 开始时间,end

                    //2.如果结束时间为null，则开始时间必须要小于end,共计有三种情况
                    //⑦开始时间小于start    ==> start,end
                    //⑧开始时间等于start    ==> start,end
                    //⑨开始时间大于start    ==> 开始时间,end

                    //3.总共分成四种情况
                    //start-结束时间
                    //start-end
                    //开始时间-结束时间
                    //开始时间-end
                    PmsProduceOrder order = orderMap.get(batch.getProduceOrderId());
                    if (Objects.isNull(order)) {
                        continue;
                    }
                    DwProduceOrderDTO orderDTO = new DwProduceOrderDTO();
                    BeanUtil.copyProperties(batch, orderDTO);
                    orderDTO.setClassDate(currentDate);
                    orderDTO.setProductLineId(order.getProductionLineId());
                    orderDTO.setProduceOrderId(order.getId());
                    orderDTO.setProduceOrderNo(order.getCode());
                    orderDTO.setMaterialId(order.getMaterialId());
                    LocalDateTime tempStart = LocalDateUtil.gt(batch.getStartTime(), start) ? batch.getStartTime() : start;
                    LocalDateTime tempEnd = Objects.isNull(batch.getEndTime()) || LocalDateUtil.ge(batch.getEndTime(), end) ? end : batch.getEndTime();
                    orderDTO.setStart(tempStart);
                    orderDTO.setEnd(tempEnd);
                    orderDTO.setThick(materialMap.getOrDefault(orderDTO.getMaterialId(), new EqpSpareVO()).getThickQuantity());
                    //考虑开始时间和结束时间是否跨班次问题，如果跨班次，则基于班次进行分割。如果不跨班次，则直接返回
                    if (!CalculateDateUtil.isSameClassType(tempStart, tempEnd)) {
                        DwProduceOrderDTO left = new DwProduceOrderDTO();
                        DwProduceOrderDTO right = new DwProduceOrderDTO();
                        BeanUtil.copyProperties(orderDTO, left);
                        BeanUtil.copyProperties(orderDTO, right);
                        left.setStart(tempStart);
                        left.setEnd(LocalDateUtil.getNight(currentDate));
                        left.setClassId(ClassUtil.MORNING);
                        resList.add(left);
                        right.setStart(LocalDateUtil.getNight(currentDate));
                        right.setEnd(tempEnd);
                        right.setClassId(ClassUtil.NIGHT);
                        resList.add(right);
                    } else {
                        QtTaskClassCalculate qtTaskClassCalculate = CalculateDateUtil.calculateClassTime(start);
                        orderDTO.setClassId(qtTaskClassCalculate.getClassType());
                        resList.add(orderDTO);
                    }
                }
            }
        }
        return resList;
    }

    @Override
    public List<ProduceOrderDataInfoVO> getExecuteOrderInfo() {
        //1.获取16条产线的信息
        SysOfficeTypeKeyDTO sysOfficeTypeKeyDTO = new SysOfficeTypeKeyDTO();
        sysOfficeTypeKeyDTO.setType(Lists.newArrayList(PmManagerConstant.NUM_9));
        sysOfficeTypeKeyDTO.setKeyList(Lists.newArrayList(PmManagerConstant.UROPATAGIA, PmManagerConstant.DEEP_PROCESSING));
        List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(() -> remoteOfficeFeign.listMultiFilter(sysOfficeTypeKeyDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<Integer> officeIdSet = sysOfficeVOS.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        Map<Integer, String> officeNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //2.查询16条产线下正在执行中的订单
        List<PmsProduceOrder> list = list(new LambdaQueryWrapper<PmsProduceOrder>().in(PmsProduceOrder::getProductionLineId, officeIdSet).eq(PmsProduceOrder::getState, PmManagerConstant.NUM_2));
        //3.定义返回数据的集合
        List<ProduceOrderDataInfoVO> infoVOList = Lists.newArrayList();
        //4.处理数据
        if (CollUtil.isNotEmpty(list)) {
            //获取当前产量
            Map<Long, BigDecimal> currentProductionMap = getCurrentProduction(list.stream().map(PmsProduceOrder::getId).collect(Collectors.toSet()));
            for (PmsProduceOrder pmsProduceOrder : list) {
                ProduceOrderDataInfoVO produceOrderDataInfoVO = BeanUtil.copyProperties(pmsProduceOrder, ProduceOrderDataInfoVO.class);
                produceOrderDataInfoVO.setProductionLineName(officeNameMap.get(produceOrderDataInfoVO.getProductionLineId()));
                produceOrderDataInfoVO.setPlanQuantity(pmsProduceOrder.getQuantity());
                produceOrderDataInfoVO.setCurrentProduction(currentProductionMap.get(pmsProduceOrder.getId()));
                Integer[] convert = WorkLineConvertUtil.convert(produceOrderDataInfoVO.getProductionLineName());
                produceOrderDataInfoVO.setWorkNumberFlag(convert[0]);
                produceOrderDataInfoVO.setLineNumberFlag(convert[1]);
                infoVOList.add(produceOrderDataInfoVO);
            }
            //获取单位名称
            Set<Long> dictIds = infoVOList.stream().map(ProduceOrderDataInfoVO::getUnitId).collect(Collectors.toSet());
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取物料信息
            List<Long> materialIds = infoVOList.stream().map(ProduceOrderDataInfoVO::getMaterialId).collect(Collectors.toList());
            Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //处理规格数据以及单位数据
            for (ProduceOrderDataInfoVO produceOrderDataInfoVO : infoVOList) {
                EqpSpareVO material = materialMap.get(produceOrderDataInfoVO.getMaterialId());
                produceOrderDataInfoVO.setSpecification(material.getSpecifications());
                produceOrderDataInfoVO.setUnitName(dictMap.get(produceOrderDataInfoVO.getUnitId()));
                produceOrderDataInfoVO.setLengthQuantity(material.getLengthQuantity());
                produceOrderDataInfoVO.setLengthUnitName(material.getLengthUnitName());
                produceOrderDataInfoVO.setWideQuantity(material.getWideQuantity());
                produceOrderDataInfoVO.setWideUnidName(material.getWideUnidName());
                produceOrderDataInfoVO.setHighQuantity(material.getHighQuantity());
                produceOrderDataInfoVO.setHighUnitName(material.getHighUnitName());
                if (produceOrderDataInfoVO.getProductionLineName().startsWith(PmManagerConstant.UROPATAGIA)) {
                    produceOrderDataInfoVO.setLineFlag(0);
                }
            }
            //排序
            infoVOList.sort(Comparator.comparing(ProduceOrderDataInfoVO::getLineFlag)
                    .thenComparing(ProduceOrderDataInfoVO::getWorkNumberFlag)
                    .thenComparing(ProduceOrderDataInfoVO::getLineNumberFlag));
        }
        return infoVOList;
    }

    @Override
    public Page<PanoramicTracingVO> tracing(PanoramicTracingDTO panoramicTracingDTO) {
        List<PanoramicTracingVO> tracingVOList = null;
        if (Objects.nonNull(panoramicTracingDTO.getCustomerBarcode())){
            List<String> caseNos = FeignOptional.of(() -> remoteWmsCustomerBarcodeFeignApi.listByCustomerBarcode(panoramicTracingDTO.getCustomerBarcode()), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
            panoramicTracingDTO.setCaseNos(caseNos);
            if (CollUtil.isNotEmpty(panoramicTracingDTO.getCaseNos())){
                //查询客户条码相关生产订单数据
                tracingVOList = baseMapper.tracingCustomerBarcode(panoramicTracingDTO);
            }
        }else {
            //查询生产订单数据
            tracingVOList = baseMapper.tracing(panoramicTracingDTO);
        }
       if (CollUtil.isNotEmpty(tracingVOList)) {
           if (Objects.nonNull(panoramicTracingDTO.getSortField())) {
               //自定义定义排序条件
               if (PmManagerConstant.PMS_ORDER_CODE.equals(panoramicTracingDTO.getSortField())) {
                   tracingVOList.sort(Comparator.comparing(PanoramicTracingVO::getOrderCode, Comparator.nullsFirst(String::compareTo)));
                   if (!panoramicTracingDTO.getAscFlag()) {
                       tracingVOList.sort(Comparator.comparing(PanoramicTracingVO::getOrderCode, Comparator.nullsFirst(String::compareTo)).reversed());
                   }
               } else if (PmManagerConstant.PMS_ORDER_BATCH_NO.equals(panoramicTracingDTO.getSortField())) {
                   tracingVOList.sort(Comparator.comparing(PanoramicTracingVO::getBatchNo, Comparator.nullsFirst(String::compareTo)));
                   if (!panoramicTracingDTO.getAscFlag()) {
                       tracingVOList.sort(Comparator.comparing(PanoramicTracingVO::getBatchNo, Comparator.nullsFirst(String::compareTo)).reversed());
                   }
               } else if (PmManagerConstant.PMS_ORDER_CASE_NO.equals(panoramicTracingDTO.getSortField())) {
                   tracingVOList.sort(Comparator.comparing(PanoramicTracingVO::getCaseNo, Comparator.nullsFirst(String::compareTo)));
                   if (!panoramicTracingDTO.getAscFlag()) {
                       tracingVOList.sort(Comparator.comparing(PanoramicTracingVO::getCaseNo, Comparator.nullsFirst(String::compareTo)).reversed());
                   }
               } else if (PmManagerConstant.PMS_MATERIAL_NAME.equals(panoramicTracingDTO.getSortField())) {
                   tracingVOList.sort(Comparator.comparing(PanoramicTracingVO::getMaterialName, Comparator.nullsFirst(String::compareTo)));
                   if (!panoramicTracingDTO.getAscFlag()) {
                       tracingVOList.sort(Comparator.comparing(PanoramicTracingVO::getMaterialName, Comparator.nullsFirst(String::compareTo)).reversed());
                   }
               } else if (PmManagerConstant.PMS_MATERIAL_CODE.equals(panoramicTracingDTO.getSortField())) {
                   tracingVOList.sort(Comparator.comparing(PanoramicTracingVO::getMaterialCode, Comparator.nullsFirst(String::compareTo)));
                   if (!panoramicTracingDTO.getAscFlag()) {
                       tracingVOList.sort(Comparator.comparing(PanoramicTracingVO::getMaterialCode, Comparator.nullsFirst(String::compareTo)).reversed());
                   }
               }
           } else {
               //默认排序
               tracingVOList.sort(Comparator.comparing(PanoramicTracingVO::getOrderCode, Comparator.nullsFirst(String::compareTo)).reversed()
                       .thenComparing(PanoramicTracingVO::getBatchNo, Comparator.nullsFirst(String::compareTo)).reversed()
                       .thenComparing(PanoramicTracingVO::getCaseNo, Comparator.nullsFirst(String::compareTo)).reversed());
           }

       }
        //逻辑分页
        Page<PanoramicTracingVO> page = LogicalPageUtil.page(tracingVOList, panoramicTracingDTO.getCurrent(), panoramicTracingDTO.getSize());

        if (CollUtil.isNotEmpty(page.getRecords())){
            //获取客户条码数据
            Set<String> caseNos = page.getRecords().stream().map(PanoramicTracingVO::getCaseNo).collect(Collectors.toSet());
            Map<String, String> customerBarcodeMap = FeignOptional.of(() -> remoteWmsCustomerBarcodeFeignApi.customerBarcodeByCaseNos(caseNos), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
            page.getRecords().forEach( panoramicTracingVO -> {
                panoramicTracingVO.setCustomerBarcode(customerBarcodeMap.get(panoramicTracingVO.getCaseNo()));
            });
        }

        return page;
    }

    @Override
    public CustomerIdAndNameVO getCustomer(QmsCustomerDTO dto) {
        CustomerIdAndNameVO result = new CustomerIdAndNameVO();
        PmsProduceOrder one = new PmsProduceOrder();
        if(dto.getType().equals(Integer.parseInt(PmManagerConstant.NUM_2))){
            //2）仅当手动输入生产订单号后校验为有效单号且该订单上的客户非空，预填该客户（若字段已填值，覆盖已填值）
            if(StrUtil.isNotBlank(dto.getProductOrder())){
                one = getOne(new LambdaQueryWrapper<PmsProduceOrder>().eq(PmsProduceOrder::getCode, dto.getProductOrder()), false);
            }else {
                throw new BusinessException("生产订单不能为空");
            }
        }else if(dto.getType().equals(Integer.parseInt(PmManagerConstant.NUM_3))){
            //3）仅当生产产线的值更新且值非空且检验类型为首检、巡检时，查询生产管理系统当前产线、当前时间执行中的生产订单的“客户”属性，若非空，系统自动带出该值（若字段已填值，覆盖已填值)
            if(Objects.nonNull(dto.getProductLineId())){
                one = getOne(new LambdaQueryWrapper<PmsProduceOrder>()
                        .eq(PmsProduceOrder::getProductionLineId, dto.getProductLineId())
                        .eq(PmsProduceOrder::getState,PmManagerConstant.OrderStateEnum.TWO.getCode()), false);
            }else {
                throw new BusinessException("产线不能为空");
            }
        }else if(dto.getType().equals(Integer.parseInt(PmManagerConstant.NUM_4))){
            if(Objects.nonNull(dto.getTimeSpot())&&Objects.nonNull(dto.getProductLineId())){
                one = getOne(new LambdaQueryWrapper<PmsProduceOrder>()
                        .and(wrapper->wrapper.eq(PmsProduceOrder::getProductionLineId, dto.getProductLineId())
                                .lt(PmsProduceOrder::getActualStartTime,dto.getTimeSpot()))
                        .and(wrapper->wrapper.gt(PmsProduceOrder::getActualEndTime,dto.getTimeSpot())
                                .or()
                                .isNull(PmsProduceOrder::getActualEndTime))
                        , false);
            }else {
                throw new BusinessException("产线、时间点不能为空");
            }
        }
        if(Objects.isNull(one) || one.getCustomerId()==null){
            return result;
        }
        result.setCustomerId(one.getCustomerId());
        String customerName = FeignOptional.of(() -> remoteOfficeFeign.getNameById(result.getCustomerId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        result.setCustomerName(customerName);
        return result;
    }

    @Override
    public Map<Long, Long> maiMaterialAByOrderId(Set<Long> orderIds) {
        Map<Long, Long> map = Maps.newHashMap();
        if (CollUtil.isNotEmpty(orderIds)) {
            List<PmsWorkOrderBom> list = pmsWorkOrderBomService.list(new LambdaQueryWrapper<PmsWorkOrderBom>()
                    .in(PmsWorkOrderBom::getWorkProduceId, orderIds)
                    .eq(PmsWorkOrderBom::getMainMaterialFlag, 1));
            if (CollUtil.isNotEmpty(list)){
                for (PmsWorkOrderBom pmsWorkOrderBom : list) {
                    map.put(pmsWorkOrderBom.getWorkProduceId(),pmsWorkOrderBom.getModuleId());
                }
            }
        }
        return map;
    }

    @Override
    public List<PmsProduceOrderVO> listOrderByProduceLineId(Integer productLineId) {
        List<PmsProduceOrder> list = list(new LambdaQueryWrapper<PmsProduceOrder>().eq(PmsProduceOrder::getProductionLineId, productLineId));
        return BeanUtil.copyToList(list,PmsProduceOrderVO.class);
    }

    @Override
    public RepaireBatchVO repaireBatchList(RepaireBatchListDTO dto) {
        RepaireBatchVO resVO = new RepaireBatchVO();
        //根据时间区间和产线筛选出生产批次信息
        //根据生产批次获取缺陷数据和领用数据
        LocalDateTime start = LocalDateUtil.getMorning(dto.getStart());
        LocalDateTime end = LocalDateUtil.getMorning(dto.getEnd().plusDays(1));
        //从数据字典中获取返修的标识
        SysDictVO typeSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode("1", "order_type"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //时间区间考虑
        List<RepaireBatchListVO> sourceList = baseMapper.repaireBatchList(dto.getProductLineList(), start, end, typeSysDict.getId());
        Assert.notEmpty(sourceList, "暂无生产批次");
        //根据物料获取规格和产线名称
        Set<Integer> lineIdSet = Sets.newHashSet();
        Set<Long> materialIdSet = Sets.newHashSet();
        Set<String> batchSet = Sets.newHashSet();
        sourceList.forEach(ele -> {
            materialIdSet.add(ele.getMaterialId());
            lineIdSet.add(ele.getProductLine());
            batchSet.add(ele.getBatchNo());
        });

        Map<Long, EqpSpareVO> spareVOMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(Lists.newArrayList(materialIdSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Integer, SysOfficeVO> lineMap = FeignOptional.of(() -> remoteOfficeFeign.listById(lineIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<String> specSet = Sets.newHashSet();
        sourceList.forEach(ele -> {
            ele.setProductLineName(lineMap.getOrDefault(ele.getProductLine(), new SysOfficeVO()).getOfficeName());
            ele.setSpecifications(spareVOMap.getOrDefault(ele.getMaterialId(), new EqpSpareVO()).getSpecifications());
            specSet.add(ele.getSpecifications());

        });
        //遍历所有数据，以规格分组 + 日期[选中日期] +产线 + 班次 + 生产订单号分组
        List<String> specList = new ArrayList<>(specSet);
        Collections.sort(specList);
        List<LocalDate> timeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), dto.getStart(), dto.getEnd());
        //获取产线集合
        List<SysOfficeVO> lineList = lineMap.values().stream().sorted(Comparator.comparingInt(SysOfficeVO::getTreeSort)).collect(Collectors.toList());

        //定义返回数据
        List<RepaireBatchListVO> resList = Lists.newArrayList();
        resVO.setDataList(resList);

        //获取班组
        TupleDTO<LocalDate, LocalDate> dateDTO = new TupleDTO<>(dto.getStart(), dto.getEnd().plusDays(1));
        Map<String, OaGroupCountDTO> classGroupMap = FeignOptional.of(() -> oaInfoService.getGroupInfo(dateDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        OaGroupCountDTO defaultOaGroupCountDTO = new OaGroupCountDTO();
        //根据生产批次获取报工记录
        Map<String, List<PmsReportWork>> batchLinkReportWorkMap = pmsReportWorkService.batchLinkReportWorkMap(batchSet);

        //获取磨边工位
        List<Integer> lineIdList = lineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        Set<Integer> edgStation = getEdgStation(lineIdList);

        SysDictVO morningDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_0, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO nightDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_1, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(morningDict, "早班数据字典不存在");
        Assert.notNull(nightDict, "晚班数据字典不存在");
        List<SysDictVO> classList = Lists.newArrayList(morningDict,nightDict);

        //获取物料转换单位数据字典数据及单位转换逻辑
        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO meterSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(meterSysDict, "片单位不存在");
        Assert.notNull(meterSysDict, "平方米单位不存在");
        Map<Long, List<SysSpareUnitConversionVO>> unitConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        QtChartParamDTO qtChartParamDTO = new QtChartParamDTO();
        //获取在线检缺陷
        qtChartParamDTO.setDateStart(dto.getStart());
        qtChartParamDTO.setDateEnd(dto.getEnd().plusDays(1));
        qtChartParamDTO.setProductLineIdList(lineIdList);
        qtChartParamDTO.setProductBatchList(Lists.newArrayList(batchLinkReportWorkMap.keySet()));
        List<QtCheckTaskDetailReportVO> checkTaskList = FeignOptional.of(() -> remoteCheckTaskDetailFeignApi.defectFlat(qtChartParamDTO), FeignConstant.STECCN_QMS_SERVICE_NAME).get();

        //获取原片不良list
        String defectCategoryName = "原片不良";
        List<QtDefectiveItemVO> defectItemList = FeignOptional.of(() -> remoteItemFeignApi.getDefectItemByCategory(defectCategoryName), FeignConstant.STECCN_QMS_SERVICE_NAME).get();
        Assert.notEmpty(defectItemList, "缺陷类别不存在");

        //构建返回表头
        List<String> defectNameHeaderList = defectItemList.stream().map(QtDefectiveItemVO::getName).collect(Collectors.toList());
        BaseReportHeaderVO defectHeader = new BaseReportHeaderVO(defectCategoryName, defectNameHeaderList);
        resVO.setHeaderList(Lists.newArrayList(defectHeader));

        //总计
        BigDecimal sumUsePiece = BigDecimal.ZERO;
        BigDecimal sumUseSquare = BigDecimal.ZERO;
        Map<String, Integer> defectSumMap = Maps.newHashMap();

        for (String spec : specList) {
            //规格合计
            BigDecimal specSumUsePiece = BigDecimal.ZERO;
            BigDecimal specSumUseSquare = BigDecimal.ZERO;
            Map<String, Integer> specDefectSumMap = Maps.newHashMap();
            for (LocalDate classDate : timeList) {
                for (SysOfficeVO line : lineList) {
                    for (SysDictVO classIdType : classList) {
                        int classId = Integer.parseInt(classIdType.getCode());
                        //根据规格，班次日期过滤出数据
                        LocalDateTime[] split = split(classId, classDate);
                        OaGroupCountDTO oaGroupCountDTO = classGroupMap.getOrDefault(classDate + ":" + classIdType.getName(), defaultOaGroupCountDTO);
                        List<RepaireBatchListVO> dataList = sourceList.stream()
                                .filter(v -> v.getSpecifications().equals(spec) &&
                                        v.getProductLineName().equals(line.getOfficeName()) &&
                                        overlap(split[0], split[1], v.getStartTime(), Objects.isNull(v.getEndTime()) ? LocalDateTime.now().plusDays(1) : v.getEndTime())).collect(Collectors.toList());
                        for (RepaireBatchListVO data : dataList) {
                            RepaireBatchListVO resVo = BeanUtil.copyProperties(data, RepaireBatchListVO.class);
                            resVo.setType(0);
                            resVo.setClassDate(classDate);
                            resVo.setClassId(classId);
                            resVo.setClassName(classIdType.getName());
                            resVo.setClassGroupId(oaGroupCountDTO.getGroupId());
                            resVo.setClassGroupName(oaGroupCountDTO.getGroupName());
                            //根据批次获取报工数据
                            List<PmsReportWork> reportWorkList = batchLinkReportWorkMap.getOrDefault(data.getBatchNo(), Lists.newArrayList());
                            //报工记录的生产批次为所在行生产批次
                            //报工记录的产线为所在行产线
                            //报工记录的加工时间在所在行班次日期&班次之内
                            //报工记录的工序为“磨边工序”
                            List<PmsReportWork> consumeList = reportWorkList.stream().filter(ele -> ele.getProductLineId().equals(line.getId()) &&
                                    ele.getClassesId().equals(classIdType.getId()) &&
                                    LocalDateUtil.isRange(ele.getStartTime(), split[0], split[1], true, true) &&
                                    edgStation.contains(ele.getStationId())
                            ).collect(Collectors.toList());
                            //根据物料耗用
                            List<BigDecimal> collect = consumeList.stream().map(PmsReportWork::getProcessQuantity).collect(Collectors.toList());
                            BigDecimal sum = BigDecimalUtil.sum(collect);
                            sum = BigDecimalUtil.setScale(sum, UnitLengthUtil.UNIT_PIECE_LENGTH);
                            //片转平方
                            BigDecimal square = UnitConvertUtil.convertMaterial(unitConvertMap.get(data.getMaterialId()), pieceSysDict.getId(), meterSysDict.getId(), sum, UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
                            sumUsePiece = BigDecimalUtil.add(sumUsePiece, sum);
                            specSumUsePiece = BigDecimalUtil.add(specSumUsePiece, sum);
                            sumUseSquare = BigDecimalUtil.add(sumUsePiece, square);
                            specSumUseSquare = BigDecimalUtil.add(specSumUseSquare, square);
                            resVo.setUseCountPiece(sum);
                            resVo.setUseCountSquare(square);
                            //原片不良的
                            //查询满足下列条件的在线检任务，记为集合T：
                            //状态为已检验
                            //产线为所在行产线
                            //生产批次为所在行生产批次
                            //缺陷记录时段在所在行班次日期&班次内
                            List<String> valueDataList = Lists.newArrayList();
                            Integer defectSumRow = null;
                            for (QtDefectiveItemVO defectItem : defectItemList) {
                                List<Integer> valueList = checkTaskList.stream().filter(ele ->
                                        ele.getDefectCode().equals(defectItem.getCode()) &&
                                        ele.getProductLineId().equals(line.getId()) &&
                                        Objects.equals(ele.getProductBatch(),resVo.getBatchNo()) &&
                                        ele.getClassId().equals(resVo.getClassId()) &&
                                        LocalDateUtil.isRange(ele.getDefectStartTime(), split[0], split[1], true, true)
                                ).map(QtCheckTaskDetailReportVO::getDefectNum).collect(Collectors.toList());
                                Integer sumDefect = IntegerUtil.sum(valueList);
                                MapUtil.add(specDefectSumMap, defectItem.getCode(), sumDefect, (x, y) -> IntegerUtil.add(x, y));
                                MapUtil.add(defectSumMap, defectItem.getCode(), sumDefect, (x, y) -> IntegerUtil.add(x, y));
                                valueDataList.add(Objects.nonNull(sumDefect) ? String.valueOf(sumDefect) : null);
                                defectSumRow = IntegerUtil.add(defectSumRow, sumDefect);
                            }
                            resVo.setModule(Lists.newArrayList(new FurtherValueStrVO(valueDataList)));
                            resVo.setDefectSum(Objects.nonNull(defectSumRow) ? BigDecimal.valueOf(defectSumRow) : null);
                            resVo.setDefectRate(BigDecimalUtil.percent(resVo.getDefectSum(), resVo.getUseCountPiece()));
                            resList.add(resVo);
                        }
                    }
                }
            }
            if(CollUtil.isNotEmpty(resList)){
                //插入合计行
                resList.add(rowCount(1, specSumUsePiece, specSumUseSquare, defectItemList, specDefectSumMap));
            }
        }
        //插入总计行
        if(CollUtil.isNotEmpty(resList)){
            resList.add(rowCount(2, sumUsePiece, sumUseSquare, defectItemList, defectSumMap));
        }
        return resVO;
    }

    @Override
    public void repaireBatchListDownload(RepaireBatchListDTO dto, HttpServletResponse response) throws IOException {
        RepaireBatchVO result = repaireBatchList(dto);
        List<RepaireBatchListVO> rowList = result.getDataList();
        SteccnTableAssertUtil.notEmpty(rowList, "数据为空");
        //写表头
        List<List<String>> list = new ArrayList<>();
        list.add(Lists.newArrayList("规格（长X宽X厚度）"));
        list.add(Lists.newArrayList("日期"));
        list.add(Lists.newArrayList("产线"));
        list.add(Lists.newArrayList("班次"));
        list.add(Lists.newArrayList("班组"));
        list.add(Lists.newArrayList("订单号"));
        list.add(Lists.newArrayList("物料名称"));
        result.getHeaderList().forEach(ele -> {
            ele.getValue().forEach(aa -> list.add(Lists.newArrayList(ele.getLabel(), aa)));
        });
        list.add(Lists.newArrayList("合计"));
        list.add(Lists.newArrayList("合计不良率"));
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("返洗不良报表", "UTF-8");
        fileName += LocalDateUtil.localDateFormat(LocalDate.now(), "yyyy-MM-dd");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        //数据
        List<List<Object>> dataList = Lists.newArrayList();
        rowList.forEach(ele -> {
            List<Object> dataEle = Lists.newArrayList();
            dataEle.add(ele.getSpecifications());
            dataEle.add(LocalDateUtil.format(ele.getClassDate(),"yyyy-MM-dd"));
            dataEle.add(ele.getProductLineName());
            dataEle.add(ele.getClassName());
            dataEle.add(ele.getClassGroupName());
            dataEle.add(ele.getProduceOrderNo());
            dataEle.add(ele.getMaterialName());
            ele.getModule().forEach(aa ->{
                aa.getValue().forEach(bb ->{
                    dataEle.add(bb);
                });
            });
            dataEle.add(ele.getDefectSum());
            dataEle.add(BigDecimalUtil.jointPercent(ele.getDefectRate()));
            dataList.add(dataEle);
        });
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(0, "返洗不良报表")
                .head(list)
                .registerWriteHandler(new RowHeightStyleStrategy(30, 20))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .registerWriteHandler(new HeadContentStyleStrategy())
                .build();
        excelWriter.write(dataList, writeSheet);
        excelWriter.finish();
        response.flushBuffer();
    }

    /**
     * 判断A时间区间与B时间区间是否有交叉
     *
     * @param aStart A区间开始时间
     * @param aEnd   A区间结束时间
     * @param bStart B区间开始时间
     * @param bEnd   B区间结束时间
     * @return
     */
    private boolean overlap(LocalDateTime aStart, LocalDateTime aEnd, LocalDateTime bStart, LocalDateTime bEnd) {
        if (LocalDateUtil.gt(aStart, bEnd) || LocalDateUtil.lt(aEnd, bStart)) {
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    @Override
    public OutsourcingDayReportVO outsourcing(DayReportCommonDTO dto) {
        /**
         * 报工记录的产线属于已选中的产线
         * 报工记录的加工时段在区间[起始日期8:00, 终止日期+1 8:00]内
         * 报工记录的物料消耗列表中包含编码“P1.W.P”开头的物料（即外购原片）
         */
        LocalDateTime begin = LocalDateUtil.getMorning(dto.getStart());
        LocalDateTime over = LocalDateUtil.getMorning(dto.getEnd().plusDays(1));
        List<OutSourcingListDTO> batchList = pmsReportWorkMapper.outsourcingClass(dto.getProductLineList(), begin, over);
        Assert.notEmpty(batchList, "暂无数据");
        OutsourcingDayReportVO resVo = new OutsourcingDayReportVO();
        List<OutSourcingListDTO> dataList = Lists.newArrayList();
        resVo.setDataList(dataList);
        Set<Long> materialIdSet = Sets.newHashSet();
        Set<String> batchNoSet = Sets.newHashSet();
        Set<String> supplierSet = Sets.newHashSet();
        Set<Integer> lineIdSet = Sets.newHashSet();
        batchList.forEach(ele -> {
            materialIdSet.add(ele.getMaterialId());
            batchNoSet.add(ele.getBatchNo());
            supplierSet.add(ele.getSupplierCode());
            lineIdSet.add(ele.getProductLineId());
        });

        Map<String, SysOfficeVO> supplierCodeMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(supplierSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Integer, String> lineIdNameMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(lineIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, List<SysSpareUnitConversionVO>> convertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(Lists.newArrayList(materialIdSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //查询所有报工的物料消耗关联的供应商
        SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDict1, "换算单位1不存在");
        Assert.notNull(sysDict2, "换算单位2不存在");

        //获取批次的缺陷信息
        QtChartParamDTO qtChartParamDTO = new QtChartParamDTO();
        //获取在线检缺陷
        qtChartParamDTO.setDateStart(dto.getStart());
        qtChartParamDTO.setDateEnd(dto.getEnd().plusDays(1));
        qtChartParamDTO.setProductLineIdList(dto.getProductLineList());
        qtChartParamDTO.setProductBatchList(Lists.newArrayList(batchNoSet));
        List<QtCheckTaskDetailReportVO> checkTaskList = FeignOptional.of(() -> remoteCheckTaskDetailFeignApi.defectFlat(qtChartParamDTO), FeignConstant.STECCN_QMS_SERVICE_NAME).get();

        //获取原片不良的缺陷项集合
        List<QtDefectiveItemVO> defectItemList = FeignOptional.of(() -> remoteItemFeignApi.getDefectItemByCategory("原片不良"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //构建表头
        List<BaseReportHeaderVO> headerList = Lists.newArrayList();
        List<String> nameList = Lists.newArrayList();
        defectItemList.forEach(ele -> nameList.add(ele.getName()));
        headerList.add(new BaseReportHeaderVO("原片不良", nameList));
        resVo.setHeaderList(headerList);

        SysDictVO morningDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_0, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO nightDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_1, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(morningDict, "早班数据字典不存在");
        Assert.notNull(nightDict, "晚班数据字典不存在");

        //获取班组
        TupleDTO<LocalDate, LocalDate> dateDTO = new TupleDTO<>(dto.getStart(), dto.getEnd().plusDays(1));
        Map<String, OaGroupCountDTO> classGroupMap = FeignOptional.of(() -> oaInfoService.getGroupInfo(dateDTO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        Set<Long> rwIdSet = batchList.stream().map(OutSourcingListDTO::getReportWorkId).collect(Collectors.toSet());
        Map<Long, String> rwLinkSupplierMap = FeignOptional.of(() -> remoteWmsStorageReceiptFeignApi.rwLinkSupplier(rwIdSet), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        batchList.forEach(ele -> {
            ele.setSupplierCode(rwLinkSupplierMap.get(ele.getReportWorkId()));
            EqpSpareVO eqp = materialMap.getOrDefault(ele.getMaterialId(), new EqpSpareVO());
            ele.setMaterialName(eqp.getName());
            ele.setSpecifications(eqp.getSpecifications());
            BigDecimal quantitySq = UnitConvertUtil.convertMaterial(convertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), UnitLengthUtil.UNIT_SQUARE_METER_LENGTH);
            ele.setQuantitySq(quantitySq);
            if (Objects.isNull(ele.getSupplierCode())) {
                ele.setSupplierCode(Strings.EMPTY);
            }
            if (Objects.isNull(ele.getSpecifications())) {
                ele.setSpecifications(Strings.EMPTY);
            }
        });
        //根据供应商，规格，班次日期，产线，班次，物料名称
        Map<String, BigDecimal> totalCountBdMap = Maps.newHashMap();
        Map<String, Integer> totalCountIntMap = Maps.newHashMap();
        Map<String, List<OutSourcingListDTO>> supplierMap = batchList.stream().collect(Collectors.groupingBy(OutSourcingListDTO::getSupplierCode));
        for (Map.Entry<String, List<OutSourcingListDTO>> supplierEntry : supplierMap.entrySet()) {
            //定义规格的统计map
            Map<String, BigDecimal> supplierCountBdMap = Maps.newHashMap();
            Map<String, Integer> supplierCountIntMap = Maps.newHashMap();
            String supplierName = supplierCodeMap.getOrDefault(supplierEntry.getKey(), new SysOfficeVO()).getOfficeName();
            //基于规格分组
            Map<String, List<OutSourcingListDTO>> specMap = supplierEntry.getValue().stream().collect(Collectors.groupingBy(OutSourcingListDTO::getSpecifications));
            for (Map.Entry<String, List<OutSourcingListDTO>> specEntry : specMap.entrySet()) {
                //定义规格的统计map
                Map<String, BigDecimal> specCountBdMap = Maps.newHashMap();
                Map<String, Integer> specCountIntMap = Maps.newHashMap();
                //基于班次日期分组
                Map<LocalDate, List<OutSourcingListDTO>> classDateMap = specEntry.getValue().stream().collect(Collectors.groupingBy(OutSourcingListDTO::getClassDate));
                for (Map.Entry<LocalDate, List<OutSourcingListDTO>> classDateEntry : classDateMap.entrySet()) {

                    //基于产线，班次，生产批次，物料名称
                    Map<String, List<OutSourcingListDTO>> gpMap = classDateEntry.getValue().stream().collect(Collectors.groupingBy(v -> JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, v.getProductLineId(), v.getClassId(), v.getBatchNo(), v.getMaterialName())));
                    for (Map.Entry<String, List<OutSourcingListDTO>> gpEntry : gpMap.entrySet()) {
                        //聚合计算
                        OutSourcingListDTO dataRow = gpEntry.getValue().get(0);
                        List<BigDecimal> quantityList = Lists.newArrayList();
                        List<BigDecimal> quantitySqList = Lists.newArrayList();
                        gpEntry.getValue().forEach(ele -> {
                            quantityList.add(ele.getQuantity());
                            quantitySqList.add(ele.getQuantitySq());
                        });
                        if (morningDict.getId().equals(dataRow.getClassId())) {
                            dataRow.setClassName(morningDict.getName());
                            dataRow.setClassIdInt(0);
                        } else {
                            dataRow.setClassName(nightDict.getName());
                            dataRow.setClassIdInt(1);
                        }
                        List<FurtherValueStrVO> furtherValueStrVOList = Lists.newArrayList();
                        List<String> numList = Lists.newArrayList();
                        Integer defectSum = null;
                        //根据产线，物料id，批次号，班次日期，班次，缺陷获取缺陷数
                        for (QtDefectiveItemVO ele : defectItemList) {
                            List<Integer> defectList = checkTaskList.stream().filter(a -> a.getProductLineId().equals(dataRow.getProductLineId()) &&
                                    a.getProductBatch().equals(dataRow.getBatchNo()) &&
                                    a.getDefectCode().equals(ele.getCode()) &&
                                    a.getClassesDate().isEqual(dataRow.getClassDate()) &&
                                    a.getClassId().equals(dataRow.getClassIdInt())).map(QtCheckTaskDetailReportVO::getDefectNum).collect(Collectors.toList());
                            Integer sum = IntegerUtil.sum(defectList);
                            MapUtil.add(specCountIntMap, ele.getCode(), sum, (a, b) -> IntegerUtil.add(a, b));
                            MapUtil.add(supplierCountIntMap, ele.getCode(), sum, (a, b) -> IntegerUtil.add(a, b));
                            MapUtil.add(totalCountIntMap, ele.getCode(), sum, (a, b) -> IntegerUtil.add(a, b));
                            numList.add(IntegerUtil.format(sum));
                            defectSum = IntegerUtil.add(sum, defectSum);
                        }
                        OaGroupCountDTO oaGroupCountDTO = classGroupMap.get(classDateEntry.getKey() + ":" + dataRow.getClassName());
                        if (Objects.nonNull(oaGroupCountDTO)) {
                            dataRow.setClassGroupName(oaGroupCountDTO.getGroupName());
                        }
                        dataRow.setModule(furtherValueStrVOList);
                        FurtherValueStrVO onlyEle = new FurtherValueStrVO();
                        onlyEle.setValue(numList);
                        dataRow.setModule(Lists.newArrayList(onlyEle));
                        dataRow.setType(0);
                        dataRow.setQuantity(BigDecimalUtil.setScale(BigDecimalUtil.sum(quantityList), UnitLengthUtil.UNIT_PIECE_LENGTH));
                        dataRow.setQuantitySq(BigDecimalUtil.sum(quantitySqList));
                        dataRow.setDefectNum(defectSum);
                        dataRow.setDefectRate(BigDecimalUtil.percent(BigDecimalUtil.valueOf(dataRow.getDefectNum()), dataRow.getQuantity()));
                        //获取产线名称
                        dataRow.setProductLineName(lineIdNameMap.get(dataRow.getProductLineId()));
                        dataList.add(dataRow);
                        MapUtil.add(specCountBdMap, "0", dataRow.getQuantity(), (a, b) -> BigDecimalUtil.add(a, b));
                        MapUtil.add(specCountBdMap, "1", dataRow.getQuantitySq(), (a, b) -> BigDecimalUtil.add(a, b));
                        MapUtil.add(specCountIntMap, "0", dataRow.getDefectNum(), (a, b) -> IntegerUtil.add(a, b));
                        MapUtil.add(supplierCountBdMap, "0", dataRow.getQuantity(), (a, b) -> BigDecimalUtil.add(a, b));
                        MapUtil.add(supplierCountBdMap, "1", dataRow.getQuantitySq(), (a, b) -> BigDecimalUtil.add(a, b));
                        MapUtil.add(supplierCountIntMap, "0", dataRow.getDefectNum(), (a, b) -> IntegerUtil.add(a, b));
                        MapUtil.add(totalCountBdMap, "0", dataRow.getQuantity(), (a, b) -> BigDecimalUtil.add(a, b));
                        MapUtil.add(totalCountBdMap, "1", dataRow.getQuantitySq(), (a, b) -> BigDecimalUtil.add(a, b));
                        MapUtil.add(totalCountIntMap, "0", dataRow.getDefectNum(), (a, b) -> IntegerUtil.add(a, b));
                    }
                }
                //插入小计（单规格）
                dataList.add(outSourcingCountRow(1, supplierName, "小计（单规格）", defectItemList, specCountBdMap, specCountIntMap));
            }
            //插入合计（单供应商）
            dataList.add(outSourcingCountRow(2, supplierName, "合计（单供应商）", defectItemList, supplierCountBdMap, supplierCountIntMap));
        }
        //插入总计
        dataList.add(outSourcingCountRow(3, "总计", Strings.EMPTY, defectItemList, totalCountBdMap, totalCountIntMap));

        return resVo;
    }


    @Override
    public Map<String, BigDecimal> orderDetailOtherEnergy(LocalDate start ,LocalDate end, List<Integer> officeIdList) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        if (Objects.nonNull(start) && Objects.nonNull(end) && CollUtil.isNotEmpty(officeIdList)) {
            //考虑日期范围
            Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(officeIdList)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            List<Integer> allLineIdList = Lists.newArrayList();
            if (CollUtil.isNotEmpty(officeMap)) {
                Collection<SysOfficeVO> sysOfficeVOList = officeMap.values();
                //查询类型为产线
                List<Integer> lineIdList = sysOfficeVOList.stream().filter(ele -> SysOfficeConstant.PRODUCT_LINE.equals(ele.getOfficeType())).map(SysOfficeVO::getId).collect(Collectors.toList());
                allLineIdList.addAll(lineIdList);
                //查询类型为产线孩子的
                List<Integer> lineChildIdList = sysOfficeVOList.stream().filter(ele -> SysOfficeConstant.STATION.equals(ele.getOfficeType())).map(SysOfficeVO::getId).collect(Collectors.toList());
                Map<Integer, Integer> parentLineMap = Maps.newHashMap();
                if (CollUtil.isNotEmpty(lineChildIdList)) {
                    parentLineMap = FeignOptional.of(() -> remoteOfficeFeign.parentLine(Sets.newHashSet(lineChildIdList)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    allLineIdList.addAll(parentLineMap.values());
                }
                //查询类型为产线父节点的
                List<Integer> lineParentIdList = sysOfficeVOList.stream().filter(ele -> SysOfficeConstant.DEPARTMENT.equals(ele.getOfficeType()) || SysOfficeConstant.PROVINCIAL_COMPANY.equals(ele.getOfficeType())).map(SysOfficeVO::getId).collect(Collectors.toList());
                Map<Integer, List<Integer>> childLineMap = Maps.newHashMap();
                if (CollUtil.isNotEmpty(lineParentIdList)) {
                    childLineMap = FeignOptional.of(() -> remoteOfficeFeign.childType(new TupleDTO<>(SysOfficeConstant.PRODUCT_LINE, Sets.newHashSet(lineParentIdList))), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    List<Integer> childLineIdList = childLineMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
                    allLineIdList.addAll(childLineIdList);
                }
                Map<Integer, SysOfficeVO> lineIdMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(allLineIdList)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                List<Integer> jpLineIdList = lineIdMap.values().stream().filter(ele -> ele.getOfficeName().contains("基片")).map(SysOfficeVO::getId).collect(Collectors.toList());
                List<Integer> sjgLineIdList = lineIdMap.values().stream().filter(ele -> ele.getOfficeName().contains("深加工")).map(SysOfficeVO::getId).collect(Collectors.toList());
                Map<String, BigDecimal> valueMap1 = pmsReportWorkService.dateLinePmUnit(jpLineIdList, start, end, 0, PmManagerConstant.MEASURE_UNIT_TON);
                Map<String, BigDecimal> valueMap2 = pmsReportWorkService.dateLinePmUnit(sjgLineIdList, start, end, 1, PmManagerConstant.MEASURE_UNIT_SM);
                log.info("=== {}", valueMap1);
                log.info("=== {}", valueMap2);
                List<LocalDate> timeList = ShardTimeUtil.cutDate(QtManagerConstant.QtGranularityEnum.DAY.getType(), start, end);
                //考虑返回数据
                for (int i = 1; i < timeList.size(); i++) {
                    LocalDate classDate = timeList.get(i - 1);
                    for (Integer officeId : officeIdList) {
                        SysOfficeVO currentNode = officeMap.get(officeId);
                        if (Objects.nonNull(currentNode)) {
                            if (SysOfficeConstant.PRODUCT_LINE.equals(currentNode.getOfficeType())) {
                                String joint = JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, currentNode.getId(), classDate);
                                BigDecimal bigDecimal1 = valueMap1.get(joint);
                                BigDecimal bigDecimal2 = valueMap2.get(joint);
                                resMap.put(joint, BigDecimalUtil.add(bigDecimal1, bigDecimal2));
                            }
                        }else if(SysOfficeConstant.STATION.equals(currentNode.getOfficeType())){
                            Integer lineId = parentLineMap.get(officeId);
                            String joint = JointUtil.joint(JointUtil.SEPARATOR_UNDERLINE, lineId, classDate);
                            BigDecimal bigDecimal1 = valueMap1.get(joint);
                            BigDecimal bigDecimal2 = valueMap2.get(joint);
                            resMap.put(joint, BigDecimalUtil.add(bigDecimal1, bigDecimal2));
                        }else {
                            List<Integer> childLineIdList = childLineMap.get(officeId);


                        }
                    }
                }
            }
        }
        return resMap;
    }

    /**
     * 外购原片不良日报表插入小计行
     * @param type 类型
     * @param supplierName 供应商名称
     * @param spec 规格
     * @param defectItemList 缺陷项
     * @param bdMap 统计map
     * @param intMap 统计map
     * @return
     */
    private OutSourcingListDTO outSourcingCountRow(Integer type, String supplierName, String spec, List<QtDefectiveItemVO> defectItemList, Map<String, BigDecimal> bdMap, Map<String, Integer> intMap) {
        OutSourcingListDTO model = new OutSourcingListDTO();
        model.setType(type);
        model.setSupplierName(supplierName);
        model.setSpecifications(spec);
        model.setQuantity(bdMap.get("0"));
        model.setQuantitySq(bdMap.get("1"));
        List<String> numList = Lists.newArrayList();
        defectItemList.forEach(ele -> numList.add(IntegerUtil.format(intMap.get(ele.getCode()))));
        model.setDefectNum(intMap.get("0"));
        FurtherValueStrVO onlyEle = new FurtherValueStrVO();
        onlyEle.setValue(numList);
        model.setModule(Lists.newArrayList(onlyEle));
        model.setDefectRate(BigDecimalUtil.percent(BigDecimalUtil.valueOf(model.getDefectNum()), model.getQuantity()));
        return model;
    }


    /**
     * 获取总计行或者单规格行
     * @param type
     * @param usePiece
     * @param useSquare
     * @param defectItemList
     * @param defectSumMap
     * @return
     */
    private RepaireBatchListVO rowCount(Integer type, BigDecimal usePiece, BigDecimal useSquare, List<QtDefectiveItemVO> defectItemList, Map<String, Integer> defectSumMap) {
        RepaireBatchListVO row = new RepaireBatchListVO();
        row.setType(type);
        if (type == 1) {
            row.setSpecifications("合计（单规格）");
        } else {
            row.setSpecifications("总计");
        }
        row.setUseCountPiece(usePiece);
        row.setUseCountSquare(useSquare);
        List<String> valueDataList = Lists.newArrayList();
        Integer countSum = null;
        for (QtDefectiveItemVO defectItem : defectItemList) {
            Integer count = defectSumMap.get(defectItem.getCode());
            countSum = IntegerUtil.add(count, countSum);
            valueDataList.add(Objects.nonNull(count) ? String.valueOf(count) : null);
        }
        row.setDefectSum(Objects.nonNull(countSum) ? BigDecimal.valueOf(countSum) : null);
        row.setDefectRate(BigDecimalUtil.percent(row.getDefectSum(), usePiece));
        row.setModule(Lists.newArrayList(new FurtherValueStrVO(valueDataList)));
        return row;
    }

    /**
     * 获取这些产线磨边工位
     *
     * @param lineList 产线集合
     * @return
     */
    private Set<Integer> getEdgStation(List<Integer> lineList) {
        //获取磨边工位
        SysOfficeParentDTO parentDTO = new SysOfficeParentDTO();
        parentDTO.setParentIds(Sets.newHashSet(lineList));
        parentDTO.setOfficeType(PmManagerConstant.NUM_13);
        //获取产线下的工位
        List<SysOfficeVO> officeList = FeignOptional.of(() -> remoteOfficeFeign.listByParentIdAndType(parentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //包含磨边工位的
        return officeList.stream().filter(office -> office.getOfficeName().contains("磨边工位")).map(SysOfficeVO::getId).collect(Collectors.toSet());
    }



    /**
     * 根据班次切分班次日期
     * @param type 班次 0 ， 1 早班 晚班
     * @param date 班次日期
     * @return
     */
    private LocalDateTime[] split(Integer type, LocalDate date) {
        //type只能是0或1
        LocalDateTime night = LocalDateUtil.getNight(date);
        LocalDateTime x = night.plusHours(type == 0 ? -12 : 12);
        return type == 1 ? new LocalDateTime[]{night, x} : new LocalDateTime[]{x, night};
    }



//    /**
//     * 处理订单的当前产量
//     *
//     * @param orderIdSet
//     * @return
//     */
//    private Map<Long, BigDecimal> getCurrentProduction(Set<Long> orderIdSet) {
//        Map<Long, BigDecimal> resMap = Maps.newHashMap();
//        if (CollUtil.isNotEmpty(orderIdSet)) {
//            //获取订单的执行工位
//            Map<Long, Set<Integer>> stationMap = getStation(orderIdSet);
//            //获取订单的批次时间
//            Map<Long, PmReportWorkPreloadDTO> orderDateMap = getOrderDate(orderIdSet);
//            //获取工位绑定的的采集点
//            Set<Integer> stationIdSet = stationMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
//            Map<Integer, String> codeMap = FeignOptional.of(() -> remoteSysOfficeCollectFeignApi.listByStation(stationIdSet, 1), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
//            resMap = orderIdSet.stream().collect(Collectors.toMap(orderId -> orderId, orderId -> {
//                PmReportWorkPreloadDTO pmReportWorkPreloadDTO = orderDateMap.get(orderId);
//                Set<Integer> stationSet = stationMap.get(orderId);
//                BigDecimal currentProduction = BigDecimal.ZERO;
//                if (CollUtil.isNotEmpty(stationSet)) {
//                    currentProduction = stationSet.stream()
//                            .map(stationId -> codeMap.get(stationId))
//                            .filter(Objects::nonNull)
//                            .map(code -> {
//                                pmReportWorkPreloadDTO.setCode(code);
//                                return FeignOptional.of(() -> remoteDataPlatformFeignApi.preload(pmReportWorkPreloadDTO), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
//                            })
//                            .reduce(BigDecimal.ZERO, BigDecimal::add);
//                }
//                return currentProduction;
//            }));
//        }
//        return resMap;
//    }



    /**
     * 处理订单的当前产量
     *
     * @param orderIdSet
     * @return
     */
    private Map<Long, BigDecimal> getCurrentProduction(Set<Long> orderIdSet) {
        Map<Long, BigDecimal> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(orderIdSet)) {
            //获取订单的执行工位
            Map<Long, Set<Integer>> stationMap = getStation(orderIdSet);
            //获取订单的批次时间
            Map<Long, PmReportWorkPreloadDTO> orderDateMap = getOrderDate(orderIdSet);
            //获取工位绑定的的采集点
            Set<Integer> stationIdSet = Sets.newHashSet();
            for (Map.Entry<Long, Set<Integer>> entry : stationMap.entrySet()) {
                stationIdSet.addAll(entry.getValue());
            }
            Map<Integer, List<String>> codeMap = FeignOptional.of(() -> remoteSysOfficeCollectFeignApi.listByStation(stationIdSet, 1), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long,List<String>> orderReportCodeMap = Maps.newHashMap();
            for (Map.Entry<Long, Set<Integer>> entry : stationMap.entrySet()) {
                if (CollUtil.isNotEmpty(entry.getValue())){
                    for (Integer stationId : entry.getValue()) {
                        if (Objects.nonNull(codeMap.get(stationId))){
                            List<String> codes = codeMap.get(stationId);
                            if (CollUtil.isNotEmpty(codes)){
                                orderReportCodeMap.put(entry.getKey(),codes);
                            }
                        }
                    }
                }
            }
            log.info("请求数仓前——>"+LocalDateTime.now());
            for (Long orderId : orderIdSet) {
                BigDecimal currentProduction = BigDecimal.ZERO;
                PmReportWorkPreloadDTO pmReportWorkPreloadDTO = orderDateMap.get(orderId);
                List<String> codes = orderReportCodeMap.get(orderId);
                if (CollUtil.isNotEmpty(codes)){
                    for (String code : codes) {
                        pmReportWorkPreloadDTO.setCode(code);
                        BigDecimal bigDecimal = FeignOptional.of(() -> remoteDataPlatformFeignApi.preload(pmReportWorkPreloadDTO), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
                        if (Objects.nonNull(bigDecimal)) {
                            currentProduction = currentProduction.add(bigDecimal);
                        }
                    }
                }
                resMap.put(orderId,currentProduction);
            }
            log.info("请求数仓后——>"+","+LocalDateTime.now());
//            for (Long orderId : orderIdSet) {
//                PmReportWorkPreloadDTO pmReportWorkPreloadDTO = orderDateMap.get(orderId);
//                Set<Integer> stationSet = stationMap.get(orderId);
//                BigDecimal currentProduction = BigDecimal.ZERO;
//                if (CollUtil.isNotEmpty(stationSet)) {
//                    for (Integer stationId : stationSet) {
//                        if (Objects.nonNull(codeMap.get(stationId))) {
//                            List<String> codes = codeMap.get(stationId);
//                            if (CollUtil.isNotEmpty(codes)){
//                                for (String code : codes) {
//                                    pmReportWorkPreloadDTO.setCode(code);
//                                    BigDecimal bigDecimal = FeignOptional.of(() -> remoteDataPlatformFeignApi.preload(pmReportWorkPreloadDTO), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
//                                    if (Objects.nonNull(bigDecimal)) {
//                                        currentProduction = currentProduction.add(bigDecimal);
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                resMap.put(orderId,currentProduction);
//            }
        }
        return resMap;
    }



    /**
     * 根据订单ID集合获取工位信息:获取订单最后一个工序的执行工位(20230511改为取下片工序)
     * @param orderIdSet 订单ID集合
     * @return 工位信息Map，key为订单ID，value为工位ID集合
     */
    private Map<Long, Set<Integer>> getStation(Set<Long> orderIdSet) {
        Map<Long, Set<Integer>> map = new HashMap<>(); // 创建一个Map对象，用于存储订单ID和对应的工位ID集合
        Map<Long, List<PmsProduceOrderCraftVO>> craftMap = pmsProduceOrderCraftService.crafts(orderIdSet); // 调用服务获取订单ID和对应的工艺信息
        if (CollUtil.isNotEmpty(craftMap)) { // 判断工艺信息是否为空
            for (Map.Entry<Long, List<PmsProduceOrderCraftVO>> entry : craftMap.entrySet()) { // 遍历工艺信息Map
                List<PmsProduceOrderCraftVO> value = entry.getValue(); // 获取工艺信息列表
                if (CollUtil.isNotEmpty(value)) { // 判断工艺信息列表是否为空
                    Optional<PmsProduceOrderCraftVO> result = value.stream() // 通过流处理获取工艺信息列表中的第一个符合条件的工艺信息
                            .filter(item -> PmManagerConstant.XPGX.equals(item.getCode()))
                            .findFirst();
                    result.ifPresent(pmsProduceOrderCraftVO -> { // 如果存在符合条件的工艺信息，则将其对应的工位ID集合存入Map中
                        Set<Integer> stationIdSet = pmsProduceOrderCraftVO.getStationList().stream()
                                .map(PmsWorkOrderCraftStationVO::getExecuteStationId)
                                .map(Long::intValue)
                                .collect(Collectors.toSet());
                        map.put(entry.getKey(), stationIdSet);
                    });
                }
            }
        }
        return map; // 返回工位信息Map
    }



    /**
     * 获取订单的执行时间段
     *
     * @param orderIdSet
     * @return
     */
    private Map<Long, PmReportWorkPreloadDTO> getOrderDate(Set<Long> orderIdSet) {
        Map<Long, PmReportWorkPreloadDTO> resMap = Maps.newHashMap();
        Map<Long, List<PmsWorkOrderBatch>> batchMap = pmsWorkOrderBatchService.listByOrderId(orderIdSet);
        if (CollUtil.isNotEmpty(batchMap)) {
            for (Map.Entry<Long, List<PmsWorkOrderBatch>> batchEntry : batchMap.entrySet()) {
                LocalDateTime startTime = Collections.min(batchEntry.getValue().stream().map(PmsWorkOrderBatch::getStartTime).collect(Collectors.toList()));
                LocalDateTime endTime = Collections.max(batchEntry.getValue().stream().map(PmsWorkOrderBatch::getStartTime).collect(Collectors.toList()));
                for (PmsWorkOrderBatch pmsWorkOrderBatch : batchEntry.getValue()) {
                    if (Objects.isNull(pmsWorkOrderBatch.getEndTime())) {
                        //如果当前批次的结束时间为空，那么结束时间为当前时间
                        endTime = LocalDateTime.now();
                    }
                }
                PmReportWorkPreloadDTO pmReportWorkPreloadDTO = new PmReportWorkPreloadDTO();
                pmReportWorkPreloadDTO.setStartTime(startTime);
                pmReportWorkPreloadDTO.setEndTime(endTime);
                resMap.put(batchEntry.getKey(), pmReportWorkPreloadDTO);
            }
        }
        return resMap;
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
        PmsWorkOrderBatch currentBatch = pmsWorkOrderBatchService.getOne(new LambdaQueryWrapper<PmsWorkOrderBatch>().eq(PmsWorkOrderBatch::getProduceOrderId, order.getId()).orderByDesc(PmsWorkOrderBatch::getCode).last(MpConstant.STR_LIMIT_1));
        Assert.notNull(currentBatch, "订单" + order.getCode() + "无当前批次");

        //查询订单的工序craft
        List<PmsProduceOrderCraft> craftList = pmsProduceOrderCraftService.list(new LambdaQueryWrapper<PmsProduceOrderCraft>().eq(PmsProduceOrderCraft::getProduceOrderId, order.getId()).select(PmsProduceOrderCraft::getId));
        Assert.notEmpty(craftList, "该订单暂未配置工序信息");
        Set<Long> craftIdSet = craftList.stream().map(PmsProduceOrderCraft::getId).collect(Collectors.toSet());
        //2.查询工位
        PmsWorkOrderCraftStation station = pmsWorkOrderCraftStationService.getOne(new LambdaQueryWrapper<PmsWorkOrderCraftStation>().eq(PmsWorkOrderCraftStation::getExecuteStationId, stationId).in(PmsWorkOrderCraftStation::getProduceOrderCraftId, craftIdSet).last(MpConstant.STR_LIMIT_1));
        Assert.notNull(station, "该工位暂无工序配置");
        //3.查询工序
        PmsProduceOrderCraft craft = pmsProduceOrderCraftService.getById(station.getProduceOrderCraftId());
        Assert.notNull(craft, "工序不存在");
        //4.定义返回数据
        ReportWorkOrderVO vo = new ReportWorkOrderVO();
        BeanUtils.copyProperties(order, vo);

        vo.setUnitId(order.getUnitId());
        Map<Long, String> unitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(Sets.newHashSet(order.getUnitId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
        PmsProduceOrder maxCodeModel = getOne(new LambdaQueryWrapper<PmsProduceOrder>().between(PmsProduceOrder::getCreateTime, min, max).orderByDesc(PmsProduceOrder::getCode).last(MpConstant.STR_LIMIT_1));
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




