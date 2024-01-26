package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.Tuple4DTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.*;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.QtTaskClassCalculate;
import com.steccn.common.vo.R;
import com.steccn.data.dto.PmReportWorkPreloadDTO;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.oa.vo.OaClassVO;
import com.steccn.platform.feign.*;
import com.steccn.platform.vo.*;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.*;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.pms.vo.*;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.feign.RemoteCheckTaskDetailFeignApi;
import com.steccn.qms.util.MysqlDateKeyUtil;
import com.steccn.wms.dto.*;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_report_work(工单报工)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
@Slf4j
public class PmsReportWorkServiceImpl extends ServiceImpl<PmsReportWorkMapper, PmsReportWork> implements PmsReportWorkService {

    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private PmsReportWorkHourService reportWorkHourService;
    @Autowired
    private PmsProduceOrderService produceOrderService;
    @Autowired
    private PmsProduceOrderCraftService produceOrderCraftService;
    @Autowired
    private PmsWorkOrderBatchService workOrderBatchService;
    @Autowired
    private PmsWorkOrderBomService workOrderBomService;
    @Autowired
    private RemoteSpareFeignApi remoteSpareFeignApi;
    @Autowired
    private PmsCompleteDeliveryService completeDeliveryService;
    @Autowired
    private PmsCompleteDeliveryInfoService completeDeliveryInfoService;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private PmsReportWorkConsumeService pmsReportWorkConsumeService;
    @Autowired
    private RemoteWmsInventoryFeignApi remoteWmsInventoryFeignApi;
    @Autowired
    private RemoteSysOfficeCollectFeignApi remoteSysOfficeCollectFeignApi;
    @Autowired
    private RemoteDataPlatformFeignApi remoteDataPlatformFeignApi;
    @Autowired
    private RemoteStationWarehouseFeignApi remoteStationWarehouseFeignApi;
    @Autowired
    private PmsCompleteStorageService pmsCompleteStorageService;
    @Autowired
    private RemoteSpareUnitConvertFeignApi spareUnitConvertFeignApi;
    @Autowired
    private PmsProduceOrderCraftService pmsProduceOrderCraftService;
    @Autowired
    private PmProcessService pmProcessService;
    @Autowired
    private OaInfoService oaInfoService;
    @Autowired
    private RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;
    @Autowired
    private RemoteCheckTaskDetailFeignApi remoteCheckTaskDetailFeignApi;
    @Autowired
    private PmsOfficeManager pmsOfficeManager;
    @Autowired
    private PmsUnitConvertManager pmsUnitConvertManager;

    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(3, 5, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000), Executors.defaultThreadFactory());


    @Override
    public Page<ProcessReportWorkPageVO> pageList(ProcessReportWorkPageDTO dto) {
        //1.将日期拼接最大时间和最小时间
        jointConditionTime(dto);

        //2.自定义sql查询
        Page<ProcessReportWorkPageVO> page = this.baseMapper.processReportPage(new Page(dto.getCurrent(), dto.getSize()), dto);

        //3.将id映射成name
        List<ProcessReportWorkPageVO> records = page.getRecords();
        Set<Long> orderSet = Sets.newHashSet();
        Set<Integer> organIdSet = Sets.newHashSet();
        Set<Long> dictIdSet = Sets.newHashSet();
        records.forEach(ele -> {
            orderSet.add(ele.getProduceOrderId());
            organIdSet.add(ele.getStationId());
            organIdSet.add(ele.getProductLineId());
            dictIdSet.add(ele.getClassesId());
            dictIdSet.add(ele.getClassGroupId());
        });
        Map<Long, BaseSelectVO<Long>> orderUnitMap = produceOrderService.listUnit(orderSet);

        Map<Integer, String> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(organIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        records.forEach(ele -> {
            BaseSelectVO<Long> baseSelectVO = orderUnitMap.get(ele.getProduceOrderId());
            ele.setUnitId(null);
            ele.setUnitName(null);
            if (Objects.nonNull(baseSelectVO)) {
                ele.setUnitId(baseSelectVO.getId());
                ele.setUnitName(baseSelectVO.getName());
            }
            ele.setClassesName(dictMap.get(ele.getClassesId()));
            ele.setClassGroupName(dictMap.get(ele.getClassGroupId()));
            ele.setStationName(sysOfficeMap.get(ele.getStationId()));
            ele.setProductLineName(sysOfficeMap.get(ele.getProductLineId()));
        });
        return page;
    }

    /**
     * 拼接条件时间
     *
     * @param dto
     */
    private void jointConditionTime(ProcessReportWorkPageDTO dto) {
        //创建时间-开始
        if (Objects.nonNull(dto.getCreateDtStart())) {
            dto.setCreateTimeStart(LocalDateTime.of(dto.getCreateDtStart(), LocalTime.MIN));
        }
        //创建时间-结束
        if (Objects.nonNull(dto.getCreateDtEnd())) {
            dto.setCreateTimeEnd(LocalDateTime.of(dto.getCreateDtEnd(), LocalTime.MAX));
        }
        //开始时间-开始
        if (Objects.nonNull(dto.getStartDtStart())) {
            dto.setStartTimeStart(LocalDateTime.of(dto.getStartDtStart(), LocalTime.MIN));
        }
        //开始时间-结束
        if (Objects.nonNull(dto.getStartDtEnd())) {
            dto.setStartTimeEnd(LocalDateTime.of(dto.getStartDtEnd(), LocalTime.MAX));
        }
        //结束时间-开始
        if (Objects.nonNull(dto.getEndDtStart())) {
            dto.setEndTimeStart(LocalDateTime.of(dto.getEndDtStart(), LocalTime.MIN));
        }
        //结束时间-结束
        if (Objects.nonNull(dto.getEndDtEnd())) {
            dto.setEndTimeEnd(LocalDateTime.of(dto.getEndDtEnd(), LocalTime.MAX));
        }
    }

    @Override
    public Boolean submitReportWork(ProcessReportWorkSubmitDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_PMS_REPORT_WORK);
        lock.lock();
        try {
            transactionTemplate.execute(status -> {
                //检验生产订单是否存在
                PmsProduceOrder pmsProduceOrder = produceOrderService.getById(dto.getProduceOrderId());
                Assert.notNull(pmsProduceOrder, "生产订单不存在");
                //校验产线和工位是否存在
                Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(dto.getProductLineId(), dto.getStationId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                Assert.checkBetween(sysOfficeMap.size(), 2, 2, "产线或工位不存在");
                //校验工序是否存在
                PmsProduceOrderCraft pmsProduceOrderCraft = produceOrderCraftService.getById(dto.getProcessId());
                Assert.notNull(pmsProduceOrderCraft, "工序不存在");

                validMaterialConsume(dto, pmsProduceOrderCraft.getProcessId());

                //校验批次是否存在
                PmsWorkOrderBatch pmsWorkOrderBatch = workOrderBatchService.getById(dto.getBatch());
                Assert.notNull(pmsWorkOrderBatch, "批次不存在");
                //校验班次和班组是否存在
                //校验单位是否存在
                Boolean unitBoolean = FeignOptional.of(() -> remoteDictFeignApi.existTypeId(SysDictConstant.TYPE_MEASURE_UNIT, dto.getUnitId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                Assert.isTrue(unitBoolean, "单位不存在");
                //校验是否物料的条数是否正确
                //生成报工记录
                PmsReportWork pmsReportWork = new PmsReportWork();
                BeanUtils.copyProperties(dto, pmsReportWork);
                pmsReportWork.setCode(reportWorkCode(pmsProduceOrder.getCode(), pmsProduceOrderCraft.getSort()));
                pmsReportWork.setProduceOrderNo(pmsProduceOrder.getCode());
                pmsReportWork.setBatchNo(pmsWorkOrderBatch.getCode());
                pmsReportWork.setId(IdUtil.getSnowflake().nextId());
                pmsReportWork.setClassDate(LocalDateUtil.getClassDate(dto.getStartTime(), Boolean.FALSE));
                save(pmsReportWork);
                //保存工时信息
                reportWorkHourService.saveHourList(pmsReportWork.getId(), dto.getHourDTOList());
                //保存能耗信息
                pmsReportWorkConsumeService.saveConsume(pmsReportWork.getId(), dto.getConsumeDTOList());
                //插入库存
                insertDelivery(dto, pmsReportWork);
                if(CollUtil.isNotEmpty(dto.getHourDTOList())){
                    poolExecutor.execute(()->{
                        dto.getHourDTOList().forEach(tmp->{
                            if(Objects.nonNull(tmp.getStartTime())){
                                calculatePullamountTask(tmp.getStartTime());
                            }
                        });
                    });
                }
                return Boolean.TRUE;
            });
        } finally {
            lock.unlock();
        }
        return Boolean.TRUE;
    }

    /**
     * 校验物料消耗
     *
     * @param dto
     * @return
     */
    private void validMaterialConsume(ProcessReportWorkSubmitDTO dto, Long processId) {
        if (CollUtil.isNotEmpty(dto.getMaterialConsumeList())) {
            //根据生产订单id和工序
//            List<PmsWorkOrderBom> pmsWorkOrderBomList = workOrderBomService.list(new LambdaQueryWrapper<PmsWorkOrderBom>().eq(PmsWorkOrderBom::getWorkProduceId, dto.getProduceOrderId()).eq(PmsWorkOrderBom::getProcessId, processId));
//            if (CollUtil.isNotEmpty(pmsWorkOrderBomList)) {
//                Set<Long> materialIdSet = pmsWorkOrderBomList.stream().map(PmsWorkOrderBom::getModuleId).collect(Collectors.toSet());
//                Set<Long> consumeSet = dto.getMaterialConsumeList().stream().map(ProcessReportWorkMaterialListDTO::getModuleId).collect(Collectors.toSet());
//                if (!consumeSet.containsAll(materialIdSet)) {
//                    throw new BusinessException("部分物料库存不足，请补库存");
//                }
            //可用数量必须大于实际数量
            for (ProcessReportWorkMaterialListDTO materialListDTO : dto.getMaterialConsumeList()) {
                if (Objects.nonNull(materialListDTO.getQuantity()) && Objects.nonNull(materialListDTO.getAvailableQuantity()) && materialListDTO.getQuantity().compareTo(materialListDTO.getAvailableQuantity()) == 1) {
                    throw new BusinessException("物料消耗实际数量不可以大于可用数量");
                }
            }
            //去除物料消耗list
            List<ProcessReportWorkMaterialListDTO> materialListDTOList = dto.getMaterialConsumeList().stream().filter(v -> Objects.nonNull(v.getInventoryId()) || Objects.nonNull(v.getId())).collect(Collectors.toList());
            dto.setMaterialConsumeList(materialListDTOList);
        }
    }

    /**
     * 插入库存
     *
     * @param dto           dto
     * @param pmsReportWork 报工信息
     */
    private void insertDelivery(ProcessReportWorkSubmitDTO dto, PmsReportWork pmsReportWork) {
        //插入数据库中pms_complete_delivery，pms_complete_delivery_info
        List<ProcessReportWorkMaterialListDTO> materialConsumeList = dto.getMaterialConsumeList();
        if (CollUtil.isNotEmpty(materialConsumeList)) {
            List<WmsDeliveryReceiptInfoDTO> materialList = Lists.newArrayList();
            PmsCompleteDelivery completeDelivery = new PmsCompleteDelivery();
            completeDelivery.setId(IdUtil.getSnowflake().nextId());
            List<PmsCompleteDeliveryInfo> modelList = new ArrayList<>(materialConsumeList.size());
            BigDecimal sum = BigDecimal.ZERO;
            WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO = new WmsDeliveryReceiptDTO();
            for (ProcessReportWorkMaterialListDTO materialDTO : materialConsumeList) {
                sum = sum.add(materialDTO.getQuantity());
                PmsCompleteDeliveryInfo info = new PmsCompleteDeliveryInfo();
                BeanUtils.copyProperties(materialDTO, info);
                info.setMaterialId(materialDTO.getModuleId());
                info.setMaterialCode(materialDTO.getModuleCode());
                info.setMaterialName(materialDTO.getModuleName());
                info.setCaseNo(materialDTO.getCaseNo());
                info.setBatchNo(materialDTO.getBatchNo());
                info.setPackTime(materialDTO.getPackTime());
                info.setWarehouseId(materialDTO.getWarehouseId());
                info.setShelfId(materialDTO.getShelfId());
                info.setCompleteDeliveryId(completeDelivery.getId());
                //新增可用数量
                info.setAvaQuantity(materialDTO.getAvailableQuantity());
                modelList.add(info);
                wmsDeliveryReceiptDTO.setWarehouseId(materialDTO.getWarehouseId());
                wmsDeliveryReceiptDTO.setWarehouseName(materialDTO.getWarehouseName());
                WmsDeliveryReceiptInfoDTO wmsDeliveryReceiptInfoDTO = new WmsDeliveryReceiptInfoDTO();
                BeanUtils.copyProperties(materialDTO, wmsDeliveryReceiptInfoDTO);
                wmsDeliveryReceiptInfoDTO.setMaterialId(materialDTO.getModuleId());
                materialList.add(wmsDeliveryReceiptInfoDTO);
            }
            SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(SysDictConstant.CODE_0, SysDictConstant.TYPE_OUTBOUND_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            completeDelivery.setCode(maxOutVouchCode());
            completeDelivery.setOutStorageVoucher(completeDelivery.getCode());
            completeDelivery.setDeliveryTime(LocalDateTime.now());
            completeDelivery.setQuantity(sum);
            completeDelivery.setUnitId(materialConsumeList.get(0).getUnitId());
            completeDelivery.setTypeId(sysDictVO.getId());
            completeDelivery.setProduceOrderId(pmsReportWork.getProduceOrderId());
            completeDelivery.setState(PmManagerConstant.InVouchEnum.FINISH.getCode());
            completeDelivery.setProcessId(dto.getProcessId());
            completeDelivery.setReportWorkId(pmsReportWork.getId());
            completeDeliveryService.save(completeDelivery);
            completeDeliveryInfoService.saveBatch(modelList);
            //物料消耗 ，扣除库存
            wmsDeliveryReceiptDTO.setLinkedId(pmsReportWork.getId());
            wmsDeliveryReceiptDTO.setType(sysDictVO.getId());
            wmsDeliveryReceiptDTO.setList(materialList);
            wmsDeliveryReceiptDTO.setAffirmFlag(PmManagerConstant.NUM_1);
            wmsDeliveryReceiptDTO.setCreateBy(SecurityUtils.getUser().getUserId());
            wmsDeliveryReceiptDTO.setCreateName(SecurityUtils.getUser().getName());
            R<Boolean> booleanR = null;
            try {
                booleanR = remoteWmsInventoryFeignApi.generateOutStorageVoucher(wmsDeliveryReceiptDTO);
                if (!booleanR.getData()) {
                    log.info("生成单据失败");
                }
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
            Boolean aBoolean = FeignOptional.of(booleanR).get();
            if (Objects.isNull(aBoolean) || aBoolean.equals(Boolean.FALSE)) {
                throw new BusinessException("库存变化，请重新报工");
            }
        }
    }

    /**
     * 最大出库code
     *
     * @return
     */
    private String maxOutVouchCode() {
        String prefix = PmsCodeUtil.PREFIX_SO + LocalDate.now().toString().replaceAll(PmsCodeUtil.SHORT_LINE, Strings.EMPTY) + PmsCodeUtil.SHORT_LINE;
        PmsCompleteDelivery one = completeDeliveryService.getOne(new LambdaQueryWrapper<PmsCompleteDelivery>().likeRight(PmsCompleteDelivery::getCode, prefix).orderByDesc(PmsCompleteDelivery::getCode).last(MpConstant.STR_LIMIT_1));
        long currentMax = PmsCodeUtil.DEFAULT_PADDING;
        if (Objects.nonNull(one)) {
            String substring = one.getCode().substring(one.getCode().length() - PmsCodeUtil.NUM_BIT_4);
            try {
                currentMax = Long.parseLong(substring);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return PmsCodeUtil.getNextCode(prefix, PmsCodeUtil.NUM_BIT_4, currentMax);
    }

    @Override
    public Page<ProcessReportWorkPageVO> recordPage(ProcessReportWorkRecordDTO dto) {
        ProcessReportWorkPageDTO pageDTO = new ProcessReportWorkPageDTO();
        BeanUtils.copyProperties(dto, pageDTO);
        return pageList(pageDTO);
    }

    @Override
    public List<ProcessReportWorkMaterialListDTO> materialConsume(ProcessReportWorkMaterialConsumeDTO dto) {
        //检验生产订单是否存在
        PmsProduceOrder pmsProduceOrder = produceOrderService.getById(dto.getProduceOrderId());
        Assert.notNull(pmsProduceOrder, "生产订单不存在");

        //校验工序是否存在
        PmsProduceOrderCraft pmsProduceOrderCraft = produceOrderCraftService.getById(dto.getProcessId());
        Assert.notNull(pmsProduceOrderCraft, "工序不存在");

        //工位是否存在
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(dto.getStationId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.checkBetween(sysOfficeMap.size(), 1, 1, "工位不存在");

        //根据生产订单id和工序
        List<PmsWorkOrderBom> pmsWorkOrderBomList = workOrderBomService.list(new LambdaQueryWrapper<PmsWorkOrderBom>().eq(PmsWorkOrderBom::getWorkProduceId, pmsProduceOrder.getId()).eq(PmsWorkOrderBom::getProcessId, pmsProduceOrderCraft.getProcessId()));
        //定义返回集合
        List<ProcessReportWorkMaterialListDTO> resList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(pmsWorkOrderBomList)) {
            EqpSpareVO defaultEqpSpareVO = new EqpSpareVO();
            Set<Long> unitIdList = Sets.newHashSet();
            List<Long> idList = Lists.newArrayList();
            Map<Long, Long> materialUnitMap = Maps.newHashMap();
            for (PmsWorkOrderBom pmsWorkOrderBom : pmsWorkOrderBomList) {
                unitIdList.add(pmsWorkOrderBom.getUnitId());
                idList.add(pmsWorkOrderBom.getModuleId());
                materialUnitMap.put(pmsWorkOrderBom.getModuleId(), pmsWorkOrderBom.getUnitId());
            }
            unitIdList.add(pmsProduceOrder.getUnitId());
            Map<Long, String> sysDictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(unitIdList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, EqpSpareVO> eqpSpareMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(idList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

            List<MaterialStorageInfoDTO> messMaterialStorageList = Lists.newArrayList();
            Map<Long, Long> unitMaterialMap = Maps.newHashMap();
            pmsWorkOrderBomList.forEach(ele -> {
                BigDecimal divide = ele.getQuantity().multiply(dto.getProcessQuantity()).divide(pmsProduceOrder.getQuantity(), 3, RoundingMode.HALF_UP);
                MaterialStorageInfoDTO storageDTO = new MaterialStorageInfoDTO();
                storageDTO.setMaterialId(ele.getModuleId());
                storageDTO.setQuantity(divide);
                messMaterialStorageList.add(storageDTO);
                unitMaterialMap.put(ele.getModuleId(), ele.getUnitId());
            });
            List<WmsStockInventoryDTO> messList = FeignOptional.of(() -> remoteWmsInventoryFeignApi.materialConsume(messMaterialStorageList), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
            if (CollUtil.isNotEmpty(messList)) {
                Set<Integer> storageSet = Sets.newHashSet();
                for (WmsStockInventoryDTO messDto : messList) {
                    storageSet.add(messDto.getWarehouseId());
                    storageSet.add(messDto.getShelfId());
                }
                Map<Integer, SysOfficeVO> sysOfficeCodeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(storageSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                SysOfficeVO defaultSysOfficeVO = new SysOfficeVO();
                for (WmsStockInventoryDTO messDto : messList) {
                    ProcessReportWorkMaterialListDTO resDTO = new ProcessReportWorkMaterialListDTO();
                    BeanUtils.copyProperties(messDto, resDTO);
                    resDTO.setProduceOrderId(pmsProduceOrder.getId());
                    resDTO.setProcessId(pmsProduceOrderCraft.getId());
                    resDTO.setProcessName(pmsProduceOrderCraft.getName());
                    resDTO.setUnitId(unitMaterialMap.get(messDto.getMaterialId()));
                    resDTO.setUnitName(sysDictMap.get(resDTO.getUnitId()));
                    resDTO.setModuleId(messDto.getMaterialId());
                    EqpSpareVO eqpSpareVO = eqpSpareMap.getOrDefault(messDto.getMaterialId(), defaultEqpSpareVO);
                    resDTO.setModuleName(eqpSpareVO.getName());
                    resDTO.setModuleCode(eqpSpareVO.getCode());
                    resDTO.setQuantity(BigDecimalUtil.setScale(messDto.getQuantity(), 3));
                    resDTO.setAvailableQuantity(messDto.getAvailableQuantity());
                    SysOfficeVO warehouse = sysOfficeCodeMap.getOrDefault(messDto.getWarehouseId(), defaultSysOfficeVO);
                    resDTO.setWarehouseId(warehouse.getId());
                    resDTO.setWarehouseName(warehouse.getOfficeName());
                    SysOfficeVO shelf = sysOfficeCodeMap.getOrDefault(messDto.getShelfId(), defaultSysOfficeVO);
                    resDTO.setShelfId(shelf.getId());
                    resDTO.setShelfName(shelf.getOfficeName());
                    resList.add(resDTO);
                }
            }
        }
        return resList;
    }

    @Override
    public Page<PmsReportWorkVO> reportWorkPageList(PmsReportWorkPageDTO dto) {
        //1.定义查询条件
        LambdaQueryWrapper<PmsReportWork> eq = new LambdaQueryWrapper<PmsReportWork>().eq(Objects.nonNull(dto.getProcessId()), PmsReportWork::getProcessId, dto.getProcessId()).eq(Objects.nonNull(dto.getProduceId()), PmsReportWork::getProcessId, dto.getProduceId()).eq(Objects.nonNull(dto.getBatchNo()), PmsReportWork::getBatchNo, dto.getBatchNo()).eq(Objects.nonNull(dto.getStationId()), PmsReportWork::getStationId, dto.getStationId()).ge(Objects.nonNull(dto.getStartTime()), PmsReportWork::getCreateTime, dto.getStartTime()).le(Objects.nonNull(dto.getEndTime()), PmsReportWork::getCreateTime, dto.getStartTime());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage resPage = page(page, eq);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        //5.拷贝返回的信息
        List<PmsReportWorkVO> res = BeanUtil.copyToList(resPage.getRecords(), PmsReportWorkVO.class);
        //6.处理数据
        Set<Long> dictIds = Sets.newHashSet();
        Set<Integer> stationIds = Sets.newHashSet();
        res.forEach(pmsReportWorkVO -> {
            dictIds.add(pmsReportWorkVO.getUnitId());
            stationIds.add(pmsReportWorkVO.getStationId());
        });
        //查询字典数据
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //查询机构数据
        Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(stationIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        res.forEach(pmsReportWorkVO -> {
            pmsReportWorkVO.setStation(officeMap.get(pmsReportWorkVO.getStationId()));
            pmsReportWorkVO.setUnit(dictMap.get(pmsReportWorkVO.getUnitId()));
        });
        //todo 班次数据待定(OA获取)
        return page.setRecords(res);
    }

    @Override
    public Map<Long, PmsReportWork> dateProductNum(Integer productLineId, LocalDate date) {
        Map<Long, PmsReportWork> resMap = Maps.newHashMap();
        if (Objects.nonNull(productLineId) && Objects.nonNull(date)) {
            List<PmsReportWork> resList = this.baseMapper.dateProductNum(productLineId, date);
            if (CollUtil.isNotEmpty(resList)) {
                resList.forEach(ele -> {
                    resMap.put(ele.getClassesId(), ele);
                });
            }
        }
        return resMap;
    }

    @Override
    public List<PmsReportWorkVO> listByClassTime(LocalDateTime startTime, LocalDateTime endTime) {
        List<PmsReportWorkVO> pmsReportWorkVOList = new ArrayList<>();

        List<PmsReportWork> list = list(new LambdaQueryWrapper<PmsReportWork>().ge(PmsReportWork::getStartTime, startTime).lt(PmsReportWork::getEndTime, endTime));
        Set<Integer> productLineIdSet = list.stream().map(PmsReportWork::getProductLineId).collect(Collectors.toSet());
        Map<Integer, SysOfficeVO> parentOfficeMap = remoteOfficeFeign.getParentOfficeMap(new ArrayList<>(productLineIdSet)).getData();
        Set<Long> orderIdSet = list.stream().map(PmsReportWork::getProduceOrderId).collect(Collectors.toSet());
        if (CollUtil.isNotEmpty(orderIdSet)) {
            List<PmsProduceOrder> pmsProduceOrderList = produceOrderService.list(new LambdaQueryWrapper<PmsProduceOrder>().in(PmsProduceOrder::getId, orderIdSet));
            for (PmsReportWork pmsReportWork : list) {
                Optional<PmsProduceOrder> optional = pmsProduceOrderList.stream().filter(pmsProduceOrder -> pmsProduceOrder.getId().equals(pmsReportWork.getProduceOrderId())).findAny();
                if (optional.isPresent()) {
                    PmsReportWorkVO pmsReportWorkVO = new PmsReportWorkVO();
                    BeanUtils.copyProperties(pmsReportWork, pmsReportWorkVO);
                    pmsReportWorkVO.setWorkshopName(parentOfficeMap.get(pmsReportWork.getProductLineId()).getParentName());
                    pmsReportWorkVO.setMaterialId(optional.get().getMaterialId());
                    pmsReportWorkVOList.add(pmsReportWorkVO);
                }
            }
        }
        return pmsReportWorkVOList;
    }

    @Override
    public Map<Long, Long> reportWorkClassGroup(Set<Long> reportWorkIdSet) {
        Map<Long, Long> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(reportWorkIdSet)) {
            List<PmsReportWork> pmsReportWorks = listByIds(reportWorkIdSet);
            if (CollUtil.isNotEmpty(pmsReportWorks)) {
                pmsReportWorks.forEach(ele -> resMap.put(ele.getId(), ele.getClassGroupId()));
            }
        }
        return resMap;
    }

    @Override
    public BigDecimal preload(Integer stationId, LocalDateTime startTime, LocalDateTime endTime) {
        BigDecimal res = null;
        //根据工位id查询
        SysOfficeCollectVO sysOfficeCollectVO = FeignOptional.of(() -> remoteSysOfficeCollectFeignApi.outCollect(stationId), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //如果采集点不为空
        if (Objects.nonNull(sysOfficeCollectVO)) {
            PmReportWorkPreloadDTO reportWorkPreloadDTO = new PmReportWorkPreloadDTO();
            reportWorkPreloadDTO.setCode(sysOfficeCollectVO.getCode());
            reportWorkPreloadDTO.setStartTime(startTime);
            reportWorkPreloadDTO.setEndTime(endTime);
            res = FeignOptional.of(() -> remoteDataPlatformFeignApi.preload(reportWorkPreloadDTO), FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME).get();
        }
        return res;
    }

    @Override
    public List<ProcessReportWorkMaterialListDTO> materialLineWarehouse(Integer id) {
        //获取工位线边仓数据
        Assert.isTrue(Objects.nonNull(id), "工位数据不能为空");
        Set<Integer> set = Sets.newHashSet(id);
        Map<Integer, List<SysStationWarehouseVO>> integerListMap = FeignOptional.of(() -> remoteStationWarehouseFeignApi.listByStationIds(set), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //根据仓库获取物料数据
        Set<Integer> idSet = Sets.newHashSet();
        integerListMap.forEach((key, value) -> {
            value.forEach(v -> idSet.add(v.getWarehouseId()));
        });
        List<ProcessReportWorkMaterialListDTO> list = Lists.newArrayList();
        Map<Integer, List<WmsStockInventoryDTO>> map = FeignOptional.of(() -> remoteWmsInventoryFeignApi.materialByWarehouse(idSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if (CollUtil.isNotEmpty(map)) {
            Set<Long> ids = Sets.newHashSet();
            map.forEach((key, value) -> {
                value.forEach(dto -> {
                    ProcessReportWorkMaterialListDTO material = new ProcessReportWorkMaterialListDTO();
                    BeanUtil.copyProperties(dto, material);
                    material.setModuleId(dto.getMaterialId());
                    material.setModuleName(dto.getMaterialName());
                    material.setModuleCode(dto.getMaterialCode());
                    material.setUnitId(dto.getUnit());
                    ids.add(dto.getUnit());
                    list.add(material);
                });
            });
            //获取单位名称
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(ids), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            list.forEach(d -> d.setUnitName(dictMap.get(d.getUnitId())));
        }
        return list;
    }

    @Override
    public ProcessReportWorkSubmitDTO info(Long id) {
        PmsReportWork model = getById(id);
        Assert.notNull(model, "报工信息不存在");
        PmsProduceOrder pmsProduceOrder = produceOrderService.getById(model.getProduceOrderId());
        Assert.notNull(model, "生产订单信息不存在");
        ProcessReportWorkSubmitDTO resVo = BeanUtil.copyProperties(model, ProcessReportWorkSubmitDTO.class);
        resVo.setProduceOrderNo(pmsProduceOrder.getCode());
        resVo.setMaterialId(pmsProduceOrder.getMaterialId());
        resVo.setMaterialCode(pmsProduceOrder.getMaterialCode());
        resVo.setMaterialName(pmsProduceOrder.getMaterialName());
        //赋值processName,unitName,batch
        PmsProduceOrderCraft processModel = produceOrderCraftService.getById(model.getProcessId());
        Assert.notNull(processModel, "工序不存在");
        resVo.setProcessName(processModel.getName());
        PmsWorkOrderBatch workOrderBatch = workOrderBatchService.getByCode(resVo.getBatchNo());
        Assert.notNull(workOrderBatch, "生产订单批次信息不存在");
        resVo.setBatch(workOrderBatch.getId());
        //赋值数据字典
        Set<Long> dictIdSet = Sets.newHashSet(resVo.getUnitId(), resVo.getClassesId(), resVo.getClassGroupId());
        Map<Long, String> dictIdMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        resVo.setClassGroupName(dictIdMap.get(resVo.getClassGroupId()));
        resVo.setClassesName(dictIdMap.get(resVo.getClassesId()));
        resVo.setUnitName(dictIdMap.get(resVo.getUnitId()));
        //赋值组织机构
        Set<Integer> organIdSet = Sets.newHashSet(resVo.getProductLineId(), resVo.getStationId());
        Map<Integer, String> organIdMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(organIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        resVo.setProductLineName(organIdMap.get(resVo.getProductLineId()));
        resVo.setStationName(organIdMap.get(resVo.getStationId()));
        //获取工时信息
        List<ProcessReportWorkHourDTO> hourDTOList = reportWorkHourService.getReportWorkHour(id);
        //获取物料消息信息
        List<ProcessReportWorkMaterialListDTO> materialConsumeList = completeDeliveryService.getMaterialConsume(id);
        //获取能耗信息
        List<ProcessReportWorkConsumeDTO> consumeDTOList = pmsReportWorkConsumeService.getConsumeList(id);
        resVo.setHourDTOList(hourDTOList);
        resVo.setMaterialConsumeList(materialConsumeList);
        resVo.setConsumeDTOList(consumeDTOList);
        return resVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(ProcessReportWorkSubmitDTO dto) {
        //更新报工主表信息
        PmsReportWork model = getById(dto);
        Assert.notNull(model, "编辑的报工记录不存在");
        getReportWork(dto, model);
        //设置报工的班次日期
        model.setClassDate(LocalDateUtil.getClassDate(dto.getStartTime(), Boolean.FALSE));
        updateById(model);
        //更新报工工时信息
        List<PmsReportWorkHour> reportWorkHourList = reportWorkHourService.list(new LambdaQueryWrapper<PmsReportWorkHour>().eq(PmsReportWorkHour::getReportWorkId, dto.getId()));
        reportWorkHourService.updateHour(dto.getId(), dto.getHourDTOList());
        //更新报工能耗信息
        pmsReportWorkConsumeService.updateConsume(dto.getId(), dto.getConsumeDTOList());
        //更新物料消耗信息
        updateMaterialConsume(dto, dto.getMaterialConsumeList());
        //如果有停机信息则重新计算拉引量
        if(CollUtil.isNotEmpty(dto.getHourDTOList())){
            poolExecutor.execute(()->{
                dto.getHourDTOList().forEach(tmp->{
                    if(Objects.nonNull(tmp.getStartTime())){
                        calculatePullamountTask(tmp.getStartTime());
                    }
                });
            });
        }
        if(CollUtil.isNotEmpty(reportWorkHourList)&&CollUtil.isEmpty(dto.getHourDTOList())){
            poolExecutor.execute(()->{
                reportWorkHourList.forEach(tmp->{
                    if(Objects.nonNull(tmp.getStartTime())){
                        calculatePullamountTask(tmp.getStartTime());
                    }
                });
            });
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long id) {
        PmsReportWork pmsReportWork = getById(id);
        Assert.notNull(pmsReportWork, "报工信息不存在");
        //删除报工信息
        removeById(id);
        //删除工时信息
        List<PmsReportWorkHour> reportWorkHourList = reportWorkHourService.list(new LambdaQueryWrapper<PmsReportWorkHour>().eq(PmsReportWorkHour::getReportWorkId, id));
        reportWorkHourService.remove(new LambdaQueryWrapper<PmsReportWorkHour>().eq(PmsReportWorkHour::getReportWorkId, id));
        //删除能耗信息
        pmsReportWorkConsumeService.remove(new LambdaQueryWrapper<PmsReportWorkConsume>().eq(PmsReportWorkConsume::getReportWorkId, id));
        //根据物料消耗信息生成
        PmsCompleteDelivery delivery = completeDeliveryService.getOne(new LambdaQueryWrapper<PmsCompleteDelivery>().eq(PmsCompleteDelivery::getReportWorkId, id).last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(delivery)) {
            List<PmsCompleteDeliveryInfo> infoList = completeDeliveryInfoService.list(new LambdaQueryWrapper<PmsCompleteDeliveryInfo>().eq(PmsCompleteDeliveryInfo::getCompleteDeliveryId, delivery.getId()));
            //生成对应的入库单【冲销报工入库】
            ProcessReportWorkSubmitDTO dto = new ProcessReportWorkSubmitDTO();
            BeanUtils.copyProperties(pmsReportWork, dto);
            pmsCompleteStorageService.generateByDelivery(dto, infoList);
        }
        //删除时重新计算拉引量
        if(CollUtil.isNotEmpty(reportWorkHourList)){
            poolExecutor.execute(()->{
                reportWorkHourList.forEach(tmp->{
                    calculatePullamountTask(tmp.getStartTime());
                });
            });
        }
        return Boolean.TRUE;
    }

    @Override
    public List<EpMeterReadRowDTO> sjgOrderRw(List<EpMeterReadRowDTO> sjgDataList) {
        //深加工产量 = 将满足下列条件的报工记录上报的片数折算成面积，单位为㎡
        //报工记录关联的生产订单为当前行的生产订单
        //报工记录的时段在当前行的班次时段内
        if (CollUtil.isNotEmpty(sjgDataList)) {
            //获取生产订单主键集合
            Set<Long> orderIdSet = sjgDataList.stream().map(EpMeterReadRowDTO::getProduceOrderId).collect(Collectors.toSet());
            List<PmsReportWork> dbList = list(new LambdaQueryWrapper<PmsReportWork>().in(PmsReportWork::getProduceOrderId, orderIdSet));
            if (CollUtil.isNotEmpty(dbList)) {
                //获取生产订单集合
                List<PmsProduceOrder> orderList = produceOrderService.listByIds(orderIdSet);
                //定义生产订单的所有物料，用于物料的单位转换
                Set<Long> materialIdSet = Sets.newHashSet();
                //key-生产订单主键 value-物料主键
                Map<Long, Long> orderMaterialMap = Maps.newHashMap();
                //获取工位id
                Set<Integer> stationIdSet = dbList.stream().map(PmsReportWork::getStationId).collect(Collectors.toSet());
                Map<Integer, SysOfficeVO> stationIdMap = FeignOptional.of(() -> remoteOfficeFeign.listById(stationIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                //过滤工位
                dbList = dbList.stream().filter(ele -> {
                    SysOfficeVO stationOffice = stationIdMap.getOrDefault(ele.getStationId(), new SysOfficeVO());
                    if (StrUtil.isNotEmpty(stationOffice.getOfficeName()) && stationOffice.getOfficeName().contains("磨边")) {
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }).collect(Collectors.toList());
                Map<Long, List<PmsReportWork>> orderMap = dbList.stream().collect(Collectors.groupingBy(PmsReportWork::getProduceOrderId));
                orderList.forEach(order -> {
                    materialIdSet.add(order.getMaterialId());
                    orderMaterialMap.put(order.getId(), order.getMaterialId());
                });
                //获取平方米单位数据字典
                SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(SysDictConstant.CODE_PFM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                Map<Long, List<SysSpareUnitConversionVO>> convertMap = FeignOptional.of(() -> spareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                sjgDataList.forEach(ele -> {
                    //获取订单报工数据集合
                    List<PmsReportWork> orderRwList = orderMap.getOrDefault(ele.getProduceOrderId(), Lists.newArrayList());
                    //定义班次开始时间，班次结束时间
                    LocalDateTime classStart;
                    LocalDateTime classEnd;
                    if (ele.getClassId() == 0) {
                        //早班
                        classStart = LocalDateUtil.getMorning(ele.getStart());
                        classEnd = classStart.plusHours(12);
                    } else {
                        //晚班
                        classStart = LocalDateUtil.getNight(ele.getStart());
                        classEnd = classStart.plusHours(12);
                    }
                    LocalDateTime finalClassStart = classStart;
                    LocalDateTime finalClassEnd = classEnd;
                    //获取满足条件的报工数据
                    List<PmsReportWork> passRwList = orderRwList.stream().filter(v -> LocalDateUtil.isRange(v.getStartTime(), finalClassStart, finalClassEnd, true, false)).collect(Collectors.toList());
                    Set<Long> rwIdSet = Sets.newHashSet();
                    //定义加工的片数
                    BigDecimal yieldPs = BigDecimal.ZERO;
                    for (PmsReportWork pmsReportWork : passRwList) {
                        rwIdSet.add(pmsReportWork.getId());
                        //加工数累计
                        yieldPs = BigDecimalUtil.add(yieldPs, pmsReportWork.getProcessQuantity());
                    }
                    //将加工的片数转换成面积
                    //根据生产订单获取物料
                    Long materialId = orderMaterialMap.get(ele.getProduceOrderId());
                    //根据物料获取单位转换
                    List<SysSpareUnitConversionVO> convertList = convertMap.getOrDefault(materialId, Lists.newArrayList());


                    if (CollUtil.isNotEmpty(convertList) && Objects.nonNull(sysDictVO)) {
                        Optional<SysSpareUnitConversionVO> first = convertList.stream().filter(v -> v.getConversionUnitId().equals(sysDictVO.getId())).findFirst();
                        if (first.isPresent()) {
                            SysSpareUnitConversionVO unitConversionVO = first.get();
                            yieldPs = BigDecimalUtil.multiply(yieldPs, unitConversionVO.getConversionQuantity());
                            yieldPs = BigDecimalUtil.divide(yieldPs, unitConversionVO.getQuantity());
                        }
                    }
                    ele.setYield(yieldPs);
                    ele.setReportWorkIdSet(rwIdSet);
                });
            }
        }
        return sjgDataList;
    }

    @Override
    public Map<String, BigDecimal> classDateProductNum(Set<Integer> lineSet, LocalDate startLd, LocalDate endLd) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        Map<Integer, String> lineIdMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(lineSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<Integer> jpLineIdList = Lists.newArrayList();
        List<Integer> sjgLineIdList = Lists.newArrayList();
        lineIdMap.forEach((k, v) -> {
            if (v.contains("基片")) {
                jpLineIdList.add(k);
            } else {
                sjgLineIdList.add(k);
            }
        });
        Map<String, BigDecimal> valueMap1 = dateLinePmUnit(jpLineIdList, startLd, endLd, 0, PmManagerConstant.MEASURE_UNIT_TON);
        Map<String, BigDecimal> valueMap2 = dateLinePmUnit(sjgLineIdList, startLd, endLd, 1, PmManagerConstant.MEASURE_UNIT_SM);
        resMap.putAll(valueMap1);
        resMap.putAll(valueMap2);
        return resMap;
    }

    @Override
    public Map<String, Long> workshopClassGroupMap(Set<LocalDate> dateSet, Map<Integer, Set<Integer>> workshopStationMap) {
        Map<String, Long> resMap = Maps.newHashMap();
        List<Integer> stationIdList = workshopStationMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(stationIdList) && CollUtil.isNotEmpty(dateSet)) {
            List<PmsReportWork> dbList = list(new QueryWrapper<PmsReportWork>().select(" class_date , classes_id, station_id , class_group_id , count(class_group_id) as create_by")
                    .lambda()
                    .in(PmsReportWork::getClassDate, dateSet)
                    .in(PmsReportWork::getStationId, stationIdList)
                    .groupBy(Lists.newArrayList(PmsReportWork::getStationId, PmsReportWork::getClassDate, PmsReportWork::getClassesId, PmsReportWork::getClassGroupId)));

            if (CollUtil.isNotEmpty(dbList)) {
                //获取早班和晚班
                List<SysDictVO> sysDictVOList = FeignOptional.of(() -> remoteDictFeignApi.listByType(PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                for (LocalDate date : dateSet) {
                    workshopStationMap.forEach((k, v) -> {
                        //遍历早班和晚班
                        for (SysDictVO classIdVo : sysDictVOList) {
                            Map<Long, List<PmsReportWork>> groupMap = dbList.stream().filter(ele -> v.contains(ele.getStationId()) && classIdVo.getId().equals(ele.getClassesId()) && date.isEqual(ele.getClassDate())).collect(Collectors.groupingBy(a -> a.getClassGroupId()));
                            Long groupId = null;
                            Long groupSum = 0L;
                            for (Map.Entry<Long, List<PmsReportWork>> entry : groupMap.entrySet()) {
                                long sum = entry.getValue().stream().mapToLong(PmsReportWork::getCreateBy).sum();
                                if (Objects.isNull(groupId) || groupSum < sum) {
                                    groupId = entry.getKey();
                                    groupSum = sum;
                                }
                            }
                            if (Objects.nonNull(groupId)) {
                                resMap.put(date + "_" + classIdVo.getCode() + "_" + k, groupId);
                            }
                        }
                    });
                }
            }
        }
        return resMap;
    }

    @Override
    public List<PmsReportWorkVO> listBylineIds(List<Integer> lineIds, LocalDate date) {
        LocalDateTime start = LocalDateUtil.getMorning(date);
        LocalDateTime end = LocalDateUtil.getMorning(date.plusDays(1));
        List<PmsReportWork> list = list(new LambdaQueryWrapper<PmsReportWork>().in(PmsReportWork::getProductLineId, lineIds).ge(PmsReportWork::getStartTime, start)
                .le(PmsReportWork::getEndTime, end));
        List<PmsReportWorkVO> resList = new ArrayList<>();
        if (CollUtil.isNotEmpty(list)) {

            //获取班次的id
            Set<String> classNames = Sets.newHashSet(PmManagerConstant.FORE_SHIFT, PmManagerConstant.NIGHT_SHIFT);
            Map<String, Long> classNameMap = FeignOptional.of(() -> remoteDictFeignApi.listByNameCh(classNames), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //早班
            Long foreShiftId = classNameMap.get(PmManagerConstant.FORE_SHIFT);
            List<Long> collect = new ArrayList<>(list.stream().map(PmsReportWork::getProduceOrderId).collect(Collectors.toSet()));
            Set<Long> workIds = list.stream().map(PmsReportWork::getId).collect(Collectors.toSet());
            //获取报工工时信息
            List<PmsReportWorkHour> pmsReportWorkHours = reportWorkHourService.list(new LambdaQueryWrapper<PmsReportWorkHour>().in(PmsReportWorkHour::getReportWorkId, workIds));
            Map<Long, List<PmsReportWorkHour>> reportWorkHoursMap = pmsReportWorkHours.stream().collect(Collectors.groupingBy(PmsReportWorkHour::getReportWorkId));
            Map<Long, PmsProduceOrder> orderMap = produceOrderService.orderList(collect);
            for (PmsReportWork pmsReportWork : list) {
                PmsReportWorkVO pmsReportWorkVO = new PmsReportWorkVO();
                BeanUtil.copyProperties(pmsReportWork, pmsReportWorkVO);
                pmsReportWorkVO.setMaterialId(orderMap.get(pmsReportWork.getProduceOrderId()).getMaterialId());
                pmsReportWorkVO.setProduceOrder(orderMap.get(pmsReportWork.getProduceOrderId()).getCode());
                pmsReportWorkVO.setProduceOrderId(orderMap.get(pmsReportWork.getProduceOrderId()).getId());
                pmsReportWorkVO.setClassGroupId(pmsReportWork.getClassGroupId());
                pmsReportWorkVO.setClasses(PmManagerConstant.NIGHT_SHIFT);
                if (foreShiftId.equals(pmsReportWork.getClassesId())) {
                    pmsReportWorkVO.setClasses(PmManagerConstant.FORE_SHIFT);
                }
                resList.add(pmsReportWorkVO);
                List<PmsReportWorkHour> pmsReportWorkHours1 = reportWorkHoursMap.get(pmsReportWork.getId());
                if (CollUtil.isNotEmpty(pmsReportWorkHours1)) {
                    pmsReportWorkVO.setList(BeanUtil.copyToList(pmsReportWorkHours1, PmsReportWorkHourVO.class));
                }
            }
        }
        return resList;
    }

    @Override
    public List<PmsReportWorkVO> monthlyData(LocalDate date, List<Integer> lineIds) {

        //当前日期的一号早八点
        LocalDateTime start = LocalDateUtil.getMorning(date.withDayOfMonth(1));
        //当前日期的晚班结束时间
        LocalDateTime end = LocalDateUtil.getMorning(date).plusDays(1);
        List<PmsReportWorkVO> list = baseMapper.listReportWorkInfo(lineIds, start, end);
        return list;
    }

    @Override
    public Map<Long, Long> mainMaterialByReportWork(Set<Long> reportIds) {
        Map<Long, Long> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(reportIds)) {
            List<PmsReportWork> list = list(new LambdaQueryWrapper<PmsReportWork>().in(PmsReportWork::getId, reportIds));
            if (CollUtil.isNotEmpty(list)) {
                Set<Long> orderIds = list.stream().map(PmsReportWork::getProduceOrderId).collect(Collectors.toSet());
                Map<Long, Long> orderMap = produceOrderService.maiMaterialAByOrderId(orderIds);
                for (PmsReportWork pmsReportWork : list) {
                    resMap.put(pmsReportWork.getId(), orderMap.get(pmsReportWork.getProduceOrderId()));
                }
            }
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> getUseCount(List<Integer> lineList, LocalDateTime start, LocalDateTime end) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        //获取(start,end)时间范围内的报工信息
        List<PmsUseCountDTO> dbList = baseMapper.getUseCount(lineList, start, end);
        if (CollUtil.isNotEmpty(dbList)) {
            //获取磨边工位
            Set<Integer> stationIds = getEdgStation(lineList);
            dbList.forEach(ele -> {
                if (stationIds.contains(ele.getStationId())) {
                    resMap.put(ele.getMonthStr() + "_" + ele.getProductLineId(), ele.getQuantity());
                }
            });
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> getUseCountUnit(List<Integer> lineList, LocalDateTime start, LocalDateTime end, String unit) {
        //单位转换
        SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDict1, "换算单位1不存在");
        Assert.notNull(sysDict2, "换算单位2不存在");

        Map<String, BigDecimal> resMap = Maps.newHashMap();
        //获取(start,end)时间范围内的报工信息
        List<PmsUseCountDTO> dbList = baseMapper.getUseCountClassDate(lineList, start, end);
        if (CollUtil.isNotEmpty(dbList)) {
            Map<Long, List<PmsUseCountDTO>> materialMap = dbList.stream().collect(Collectors.groupingBy(PmsUseCountDTO::getMaterialId));
            //根据单位查询转换关系
            Map<Long, List<SysSpareUnitConversionVO>> convertMap = FeignOptional.of(() -> spareUnitConvertFeignApi.convert(materialMap.keySet()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取磨边工位
            Set<Integer> stationIds = getEdgStation(lineList);
            materialMap.forEach((k,v) ->{
                List<SysSpareUnitConversionVO> relationList = convertMap.get(k);
                v.forEach(ele ->{
                    if (stationIds.contains(ele.getStationId())) {
                        BigDecimal quantity = ele.getQuantity();
                        if(!sysDict1.equals(sysDict2)){
                            quantity = UnitConvertUtil.convertMaterial(relationList,sysDict1.getId(),sysDict2.getId(),quantity,3);
                        }
                        MapUtil.add(resMap, ele.getClassDate() + "_" + ele.getProductLineId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                    }
                });
            });
        }
        return resMap;
    }


    @Override
    public Map<Integer, BigDecimal> getDateRangeUseCountUnit(List<Integer> lineList, LocalDate start, LocalDate end, String unit) {
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.getDateRangeUseCount(lineList, startLdt, endLdt);
        //单位转换
        SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDict1, "换算单位1不存在");
        Assert.notNull(sysDict2, "换算单位2不存在");

        Map<Integer, BigDecimal> resMap = Maps.newHashMap();
        //获取(start,end)时间范围内的报工信息
        if (CollUtil.isNotEmpty(dbList)) {
            Map<Long, List<PmsUseCountDTO>> materialMap = dbList.stream().collect(Collectors.groupingBy(PmsUseCountDTO::getMaterialId));
            //根据单位查询转换关系
            Map<Long, List<SysSpareUnitConversionVO>> convertMap = FeignOptional.of(() -> spareUnitConvertFeignApi.convert(materialMap.keySet()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取磨边工位
            Set<Integer> stationIds = getEdgStation(lineList);
            materialMap.forEach((k,v) ->{
                List<SysSpareUnitConversionVO> relationList = convertMap.get(k);
                v.forEach(ele ->{
                    if (stationIds.contains(ele.getStationId())) {
                        BigDecimal quantity = ele.getQuantity();
                        if(!sysDict1.equals(sysDict2)){
                            quantity = UnitConvertUtil.convertMaterial(relationList,sysDict1.getId(),sysDict2.getId(),quantity,3);
                        }
                        MapUtil.add(resMap, ele.getProductLineId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                    }
                });
            });
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> useCountPerOrder(List<Integer> lineList, LocalDateTime start, LocalDateTime end) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        //获取(start,end)时间范围内的报工信息
        List<PmsUseCountDTO> dbList = baseMapper.useCountPerOrder(lineList, start, end);
        if (CollUtil.isNotEmpty(dbList)) {
            Set<Long> dictIdSet = dbList.stream().map(PmsUseCountDTO::getClassId).collect(Collectors.toSet());
            Map<Long, SysDictVO> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listById(Lists.newArrayList(dictIdSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Set<Integer> stationIds = getEdgStation(lineList);
            dbList.forEach(ele -> {
                if (stationIds.contains(ele.getStationId()) && dictMap.containsKey(ele.getClassId())) {
                    SysDictVO sysDictVO = dictMap.get(ele.getClassId());
                    resMap.put(ele.getProductLineId() + "_" + ele.getClassDate() + "_" + sysDictVO.getCode() + "_" + ele.getProduceOrderNo(), ele.getQuantity());
                }
            });
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> useCountWeek(List<Integer> lineList, LocalDateTime start, LocalDateTime end) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        //获取(start,end)时间范围内的报工信息
        List<PmsUseCountDTO> dbList = baseMapper.useCountWeek(lineList, start, end);
        if (CollUtil.isNotEmpty(dbList)) {
            Set<Integer> stationIds = getEdgStation(lineList);
            dbList.forEach(ele -> {
                if (stationIds.contains(ele.getStationId())) {
                    resMap.put(ele.getProductLineId() + "_" + ele.getWeekStr(), ele.getQuantity());
                }
            });
        }
        return resMap;
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

    @Override
    public Map<String, BigDecimal> useCountMonth(List<Integer> lineList, LocalDateTime start, LocalDateTime end) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        //获取(start,end)时间范围内的报工信息
        List<PmsUseCountDTO> dbList = baseMapper.useCountMonth(lineList, start, end);
        if (CollUtil.isNotEmpty(dbList)) {
            //获取磨边工位
            Set<Integer> stationIds = getEdgStation(lineList);
            dbList.forEach(ele -> {
                if (stationIds.contains(ele.getStationId())) {
                    resMap.put(ele.getProductLineId() + "_" + ele.getWeekStr(), ele.getQuantity());
                }
            });
        }
        return resMap;
    }

    @Override
    public Map<Long, Map<Integer, BigDecimal>> getTodayPm(List<Integer> lineList, LocalDate today, Integer type) {
        Map<Long, Map<Integer, BigDecimal>> resMap = Maps.newHashMap();
        List<PmsUseCountDTO> dbList = baseMapper.getTodayPm(lineList, today);
        if (CollUtil.isNotEmpty(dbList)) {
            Map<Long, List<PmsUseCountDTO>> materialMap = dbList.stream().collect(Collectors.groupingBy(PmsUseCountDTO::getMaterialId));
            if (type == 0) {
                //基片
                materialMap.forEach((k, v) -> {
                    Map<Integer, BigDecimal> lineNumMap = Maps.newHashMap();
                    resMap.put(k, lineNumMap);
                    Map<Integer, List<PmsUseCountDTO>> lineMap = v.stream().collect(Collectors.groupingBy(PmsUseCountDTO::getProductLineId));
                    lineMap.forEach((k1, v1) -> {
                        List<BigDecimal> valueList = v1.stream().map(PmsUseCountDTO::getQuantity).collect(Collectors.toList());
                        lineNumMap.put(k1, BigDecimalUtil.sum(valueList));
                    });
                });
            } else {
                Set<Integer> stationIds = getEdgStation(lineList);

                materialMap.forEach((k, v) -> {
                    Map<Integer, BigDecimal> lineNumMap = Maps.newHashMap();
                    resMap.put(k, lineNumMap);
                    //深加工
                    v.forEach(ele -> {
                        if (stationIds.contains(ele.getStationId())) {
                            lineNumMap.put(ele.getProductLineId(), BigDecimalUtil.subtract(ele.getQuantity(), ele.getBadQuantity()));
                        }
                    });
                });
            }
        }
        return resMap;
    }

    @Override
    public Map<Long, BigDecimal> getDateRangePm(List<Integer> lineList, LocalDate start, LocalDate end, Integer type) {
        Map<Long, BigDecimal> resMap = Maps.newHashMap();
        List<PmsUseCountDTO> dbList = baseMapper.getDateRangePm(lineList, start, end);
        if (CollUtil.isNotEmpty(dbList)) {
            Map<Long, List<PmsUseCountDTO>> materialMap = dbList.stream().collect(Collectors.groupingBy(PmsUseCountDTO::getMaterialId));
            if (type == 0) {
                //基片
                materialMap.forEach((k, v) -> {
                    List<BigDecimal> valueList = Lists.newArrayList();
                    v.forEach(ele -> {
                        valueList.add(ele.getQuantity());
                    });
                    resMap.put(k, BigDecimalUtil.sum(valueList));
                });
            } else {
                Set<Integer> stationIds = getEdgStation(lineList);

                materialMap.forEach((k, v) -> {
                    List<BigDecimal> valueList = Lists.newArrayList();
                    //深加工
                    v.forEach(ele -> {
                        if (stationIds.contains(ele.getStationId())) {
                            valueList.add(BigDecimalUtil.subtract(ele.getQuantity(), ele.getBadQuantity()));
                        }
                    });
                    resMap.put(k, BigDecimalUtil.sum(valueList));
                });
            }
        }
        return resMap;
    }

    @Override
    public Map<Integer, BigDecimal> getDateRangePmUnit(List<Integer> lineList, LocalDate start, LocalDate end, int type, String unit) {
        List<PmsUseCountDTO> dbList = baseMapper.getDateRangePm(lineList, start, end);
        return linePmUnit(dbList, lineList, type, unit);
    }

    /**
     * 获取产线的产量-包含单位转换
     *
     * @param lineList 产线集合
     * @param dbList   数据集合
     * @param type     类型-0基片 1深加工
     * @param unit     单位转换
     * @return
     */
    private Map<Integer, BigDecimal> linePmUnit(List<PmsUseCountDTO> dbList, List<Integer> lineList, Integer type, String unit) {
        //单位转换
        SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDict1, "换算单位1不存在");
        Assert.notNull(sysDict2, "换算单位2不存在");
        Map<Integer, BigDecimal> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(dbList)) {
            Map<Long, List<PmsUseCountDTO>> materialMap = dbList.stream().collect(Collectors.groupingBy(PmsUseCountDTO::getMaterialId));
            //根据单位查询转换关系
            Map<Long, List<SysSpareUnitConversionVO>> convertMap = FeignOptional.of(() -> spareUnitConvertFeignApi.convert(materialMap.keySet()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (type == 0) {
                //基片
                materialMap.forEach((k, v) -> {
                    //获取物料的单位转换关系
                    List<SysSpareUnitConversionVO> convertRelationList = convertMap.get(k);
                    Map<Integer, List<PmsUseCountDTO>> lineMap = v.stream().collect(Collectors.groupingBy(PmsUseCountDTO::getProductLineId));
                    lineMap.forEach((k1, v1) -> {
                        List<BigDecimal> valueList = v1.stream().map(PmsUseCountDTO::getQuantity).collect(Collectors.toList());
                        BigDecimal sum = BigDecimalUtil.sum(valueList);
                        if (!sysDict1.equals(sysDict2)) {
                            sum = UnitConvertUtil.convertMaterial(convertRelationList, sysDict1.getId(), sysDict2.getId(), sum, 2);
                        }
                        MapUtil.add(resMap, k1, sum, (x, y) -> BigDecimalUtil.add(x, y));
                    });
                });
            } else {
                Set<Integer> stationIds = getEdgStation(lineList);
                materialMap.forEach((k, v) -> {
                    //获取物料的单位转换关系
                    List<SysSpareUnitConversionVO> convertRelationList = convertMap.get(k);
                    //深加工
                    v.forEach(ele -> {
                        if (stationIds.contains(ele.getStationId())) {
                            BigDecimal quantity = BigDecimalUtil.subtract(ele.getQuantity(), ele.getBadQuantity());
                            if (!sysDict1.equals(sysDict2)) {
                                quantity = UnitConvertUtil.convertMaterial(convertRelationList, sysDict1.getId(), sysDict2.getId(), quantity, 2);
                            }
                            MapUtil.add(resMap, ele.getProductLineId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                        }
                    });
                });
            }
        }
        return resMap;
    }


    @Override
    public Map<Long, Map<Integer, BigDecimal>> getTodayInStorage(List<Integer> lineIdList, LocalDate date) {
        Map<Long, Map<Integer, BigDecimal>> resMap = Maps.newHashMap();
        LocalDateTime start = LocalDateUtil.getMorning(date);
        LocalDateTime end = start.plusDays(1);
        List<PmsUseCountDTO> dbList = baseMapper.getDateRangeInStorage(lineIdList, start, end);
        if (CollUtil.isNotEmpty(dbList)) {
            Map<Long, List<PmsUseCountDTO>> collect = dbList.stream().collect(Collectors.groupingBy(PmsUseCountDTO::getMaterialId));
            collect.forEach((k, v) -> {
                Map<Integer, BigDecimal> lineNumMap = Maps.newHashMap();
                resMap.put(k, lineNumMap);
                v.forEach(ele -> {
                    lineNumMap.put(ele.getProductLineId(), ele.getQuantity());
                });

            });
        }
        return resMap;
    }

    @Override
    public Map<Long, BigDecimal> getDateRangeInStorage(List<Integer> lineIdList, LocalDate start, LocalDate end) {
        Map<Long, BigDecimal> resMap = Maps.newHashMap();
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end).plusDays(1);
        List<PmsUseCountDTO> dbList = baseMapper.getDateRangeInStorage(lineIdList, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            Map<Long, List<PmsUseCountDTO>> collect = dbList.stream().collect(Collectors.groupingBy(PmsUseCountDTO::getMaterialId));
            collect.forEach((k, v) -> {
                List<BigDecimal> valueList = Lists.newArrayList();
                v.forEach(ele -> {
                    valueList.add(ele.getQuantity());
                });

                resMap.put(k, BigDecimalUtil.sum(valueList));
            });
        }
        return resMap;
    }

    @Override
    public Map<Integer, BigDecimal> getDateRangeInStorageUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit) {
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.getDateRangeInStorage(lineIdList, startLdt, endLdt);
        //单位转换
        SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(sysDict1, "换算单位1不存在");
        Assert.notNull(sysDict2, "换算单位2不存在");
        Map<Integer, BigDecimal> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(dbList)) {
            Map<Long, List<PmsUseCountDTO>> collect = dbList.stream().collect(Collectors.groupingBy(PmsUseCountDTO::getMaterialId));
            //根据单位查询转换关系
            Map<Long, List<SysSpareUnitConversionVO>> convertMap = FeignOptional.of(() -> spareUnitConvertFeignApi.convert(collect.keySet()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            collect.forEach((k, v) -> {
                List<SysSpareUnitConversionVO> converRelationMap = convertMap.get(k);
                v.forEach(ele -> {
                    BigDecimal quantity = ele.getQuantity();
                    if (!sysDict1.equals(sysDict2)) {
                        quantity = UnitConvertUtil.convertMaterial(converRelationMap, sysDict1.getId(), sysDict1.getId(), quantity, 3);
                    }
                    MapUtil.add(resMap, ele.getProductLineId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                });
            });
        }
        return resMap;
    }

    @Override
    public Map<Long, BigDecimal> getPmByOrder(List<Integer> lineList, Set<Long> orderIdSet, Integer type) {
        Map<Long, BigDecimal> resMap = Maps.newHashMap();
        List<PmsUseCountDTO> dbList = baseMapper.getPmByOrder(orderIdSet);
        if (CollUtil.isNotEmpty(dbList)) {
            if (type == 0) {
                //基片
                //所有工位报工汇总
                dbList.forEach(ele -> {
                    MapUtil.add(resMap, ele.getProduceOrderId(), ele.getQuantity(), (x, y) -> BigDecimalUtil.add(x, y));
                });
            } else {
                Set<Integer> stationIds = getEdgStation(lineList);
                dbList.forEach(ele -> {
                    if (stationIds.contains(ele.getStationId())) {
                        MapUtil.add(resMap, ele.getProduceOrderId(), BigDecimalUtil.subtract(ele.getQuantity(), ele.getBadQuantity()), (x, y) -> BigDecimalUtil.add(x, y));
                    }
                });
            }
        }
        return resMap;
    }

    @Override
    public Map<Long, BigDecimal> getInStorageByOrder(Set<Long> orderIdSet) {
        Map<Long, BigDecimal> resMap = Maps.newHashMap();
        List<PmsUseCountDTO> dbList = baseMapper.getInStorageByOrder(orderIdSet);
        if (CollUtil.isNotEmpty(dbList)) {
            dbList.forEach(ele -> {
                resMap.put(ele.getProduceOrderId(), ele.getQuantity());
            });
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> lineClassPm(Set<Integer> lineIdSet, LocalDate start, LocalDate end, int type, String unit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        List<PmsUseCountDTO> dbList = baseMapper.getPmByLineClass(lineIdSet, start, end);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (type == 0) {
                //基片
                //所有工位报工汇总
                dbList.forEach(ele -> {
                    BigDecimal quantity = ele.getQuantity();
                    if (!sysDict1.equals(sysDict2)) {
                        quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), BigDecimalUtil.SCALE_2);
                    }
                    MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getClassId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                });
            } else {
                Set<Integer> stationIds = getEdgStation(new ArrayList<>(lineIdSet));
                dbList.forEach(ele -> {
                    if (stationIds.contains(ele.getStationId())) {
                        BigDecimal quantity = BigDecimalUtil.subtract(ele.getQuantity(), ele.getBadQuantity());
                        if (!sysDict1.equals(sysDict2)) {
                            quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), quantity, BigDecimalUtil.SCALE_2);
                        }
                        MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getClassId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                    }
                });
            }
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> lineClassInStorage(Set<Integer> lineIdSet, LocalDate start , LocalDate end, String unit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.getInStorageByLineClass(lineIdSet, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            dbList.forEach(ele -> {
                BigDecimal quantity = ele.getQuantity();
                if (!sysDict1.equals(sysDict2)) {
                    quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), BigDecimalUtil.SCALE_2);
                }
                MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getClassId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> lineClassUseCount(Set<Integer> lineIdSet, LocalDate start, LocalDate end, String unit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        //获取(start,end)时间范围内的报工信息
        List<PmsUseCountDTO> dbList = baseMapper.getUserCountByLineClass(lineIdSet, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            Set<Integer> stationIds = getEdgStation(new ArrayList<>(lineIdSet));
            for (PmsUseCountDTO ele : dbList) {
                if (stationIds.contains(ele.getStationId())) {
                    BigDecimal quantity = ele.getQuantity();
                    if (!sysDict1.equals(sysDict2)) {
                        quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), BigDecimalUtil.SCALE_2);
                    }
                    MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getClassId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                }
            }
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> lineUseCount(Set<Integer> lineIdSet, Set<Integer> stationIds, LocalDate start, LocalDate end, QtManagerConstant.QtGranularityEnum granularity) {
        List<Tuple4DTO<String, Long, BigDecimal,Long>> consumptionList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            consumptionList = baseMapper.consumptionDay(start, end, lineIdSet,stationIds);
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            consumptionList = baseMapper.consumptionMonth(start, end, lineIdSet,stationIds);
        } else {
            consumptionList = baseMapper.consumptionYear(start, end, lineIdSet,stationIds);
        }
        SysDictVO sqSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
       return unitConvert(sqSysDict,consumptionList,3);
    }

    @Override
    public List<ProcessReportWorkHourVO> getReportWorkHour(ProcessReportWorkHourQmsDTO dto) {
        List<ProcessReportWorkHourVO> result = Lists.newArrayList();
        List<ProcessReportWorkHourVO> processWorkHour = this.baseMapper.getProcessWorkHour(dto);
        if(CollUtil.isNotEmpty(processWorkHour)){
            result = processWorkHour;
        }
        return result;
    }

    @Override
    public Map<String, BigDecimal> dateClassLinePmUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, int type, String unit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.dateClassLinePmUnit(lineIdList, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (type == 0) {
                //基片
                //所有工位报工汇总
                dbList.forEach(ele -> {
                    BigDecimal quantity = ele.getQuantity();
                    if (!sysDict1.equals(sysDict2)) {
                        quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), quantity, BigDecimalUtil.SCALE_2);
                    }
                    MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getClassDate() + "_" + ele.getClassId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                });
            } else {
                Set<Integer> stationIds = getEdgStation(lineIdList);
                dbList.forEach(ele -> {
                    if (stationIds.contains(ele.getStationId())) {
                        BigDecimal quantity = BigDecimalUtil.subtract(ele.getQuantity(), ele.getBadQuantity());
                        if (!sysDict1.equals(sysDict2)) {
                            quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), quantity, BigDecimalUtil.SCALE_2);
                        }
                        MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getClassDate() + "_" + ele.getClassId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                    }
                });
            }
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> dateLinePmUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, int type, String unit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        if (CollUtil.isEmpty(lineIdList)) {
            return resMap;
        }
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.dateLinePmUnit(lineIdList, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (type == 0) {
                //基片
                //所有工位报工汇总
                dbList.forEach(ele -> {
                    BigDecimal quantity = ele.getQuantity();
                    if (!sysDict1.equals(sysDict2)) {
                        quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), quantity, BigDecimalUtil.SCALE_2);
                    }
                    MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getClassDate(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                });
            } else {
                Set<Integer> stationIds = getEdgStation(lineIdList);
                dbList.forEach(ele -> {
                    if (stationIds.contains(ele.getStationId())) {
                        BigDecimal quantity = BigDecimalUtil.subtract(ele.getQuantity(), ele.getBadQuantity());
                        if (!sysDict1.equals(sysDict2)) {
                            quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), quantity, BigDecimalUtil.SCALE_2);
                        }
                        MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getClassDate(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                    }
                });
            }
        }
        return resMap;
    }


    @Override
    public Map<String, BigDecimal> dateLineClassThickPmUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, int type, String unit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        if (CollUtil.isEmpty(lineIdList)) {
            return resMap;
        }
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.dateClassLinePmUnit(lineIdList, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //获取班次的数据字典
            SysDictVO morningDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_0, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO nightDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.NUM_1, PmManagerConstant.SYS_SHIFT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(morningDict, "早班数据字典不存在");
            Assert.notNull(nightDict, "晚班数据字典不存在");

            //考虑物料的单位转换
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(Lists.newArrayList(materialIdSet)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (type == 0) {
                //基片
                //所有工位报工汇总
                dbList.forEach(ele -> {
                    Integer classId = 0;
                    if (nightDict.getId().equals(ele.getClassId())) {
                        classId = 1;
                    }
                    BigDecimal quantity = ele.getQuantity();
                    if (!sysDict1.equals(sysDict2)) {
                        quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), quantity, BigDecimalUtil.SCALE_2);
                    }
                    EqpSpareVO material = materialMap.getOrDefault(ele.getMaterialId(), new EqpSpareVO());

                    MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getClassDate() + "_" + classId + "_" + material.getThickQuantity(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                });
            } else {
                Set<Integer> stationIds = getEdgStation(lineIdList);
                dbList.forEach(ele -> {
                    Integer classId = 0;
                    if (nightDict.getId().equals(ele.getClassId())) {
                        classId = 1;
                    }
                    if (stationIds.contains(ele.getStationId())) {
                        BigDecimal quantity = BigDecimalUtil.subtract(ele.getQuantity(), ele.getBadQuantity());
                        if (!sysDict1.equals(sysDict2)) {
                            quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), quantity, BigDecimalUtil.SCALE_2);
                        }
                        EqpSpareVO material = materialMap.getOrDefault(ele.getMaterialId(), new EqpSpareVO());
                        MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getClassDate() + "_" + classId + "_" + material.getThickQuantity(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                    }
                });
            }
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> dateClassLineInStorageUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.dateClassLineInStorageUnit(lineIdList, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            dbList.forEach(ele -> {
                BigDecimal quantity = ele.getQuantity();
                if (!sysDict1.equals(sysDict2)) {
                    quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), BigDecimalUtil.SCALE_2);
                }
                MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getClassDate() + "_" + ele.getClassId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        return resMap;
    }


    @Override
    public Map<String, BigDecimal> dateLineInStorageUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.dateClassLineInStorageUnit(lineIdList, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            dbList.forEach(ele -> {
                BigDecimal quantity = ele.getQuantity();
                if (!sysDict1.equals(sysDict2)) {
                    quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), BigDecimalUtil.SCALE_2);
                }
                MapUtil.add(resMap, ele.getProductLineId() + "_" + ele.getClassDate(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        return resMap;
    }

    @Override
    public Map<String, List<PmsReportWork>> batchLinkReportWorkMap(Set<String> batchSet) {
        Map<String, List<PmsReportWork>> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(batchSet)) {
            List<PmsReportWork> dbList = list(new LambdaQueryWrapper<PmsReportWork>().in(PmsReportWork::getBatchNo, batchSet));
            if (CollUtil.isNotEmpty(dbList)) {
                dbList.forEach(ele -> MapUtil.compute(resMap, ele.getBatchNo(), ele));
            }
        }
        return resMap;
    }

    @Override
    public Map<String, TupleDTO<BigDecimal, BigDecimal>> lineProcessPmMap(List<Integer> lineIdList, LocalDate start, LocalDate end) {
        Map<String, TupleDTO<BigDecimal, BigDecimal>> resMap = Maps.newHashMap();
        List<PmsUseCountDTO> dbList = baseMapper.lineProcessPmMap(lineIdList,start,end);
        if(CollUtil.isNotEmpty(dbList)){
            Map<String,BigDecimal> pmMap = Maps.newHashMap();
            Map<String,BigDecimal> sumMap = Maps.newHashMap();
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            dbList.forEach(ele -> {
                BigDecimal quantity = BigDecimalUtil.subtract(ele.getQuantity(), ele.getBadQuantity());
                BigDecimal quantity1 = ele.getQuantity();
                if (!sysDict1.equals(sysDict2)) {
                    quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), quantity, BigDecimalUtil.SCALE_2);
                    quantity1 = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), quantity1, BigDecimalUtil.SCALE_2);
                }
                MapUtil.add(pmMap, ele.getStationId() + "_" + ele.getProductLineId(), quantity, (x, y) -> BigDecimalUtil.add(x, y, BigDecimalUtil.SCALE_2));
                MapUtil.add(sumMap, ele.getStationId() + "_" + ele.getProductLineId(), quantity1, (x, y) -> BigDecimalUtil.add(x, y, BigDecimalUtil.SCALE_2));
            });
            //单位转换完成,考虑计算产量，百分比
            pmMap.forEach((k,v) ->{
                resMap.put(k, new TupleDTO<>(v, sumMap.get(k)));
            }) ;
        }
        return resMap;
    }

    @Override
    public Map<Integer, BigDecimal> lineInStorage(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit) {
        Map<Integer, BigDecimal> resMap = Maps.newHashMap();
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.getInStorageByLineClass(Sets.newHashSet(lineIdList), startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            dbList.forEach(ele -> {
                BigDecimal quantity = ele.getQuantity();
                if (!sysDict1.equals(sysDict2)) {
                    quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), BigDecimalUtil.SCALE_2);
                }
                MapUtil.add(resMap, ele.getProductLineId(), quantity, (x, y) -> BigDecimalUtil.add(x, y, BigDecimalUtil.SCALE_2));
            });
        }
        return resMap;
    }

    @Override
    public Map<Integer, BigDecimal> getGhQuantity(LocalDate start, LocalDate end, List<Integer> lineIdList) {
        Map<Integer, BigDecimal> resMap = Maps.newHashMap();
        Map<String, TupleDTO<BigDecimal, BigDecimal>> currentPmMap = lineProcessPmMap(lineIdList, start, end);
        PmProcess pmProcess = pmProcessService.getOne(new LambdaQueryWrapper<PmProcess>().eq(PmProcess::getCode, "GHGX"));
        Assert.notNull(pmProcess,"钢化工序不存在");
        if (CollUtil.isNotEmpty(currentPmMap)) {
            currentPmMap.forEach((k, v) -> {
                if (k.indexOf(pmProcess.getId() + "_") != -1) {
                    String[] split = k.split("_");
                    Integer line = Integer.parseInt(split[1]);
                    MapUtil.add(resMap, line, v.getField1(), (x, y) -> BigDecimalUtil.add(x, y, BigDecimalUtil.SCALE_2));
                }
            });
        }
        return resMap;
    }

    @Override
    public CommonReportDTO jpPmDateGranularity(Integer workShopId, LocalDate start, LocalDate end, Integer granularityInt, String unit) {
        //粒度存在
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(granularityInt);
        CommonReportDTO commonReportDTO = new CommonReportDTO();
        commonReportDTO.setName("生产产量");
        commonReportDTO.setUnit("T");
        Map<String, BigDecimal> valueMap = Maps.newHashMap();
        commonReportDTO.setValueMap(valueMap);
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(workShopId);
        Assert.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.getDatePmUnit(lineIdList, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            dbList.forEach(ele ->{
                BigDecimal quantity = ele.getQuantity();
                if (!sysDict1.equals(sysDict2)) {
                    quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), BigDecimalUtil.SCALE_2);
                }
                String convert = MysqlDateKeyUtil.convert(granularity, ele.getClassDate());
                MapUtil.add(valueMap, convert, quantity, (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        return commonReportDTO;
    }

    @Override
    public CommonReportDTO jpInStorageDateGranularity(Integer workShopId, LocalDate start, LocalDate end, Integer granularityInt) {
        QtManagerConstant.QtGranularityEnum granularity = QtManagerConstant.QtGranularityEnum.getByCode(granularityInt);
        CommonReportDTO commonReportDTO = new CommonReportDTO();
        commonReportDTO.setName("入库成品率");
        commonReportDTO.setUnit("%");
        Map<String, BigDecimal> valueMap = Maps.newHashMap();
        commonReportDTO.setValueMap(valueMap);
        List<SysOfficeVO> wsLineList = pmsOfficeManager.getWsLineList(workShopId);
        Assert.notEmpty(wsLineList, "车间下暂无产线");
        List<Integer> lineIdList = wsLineList.stream().map(SysOfficeVO::getId).collect(Collectors.toList());
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.getDateInStorageUnit(lineIdList, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_TON, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            dbList.forEach(ele ->{
                BigDecimal quantity = ele.getQuantity();
                if (!sysDict1.equals(sysDict2)) {
                    quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), BigDecimalUtil.SCALE_2);
                }
                String convert = MysqlDateKeyUtil.convert(granularity, ele.getClassDate());
                MapUtil.add(valueMap, convert, quantity, (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        return commonReportDTO;
    }

    @Override
    public Map<String, BigDecimal> actualUse(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> lineIdSet, Integer bit) {
        List<TripleDTO<String, Long, BigDecimal>> actualList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            actualList = baseMapper.actualUseMaterialDay(start, end, lineIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            actualList = baseMapper.actualUseMaterialWeek(start, end, lineIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            actualList = baseMapper.actualUseMaterialMonth(start, end, lineIdSet);
        } else {
            actualList = baseMapper.actualUseMaterialYear(start, end, lineIdSet);
        }
        //进行单位转换
        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sqSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(pieceSysDict, "片单位不存在");
        Assert.notNull(sqSysDict, "平方米单位不存在");
        List<TripleDTO<String, Long, BigDecimal>> tripleDTOS = pmsUnitConvertManager.<TripleDTO<String, Long, BigDecimal>>unitConvert(pieceSysDict, sqSysDict, actualList, x -> x.getField2(), y -> y.getField3(), (m, n) -> m.setField3(n), bit);
        //基于班次日期的聚合
        Map<String, BigDecimal> mapRes = Maps.newHashMap();
        tripleDTOS.forEach(ele -> {
            MapUtil.add(mapRes, String.valueOf(ele.getField1()), ele.getField3(), (x, y) -> BigDecimalUtil.add(x, y, bit));
        });
        return mapRes;
    }

    @Override
    public Map<String, BigDecimal> jpComplete(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> lineIdSet,String unit, int bit) {
        List<TripleDTO<String, Long, BigDecimal>> completeList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            completeList = baseMapper.jpCompleteMaterialDay(start, end, lineIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            completeList = baseMapper.jpCompleteMaterialWeek(start, end, lineIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            completeList = baseMapper.jpCompleteMaterialMonth(start, end, lineIdSet);
        } else {
            completeList = baseMapper.jpCompleteMaterialYear(start, end, lineIdSet);
        }
        //进行单位转换
        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sqSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(pieceSysDict, "片单位不存在");
        Assert.notNull(sqSysDict, "转换单位单位不存在");
        List<TripleDTO<String, Long, BigDecimal>> tripleDTOS = pmsUnitConvertManager.<TripleDTO<String, Long, BigDecimal>>unitConvert(pieceSysDict, sqSysDict, completeList, x -> x.getField2(), y -> y.getField3(), (m, n) -> m.setField3(n), bit);
        //基于班次日期的聚合
        Map<String, BigDecimal> mapRes = Maps.newHashMap();
        tripleDTOS.forEach(ele -> {
            MapUtil.add(mapRes, String.valueOf(ele.getField1()), ele.getField3(), (x, y) -> BigDecimalUtil.add(x, y, bit));
        });
        return mapRes;
    }

    @Override
    public Map<String, BigDecimal> sjgComplete(QtManagerConstant.QtGranularityEnum granularity, LocalDate start, LocalDate end, Set<Integer> lineIdSet, int bit) {
        List<TripleDTO<String, Long, BigDecimal>> completeList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            completeList = baseMapper.sjgCompleteMaterialDay(start, end, lineIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.WEEK.equals(granularity)) {
            completeList = baseMapper.sjgCompleteMaterialWeek(start, end, lineIdSet);
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            completeList = baseMapper.sjgCompleteMaterialMonth(start, end, lineIdSet);
        } else {
            completeList = baseMapper.sjgCompleteMaterialYear(start, end, lineIdSet);
        }
        //进行单位转换
        SysDictVO pieceSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysDictVO sqSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(pieceSysDict, "片单位不存在");
        Assert.notNull(sqSysDict, "转换单位不存在");
        List<TripleDTO<String, Long, BigDecimal>> tripleDTOS = pmsUnitConvertManager.<TripleDTO<String, Long, BigDecimal>>unitConvert(pieceSysDict, sqSysDict, completeList, x -> x.getField2(), y -> y.getField3(), (m, n) -> m.setField3(n), bit);
        //基于班次日期的聚合
        Map<String, BigDecimal> mapRes = Maps.newHashMap();
        tripleDTOS.forEach(ele -> {
            MapUtil.add(mapRes, String.valueOf(ele.getField1()), ele.getField3(), (x, y) -> BigDecimalUtil.add(x, y, bit));
        });
        return mapRes;
    }

    @Override
    public Map<String, BigDecimal> badnessQuantity(Set<Integer> lineIdSet, Set<Integer> stationIds, LocalDate start, LocalDate end, QtManagerConstant.QtGranularityEnum granularity) {
        List<Tuple4DTO<String, Long, BigDecimal,Long>> consumptionList;
        if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
            consumptionList = baseMapper.badnessDay(start, end, lineIdSet,stationIds);
        } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
            consumptionList = baseMapper.badnessMonth(start, end, lineIdSet,stationIds);
        } else {
            consumptionList = baseMapper.badnessYear(start, end, lineIdSet,stationIds);
        }
        SysDictVO sqSysDict = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        return unitConvert(sqSysDict,consumptionList,3);
    }

    @Override
    public List<Tuple4DTO<String, String, BigDecimal,Long>>  materialConsumeNoMain(Set<Integer> lineIds, LocalDate start, LocalDate end, QtManagerConstant.QtGranularityEnum granularity) {
        //根据产线和时间获取生产订单的物料消耗（不为主物料的物料消耗）
        Set<Long> materialIds = baseMapper.materialConsumeNoMain(lineIds, start, end);
        List<Tuple4DTO<String, String, BigDecimal, Long>> consumptionList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(materialIds)) {
            if (QtManagerConstant.QtGranularityEnum.DAY.equals(granularity)) {
                consumptionList = baseMapper.materialConsumeDay(lineIds, materialIds, start, end);
            } else if (QtManagerConstant.QtGranularityEnum.MONTH.equals(granularity)) {
                consumptionList = baseMapper.materialConsumeMonth(lineIds, materialIds, start, end);
            } else {
                consumptionList = baseMapper.materialConsumeYear(lineIds, materialIds, start, end);
            }
        }
        return  consumptionList;
    }

    @Override
    public Map<String, BigDecimal> datePmUnit(List<Integer> lineIdSet, LocalDate start, LocalDate end, int type, String unit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.getDatePmUnit(lineIdSet, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (type == 0) {
                //基片
                //所有工位报工汇总
                dbList.forEach(ele -> {
                    BigDecimal quantity = ele.getQuantity();
                    if (!sysDict1.equals(sysDict2)) {
                        quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), BigDecimalUtil.SCALE_2);
                    }
                    MapUtil.add(resMap, ele.getClassDate()+"", quantity, (x, y) -> BigDecimalUtil.add(x, y));
                });
            } else {
                Set<Integer> stationIds = getEdgStation(new ArrayList<>(lineIdSet));
                dbList.forEach(ele -> {
                    if (stationIds.contains(ele.getStationId())) {
                        BigDecimal quantity = BigDecimalUtil.subtract(ele.getQuantity(), ele.getBadQuantity());
                        if (!sysDict1.equals(sysDict2)) {
                            quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), quantity, BigDecimalUtil.SCALE_2);
                        }
                        MapUtil.add(resMap, ele.getClassDate() +"" , quantity, (x, y) -> BigDecimalUtil.add(x, y));
                    }
                });
            }
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> dateClassPmUnit(List<Integer> lineIdSet, LocalDate start, LocalDate end, int type, String unit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.getDateClassPmUnit(lineIdSet, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if (type == 0) {
                //基片
                //所有工位报工汇总
                dbList.forEach(ele -> {
                    BigDecimal quantity = ele.getQuantity();
                    if (!sysDict1.equals(sysDict2)) {
                        quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), BigDecimalUtil.SCALE_2);
                    }
                    MapUtil.add(resMap, ele.getClassDate() + "_" + ele.getClassId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                });
            } else {
                Set<Integer> stationIds = getEdgStation(new ArrayList<>(lineIdSet));
                dbList.forEach(ele -> {
                    if (stationIds.contains(ele.getStationId())) {
                        BigDecimal quantity = BigDecimalUtil.subtract(ele.getQuantity(), ele.getBadQuantity());
                        if (!sysDict1.equals(sysDict2)) {
                            quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), quantity, BigDecimalUtil.SCALE_2);
                        }
                        MapUtil.add(resMap, ele.getClassDate() + "_" + ele.getClassId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
                    }
                });
            }
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> dateInStorageUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.getDateInStorageUnit(lineIdList, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            dbList.forEach(ele -> {
                BigDecimal quantity = ele.getQuantity();
                if (!sysDict1.equals(sysDict2)) {
                    quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), BigDecimalUtil.SCALE_2);
                }
                MapUtil.add(resMap, ele.getClassDate() + "", quantity, (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        return resMap;
    }

    @Override
    public Map<String, BigDecimal> dateClassInStorageUnit(List<Integer> lineIdList, LocalDate start, LocalDate end, String unit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        LocalDateTime startLdt = LocalDateUtil.getMorning(start);
        LocalDateTime endLdt = LocalDateUtil.getMorning(end.plusDays(1));
        List<PmsUseCountDTO> dbList = baseMapper.getDateClassInStorageUnit(lineIdList, startLdt, endLdt);
        if (CollUtil.isNotEmpty(dbList)) {
            //考虑物料的单位转换
            Set<Long> materialIdSet = dbList.stream().map(PmsUseCountDTO::getMaterialId).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取吨的单位
            SysDictVO sysDict1 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO sysDict2 = FeignOptional.of(() -> remoteDictFeignApi.getByCode(unit, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(sysDict1, "转换单位[片]不存在");
            Assert.notNull(sysDict2, "目标转换单位不存在");
            dbList.forEach(ele -> {
                BigDecimal quantity = ele.getQuantity();
                if (!sysDict1.equals(sysDict2)) {
                    quantity = UnitConvertUtil.convertMaterial(materialConvertMap.get(ele.getMaterialId()), sysDict1.getId(), sysDict2.getId(), ele.getQuantity(), BigDecimalUtil.SCALE_2);
                }
                MapUtil.add(resMap, ele.getClassDate() + "_" + ele.getClassId(), quantity, (x, y) -> BigDecimalUtil.add(x, y));
            });
        }
        return resMap;
    }

    @Override
    public ReportWorkPreinstallValueVO preinstallValue(LocalDate localDate) {
        TupleDTO<LocalDate,Integer> dto = new TupleDTO<>(localDate,Math.toIntExact( SecurityUtils.getUser().getUserId()));
        OaClassVO oaClassVO = FeignOptional.of(() -> oaInfoService.getUserClass(dto), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        return BeanUtil.copyProperties(oaClassVO, ReportWorkPreinstallValueVO.class);
    }

    /**
     * 更新物料消耗
     *
     * @param dto                 dto信息
     * @param materialConsumeList 物料消耗集合
     */
    private void updateMaterialConsume(ProcessReportWorkSubmitDTO dto, List<ProcessReportWorkMaterialListDTO> materialConsumeList) {
        //已经存在的物料消耗-不需要更新
        //新增的物料消耗，需要校验库存没有变化后，添加到出库单明细中
        //删除的物料消耗，需要创建入库单+明细
        List<ProcessReportWorkMaterialListDTO> existList = Lists.newArrayList();
        List<ProcessReportWorkMaterialListDTO> insertList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(materialConsumeList)) {
            materialConsumeList.forEach(ele -> {
                if (Objects.nonNull(ele.getId())) {
                    existList.add(ele);
                } else {
                    insertList.add(ele);
                }
            });
        }
        //出库单明细
        Set<Long> existIdSet = existList.stream().map(ProcessReportWorkMaterialListDTO::getId).collect(Collectors.toSet());

        WmsDeliveryReceiptDTO receiptDTO = new WmsDeliveryReceiptDTO();
        List<WmsDeliveryReceiptInfoDTO> materialList = Lists.newArrayList();

        //获取出库单-出库单和明细可能不存在，因为无物料消耗
        PmsCompleteDelivery pmsCompleteDelivery = completeDeliveryService.getOne(new LambdaQueryWrapper<PmsCompleteDelivery>().eq(PmsCompleteDelivery::getReportWorkId, dto.getId()).last(MpConstant.STR_LIMIT_1));
        if (Objects.isNull(pmsCompleteDelivery)) {
            pmsCompleteDelivery = new PmsCompleteDelivery();

        }
        List<PmsCompleteDeliveryInfo> infoList = completeDeliveryInfoService.list(new LambdaQueryWrapper<PmsCompleteDeliveryInfo>().eq(PmsCompleteDeliveryInfo::getCompleteDeliveryId, pmsCompleteDelivery.getId()));
        if (Objects.isNull(infoList)) {
            infoList = Lists.newArrayList();
        }

        List<PmsCompleteDeliveryInfo> newInfoList = Lists.newArrayList();
        for (ProcessReportWorkMaterialListDTO newMc : insertList) {
            //出库单明细
            PmsCompleteDeliveryInfo pmsCompleteDeliveryInfo = convert2DeliveryInfo(pmsCompleteDelivery.getId(), newMc);
            newInfoList.add(pmsCompleteDeliveryInfo);
            //校验库存
            receiptDTO.setWarehouseId(newMc.getWarehouseId());
            receiptDTO.setWarehouseName(newMc.getWarehouseName());
            WmsDeliveryReceiptInfoDTO wmsDeliveryReceiptInfoDTO = new WmsDeliveryReceiptInfoDTO();
            BeanUtils.copyProperties(newMc, wmsDeliveryReceiptInfoDTO);
            wmsDeliveryReceiptInfoDTO.setMaterialId(newMc.getModuleId());
            materialList.add(wmsDeliveryReceiptInfoDTO);
        }

        //删除出库单明细
        List<PmsCompleteDeliveryInfo> deleteList = infoList.stream().filter(v -> !existIdSet.contains(v.getId())).collect(Collectors.toList());
        //生成对应的入库单【冲销报工入库】
        pmsCompleteStorageService.generateByDelivery(dto, deleteList);
        completeDeliveryInfoService.remove(new LambdaQueryWrapper<PmsCompleteDeliveryInfo>()
                .eq(PmsCompleteDeliveryInfo::getCompleteDeliveryId, pmsCompleteDelivery.getId())
                .notIn(CollUtil.isNotEmpty(existIdSet), PmsCompleteDeliveryInfo::getId, existIdSet));
        //保存出库单明细
        if (CollUtil.isNotEmpty(newInfoList)) {
            if (Objects.isNull(pmsCompleteDelivery.getId())) {
                SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(SysDictConstant.CODE_0, SysDictConstant.TYPE_OUTBOUND_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                pmsCompleteDelivery.setCode(maxOutVouchCode());
                pmsCompleteDelivery.setOutStorageVoucher(pmsCompleteDelivery.getCode());
                pmsCompleteDelivery.setDeliveryTime(LocalDateTime.now());
                pmsCompleteDelivery.setQuantity(BigDecimal.ZERO);
                pmsCompleteDelivery.setUnitId(materialConsumeList.get(0).getUnitId());
                pmsCompleteDelivery.setTypeId(sysDictVO.getId());
                pmsCompleteDelivery.setProduceOrderId(dto.getProduceOrderId());
                pmsCompleteDelivery.setState(PmManagerConstant.InVouchEnum.FINISH.getCode());
                pmsCompleteDelivery.setProcessId(dto.getProcessId());
                pmsCompleteDelivery.setReportWorkId(dto.getId());
                completeDeliveryService.save(pmsCompleteDelivery);
            }
            completeDeliveryInfoService.saveBatch(newInfoList);
            receiptDTO.setType(pmsCompleteDelivery.getTypeId());
            Boolean generateFlag = FeignOptional.of(() -> remoteWmsInventoryFeignApi.generateOutStorageVoucher(receiptDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
            if (!generateFlag) {
                throw new BusinessException("库存变化，请重新报工");
            }
        }
        //数量需要更新，重新计算
        BigDecimal quantity = completeDeliveryInfoService.getQuantity(pmsCompleteDelivery.getId());
        pmsCompleteDelivery.setQuantity(quantity);
        completeDeliveryService.updateById(pmsCompleteDelivery);
    }

    /**
     * 转换成出库单明细
     *
     * @param deliveryId  出库单id
     * @param materialDTO 物料消耗明细
     * @return 转换后的对象
     */
    private PmsCompleteDeliveryInfo convert2DeliveryInfo(Long deliveryId, ProcessReportWorkMaterialListDTO materialDTO) {
        PmsCompleteDeliveryInfo info = new PmsCompleteDeliveryInfo();
        BeanUtils.copyProperties(materialDTO, info);
        info.setMaterialId(materialDTO.getModuleId());
        info.setMaterialCode(materialDTO.getModuleCode());
        info.setMaterialName(materialDTO.getModuleName());
        info.setCaseNo(materialDTO.getCaseNo());
        info.setBatchNo(materialDTO.getBatchNo());
        info.setPackTime(materialDTO.getPackTime());
        info.setWarehouseId(materialDTO.getWarehouseId());
        info.setShelfId(materialDTO.getShelfId());
        info.setCompleteDeliveryId(deliveryId);
        //新增可用数量
        info.setAvaQuantity(materialDTO.getAvailableQuantity());
        return info;
    }

    /**
     * 获取
     *
     * @param dto
     * @return
     */
    private void getReportWork(ProcessReportWorkSubmitDTO dto, PmsReportWork pmsReportWork) {
        PmsProduceOrder pmsProduceOrder = produceOrderService.getById(dto.getProduceOrderId());
        Assert.notNull(pmsProduceOrder, "生产订单不存在");
        //校验产线和工位是否存在
        Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(dto.getProductLineId(), dto.getStationId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.checkBetween(sysOfficeMap.size(), 2, 2, "产线或工位不存在");
        //校验工序是否存在
        PmsProduceOrderCraft pmsProduceOrderCraft = produceOrderCraftService.getById(dto.getProcessId());
        Assert.notNull(pmsProduceOrderCraft, "工序不存在");

        validMaterialConsume(dto, pmsProduceOrderCraft.getProcessId());

        //校验批次是否存在
        PmsWorkOrderBatch pmsWorkOrderBatch = workOrderBatchService.getById(dto.getBatch());
        Assert.notNull(pmsWorkOrderBatch, "批次不存在");
        //校验班次和班组是否存在
        //校验单位是否存在
        Boolean unitBoolean = FeignOptional.of(() -> remoteDictFeignApi.existTypeId(SysDictConstant.TYPE_MEASURE_UNIT, dto.getUnitId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.isTrue(unitBoolean, "单位不存在");
        //校验是否物料的条数是否正确
        //生成报工记录
        BeanUtils.copyProperties(dto, pmsReportWork);
        pmsReportWork.setProduceOrderNo(pmsProduceOrder.getCode());
    }

    /**
     * 获取报工编码
     *
     * @param orderNo     订单编号
     * @param processSort 工序号-排序字段
     * @return 报工编码
     */
    private String reportWorkCode(String orderNo, Integer processSort) {
        String prefix = orderNo + processSort;
        PmsReportWork one = getOne(new LambdaQueryWrapper<PmsReportWork>().like(PmsReportWork::getCode, prefix).orderByDesc(PmsReportWork::getCode).last(MpConstant.STR_LIMIT_1));
        long currentMax = PmsCodeUtil.DEFAULT_PADDING;
        if (Objects.nonNull(one)) {
            String substring = one.getCode().substring(one.getCode().length() - PmsCodeUtil.NUM_BIT_4);
            try {
                currentMax = Long.parseLong(substring);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return PmsCodeUtil.getNextCode(prefix, PmsCodeUtil.NUM_BIT_4, currentMax);
    }

    private void calculatePullamountTask(LocalDateTime startTime){
        TupleDTO<LocalDateTime,LocalDate> taskDTO = new TupleDTO<>();
        QtTaskClassCalculate qtTaskClassCalculate = CalculateDateUtil.calculateClassTime(startTime);
        LocalDate date = qtTaskClassCalculate.getClassDate().toLocalDate();
        LocalDateTime now = LocalDateTime.now();
        //如果是早班,那么传这一天晚班 否则,第二天早班
        if(qtTaskClassCalculate.getClassType().equals(OaConstant.NO)){
            now = date.atTime(20,0,0);
        }else {
            date = date.plusDays(1);
            now = date.atTime(8,0,0);
        }
        taskDTO.setField1(now);
        taskDTO.setField2(date);
        FeignOptional.of(()->remoteCheckTaskDetailFeignApi.calculatePullAmountTask(taskDTO),FeignConstant.STECCN_QMS_SERVICE_NAME);
    }



    /**
     * 单位转换
     * @param targetSysDict 目标单位
     * @param dataList      数据集合
     * @return map
     */
    private Map<String, BigDecimal> unitConvert( SysDictVO targetSysDict, List<Tuple4DTO<String, Long, BigDecimal,Long>> dataList, Integer bit) {
        Map<String, BigDecimal> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(dataList)) {
            Set<Long> materialIdSet = dataList.stream().map(TripleDTO::getField2).collect(Collectors.toSet());
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            dataList.forEach(ele -> {
                List<SysSpareUnitConversionVO> convertList = materialConvertMap.get(ele.getField2());
                BigDecimal convertQuantity = UnitConvertUtil.convertMaterial(convertList, ele.getField4(), targetSysDict.getId(), ele.getField3(), bit);
                MapUtil.add(resMap, String.valueOf(ele.getField1()), convertQuantity, (x, y) -> BigDecimalUtil.add(x, y, bit));
            });
        }
        return resMap;
    }
}