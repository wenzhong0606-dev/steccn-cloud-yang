package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.domain.PmsBarcodePrint;
import com.steccn.pms.domain.PmsCompleteStorage;
import com.steccn.pms.domain.PmsCompleteStorageInfo;
import com.steccn.pms.domain.PmsProduceOrder;
import com.steccn.pms.dto.*;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.mapper.PmsCompleteStorageMapper;
import com.steccn.pms.service.PmsBarcodePrintService;
import com.steccn.pms.service.PmsCompleteStorageInfoService;
import com.steccn.pms.service.PmsCompleteStorageService;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.*;
import com.steccn.wms.dto.WmsStorageReceiptDTO;
import com.steccn.wms.dto.WmsStorageReceiptInfoDTO;
import com.steccn.wms.feign.RemoteWmsInventoryFeignApi;
import com.steccn.wms.feign.RemoteWmsShelfStrategyFeignApi;
import com.steccn.wms.vo.WmsMaterialShelfUpVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
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
    private RemoteOfficeFeign remoteOfficeFeign;
    @Autowired
    private RemoteDictFeignApi remoteDictFeignApi;
    @Autowired
    private RemoteSpareFeignApi remoteSpareFeignApi;
    @Autowired
    private RemoteMessFeignApi remoteMessFeignApi;
    @Autowired
    private RemoteWmsInventoryFeignApi remoteWmsInventoryFeignApi;
    @Autowired
    private RemoteWmsShelfStrategyFeignApi remoteWmsShelfStrategyFeignApi;


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
//        HashSet<Integer> storageSet = Sets.newHashSet(pmsProduceOrder.getDefaultWarehouseId(), pmsProduceOrder.getDefaultShelfId());
//        Map<Integer, String> sysOfficeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(storageSet)).get();
//        resDTO.setWarehouseName(sysOfficeMap.get(pmsProduceOrder.getDefaultWarehouseId()));
//        resDTO.setShelfName(sysOfficeMap.get(pmsProduceOrder.getDefaultShelfId()));
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
    @Transactional(rollbackFor = Exception.class)
    public String generate(List<CompleteStorageScanDTO> dtoList) {
        Assert.notEmpty(dtoList, "请选择箱子");
        //校验一箱货只能存在于一个入库单中
        Set<String> caseNoSet = dtoList.stream().map(CompleteStorageScanDTO::getCaseNo).collect(Collectors.toSet());
        Assert.isFalse(completeStorageInfoService.existCaseNo(caseNoSet), "部分箱已生成入库单了");

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
        PmsCompleteStorage pmsCompleteStorage = new PmsCompleteStorage();
        pmsCompleteStorage.setId(IdUtil.getSnowflake().nextId());
        pmsCompleteStorage.setUnitId(dtoList.get(0).getUnitId());

        Long produceOrderId = dtoList.get(0).getProduceOrderId();
        PmsProduceOrder pmsProduceOrder = pmsProduceOrderService.getById(produceOrderId);
        Assert.notNull(pmsProduceOrder, "生产订单不存在");
        pmsCompleteStorage.setProductLineId(pmsProduceOrder.getProductionLineId());
        List<WmsStorageReceiptInfoDTO> materialList = Lists.newArrayList();

        Set<Integer> storageSet = Sets.newHashSet();
        List<Long> eqpList = Lists.newArrayList();
        for (CompleteStorageScanDTO caseInfo : dtoList) {
            storageSet.add(caseInfo.getWarehouseId());
            storageSet.add(caseInfo.getShelfId());
        }
        Map<Integer, SysOfficeVO> sysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listById(storageSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysOfficeVO defaultSysOfficeVO = new SysOfficeVO();

        Map<Long, EqpSpareVO> eqpSpareVOMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(eqpList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        EqpSpareVO defaultEqpSpareVO = new EqpSpareVO();

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
            modelInfoList.add(modelInfo);

            WmsStorageReceiptInfoDTO wmsStorageReceiptInfoDTO = new WmsStorageReceiptInfoDTO();
            BeanUtils.copyProperties(caseInfo, wmsStorageReceiptInfoDTO);
            wmsStorageReceiptInfoDTO.setQuantity(sumQuantity);
            wmsStorageReceiptInfoDTO.setUnit(caseInfo.getUnitId());
            wmsStorageReceiptInfoDTO.setShelfCode(sysOfficeVOMap.getOrDefault(caseInfo.getShelfId(), defaultSysOfficeVO).getOfficeCode());
            wmsStorageReceiptInfoDTO.setBatch("0");
            wmsStorageReceiptDTO.setWarehouseId(caseInfo.getWarehouseId());
            wmsStorageReceiptDTO.setWarehouseName(sysOfficeVOMap.getOrDefault(caseInfo.getWarehouseId(), defaultSysOfficeVO).getOfficeName());
            materialList.add(wmsStorageReceiptInfoDTO);
        }
        completeStorageInfoService.saveBatch(modelInfoList);
        //生成code 规则：SI-20220905-0001
        pmsCompleteStorage.setCode(maxCompleteCode());
        pmsCompleteStorage.setQuantity(sumQuantity);
        pmsCompleteStorage.setInStorageVoucher(pmsCompleteStorage.getCode());

        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(SysDictConstant.CODE_0, SysDictConstant.TYPE_WAREHOUSING_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        pmsCompleteStorage.setTypeId(sysDictVO.getId());
        pmsCompleteStorage.setState(PmManagerConstant.InVouchEnum.CREATE.getCode());
        save(pmsCompleteStorage);
        //生成入库单

        wmsStorageReceiptDTO.setType(sysDictVO.getId());
        //仓库id
        wmsStorageReceiptDTO.setList(materialList);
        wmsStorageReceiptDTO.setSourceType(PmManagerConstant.OrderStateEnum.ZERO.getCode());
        try {
            Boolean aBoolean = FeignOptional.of(() -> remoteWmsInventoryFeignApi.generateInStorageVoucher(wmsStorageReceiptDTO), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
            //入库必定要成功
            if (!aBoolean) {
                log.info("入库单 -> 失败");
            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            throw new BusinessException("入库单创建失败");
        }
        //箱状态修改成待入库
        pmsBarcodePrintService.reviseAlreadyPutInStatue(Lists.newArrayList(caseNoSet), PmManagerConstant.CaseStateEnum.PREPARE.getCode());
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
            Map<Long, String> sysDictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictSet)).get();
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
        Map<Long, String> sysDictMap = FeignOptional.of(remoteDictFeignApi.listByIdCh(dictList)).get();

        Map<String, PmsBarcodePrint> barcodePrintMap = pmsBarcodePrintService.caseMap(caseNoList);
        PmsBarcodePrint defaultPmsBarcodePrint = new PmsBarcodePrint();
        Set<Integer> storageSet = Sets.newHashSet();
        for (Map.Entry<String, PmsBarcodePrint> entry : barcodePrintMap.entrySet()) {
            PmsBarcodePrint pbp = entry.getValue();
            storageSet.add(pbp.getWarehouseId());
            storageSet.add(pbp.getShelfId());
        }
        Map<Integer, String> sysOfficeMap = FeignOptional.of(remoteOfficeFeign.listByIdCh(storageSet)).get();

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
            //获取单位信息
            R<Map<Long, SysDictVO>> mapR = remoteDictFeignApi.listById(ids);
            //获取仓库以及货架信息
            Map<String, CaseSiteDTO> caseSiteDTOMap = pmsBarcodePrintService.caseSite(warehouseCodes);
            CaseSiteDTO defaultCaseSiteDTO = new CaseSiteDTO();
            records.forEach(pmsCompleteStorageVO -> {
                CaseSiteDTO caseSiteDTO = caseSiteDTOMap.getOrDefault(pmsCompleteStorageVO.getCaseNo(), defaultCaseSiteDTO);
                pmsCompleteStorageVO.setUnit(mapR.getData().get(pmsCompleteStorageVO.getUnitId()).getName());
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
    @Transactional(rollbackFor = Exception.class)
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