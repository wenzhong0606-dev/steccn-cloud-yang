package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Tuple;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.BarcodePrintDTO;
import com.steccn.pms.dto.BarcodePrintRecordDTO;
import com.steccn.pms.dto.CaseSiteDTO;
import com.steccn.pms.dto.PmsCompleteStorageNumDTO;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.service.PmsBarcodePrintRecordService;
import com.steccn.pms.service.PmsBarcodePrintService;
import com.steccn.pms.mapper.PmsBarcodePrintMapper;
import com.steccn.pms.service.PmsCompleteStorageService;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.BarcodePrintRecordVO;
import com.steccn.pms.vo.WorkOrderMaterialVO;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_barcode_print(条码打印)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsBarcodePrintServiceImpl extends ServiceImpl<PmsBarcodePrintMapper, PmsBarcodePrint>
        implements PmsBarcodePrintService {

    @Autowired
    private PmsCompleteStorageService pmsCompleteStorageService;
    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private PmsProduceOrderService pmsProduceOrderService;
    @Autowired
    private RemoteWmsInventoryFeignApi remoteWmsInventoryFeignApi;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private TransactionTemplate transactionTemplate;
    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private RemoteSpareFeignApi remoteSpareFeignApi;
    @Autowired
    private PmsBarcodePrintRecordService pmsBarcodePrintRecordService;


    @Override
    public Boolean print(BarcodePrintDTO dto) {
        Integer printStrip = dto.getPrintStrip();
        List<PmsBarcodePrint> modelList = new ArrayList<>(printStrip);

        //生产订单
        PmsProduceOrder pmsProduceOrder = pmsProduceOrderService.getById(dto.getProduceOrderId());
        Assert.notNull(pmsProduceOrder, "生产订单不能为空");

        //加锁
        RLock lock = redissonClient.getLock(LockConstant.TABLE_PMS_BARCODE_PRINT);
        lock.lock();
        try {
            //获取当前订单最大的
            long currentMaxNo = newOrderMaxCaseNo(dto.getProduceOrderId(), dto.getClassesId(), dto.getClassGroupId(), dto.getProductionLine());
            //获取打印的订单流水
            PmsBarcodePrintRecord record = pmsBarcodePrintRecordService.getOne(new LambdaQueryWrapper<PmsBarcodePrintRecord>()
                    .eq(PmsBarcodePrintRecord::getClassesId, dto.getClassesId())
                    .eq(PmsBarcodePrintRecord::getClassGroupId, dto.getClassGroupId())
                    .eq(PmsBarcodePrintRecord::getProduceOrderId, dto.getProduceOrderId())
                    .eq(PmsBarcodePrintRecord::getPrintDate, LocalDate.now())
                    .orderByDesc(PmsBarcodePrintRecord::getSerialNum)
                    .last(MpConstant.STR_LIMIT_1));
            Integer num = 1;
            if (Objects.isNull(record)){
                PmsBarcodePrintRecord pmsBarcodePrintRecord = pmsBarcodePrintRecordService.getOne(new LambdaQueryWrapper<PmsBarcodePrintRecord>()
                        .eq(PmsBarcodePrintRecord::getClassesId, dto.getClassesId())
                        .eq(PmsBarcodePrintRecord::getClassGroupId, dto.getClassGroupId())
                        .eq(PmsBarcodePrintRecord::getPrintDate, LocalDate.now())
                        .orderByDesc(PmsBarcodePrintRecord::getSerialNum)
                        .last(MpConstant.STR_LIMIT_1));
                if (Objects.nonNull(pmsBarcodePrintRecord)){
                    num = pmsBarcodePrintRecord.getSerialNum()+1;
                }
                //增加一条记录
                PmsBarcodePrintRecord printRecord = new PmsBarcodePrintRecord();
                printRecord.setClassesId(dto.getClassesId());
                printRecord.setClassGroupId(dto.getClassGroupId());
                printRecord.setPrintDate(LocalDate.now());
                printRecord.setProduceOrderId(dto.getProduceOrderId());
                printRecord.setSerialNum(num);
                pmsBarcodePrintRecordService.save(printRecord);
            }else {
                num = record.getSerialNum();
            }
            List<Long> ids = Lists.newArrayList();
            ids.add(dto.getClassesId());
            ids.add(dto.getClassGroupId());
            Set<Integer> lineIds = new HashSet<>();
            lineIds.add(dto.getProductionLine());
            Map<Long, SysDictVO> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listById(ids), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            String viewCode = FeignOptional.of(() -> remoteOfficeFeign.listById(lineIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().get(dto.getProductionLine()).getViewCode();
            Integer classesCode = PmManagerConstant.classesType.getCodeByName(dictMap.get(dto.getClassesId()).getName());
            String classesGroupCode = PmManagerConstant.classGroupType.getCodeByName(dictMap.get(dto.getClassGroupId()).getName());

            //遍历打印条数
            for (int count = 0; count < printStrip; count++) {
                PmsBarcodePrint printModel = new PmsBarcodePrint();
                BeanUtils.copyProperties(dto, printModel);
                printModel.setCaseState(PmManagerConstant.CaseStateEnum.FINISH.getCode());
                printModel.setCaseNo(PmsCodeUtil.getNextCode(LocalDate.now() + PmsCodeUtil.SHORT_LINE + classesGroupCode + classesCode + PmsCodeUtil.SHORT_LINE + viewCode + PmsCodeUtil.SHORT_LINE + num + PmsCodeUtil.SHORT_LINE, PmsCodeUtil.NUM_BIT_2, currentMaxNo++));


                if (Objects.isNull(dto.getShelfId())) {
                    printModel.setShelfId(pmsProduceOrder.getDefaultShelfId());
                }
                if (Objects.isNull(dto.getWarehouseId())) {
                    printModel.setWarehouseId(pmsProduceOrder.getDefaultWarehouseId());
                }
                modelList.add(printModel);
            }
            transactionTemplate.execute(status -> {
                saveBatch(modelList);
                return Boolean.TRUE;
            });
        } finally {
            lock.unlock();
        }
        return Boolean.TRUE;
    }

    @Override
    public Long saveOneCase(BarcodePrintDTO dto) {
        //生产订单
        PmsProduceOrder pmsProduceOrder = pmsProduceOrderService.getById(dto.getProduceOrderId());
        Assert.notNull(pmsProduceOrder, "生产订单不能为空");
        PmsBarcodePrint printModel = new PmsBarcodePrint();
        //加锁
        RLock lock = redissonClient.getLock(LockConstant.TABLE_PMS_BARCODE_PRINT);
        lock.lock();
        try {
            //获取当前订单最大的
//            long currentMaxNo = orderMaxCaseNo(dto.getProduceOrderId());
            //获取当前订单最大的
            long currentMaxNo = newOrderMaxCaseNo(dto.getProduceOrderId(), dto.getClassesId(), dto.getClassGroupId(), dto.getProductionLine());
            //获取打印的订单流水
            PmsBarcodePrintRecord record = pmsBarcodePrintRecordService.getOne(new LambdaQueryWrapper<PmsBarcodePrintRecord>()
                    .eq(PmsBarcodePrintRecord::getClassesId, dto.getClassesId())
                    .eq(PmsBarcodePrintRecord::getClassGroupId, dto.getClassGroupId())
//                    .eq(PmsBarcodePrintRecord::getProduceOrderId, dto.getProduceOrderId())
                    .eq(PmsBarcodePrintRecord::getPrintDate, LocalDate.now())
                    .orderByDesc(PmsBarcodePrintRecord::getSerialNum)
                    .last(MpConstant.STR_LIMIT_1));
            Integer num = 01;
            if (Objects.isNull(record)) {
                //增加一条记录
                PmsBarcodePrintRecord printRecord = new PmsBarcodePrintRecord();
                printRecord.setClassesId(dto.getClassesId());
                printRecord.setClassGroupId(dto.getClassGroupId());
                printRecord.setPrintDate(LocalDate.now());
                printRecord.setProduceOrderId(dto.getProduceOrderId());
                printRecord.setSerialNum(num);
                pmsBarcodePrintRecordService.save(printRecord);
            } else {
                if (!dto.getProduceOrderId().equals(record.getProduceOrderId())) {
                    //增加一条记录
                    PmsBarcodePrintRecord printRecord = new PmsBarcodePrintRecord();
                    printRecord.setClassesId(dto.getClassesId());
                    printRecord.setClassGroupId(dto.getClassGroupId());
                    printRecord.setPrintDate(LocalDate.now());
                    printRecord.setProduceOrderId(dto.getProduceOrderId());
                    printRecord.setSerialNum(record.getSerialNum() + 1);
                    pmsBarcodePrintRecordService.save(printRecord);
                    num = printRecord.getSerialNum();
                } else {
                    num = record.getSerialNum();
                }

            }

            List<Long> ids = Lists.newArrayList();
            ids.add(dto.getClassesId());
            ids.add(dto.getClassGroupId());
            Map<Long, SysDictVO> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listById(ids), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            String viewCode = FeignOptional.of(() -> remoteOfficeFeign.listById(new HashSet<>(dto.getProductionLine())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get().get(dto.getProductionLine()).getViewCode();
            Integer classesCode = PmManagerConstant.classesType.getCodeByName(dictMap.get(dto.getClassesId()).getName());
            String classesGroupCode = PmManagerConstant.classGroupType.getCodeByName(dictMap.get(dto.getClassGroupId()).getName());
            //遍历打印条数
            BeanUtils.copyProperties(dto, printModel);
            printModel.setCaseState(PmManagerConstant.CaseStateEnum.FINISH.getCode());
//            printModel.setCaseNo(PmsCodeUtil.getNextCode(dto.getProduceOrderNo() + PmsCodeUtil.SHORT_LINE, PmsCodeUtil.NUM_BIT_4, currentMaxNo++));
            printModel.setCaseNo(PmsCodeUtil.getNextCode(LocalDate.now() + PmsCodeUtil.SHORT_LINE + classesGroupCode + classesCode + PmsCodeUtil.SHORT_LINE + viewCode + PmsCodeUtil.SHORT_LINE + num + PmsCodeUtil.SHORT_LINE, PmsCodeUtil.NUM_BIT_2, currentMaxNo++));
            if (Objects.isNull(dto.getShelfId())) {
                printModel.setShelfId(pmsProduceOrder.getDefaultShelfId());
            }
            if (Objects.isNull(dto.getWarehouseId())) {
                printModel.setWarehouseId(pmsProduceOrder.getDefaultWarehouseId());
            }
            transactionTemplate.execute(status -> {
                save(printModel);
                return Boolean.TRUE;
            });
        } finally {
            lock.unlock();
        }
        return printModel.getId();
    }

    @Override
    public Page<BarcodePrintRecordVO> printRecord(BarcodePrintRecordDTO dto) {
        Page<BarcodePrintRecordVO> resPage = new Page<>();
        Page<PmsBarcodePrint> page = page(new Page<>(dto.getCurrent(), dto.getSize()), new LambdaQueryWrapper<PmsBarcodePrint>()
                .eq(PmsBarcodePrint::getProduceOrderId, dto.getProduceOrderId())
                .eq(PmsBarcodePrint::getBatchNo, dto.getBatchNo())
                .eq(Objects.nonNull(dto.getCaseState()), PmsBarcodePrint::getCaseState, dto.getCaseState())
                .orderByDesc(PmsBarcodePrint::getCreateTime, PmsBarcodePrint::getCaseNo));
        BeanUtils.copyProperties(page, resPage);
        if (CollUtil.isNotEmpty(page.getRecords())) {
            List<BarcodePrintRecordVO> voList = BeanUtil.copyToList(page.getRecords(), BarcodePrintRecordVO.class);
            //处理单位数据
            Set<Long> orderSet = page.getRecords().stream().map(PmsBarcodePrint::getProduceOrderId).collect(Collectors.toSet());
            //箱号
            Set<String> caseNoSet = voList.stream().map(BarcodePrintRecordVO::getCaseNo).collect(Collectors.toSet());
            Map<Long, BaseSelectVO<Long>> orderUnitMap = pmsProduceOrderService.listUnit(orderSet);
            Map<Long, PmsProduceOrder> map = pmsProduceOrderService.orderList(Lists.newArrayList(orderSet));

            Map<String, TupleDTO<Integer, Integer>> caseCntMap = pmsCompleteStorageService.casePrintAndCnt(caseNoSet);

            voList.forEach(barcodePrintRecordVO -> {
                BaseSelectVO<Long> baseSelectVO = orderUnitMap.get(barcodePrintRecordVO.getProduceOrderId());
                if (Objects.nonNull(baseSelectVO)) {
                    barcodePrintRecordVO.setUnitId(baseSelectVO.getId());
                    barcodePrintRecordVO.setUnit(baseSelectVO.getName());
                }
                PmsProduceOrder pmsProduceOrder = map.get(barcodePrintRecordVO.getProduceOrderId());
                if (Objects.nonNull(pmsProduceOrder)) {
                    barcodePrintRecordVO.setMinPackageQuantity(pmsProduceOrder.getMinPackageQuantity());
                }
                TupleDTO<Integer, Integer> tupleDTO = caseCntMap.getOrDefault(barcodePrintRecordVO.getCaseNo(), new TupleDTO<>());
                barcodePrintRecordVO.setPrintFlag(tupleDTO.getField1());
                barcodePrintRecordVO.setPrintCount(tupleDTO.getField2());
            });
            resPage.setRecords(voList);
        }
        return resPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteBatch(List<Long> idList) {
        Assert.notEmpty(idList, "删除集合不能为空");
        List<PmsBarcodePrint> printList = listByIds(idList);
        Assert.notEmpty(printList, "删除的数据集合不能为空");
        Set<String> caseNoSet = printList.stream().map(PmsBarcodePrint::getCaseNo).collect(Collectors.toSet());
        Set<String> existInVouchSet = pmsCompleteStorageService.existInVouch(caseNoSet);
        if (CollUtil.isNotEmpty(existInVouchSet)) {
            throw new BusinessException("箱号：" + existInVouchSet + "已生成入库单");
        }
        return removeByIds(idList);
    }

    @Override
    public Map<String, PmsBarcodePrint> caseMap(Set<String> caseNoSet) {
        Map<String, PmsBarcodePrint> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(caseNoSet)) {
            List<PmsBarcodePrint> dbList = list(new LambdaQueryWrapper<PmsBarcodePrint>().in(PmsBarcodePrint::getCaseNo, caseNoSet));
            if (CollUtil.isNotEmpty(dbList)) {
                dbList.forEach(ele -> {
                    resMap.put(ele.getCaseNo(), ele);
                });
            }
        }
        return resMap;
    }

    @Override
    public BarcodePrintRecordVO info(Long id) {
        PmsBarcodePrint model = getById(id);
        Assert.notNull(model, "打印详细信息不存在");
        BarcodePrintRecordVO vo = new BarcodePrintRecordVO();
        BeanUtils.copyProperties(model, vo);
        Long produceOrderId = vo.getProduceOrderId();
        if (Objects.nonNull(produceOrderId)) {
            Map<Long, BaseSelectVO<Long>> orderUnitMap = pmsProduceOrderService.listUnit(Sets.newHashSet(produceOrderId));
            if (CollUtil.isNotEmpty(orderUnitMap)) {
                BaseSelectVO<Long> baseSelectVO = orderUnitMap.get(produceOrderId);
                vo.setUnitId(baseSelectVO.getId());
                vo.setUnit(baseSelectVO.getName());
            }
        }
        return vo;
    }

    @Override
    public Map<String, PmsCompleteStorageNumDTO> batchSum(Set<String> batchNoSet) {
        Map<String, PmsCompleteStorageNumDTO> resMap = Maps.newHashMap();
        if (CollUtil.isEmpty(batchNoSet)) {
            return resMap;
        }
        List<PmsBarcodePrint> dbList = list(new QueryWrapper<PmsBarcodePrint>()
                .select("batch_no , sum(quantity) as quantity , any_value(unit_id) as unit_id, any_value(produce_order_id) as produce_order_id").lambda()
                .in(PmsBarcodePrint::getBatchNo, batchNoSet)
                .groupBy(PmsBarcodePrint::getBatchNo));
        if (CollUtil.isNotEmpty(dbList)) {
            for (PmsBarcodePrint pmsBarcodePrint : dbList) {
                PmsCompleteStorageNumDTO numDTO = new PmsCompleteStorageNumDTO();
                numDTO.setQuantity(pmsBarcodePrint.getQuantity());
                numDTO.setUnitId(pmsBarcodePrint.getUnitId());
                numDTO.setProduceOrderId(pmsBarcodePrint.getProduceOrderId());
                resMap.put(pmsBarcodePrint.getBatchNo(), numDTO);
            }
        }
        return resMap;
    }

    @Override
    public Map<Long, PmsCompleteStorageNumDTO> orderSum(Set<Long> orderSet) {
        Map<Long, PmsCompleteStorageNumDTO> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(orderSet)) {
            List<PmsBarcodePrint> dbList = list(new QueryWrapper<PmsBarcodePrint>()
                    .select("produce_order_id , sum(quantity) as quantity , any_value(unit_id) as unit_id ").lambda()
                    .in(PmsBarcodePrint::getProduceOrderId, orderSet)
                    .groupBy(PmsBarcodePrint::getProduceOrderId));
            if (CollUtil.isNotEmpty(dbList)) {
                for (PmsBarcodePrint pmsBarcodePrint : dbList) {
                    PmsCompleteStorageNumDTO numDTO = new PmsCompleteStorageNumDTO();
                    numDTO.setQuantity(pmsBarcodePrint.getQuantity());
                    numDTO.setUnitId(pmsBarcodePrint.getUnitId());
                    numDTO.setProduceOrderId(pmsBarcodePrint.getProduceOrderId());
                    resMap.put(pmsBarcodePrint.getProduceOrderId(), numDTO);
                }
            }
        }
        return resMap;
    }

    @Override
    public Boolean reviseAlreadyPutInStatue(List<String> caseNoList, Integer code) {
        if (CollUtil.isNotEmpty(caseNoList)) {
            update(new LambdaUpdateWrapper<PmsBarcodePrint>()
                    .set(PmsBarcodePrint::getCaseState, code)
                    .in(PmsBarcodePrint::getCaseNo, caseNoList));
        }
        return Boolean.TRUE;
    }

    @Override
    public Map<String, CaseSiteDTO> caseSite(Set<String> caseNoSet) {
        Map<String, CaseSiteDTO> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(caseNoSet)) {
            Map<String, CaseSiteDTO> caseSiteDTOMap = FeignOptional.of(() -> remoteWmsInventoryFeignApi.caseSite(caseNoSet), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
            if (CollUtil.isNotEmpty(caseSiteDTOMap)) {
                Set<Integer> siteSet = Sets.newHashSet();
                for (Map.Entry<String, CaseSiteDTO> entry : caseSiteDTOMap.entrySet()) {
                    siteSet.add(entry.getValue().getShelfId());
                    siteSet.add(entry.getValue().getWarehouseId());
                }
                Map<Integer, SysOfficeVO> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(siteSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                SysOfficeVO defaultSysOfficeVO = new SysOfficeVO();
                for (Map.Entry<String, CaseSiteDTO> entry : caseSiteDTOMap.entrySet()) {
                    CaseSiteDTO value = entry.getValue();
                    SysOfficeVO warehouse = sysOfficeMap.getOrDefault(value.getWarehouseId(), defaultSysOfficeVO);
                    value.setWarehouseId(warehouse.getId());
                    value.setWarehouseName(warehouse.getOfficeName());
                    SysOfficeVO shelf = sysOfficeMap.getOrDefault(value.getShelfId(), defaultSysOfficeVO);
                    value.setShelfId(shelf.getId());
                    value.setShelfName(shelf.getOfficeName());
                }
                resMap = caseSiteDTOMap;
            }
        }
        return resMap;
    }

    @Override
    public List<WorkOrderMaterialVO> getMaterialByCaseNo(String code) {
        List<WorkOrderMaterialVO> list = Lists.newArrayList();
        List<PmsBarcodePrint> printList = list(new LambdaQueryWrapper<PmsBarcodePrint>().like(PmsBarcodePrint::getCaseNo, code));
        if (CollUtil.isNotEmpty(printList)) {
            List<Long> materialIds = printList.stream().map(PmsBarcodePrint::getMaterialId).collect(Collectors.toList());
            Map<Long, EqpSpareVO> map = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            printList.forEach(pmsBarcodePrint -> {
                WorkOrderMaterialVO workOrderMaterialVO = new WorkOrderMaterialVO();
                workOrderMaterialVO.setMaterialId(pmsBarcodePrint.getMaterialId());
                workOrderMaterialVO.setMaterialCode(map.get(pmsBarcodePrint.getMaterialId()).getCode());
                workOrderMaterialVO.setMaterialName(map.get(pmsBarcodePrint.getMaterialId()).getName());
                workOrderMaterialVO.setBatchNo(pmsBarcodePrint.getBatchNo());
                workOrderMaterialVO.setCaseNo(pmsBarcodePrint.getCaseNo());
                list.add(workOrderMaterialVO);
            });
        }
        return list;
    }

    @Override
    public List<WorkOrderMaterialVO> getMaterialByBatchNo(String batchNo) {
        List<WorkOrderMaterialVO> list = Lists.newArrayList();
        List<PmsBarcodePrint> printList = list(new LambdaQueryWrapper<PmsBarcodePrint>().like(PmsBarcodePrint::getBatchNo, batchNo));
        if (CollUtil.isNotEmpty(printList)) {
            List<Long> materialIds = printList.stream().map(PmsBarcodePrint::getMaterialId).collect(Collectors.toList());
            Map<Long, EqpSpareVO> map = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            printList.forEach(pmsBarcodePrint -> {
                WorkOrderMaterialVO workOrderMaterialVO = new WorkOrderMaterialVO();
                workOrderMaterialVO.setMaterialId(pmsBarcodePrint.getMaterialId());
                workOrderMaterialVO.setMaterialCode(map.get(pmsBarcodePrint.getMaterialId()).getCode());
                workOrderMaterialVO.setMaterialName(map.get(pmsBarcodePrint.getMaterialId()).getName());
                workOrderMaterialVO.setBatchNo(pmsBarcodePrint.getBatchNo());
                list.add(workOrderMaterialVO);
            });
        }
        return list;
    }

    /**
     * 生产订单最大箱号
     *
     * @param produceOrderId 生产订单id
     * @return 最大箱号
     */
    private long orderMaxCaseNo(Long produceOrderId) {
        PmsBarcodePrint one = getOne(new LambdaQueryWrapper<PmsBarcodePrint>()
                .eq(PmsBarcodePrint::getProduceOrderId, produceOrderId)
                .orderByDesc(PmsBarcodePrint::getCaseNo)
                .last(MpConstant.STR_LIMIT_1));
        long currentMax = PmsCodeUtil.DEFAULT_PADDING;
        if (Objects.nonNull(one)) {
            String substring = one.getCaseNo().substring(one.getCaseNo().length() - PmsCodeUtil.NUM_BIT_4);
            try {
                currentMax = Long.parseLong(substring);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }


    /**
     * 生产订单最大箱号
     *
     * @param produceOrderId 生产订单id
     * @return 最大箱号
     */
    private long newOrderMaxCaseNo(Long produceOrderId, Long classesId, Long classGroupId, Integer productionLine) {
        PmsBarcodePrint one = getOne(new LambdaQueryWrapper<PmsBarcodePrint>()
                .eq(PmsBarcodePrint::getProduceOrderId, produceOrderId)
                .eq(PmsBarcodePrint::getClassesId, classesId)
                .eq(PmsBarcodePrint::getClassGroupId, classGroupId)
                .eq(PmsBarcodePrint::getProductionLine, productionLine)
                .orderByDesc(PmsBarcodePrint::getCaseNo)
                .last(MpConstant.STR_LIMIT_1));
        long currentMax = PmsCodeUtil.DEFAULT_PADDING;
        if (Objects.nonNull(one)) {
            String substring = one.getCaseNo().substring(one.getCaseNo().length() - PmsCodeUtil.NUM_BIT_2);
            try {
                currentMax = Long.parseLong(substring);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }

}




