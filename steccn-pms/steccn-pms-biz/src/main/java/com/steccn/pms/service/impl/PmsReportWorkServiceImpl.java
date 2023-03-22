package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.R;
import com.steccn.data.dto.PmReportWorkPreloadDTO;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.platform.feign.*;
import com.steccn.platform.vo.*;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.*;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.mapper.PmsReportWorkMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.MessInStorageVoucherVO;
import com.steccn.pms.vo.PmsReportWorkVO;
import com.steccn.pms.vo.ProcessReportWorkInfoVO;
import com.steccn.pms.vo.ProcessReportWorkPageVO;
import com.steccn.wms.dto.*;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
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
                save(pmsReportWork);
                //保存工时信息
                reportWorkHourService.saveHourList(pmsReportWork.getId(), dto.getHourDTOList());
                //保存能耗信息
                pmsReportWorkConsumeService.saveConsume(pmsReportWork.getId(), dto.getConsumeDTOList());
                //插入库存
                insertDelivery(dto, pmsReportWork);
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
                    if (materialListDTO.getQuantity().compareTo(materialListDTO.getAvailableQuantity()) == 1) {
                        throw new BusinessException("物料消耗实际数量不可以大于可用数量");
                    }
                }
            //去除物料消耗list
            List<ProcessReportWorkMaterialListDTO> materialListDTOList = dto.getMaterialConsumeList().stream().filter(v -> Objects.nonNull(v.getInventoryId())).collect(Collectors.toList());
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

            wmsDeliveryReceiptDTO.setType(sysDictVO.getId());
            wmsDeliveryReceiptDTO.setList(materialList);
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
    public ProcessReportWorkInfoVO info(Long id) {
        PmsReportWork model = getById(id);
        Assert.notNull(model, "报工信息不存在");
        ProcessReportWorkInfoVO resVo = BeanUtil.copyProperties(model, ProcessReportWorkInfoVO.class);
        //获取工时信息
        List<ProcessReportWorkHourDTO> hourDTOList = reportWorkHourService.getReportWorkHour(id);
        resVo.setHourDTOList(hourDTOList);
        return resVo;
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
}