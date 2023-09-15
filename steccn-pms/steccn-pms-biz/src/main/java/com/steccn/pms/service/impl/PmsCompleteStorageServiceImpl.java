package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.ClassUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.*;
import com.steccn.pms.dto.*;
import com.steccn.pms.mapper.PmsCompleteStorageMapper;
import com.steccn.pms.service.*;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.CompleteStorageListVO;
import com.steccn.pms.vo.CompleteStorageMaterialVO;
import com.steccn.pms.vo.CompleteStoragePageVO;
import com.steccn.pms.vo.PmsCompleteStorageVO;
import com.steccn.wms.dto.WmsDeliveryReceiptDTO;
import com.steccn.wms.dto.WmsStorageReceiptDTO;
import com.steccn.wms.dto.WmsStorageReceiptInfoDTO;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import com.steccn.wms.feign.RemoteWmsShelfStrategyFeignApi;
import com.steccn.wms.feign.RemoteWmsStorageReceiptFeignApi;
import com.steccn.wms.vo.WmsMaterialShelfUpVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yequan
 * @description 针对表【pms_complete_storage(生产订单-完工入库)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
@Slf4j
public class PmsCompleteStorageServiceImpl extends ServiceImpl<PmsCompleteStorageMapper, PmsCompleteStorage>
        implements PmsCompleteStorageService {
    @Autowired
    private PmsCompleteStorageInfoService completeStorageInfoService;
    @Autowired
    private PmsBarcodePrintService pmsBarcodePrintService;
    @Autowired
    private PmsProduceOrderService pmsProduceOrderService;
    @Autowired
    private PmsWorkOrderBatchService pmsWorkOrderBatchService;
    @Autowired
    private RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private RemoteSpareFeignApi remoteSpareFeignApi;
    @Autowired
    private RemoteWmsInventoryFeignApi remoteWmsInventoryFeignApi;
    @Autowired
    private RemoteWmsShelfStrategyFeignApi remoteWmsShelfStrategyFeignApi;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RemoteWmsStorageReceiptFeignApi remoteWmsStorageReceiptFeignApi;

    @Autowired
    private PmsCompleteDeliveryService pmsCompleteDeliveryService;

    @Override
    public CompleteStorageScanDTO scan(Integer warehouseId, String caseNo) {
        //1.判断装箱单（PP-XXXXX）已扫描，入库单为SI-XXXXX
        PmsCompleteStorageInfo pmsCompleteStorageInfo = completeStorageInfoService.getOne(new LambdaQueryWrapper<PmsCompleteStorageInfo>()
                .eq(PmsCompleteStorageInfo::getCaseNo, caseNo)
                .last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(pmsCompleteStorageInfo)) {
            PmsCompleteStorage pmsCompleteStorage = getById(pmsCompleteStorageInfo.getCompleteStorageId());
            throw new BusinessException("装箱单" + caseNo + "已扫描，入库单为" + pmsCompleteStorage.getCode());
        }
        //2.根据箱号查询箱信息
        PmsBarcodePrint pmsBarcodePrint = pmsBarcodePrintService.getOne(new LambdaQueryWrapper<PmsBarcodePrint>()
                .eq(PmsBarcodePrint::getCaseNo, caseNo)
                .last(MpConstant.STR_LIMIT_1));
        Assert.notNull(pmsBarcodePrint, "箱号不存在");

        //3.存放生产订单中
        PmsProduceOrder pmsProduceOrder = pmsProduceOrderService.getById(pmsBarcodePrint.getProduceOrderId());
        Assert.notNull(pmsProduceOrder, "生产订单不能为空");

        HashSet<Long> set = Sets.newHashSet(pmsProduceOrder.getMaterialId());
        Map<Long, WmsMaterialShelfUpVO> materialShelfUpMap = FeignOptional.of(() -> remoteWmsShelfStrategyFeignApi.materialShelfUpInfo(set), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        WmsMaterialShelfUpVO wmsMaterialShelfUpVO = new WmsMaterialShelfUpVO();
        WmsMaterialShelfUpVO orDefault = materialShelfUpMap.getOrDefault(pmsProduceOrder.getMaterialId(), wmsMaterialShelfUpVO);
        CompleteStorageScanDTO resDTO = new CompleteStorageScanDTO();
        BeanUtils.copyProperties(pmsBarcodePrint, resDTO);
        //数据字典
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(Sets.newHashSet(resDTO.getUnitId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        resDTO.setUnitName(dictMap.get(resDTO.getUnitId()));
        resDTO.setProduceOrderNo(pmsProduceOrder.getCode());
        resDTO.setWarehouseId(pmsProduceOrder.getDefaultWarehouseId());
        resDTO.setShelfId(pmsProduceOrder.getDefaultShelfId());
        //4.仓库和货架信息（调整为通过上架策略获取货架）
        if (warehouseId.equals(orDefault.getWarehouseId())) {
            resDTO.setWarehouseId(orDefault.getWarehouseId());
            resDTO.setShelfId(orDefault.getShelfId());
            resDTO.setWarehouseName(orDefault.getWarehouseName());
            resDTO.setShelfName(orDefault.getShelfName());
        }
        //5.箱状态
        resDTO.setCaseState(pmsBarcodePrint.getCaseState());
        //6.赋值主物料信息
        resDTO.setMaterialId(pmsProduceOrder.getMaterialId());
        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(Lists.newArrayList(pmsProduceOrder.getMaterialId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        EqpSpareVO materialVo = materialMap.getOrDefault(pmsProduceOrder.getMaterialId(), new EqpSpareVO());
        resDTO.setMaterialCode(materialVo.getCode());
        resDTO.setMaterialName(materialVo.getName());
        return resDTO;
    }

    @Override
    public String generateInfo(List<CompleteStorageScanDTO> dtoList) {
        String code = generate(dtoList);
        //调用确认入库单接口 2023-0915 去除自动确认逻辑
//        PmsCompleteStorage pmsCompleteStorage = getOne(new LambdaQueryWrapper<PmsCompleteStorage>().eq(PmsCompleteStorage::getCode, code), false);
//        Assert.notNull(pmsCompleteStorage, "入库单为空");
//        WmsStorageReceiptDTO receiptDTO = new WmsStorageReceiptDTO();
//        receiptDTO.setCode(pmsCompleteStorage.getInStorageVoucher());
//        FeignOptional.of(() -> remoteWmsInventoryFeignApi.affirm(receiptDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        return code;
    }

    /**
     * 生成入库单
     * @param dtoList
     * @return
     */
    private String generate(List<CompleteStorageScanDTO> dtoList) {
        Assert.notEmpty(dtoList, "请选择箱子");
        //分布式锁 ，保证最大入库单号
        //加锁
        PmsCompleteStorage pmsCompleteStorage = new PmsCompleteStorage();
        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(SysDictConstant.CODE_0, SysDictConstant.TYPE_WAREHOUSING_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        RLock lock = redissonClient.getLock(LockConstant.TABLE_PMS_COMPLETE_STORAGE);
        lock.lock();
        try {
            //校验一箱货只能存在于一个入库单中
            Set<String> caseNoSet = dtoList.stream().map(CompleteStorageScanDTO::getCaseNo).collect(Collectors.toSet());
            //校验重复扫码问题
            Assert.isTrue(caseNoSet.size() == dtoList.size(), "存在重复扫码，请检查");
            Assert.isFalse(completeStorageInfoService.existCaseNo(sysDictVO.getId(), caseNoSet), "部分箱已生成入库单了");
            //校验生产订单的产线是否相同不同，不可以创建入库单
            Set<Long> orderSet = dtoList.stream().map(CompleteStorageScanDTO::getProduceOrderId).collect(Collectors.toSet());
            List<PmsProduceOrder> list = pmsProduceOrderService.list(new LambdaQueryWrapper<PmsProduceOrder>()
                    .in(PmsProduceOrder::getId, orderSet)
                    .groupBy(PmsProduceOrder::getProductionLineId));
            if (!(CollUtil.isNotEmpty(list) && list.size() == 1)) {
                throw new BusinessException("不同产线的订单不可以一起生成入库单");
            }
            //校验箱子不能重复出现在不同的入库单
            List<PmsCompleteStorageInfo> modelInfoList = new ArrayList<>(dtoList.size());
            //创建主表
            BigDecimal sumQuantity = BigDecimal.ZERO;
            pmsCompleteStorage.setId(IdUtil.getSnowflake().nextId());
            pmsCompleteStorage.setUnitId(dtoList.get(0).getUnitId());

            Long produceOrderId = dtoList.get(0).getProduceOrderId();
            LocalDateTime enterTime = dtoList.get(0).getEnterTime();
            Integer classId = LocalDateUtil.getClassId(enterTime, Boolean.TRUE);
            //根据班次id查询数据字典班次id
            SysDictDTO sysDictDTO = new SysDictDTO();
            sysDictDTO.setCode(String.valueOf(classId));
            sysDictDTO.setType(PmManagerConstant.SYS_SHIFT);
            Map<String, SysDictVO> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByCode(sysDictDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO classIdSysDict = dictMap.get(sysDictDTO.getCode());
            Assert.notNull(classIdSysDict,"班次不存在");

            PmsProduceOrder pmsProduceOrder = pmsProduceOrderService.getById(produceOrderId);
            Assert.notNull(pmsProduceOrder, "生产订单不存在");
            pmsCompleteStorage.setProductLineId(pmsProduceOrder.getProductionLineId());
            List<WmsStorageReceiptInfoDTO> materialList = Lists.newArrayList();

            Set<Integer> storageSet = Sets.newHashSet();
            for (CompleteStorageScanDTO caseInfo : dtoList) {
                storageSet.add(caseInfo.getWarehouseId());
                storageSet.add(caseInfo.getShelfId());
            }
            Map<Integer, SysOfficeVO> sysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listById(storageSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysOfficeVO defaultSysOfficeVO = new SysOfficeVO();

            //获取箱信息
            Map<String, PmsBarcodePrint> caseMap = pmsBarcodePrintService.caseMap(caseNoSet);
            PmsBarcodePrint defaultPmsBarcodePrint = new PmsBarcodePrint();

            WmsStorageReceiptDTO wmsStorageReceiptDTO = new WmsStorageReceiptDTO();

            for (CompleteStorageScanDTO caseInfo : dtoList) {
                PmsCompleteStorageInfo modelInfo = new PmsCompleteStorageInfo();
                BeanUtils.copyProperties(caseInfo, modelInfo);
                modelInfo.setCompleteStorageId(pmsCompleteStorage.getId());
                modelInfo.setPackTime(caseInfo.getCreateTime());
                sumQuantity = sumQuantity.add(caseInfo.getQuantity());

                PmsBarcodePrint pmsBarcodePrint = caseMap.getOrDefault(modelInfo.getCaseNo(), defaultPmsBarcodePrint);
                modelInfo.setClassesId(pmsBarcodePrint.getClassesId());
                modelInfo.setClassGroupId(pmsBarcodePrint.getClassGroupId());
                modelInfo.setClassesId(classIdSysDict.getId());
                modelInfoList.add(modelInfo);

                WmsStorageReceiptInfoDTO wmsStorageReceiptInfoDTO = new WmsStorageReceiptInfoDTO();
                BeanUtils.copyProperties(caseInfo, wmsStorageReceiptInfoDTO);
                wmsStorageReceiptInfoDTO.setQuantity(caseInfo.getQuantity());
                wmsStorageReceiptInfoDTO.setUnit(caseInfo.getUnitId());
                wmsStorageReceiptInfoDTO.setShelfCode(sysOfficeVOMap.getOrDefault(caseInfo.getShelfId(), defaultSysOfficeVO).getOfficeCode());
                wmsStorageReceiptInfoDTO.setBatch("0");
                wmsStorageReceiptDTO.setWarehouseId(caseInfo.getWarehouseId());
                wmsStorageReceiptDTO.setWarehouseName(sysOfficeVOMap.getOrDefault(caseInfo.getWarehouseId(), defaultSysOfficeVO).getOfficeName());
                materialList.add(wmsStorageReceiptInfoDTO);
            }
            completeStorageInfoService.saveBatch(modelInfoList);
            //生成code 规则：SI-20220905-0001
//            pmsCompleteStorage.setCode(maxCompleteCode());
            pmsCompleteStorage.setQuantity(sumQuantity);
//            pmsCompleteStorage.setInStorageVoucher(pmsCompleteStorage.getCode());
            pmsCompleteStorage.setTypeId(sysDictVO.getId());
            pmsCompleteStorage.setState(PmManagerConstant.InVouchEnum.CREATE.getCode());
            //连线入库 入库时间
            pmsCompleteStorage.setEnterTime(enterTime);

            //生成入库单

            wmsStorageReceiptDTO.setType(sysDictVO.getId());
            //仓库id
            wmsStorageReceiptDTO.setList(materialList);
            wmsStorageReceiptDTO.setSourceType(PmManagerConstant.OrderStateEnum.ZERO.getCode());
            wmsStorageReceiptDTO.setCreateName(SecurityUtils.getUser().getName());
            wmsStorageReceiptDTO.setCreateBy(SecurityUtils.getUser().getUserId());
            try {
                String code = FeignOptional.of(() -> remoteWmsInventoryFeignApi.generateInStorageVoucher(wmsStorageReceiptDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
                //入库必定要成功
                if (StringUtils.isEmpty(code)) {
                    log.info("入库单 -> 失败");
                }
                pmsCompleteStorage.setCode(code);
                pmsCompleteStorage.setInStorageVoucher(pmsCompleteStorage.getCode());
                save(pmsCompleteStorage);
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
                throw new BusinessException("入库单创建失败");
            }
            //箱状态修改成待入库
            pmsBarcodePrintService.reviseAlreadyPutInStatue(Lists.newArrayList(caseNoSet), PmManagerConstant.CaseStateEnum.PREPARE.getCode());
        }catch (IllegalArgumentException e){
            throw new BusinessException(e.getLocalizedMessage());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            throw new BusinessException("入库单创建失败");
        } finally {
            lock.unlock();
        }
        return pmsCompleteStorage.getCode();
    }

    @Override
    public Page<CompleteStoragePageVO> pageList(CompleteStoragePageDTO dto) {
        //定义返回page对象
        Page<CompleteStoragePageVO> resPage = new Page<>();
        //根据条件获取筛选数据
        LambdaQueryWrapper<PmsCompleteStorage> lqw = new LambdaQueryWrapper<PmsCompleteStorage>()
                .eq(PmsCompleteStorage::getProductLineId, dto.getProductLineId());
        if (Objects.nonNull(dto.getStart())) {
            lqw.ge(PmsCompleteStorage::getCreateTime, LocalDateTime.of(dto.getStart(), LocalTime.MIN));
        }
        if (Objects.nonNull(dto.getEnd())) {
            lqw.le(PmsCompleteStorage::getCreateTime, LocalDateTime.of(dto.getEnd(), LocalTime.MAX));
        }
        Page<PmsCompleteStorage> pageList = page(new Page<>(dto.getCurrent(), dto.getSize()), lqw);

        //拷贝分页对象
        BeanUtils.copyProperties(pageList, resPage);
        //如果查询结果不为空，拷贝返回的数据
        if (CollUtil.isNotEmpty(pageList.getRecords())) {
            List<PmsCompleteStorage> modelList = pageList.getRecords();
            Set<Long> dictSet = Sets.newHashSet();
            for (PmsCompleteStorage storage : modelList) {
                dictSet.add(storage.getUnitId());
                dictSet.add(storage.getTypeId());
            }
            Map<Long, String> sysDictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            List<CompleteStoragePageVO> voList = new ArrayList<>(modelList.size());

            for (PmsCompleteStorage model : modelList) {
                CompleteStoragePageVO vo = new CompleteStoragePageVO();
                BeanUtils.copyProperties(model, vo);
                vo.setUnitName(sysDictMap.get(model.getUnitId()));
                vo.setTypeName(sysDictMap.get(vo.getTypeId()));
                voList.add(vo);
            }
            resPage.setRecords(voList);
        }
        return resPage;
    }

    @Override
    public CompleteStorageListVO info(Long id) {
        //查询入库单信息
        PmsCompleteStorage pmsCompleteStorage = getById(id);
        Assert.notNull(pmsCompleteStorage, "未查询到入库单");
        CompleteStorageListVO completeStorageListVO = new CompleteStorageListVO();
        BeanUtils.copyProperties(pmsCompleteStorage, completeStorageListVO);
        //查询所有的箱信息
        List<PmsCompleteStorageInfo> infoList = completeStorageInfoService.list(new LambdaQueryWrapper<PmsCompleteStorageInfo>().eq(PmsCompleteStorageInfo::getCompleteStorageId, id));

        //定义字典集合和箱号集合
        Set<Long> dictList = Sets.newHashSet();
        Set<String> caseNoList = Sets.newHashSet();
        Set<Long> orderSet = Sets.newHashSet();
        for (PmsCompleteStorageInfo info : infoList) {
            dictList.add(info.getUnitId());
            caseNoList.add(info.getCaseNo());
            orderSet.add(info.getProduceOrderId());
        }

        dictList.add(pmsCompleteStorage.getUnitId());
        dictList.add(pmsCompleteStorage.getTypeId());
        Map<Long, String> sysDictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        Map<String, PmsBarcodePrint> barcodePrintMap = pmsBarcodePrintService.caseMap(caseNoList);
        PmsBarcodePrint defaultPmsBarcodePrint = new PmsBarcodePrint();
        Set<Integer> storageSet = Sets.newHashSet();
        for (Map.Entry<String, PmsBarcodePrint> entry : barcodePrintMap.entrySet()) {
            PmsBarcodePrint pbp = entry.getValue();
            storageSet.add(pbp.getWarehouseId());
            storageSet.add(pbp.getShelfId());
        }
        Map<Integer, String> sysOfficeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(storageSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        List<PmsProduceOrder> pmsProduceOrders = pmsProduceOrderService.listByIds(orderSet);
        Map<Long, PmsProduceOrder> resMap = Maps.newHashMap();
        PmsProduceOrder defaultPmsProduceOrder = new PmsProduceOrder();
        if (CollUtil.isNotEmpty(pmsProduceOrders)) {
            for (PmsProduceOrder pmsProduceOrder : pmsProduceOrders) {
                resMap.put(pmsProduceOrder.getId(), pmsProduceOrder);
            }
        }
        List<CompleteStorageMaterialVO> materialVOList = new ArrayList<>(infoList.size());
        for (PmsCompleteStorageInfo info : infoList) {
            CompleteStorageMaterialVO materialVO = new CompleteStorageMaterialVO();
            BeanUtils.copyProperties(info, materialVO);
            PmsProduceOrder produceOrder = resMap.getOrDefault(info.getProduceOrderId(), defaultPmsProduceOrder);
            materialVO.setProduceOrderId(produceOrder.getId());
            materialVO.setProduceOrderNo(produceOrder.getCode());
            materialVO.setMaterialId(produceOrder.getMaterialId());
            materialVO.setMaterialCode(produceOrder.getMaterialCode());
            materialVO.setMaterialName(produceOrder.getMaterialName());
            materialVO.setUnitName(sysDictMap.get(materialVO.getUnitId()));
            materialVO.setWarehouseId(barcodePrintMap.getOrDefault(info.getCaseNo(), defaultPmsBarcodePrint).getWarehouseId());
            materialVO.setWarehouseName(sysOfficeMap.get(materialVO.getWarehouseId()));
            materialVO.setShelfId(barcodePrintMap.getOrDefault(info.getCaseNo(), defaultPmsBarcodePrint).getShelfId());
            materialVO.setShelfName(sysOfficeMap.get(materialVO.getShelfId()));
            materialVOList.add(materialVO);
        }
        completeStorageListVO.setList(materialVOList);
        completeStorageListVO.setUnitName(sysDictMap.get(completeStorageListVO.getUnitId()));
        completeStorageListVO.setTypeName(sysDictMap.get(completeStorageListVO.getTypeId()));
        return completeStorageListVO;
    }

    @Override
    public Page<PmsCompleteStorageVO> completeStoragePageList(PmsCompleteStorageDTO dto) {
        //1.查询分页数据
        Page<PmsCompleteStorageVO> page = completeStorageInfoService.pageByOrderId(dto);
        //2.处理数据
        List<Long> ids = Lists.newArrayList();
        Set<String> warehouseCodes = Sets.newHashSet();
        List<PmsCompleteStorageVO> records = page.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            records.forEach(pmsCompleteStorageVO -> {
                ids.add(pmsCompleteStorageVO.getUnitId());
                warehouseCodes.add(pmsCompleteStorageVO.getCaseNo());
            });
            //获取单位信息&仓库信息
            Map<Long, SysDictVO> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listById(ids), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取仓库以及货架信息
            Map<String, CaseSiteDTO> caseSiteDTOMap = pmsBarcodePrintService.caseSite(warehouseCodes);
            CaseSiteDTO defaultCaseSiteDTO = new CaseSiteDTO();
            records.forEach(pmsCompleteStorageVO -> {
                CaseSiteDTO caseSiteDTO = caseSiteDTOMap.getOrDefault(pmsCompleteStorageVO.getCaseNo(), defaultCaseSiteDTO);
                pmsCompleteStorageVO.setUnit(dictMap.get(pmsCompleteStorageVO.getUnitId()).getName());
                pmsCompleteStorageVO.setWarehouseName(caseSiteDTO.getWarehouseName());
                pmsCompleteStorageVO.setShelfName(caseSiteDTO.getShelfName());
                pmsCompleteStorageVO.setWarehouseId(caseSiteDTO.getWarehouseId());
                pmsCompleteStorageVO.setShelfId(caseSiteDTO.getShelfId());
            });
        }
        return page;
    }

    @Override
    public Set<String> existInVouch(Set<String> caseNoSet) {
        Set<String> resSet = Sets.newHashSet();
        List<PmsCompleteStorageInfo> dbList = completeStorageInfoService.list(new LambdaQueryWrapper<PmsCompleteStorageInfo>()
                .select(PmsCompleteStorageInfo::getCaseNo)
                .in(PmsCompleteStorageInfo::getCaseNo, caseNoSet));
        if (CollUtil.isNotEmpty(dbList)) {
            resSet = dbList.stream().map(PmsCompleteStorageInfo::getCaseNo).collect(Collectors.toSet());
        }
        return resSet;
    }

    /**
     * 根据入库单编码找到入库凭证修改完成状态
     *
     * @param code
     * @return
     */
    @Override
    public R<Boolean> completeStorageReviseStatue(String code, String confirmName) {
        if (Objects.isNull(code) || Objects.isNull(confirmName)) {
            throw new BusinessException("确认用户名或者入库单编码不能为空");
        }
        //根据入库单编号查找入库单
        PmsCompleteStorage getOneCompleteStorage = getOne(new LambdaQueryWrapper<PmsCompleteStorage>().eq(PmsCompleteStorage::getCode, code));
        if (Objects.isNull(getOneCompleteStorage)) {
            throw new BusinessException("未找到此入库单编码");
        }
        if (getOneCompleteStorage.getCode().equals(getOneCompleteStorage.getInStorageVoucher())) {
            getOneCompleteStorage.setState(PmManagerConstant.InVouchEnum.FINISH.getCode());
            getOneCompleteStorage.setInStorageTime(LocalDateTime.now());
            getOneCompleteStorage.setConfirmName(confirmName);
            //更新生产订单完成入库数据表
            updateById(getOneCompleteStorage);
        }
        List<String> caseNoList = completeStorageInfoService.getCaseNo(getOneCompleteStorage.getId());
        pmsBarcodePrintService.reviseAlreadyPutInStatue(caseNoList, PmManagerConstant.CaseStateEnum.ALREADY.getCode());
        return R.success(true);
    }

    @Override
    public String connectLine(ConnectLineStorageDTO dto) {
        //先校验生产订单是否存在
        PmsProduceOrder pmsProduceOrder = pmsProduceOrderService.getById(dto.getProduceOrderId());
        Assert.notNull(pmsProduceOrder, "生产订单不存在");
        //校验批次是否存在，且是当前订单的当前批次
        PmsWorkOrderBatch pmsWorkOrderBatch = pmsWorkOrderBatchService.getById(dto.getBatch());
        Assert.isTrue(pmsProduceOrder.getId().equals(pmsWorkOrderBatch.getProduceOrderId()) && MpConstant.YES.equals(pmsWorkOrderBatch.getCurrentBatchFlag()), "批次信息异常");
        //根据货架查询仓库
        Map<Integer, SysOfficeVO> shelfMap = FeignOptional.of(() -> remoteOfficeFeign.getParentOfficeMap(Lists.newArrayList(dto.getShelfId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysOfficeVO shelf = shelfMap.get(dto.getShelfId());
        Assert.notNull(shelf, "仓库不存在");
        //生成箱信息
        BarcodePrintDTO barcodePrint = new BarcodePrintDTO();
        BeanUtils.copyProperties(dto, barcodePrint);
        barcodePrint.setMaterialId(pmsProduceOrder.getMaterialId());
        barcodePrint.setBatchNo(pmsWorkOrderBatch.getCode());
        barcodePrint.setPrintNumber(1);
        barcodePrint.setPrintStrip(1);
        barcodePrint.setProduceOrderNo(pmsProduceOrder.getCode());
        barcodePrint.setProductionLine(pmsProduceOrder.getProductionLineId());
        barcodePrint.setWarehouseId(shelf.getParentId());
        Long caseId = pmsBarcodePrintService.saveOneCase(barcodePrint);
        PmsBarcodePrint pmsBarcodePrint = pmsBarcodePrintService.getById(caseId);
        //生成入库单信息
        List<CompleteStorageScanDTO> scanDtoList = Lists.newArrayList();
        CompleteStorageScanDTO scanDTO = new CompleteStorageScanDTO();
        BeanUtils.copyProperties(barcodePrint, scanDTO);
        scanDTO.setCaseNo(pmsBarcodePrint.getCaseNo());
        scanDTO.setCaseState(pmsBarcodePrint.getCaseState());
        scanDTO.setCreateTime(LocalDateTime.now());
        scanDTO.setMaterialCode(pmsProduceOrder.getMaterialCode());
        scanDTO.setMaterialName(pmsProduceOrder.getMaterialName());
        scanDTO.setWarehouseName(shelf.getParentName());
        scanDTO.setShelfName(shelf.getOfficeName());
        //入库时间
        scanDTO.setEnterTime(dto.getEnterTime());
        scanDtoList.add(scanDTO);
        String generate = generate(scanDtoList);
        //调用确认入库单接口
        PmsCompleteStorage pmsCompleteStorage = getOne(new LambdaQueryWrapper<PmsCompleteStorage>().eq(PmsCompleteStorage::getCode, generate), false);
        Assert.notNull(pmsCompleteStorage, "入库单为空");
        WmsStorageReceiptDTO receiptDTO = new WmsStorageReceiptDTO();
        receiptDTO.setCode(pmsCompleteStorage.getInStorageVoucher());
        FeignOptional.of(() -> remoteWmsInventoryFeignApi.affirm(receiptDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        return generate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateByDelivery(ProcessReportWorkSubmitDTO dto, List<PmsCompleteDeliveryInfo> infoList) {
        if (Objects.isNull(dto) || CollUtil.isEmpty(infoList)) {
            return null;
        }
        //分布式锁 ，保证最大入库单号
        //加锁
        RLock lock = redissonClient.getLock(LockConstant.TABLE_PMS_COMPLETE_STORAGE);
        lock.lock();
        //实体对象
        PmsCompleteStorage model = new PmsCompleteStorage();
        //获取类型-冲销入库
        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(SysDictConstant.CODE_2, SysDictConstant.TYPE_WAREHOUSING_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        try {
            //校验一箱货只能存在于一个入库单中
            Set<String> caseNoSet = infoList.stream().map(PmsCompleteDeliveryInfo::getCaseNo).collect(Collectors.toSet());
            Assert.isTrue(caseNoSet.size() == infoList.size(), "入库单中有部分箱号重复");
            //明细集合
            List<PmsCompleteStorageInfo> modelInfoList = new ArrayList<>(infoList.size());
            model.setId(IdUtil.getSnowflake().nextId());
            model.setUnitId(dto.getUnitId());
            model.setProductLineId(dto.getProductLineId());
            model.setTypeId(sysDictVO.getId());
            model.setState(PmManagerConstant.InVouchEnum.FINISH.getCode());
            model.setCode(maxCompleteCode());
            model.setInStorageVoucher(model.getCode());
            BigDecimal sumQuantity = BigDecimal.ZERO;
            //获取仓库的名称
            Set<Integer> organSet = Sets.newHashSet();
            infoList.forEach(ele -> {
                organSet.add(ele.getShelfId());
                organSet.add(ele.getWarehouseId());
            });
            Map<Integer, SysOfficeVO> organMap = FeignOptional.of(() -> remoteOfficeFeign.listById(organSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysOfficeVO defaultSysOfficeVO = new SysOfficeVO();
            //保存明细信息
            WmsStorageReceiptDTO receiptDTO = new WmsStorageReceiptDTO();
            List<WmsStorageReceiptInfoDTO> receiptInfoList = Lists.newArrayList();
            for (PmsCompleteDeliveryInfo info : infoList) {
                sumQuantity = sumQuantity.add(info.getQuantity());
                PmsCompleteStorageInfo modelInfo = new PmsCompleteStorageInfo();
                BeanUtils.copyProperties(info, modelInfo);
                modelInfo.setPackTime(info.getCreateTime());
                modelInfo.setProduceOrderId(dto.getProduceOrderId());
                modelInfo.setCompleteStorageId(model.getId());
                modelInfo.setId(IdUtil.getSnowflake().nextId());
                modelInfoList.add(modelInfo);
                receiptDTO.setWarehouseId(info.getWarehouseId());
                receiptDTO.setWarehouseName(organMap.getOrDefault(receiptDTO.getWarehouseId(), defaultSysOfficeVO).getOfficeName());
                //入库凭证详细信息
                WmsStorageReceiptInfoDTO receiptInfo = new WmsStorageReceiptInfoDTO();
                BeanUtils.copyProperties(info, receiptInfo);
                receiptInfo.setQuantity(sumQuantity);
                receiptInfo.setUnit(info.getUnitId());
                SysOfficeVO shelfOffice = organMap.getOrDefault(info.getShelfId(), defaultSysOfficeVO);
                receiptInfo.setShelfCode(shelfOffice.getOfficeCode());
                receiptInfo.setShelfName(shelfOffice.getOfficeName());
                receiptInfo.setBatch(info.getBatchNo());
                receiptInfoList.add(receiptInfo);
            }
            model.setQuantity(sumQuantity);
            save(model);
            completeStorageInfoService.saveBatch(modelInfoList);
            //生成入库单凭证[wms_delivery_receipt]
            receiptDTO.setType(sysDictVO.getId());
            //仓库id
            receiptDTO.setList(receiptInfoList);
            receiptDTO.setSourceType(PmManagerConstant.OrderStateEnum.ZERO.getCode());
            remoteWmsInventoryFeignApi.generateInStorageVoucher(receiptDTO);
        } catch (Exception e) {
            log.error("生成入库单[冲销入库]失败 -> {}", e.getLocalizedMessage());
            throw new BusinessException("生成入库单失败");
        } finally {
            lock.unlock();
        }
        return model.getId();
    }

    /**
     * 生产 -冲销入库
     *
     * @param id
     * @return
     */
    @Override
    public Boolean chargeAgainst(Long id) {
        //获取生产入库单信息
        PmsCompleteStorage pmsCompleteStorage = getById(id);
        //校验状态
        Assert.isTrue(PmManagerConstant.InVouchEnum.FINISH.getCode().equals(pmsCompleteStorage.getState()), "当前状态不可冲销");
        //调用仓储冲销功能
        WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO = FeignOptional.of(() -> remoteWmsStorageReceiptFeignApi.chargeAgainstByCode(pmsCompleteStorage.getCode(), SecurityUtils.getUser().getName()), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        if (Objects.nonNull(wmsDeliveryReceiptDTO)) {
            //生成出库单
            PmsCompleteDelivery pmsCompleteDelivery = new PmsCompleteDelivery();
            pmsCompleteDelivery.setOutStorageVoucher(wmsDeliveryReceiptDTO.getCode());
            pmsCompleteDeliveryService.generateOrderByCompleteStorage(pmsCompleteDelivery, wmsDeliveryReceiptDTO);
        }
        //修改入库单状态为已冲销
        pmsCompleteStorage.setState(PmManagerConstant.InVouchEnum.CHARGE.getCode());
        return updateById(pmsCompleteStorage);
    }

    @Override
    public Boolean getCompleteStorageByCode(String code) {
        List<PmsCompleteStorage> list = list(new LambdaQueryWrapper<PmsCompleteStorage>().eq(PmsCompleteStorage::getInStorageVoucher, code));
        if (CollUtil.isNotEmpty(list)){
            return true;
        }
        return false;
    }

    @Override
    public Boolean wmsChargeAgainst(WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO) {
        PmsCompleteStorage storage = getOne(new LambdaQueryWrapper<PmsCompleteStorage>().eq(PmsCompleteStorage::getInStorageVoucher, wmsDeliveryReceiptDTO.getChargeAgainstCode()));
        storage.setState(PmManagerConstant.InVouchEnum.CHARGE.getCode());
        updateById(storage);
        //创建对应的出库单
        PmsCompleteDelivery pmsCompleteDelivery = new PmsCompleteDelivery();
        pmsCompleteDelivery.setOutStorageVoucher(wmsDeliveryReceiptDTO.getCode());
        return pmsCompleteDeliveryService.generateOrderByCompleteStorage(pmsCompleteDelivery,wmsDeliveryReceiptDTO);
    }

    @Override
    public Boolean casePrint(List<String> caseNoList) {
        LambdaUpdateWrapper<PmsCompleteStorageInfo> updateWrapper = Wrappers.lambdaUpdate();
        updateWrapper.in(PmsCompleteStorageInfo::getCaseNo, caseNoList);
        updateWrapper.setSql("print_flag =1 , print_count = IFNULL(print_count , 0) + 1");
        return completeStorageInfoService.update(updateWrapper);
    }

    @Override
    public Map<String, TupleDTO<Integer, Integer>> casePrintAndCnt(Set<String> caseNoSet) {
        Map<String, TupleDTO<Integer, Integer>> resMap = Maps.newHashMap();
        if(CollUtil.isNotEmpty(caseNoSet)){
            List<PmsCompleteStorageInfo> dbList = completeStorageInfoService.list(new LambdaQueryWrapper<PmsCompleteStorageInfo>().in(PmsCompleteStorageInfo::getCaseNo, caseNoSet));
            if(CollUtil.isNotEmpty(dbList)){
                for (PmsCompleteStorageInfo info : dbList) {
                    resMap.put(info.getCaseNo(),new TupleDTO(info.getPrintFlag(),info.getPrintCount()));
                }
            }
        }
        return resMap;
    }

    /**
     * 最大完工code
     *
     * @return
     */
    private String maxCompleteCode() {
        String prefix = PmsCodeUtil.PREFIX_SI + LocalDate.now().toString().replaceAll(PmsCodeUtil.SHORT_LINE, Strings.EMPTY) + PmsCodeUtil.SHORT_LINE;
        PmsCompleteStorage one = getOne(new LambdaQueryWrapper<PmsCompleteStorage>()
                .likeRight(PmsCompleteStorage::getCode, prefix)
                .orderByDesc(PmsCompleteStorage::getCode)
                .last(MpConstant.STR_LIMIT_1));
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