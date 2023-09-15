package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.vo.R;
import com.steccn.mes.dto.*;
import com.steccn.mes.feign.RemoteErpFeignApi;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.dto.SrmOaWorkFlowDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.feign.RemoteSrmFeignApi;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.*;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.dto.WmsStorageReceiptExcelDTO;
import com.steccn.pms.feign.RemoteCompleteStorageFeignApi;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.pms.vo.PmsProduceOrderVO;
import com.steccn.pms.vo.PmsWorkOrderBatchVO;
import com.steccn.qms.vo.QtCheckTaskReportVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.dto.*;
import com.steccn.wms.mapper.WmsStorageReceiptMapper;
import com.steccn.wms.service.WmsDeliveryReceiptService;
import com.steccn.wms.service.WmsInventoryService;
import com.steccn.wms.service.WmsStorageReceiptInfoService;
import com.steccn.wms.service.WmsStorageReceiptService;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.WmsSalesDeliveryVO;
import com.steccn.wms.vo.WmsStorageFlagInfoVO;
import com.steccn.wms.vo.WmsStorageReceiptInfoVO;
import com.steccn.wms.vo.WmsStorageReceiptVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author W10
 * @description 针对表【wms_storage_receipt】的数据库操作Service实现
 * @createDate 2022-12-09 11:18:09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WmsStorageReceiptServiceImpl extends ServiceImpl<WmsStorageReceiptMapper, WmsStorageReceipt> implements WmsStorageReceiptService {

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final RedissonClient redissonClient;

    private final WmsInventoryService wmsInventoryService;

    private final WmsStorageReceiptInfoService wmsStorageReceiptInfoService;

    private final RemoteCompleteStorageFeignApi remoteCompleteStorageFeignApi;

    private final RemoteSpareFeignApi remoteSpareFeignApi;

    private final WmsDeliveryReceiptService wmsDeliveryReceiptService;

    private final RemoteErpFeignApi remoteErpFeignApi;

    private final RemotePmsFeignApi remotePmsFeignApi;

    private final WmsStorageReceiptMapper wmsStorageReceiptMapper;

    private final RemoteSrmFeignApi remoteSrmFeignApi;

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Page<WmsStorageReceiptVO> pageList(WmsStorageReceiptPageDTO dto) {
        //增加产线筛选条件
        if (Objects.nonNull(dto.getProduceLineId())){
            //获取在该产线下存在的生产订单
            List<PmsProduceOrderVO> pmsProduceOrderVOS = FeignOptional.of(() -> remotePmsFeignApi.listOrderByProduceLineId(dto.getProduceLineId()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            if (CollUtil.isNotEmpty(pmsProduceOrderVOS)){
                List<String> orderCodes = pmsProduceOrderVOS.stream().map(PmsProduceOrderVO::getCode).collect(Collectors.toList());
                dto.setOrderCodes(orderCodes);
            }
        }
        //1.处理筛选条件时间格式
        if (Objects.nonNull(dto.getAffirmStartDate())) {
            dto.setAffirmStartTime(LocalDateTime.of(dto.getAffirmStartDate(), LocalTime.MIN));
        }
        if (Objects.nonNull(dto.getAffirmEndDate())) {
            dto.setAffirmEndTime(LocalDateTime.of(dto.getAffirmEndDate(), LocalTime.MAX));
        }
        if (Objects.nonNull(dto.getCreateStartDate())) {
            dto.setCreateStartTime(LocalDateTime.of(dto.getCreateStartDate(), LocalTime.MIN));
        }
        if (Objects.nonNull(dto.getCreateEndDate())) {
            dto.setCreateEndTime(LocalDateTime.of(dto.getCreateEndDate(), LocalTime.MAX));
        }
        //2.自定义sql查询信息列表
        Page<WmsStorageReceiptVO> page = this.baseMapper.pageList(new Page(dto.getCurrent(), dto.getSize()), dto);
        //3.处理数据
        List<WmsStorageReceiptVO> records = page.getRecords();
        if (CollUtil.isNotEmpty(records)) {
            Set<Long> typeSet = Sets.newHashSet();
            Set<Integer> officeSet = Sets.newHashSet();
            records.forEach(receipt -> {
                typeSet.add(receipt.getType());
                officeSet.add(receipt.getWarehouseId());
            });
            Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(typeSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            records.forEach(receipt -> {
                receipt.setTypeName(dictMap.get(receipt.getType()));
                receipt.setWarehouseName(officeMap.get(receipt.getWarehouseId()));
            });
        }
        //返回数据
        return page;
    }

    @Override
    @Transactional(readOnly = false)
    public Boolean add(WmsStorageReceiptDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_STORAGE_RECEIPT);
        lock.lock();
        try {
            //1.校验数据是否存在
            Assert.isTrue(Objects.nonNull(dto), "入库凭证信息为空");

            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(Sets.newHashSet(dto.getType())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            int receiptType = 0;
            if (dictMap.get(dto.getType()).equals(WmsManagerConstant.RECEIPT_TYPE_1)) {
                receiptType = 1;
            }
            //2.定义数据模型
            WmsStorageReceipt model = new WmsStorageReceipt();
            BeanUtil.copyProperties(dto, model);
            //3.给当前入库单生成编码
            model.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_STORAGE_RECEIPT, WmsCodeUtil.NUM_BIT_4, currentMaxCode()));
            model.setStatus(WmsManagerConstant.OrderStateEnum.ZERO.getCode());
            //4.保存入库单信息
            save(model);
            //5.保存入库单明细信息
            if (Objects.nonNull(dto.getList())) {
                List<WmsInventoryDTO> infoList = Lists.newArrayList();
                List<WmsStorageReceiptInfo> receiptInfoList = Lists.newArrayList();
                Long caseNo = getCaseNo();
                List<WmsStorageReceiptInfoDTO> list = dto.getList();
                List<String> caseNos = Lists.newArrayList();
                for (WmsStorageReceiptInfoDTO info : list) {
                    //如果是生产完工入库关联单据必填且查询生产订单，生产订单必须要有批次信息，批次为生产订单带出
                    if (receiptType == 1) {
                        PmsProduceOrderVO pmsProduceOrderVO = FeignOptional.of(() -> remotePmsFeignApi.allCode(info.getAssociatedNo()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
                        if (pmsProduceOrderVO.getId() == null) {
                            throw new BusinessException("关联的生产订单不存在");
                        }
                        List<PmsWorkOrderBatchVO> pmsWorkOrderBatchVOS = FeignOptional.of(() -> remotePmsFeignApi.getAllBatchByOrderId(pmsProduceOrderVO.getId()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
                        if (CollUtil.isEmpty(pmsWorkOrderBatchVOS)) {
                            throw new BusinessException("该订单下没有批次信息");
                        }
                        if (Objects.isNull(pmsWorkOrderBatchVOS.stream().filter(v -> v.getCode().equals(info.getBatch())).findFirst().orElse(null))) {
                            throw new BusinessException("与该订单下的批次信息不符合");
                        }
                    }
                    if (Objects.isNull(info.getCaseNo()) || "".equals(info.getCaseNo())) {
                        //  namespace: 86bb5247-39e5-4887-ba42-d6830f4db689
                        info.setCaseNo(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_CASE_NO, WmsCodeUtil.NUM_BIT_4, caseNo));
                        caseNo++;
                    }
                    WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                    wmsInventoryDTO.setWarehouseId(dto.getWarehouseId());
                    wmsInventoryDTO.setWarehouseName(dto.getWarehouseName());
                    BeanUtil.copyProperties(info, wmsInventoryDTO);
                    WmsStorageReceiptInfo wmsStorageReceiptInfo = new WmsStorageReceiptInfo();
                    wmsStorageReceiptInfo.setStorageReceiptId(model.getId());
                    BeanUtil.copyProperties(info, wmsStorageReceiptInfo);
                    caseNos.add(wmsInventoryDTO.getCaseNo());
                    infoList.add(wmsInventoryDTO);
                    receiptInfoList.add(wmsStorageReceiptInfo);
                }
                long count = infoList.stream().distinct().count();
                Assert.isFalse(count < infoList.size(), "箱号重复，请修改");
                //增加库存信息
                wmsInventoryService.addInfo(infoList);
                //保存明细信息
                wmsStorageReceiptInfoService.saveBatch(receiptInfoList);
            }
            //6.如果是采购单且来源是采购系统,则自动确认
            if(dto.getType().equals(1600405739121573888L)&&dto.getIsSRMSys()!=null&&dto.getIsSRMSys().equals(OaConstant.NUM_1)){
                dto.setId(model.getId());
                wmsStorageReceiptMapper.salesAutoConfirm(dto);
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean salesAdd(WmsStorageReceiptSalesDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_STORAGE_RECEIPT_SALES);
        lock.lock();
        try {
            //组数据
            WmsStorageReceiptDTO receiptDTO = new WmsStorageReceiptDTO();
            List<WmsStorageReceiptInfoDTO> materialList = Lists.newArrayList();
            //默认是采购入库单
            receiptDTO.setType(1600405739121573888L);
            //获取仓库id
            Set<String> shelfCode = dto.getMaterials().stream().map(WmsStorageSalesMaterialDTO::getShelfCode).collect(Collectors.toSet());
            shelfCode.add(dto.getWarehouseCode());
            Map<String, SysOfficeVO> wareHouseInfo = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(shelfCode), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(wareHouseInfo.get(dto.getWarehouseCode()),"找不到编码对应的仓库");
            receiptDTO.setWarehouseId(wareHouseInfo.get(dto.getWarehouseCode()).getId());
            //获取物料信息
            //单位信息
            Set<String> unitNameList = dto.getMaterials().stream().map(WmsStorageSalesMaterialDTO::getUnitName).collect(Collectors.toSet());
            Map<String, Long> dicMap = FeignOptional.of(() -> remoteDictFeignApi.listByNameCh(unitNameList), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //日期当天
            String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            //存相同物料的caseNo
            Map<String,Integer> caseNoMap = Maps.newHashMap();
            dto.getMaterials().forEach(tmp->{
                EqpSpareVO eqpSpareVO = FeignOptional.of(() -> remoteSpareFeignApi.getByCode(tmp.getMaterialCode()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                Assert.notNull(eqpSpareVO,"系统中不存在该物料");
                //箱号规则   六位日期-料号-六位流水()
                Integer caseNo = caseNoMap.getOrDefault(tmp.getMaterialCode(), null);
                if(caseNo == null) {
                    WmsStorageReceiptInfo caseNoObj = wmsStorageReceiptInfoService.getOne(new LambdaQueryWrapper<WmsStorageReceiptInfo>()
                            .select(WmsStorageReceiptInfo::getCaseNo)
                            .likeRight(WmsStorageReceiptInfo::getCaseNo, yyyyMMdd + "-" + tmp.getMaterialCode())
                            .orderByDesc(WmsStorageReceiptInfo::getCaseNo)
                            .last("LIMIT 1"));
                    if(Objects.nonNull(caseNoObj)){
                        String[] split = caseNoObj.getCaseNo().split("-");
                        caseNo = Integer.parseInt(split[2]);
                        caseNo = caseNo + 1;
                    }else {
                        caseNo = 0;
                    }
                }else {
                    caseNo = caseNo + 1;
                }
                for(int i = 0; i < tmp.getPackagePackNum(); i++){
                    WmsStorageReceiptInfoDTO material = new WmsStorageReceiptInfoDTO();
                    material.setMaterialName(eqpSpareVO.getName());
                    material.setMaterialId(eqpSpareVO.getId());
                    material.setMaterialCode(tmp.getMaterialCode());
                    Assert.notNull(dicMap.get(tmp.getUnitName()), "单位id不存在");
                    material.setUnit(dicMap.get(tmp.getUnitName()));
                    material.setUnitName(tmp.getUnitName());
                    material.setBatch(tmp.getBatch());
                    material.setQuantity(BigDecimalUtil.divide(tmp.getNetWeight(),new BigDecimal(tmp.getPackagePackNum())));
                    String caseNoStr = yyyyMMdd + "-" + tmp.getMaterialCode() + "-" + String.format("%06d", caseNo);
                    material.setCaseNo(caseNoStr);
                    Assert.notNull(wareHouseInfo.get(tmp.getShelfCode()), "货架id不存在");
                    material.setShelfId(wareHouseInfo.get(tmp.getShelfCode()).getId());
                    material.setShelfCode(wareHouseInfo.get(tmp.getShelfCode()).getOfficeCode());
                    material.setShelfName(wareHouseInfo.get(tmp.getShelfCode()).getOfficeName());
                    material.setAssociatedNo(tmp.getAssociatedNo());
                    caseNoMap.put(tmp.getMaterialCode(),caseNo);
                    caseNo = caseNo + 1;
                    materialList.add(material);
                }
            });
            receiptDTO.setOperateId(dto.getOperateId());
            receiptDTO.setOperateName(dto.getOperateName());
            receiptDTO.setOperateTime(LocalDateTime.now());
            receiptDTO.setList(materialList);
            receiptDTO.setIsSRMSys(OaConstant.NUM_1);
            this.add(receiptDTO);
        } finally {
            lock.unlock();
        }

        return true;
    }

    @Override
    @Transactional(readOnly = false)
    public String addReturnCode(WmsStorageReceiptDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_STORAGE_RECEIPT);
        lock.lock();
        try {
            //1.校验数据是否存在
            Assert.isTrue(Objects.nonNull(dto), "入库凭证信息为空");
            //2.定义数据模型
            WmsStorageReceipt model = new WmsStorageReceipt();
            BeanUtil.copyProperties(dto, model);
            //3.给当前入库单生成编码
            model.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_STORAGE_RECEIPT, WmsCodeUtil.NUM_BIT_4, currentMaxCode()));
            model.setStatus(WmsManagerConstant.OrderStateEnum.ZERO.getCode());
            //4.保存入库单信息
            save(model);
            //5.保存入库单明细信息
            if (Objects.nonNull(dto.getList())) {
                List<WmsInventoryDTO> infoList = Lists.newArrayList();
                List<WmsStorageReceiptInfo> receiptInfoList = Lists.newArrayList();
                Long caseNo = getCaseNo();
                List<WmsStorageReceiptInfoDTO> list = dto.getList();
                List<String> caseNos = Lists.newArrayList();
                for (WmsStorageReceiptInfoDTO info : list) {
                    if (Objects.isNull(info.getCaseNo()) || "".equals(info.getCaseNo())) {
                        //生成箱号
                        info.setCaseNo(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_CASE_NO, WmsCodeUtil.NUM_BIT_4, caseNo));
                        caseNo++;
                    }
                    WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                    wmsInventoryDTO.setWarehouseId(dto.getWarehouseId());
                    wmsInventoryDTO.setWarehouseName(dto.getWarehouseName());
                    BeanUtil.copyProperties(info, wmsInventoryDTO);
                    WmsStorageReceiptInfo wmsStorageReceiptInfo = new WmsStorageReceiptInfo();
                    wmsStorageReceiptInfo.setStorageReceiptId(model.getId());
                    BeanUtil.copyProperties(info, wmsStorageReceiptInfo);
                    caseNos.add(wmsInventoryDTO.getCaseNo());
                    infoList.add(wmsInventoryDTO);
                    receiptInfoList.add(wmsStorageReceiptInfo);
                }
                long count = infoList.stream().distinct().count();
                Assert.isFalse(count < infoList.size(), "箱号重复，请修改");
                //增加库存信息
                wmsInventoryService.addInfo(infoList);
                //保存明细信息
                wmsStorageReceiptInfoService.saveBatch(receiptInfoList);
            }
            return model.getCode();
        } finally {
            lock.unlock();
        }

    }


    @Override
    @Transactional(readOnly = false)
    public Boolean edit(WmsStorageReceiptDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_STORAGE_RECEIPT);
        lock.lock();
        try {
            //1.校验数据是否存在
            Assert.isTrue(Objects.nonNull(dto), "入库凭证信息为空");
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(Sets.newHashSet(dto.getType())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            int receiptType = 0;
            if (dictMap.get(dto.getType()).equals(WmsManagerConstant.RECEIPT_TYPE_1)) {
                receiptType = 1;
            }
            //2.定义数据模型
            WmsStorageReceipt model = new WmsStorageReceipt();
            BeanUtil.copyProperties(dto, model);
            //3.修改
            updateById(model);
            //3.1 修改详情数据
            if (CollUtil.isNotEmpty(dto.getList())) {
                List<WmsStorageReceiptInfo> wmsStorageReceiptInfoList = Lists.newArrayList();
                List<WmsInventoryDTO> addWmsInventoryList = Lists.newArrayList();
                List<WmsStorageReceiptInfo> updateWmsInventoryList = Lists.newArrayList();
                List<Long> ids = Lists.newArrayList();
                List<Long> updateIds = Lists.newArrayList();
                List<String> caseNos = Lists.newArrayList();
                //获取箱号规则
                Long caseNo = getCaseNo();
                List<WmsStorageReceiptInfoDTO> infoList = dto.getList();
                for (WmsStorageReceiptInfoDTO info : infoList) {
                    //如果是生产完工入库关联单据必填且查询生产订单，生产订单必须要有批次信息，批次为生产订单带出
                    if (receiptType == 1) {
                        PmsProduceOrderVO pmsProduceOrderVO = FeignOptional.of(() -> remotePmsFeignApi.allCode(info.getAssociatedNo()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
                        if (pmsProduceOrderVO.getId() == null) {
                            throw new BusinessException("关联的生产订单不存在");
                        }
                        List<PmsWorkOrderBatchVO> pmsWorkOrderBatchVOS = FeignOptional.of(() -> remotePmsFeignApi.getAllBatchByOrderId(pmsProduceOrderVO.getId()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
                        if (CollUtil.isEmpty(pmsWorkOrderBatchVOS)) {
                            throw new BusinessException("该订单下没有批次信息");
                        }
                        if (Objects.isNull(pmsWorkOrderBatchVOS.stream().filter(v -> v.getCode().equals(info.getBatch())).findFirst().orElse(null))) {
                            throw new BusinessException("与该订单下的批次信息不符合");
                        }
                    }
                    if (Objects.isNull(info.getCaseNo()) || "".equals(info.getCaseNo())) {
                        //生成箱号
                        info.setCaseNo(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_CASE_NO, WmsCodeUtil.NUM_BIT_4, caseNo));
                        caseNo++;
                    }
                    WmsStorageReceiptInfo wmsStorageReceiptInfo = new WmsStorageReceiptInfo();
                    BeanUtil.copyProperties(info, wmsStorageReceiptInfo);
                    WmsInventoryDTO wmsInventory = new WmsInventoryDTO();
                    BeanUtil.copyProperties(info, wmsInventory);
                    wmsInventory.setWarehouseId(dto.getWarehouseId());
                    wmsInventory.setWarehouseName(dto.getWarehouseName());
                    if (Objects.isNull(info.getId())) {
                        wmsStorageReceiptInfo.setStorageReceiptId(model.getId());
                        addWmsInventoryList.add(wmsInventory);
                    } else {
                        ids.add(info.getId());
                        updateIds.add(info.getId());
                        updateWmsInventoryList.add(wmsStorageReceiptInfo);
                    }
                    caseNos.add(wmsStorageReceiptInfo.getCaseNo());
                    wmsStorageReceiptInfoList.add(wmsStorageReceiptInfo);
                }
                long count = wmsStorageReceiptInfoList.stream().distinct().count();
                Assert.isFalse(count < wmsStorageReceiptInfoList.size(), "箱号重复，请修改");
                LambdaQueryWrapper<WmsStorageReceiptInfo> wrapper = new LambdaQueryWrapper<WmsStorageReceiptInfo>().eq(WmsStorageReceiptInfo::getStorageReceiptId, model.getId()).notIn(CollUtil.isNotEmpty(ids), WmsStorageReceiptInfo::getId, ids);
                //获取需要删除的数据
                List<WmsStorageReceiptInfo> list = wmsStorageReceiptInfoService.list(wrapper);

                if (CollUtil.isNotEmpty(addWmsInventoryList)) {
                    //增加库存信息
                    wmsInventoryService.addInfo(addWmsInventoryList);
                }
                if (CollUtil.isNotEmpty(updateWmsInventoryList)) {
                    Map<Long, WmsStorageReceiptInfo> infoMap = wmsStorageReceiptInfoService.infoMap(updateIds);
                    List<WmsInventoryDTO> inventoryDTOList = Lists.newArrayList();
                    List<WmsInventoryDTO> restore = Lists.newArrayList();
                    updateWmsInventoryList.forEach(info -> {
                        WmsStorageReceiptInfo wmsStorageReceiptInfo = infoMap.get(info.getId());
                        WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                        BeanUtil.copyProperties(info, wmsInventoryDTO);
                        wmsInventoryDTO.setOriginalQuantity(wmsStorageReceiptInfo.getQuantity());
                        wmsInventoryDTO.setOriginalCaseNo(wmsStorageReceiptInfo.getCaseNo());
                        if (info.getCaseNo().equals(wmsStorageReceiptInfo.getCaseNo())) {
                            inventoryDTOList.add(wmsInventoryDTO);
                        } else {
                            restore.add(wmsInventoryDTO);
                        }
                    });
                    //修改库存信息
                    if (CollUtil.isNotEmpty(inventoryDTOList)) {
                        wmsInventoryService.updateInventory(inventoryDTOList);
                    }
                    if (CollUtil.isNotEmpty(restore)) {
                        //先还原原始库存，再新增库存
                        wmsInventoryService.restore(restore);
                        wmsInventoryService.addInfo(addWmsInventoryList);
                    }

                }
                if (CollUtil.isNotEmpty(list)) {
                    List<WmsInventoryDTO> deleteList = Lists.newArrayList();
                    list.forEach(info -> {
                        WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                        BeanUtil.copyProperties(info, wmsInventoryDTO);
                        wmsInventoryDTO.setOriginalCaseNo(info.getCaseNo());
                        wmsInventoryDTO.setOriginalQuantity(info.getQuantity());
                        deleteList.add(wmsInventoryDTO);
                    });
                    //还原之前的库存信息
                    wmsInventoryService.restore(deleteList);
                    wmsStorageReceiptInfoService.remove(wrapper);
                }
                wmsStorageReceiptInfoService.saveOrUpdateBatch(wmsStorageReceiptInfoList);
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    @Transactional(readOnly = false)
    public Boolean affirm(Long id) {
        //1.查询入库单
        WmsStorageReceipt receipt = getById(id);
        //2.判断单状态
        Assert.isTrue(WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(receipt.getStatus()), "当前状态不可确认");
        //3.确认(如果来自生产订单的数据:0)
        if (WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(receipt.getSourceType())) {
            //变更pms生产订单状态
            PmsCompleteStorageDTO pmsCompleteStorageDTO = new PmsCompleteStorageDTO();
            pmsCompleteStorageDTO.setCode(receipt.getCode());
            pmsCompleteStorageDTO.setConfirmName(SecurityUtils.getUser().getName());
            remoteCompleteStorageFeignApi.completeStorageReviseStatue(pmsCompleteStorageDTO);
        }
        //3.1获取该单据的信息
        List<WmsStorageReceiptInfo> list = wmsStorageReceiptInfoService.list(new LambdaQueryWrapper<WmsStorageReceiptInfo>().eq(WmsStorageReceiptInfo::getStorageReceiptId, id));
        if (CollUtil.isNotEmpty(list)) {
            //解锁在途库存，增加可用库存以及总库存
            List<WmsInventoryDTO> inventoryDTOList = Lists.newArrayList();
            list.forEach(info -> {
                WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                BeanUtil.copyProperties(info, wmsInventoryDTO);
                inventoryDTOList.add(wmsInventoryDTO);
            });
            Boolean aBoolean = wmsInventoryService.affirm(inventoryDTOList);
        }
        //4.修改确认单状态
        receipt.setAffirmName(SecurityUtils.getUser().getName());
        receipt.setAffirmTime(LocalDateTime.now());
        receipt.setStatus(WmsManagerConstant.OrderStateEnum.ONE.getCode());
        return updateById(receipt);
    }

    @Override
    @Transactional(readOnly = false)
    public Boolean delete(Long id) {
        WmsStorageReceipt order = getById(id);
        Assert.notNull(order, "该单不存在");
        Assert.isTrue(WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(order.getStatus()), "当前状态不可删除");
        //修改状态为已删除
        order.setStatus(WmsManagerConstant.OrderStateEnum.FOUR.getCode());
        //获取订单的明细
        List<WmsStorageReceiptInfo> list = wmsStorageReceiptInfoService.list(new LambdaQueryWrapper<WmsStorageReceiptInfo>().eq(WmsStorageReceiptInfo::getStorageReceiptId, id));
        if (CollUtil.isNotEmpty(list)) {
            List<WmsInventoryDTO> deleteList = Lists.newArrayList();
            list.forEach(info -> {
                WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                BeanUtil.copyProperties(info, wmsInventoryDTO);
                wmsInventoryDTO.setOriginalCaseNo(info.getCaseNo());
                wmsInventoryDTO.setOriginalQuantity(info.getQuantity());
                deleteList.add(wmsInventoryDTO);
            });
            //还原之前的库存信息
            wmsInventoryService.restore(deleteList);
        }
        return updateById(order);
    }

    @Override
    public WmsStorageReceiptVO get(Long id) {
        WmsStorageReceipt order = getById(id);
        Assert.notNull(order, "该入库单不存在");
        //获取订单的明细
        List<WmsStorageReceiptInfo> list = wmsStorageReceiptInfoService.list(new LambdaQueryWrapper<WmsStorageReceiptInfo>().eq(WmsStorageReceiptInfo::getStorageReceiptId, id));
        //处理数据
        WmsStorageReceiptVO wmsStorageReceiptVO = new WmsStorageReceiptVO();
        BeanUtil.copyProperties(order, wmsStorageReceiptVO);
        Set<Long> dictSet = Sets.newHashSet();
        Set<Integer> officeSet = Sets.newHashSet();
        List<Long> materialIds = Lists.newArrayList();
        dictSet.add(wmsStorageReceiptVO.getType());
        officeSet.add(wmsStorageReceiptVO.getWarehouseId());
        //处理明细数据
        list.forEach(wmsStorageReceiptInfo -> {
            dictSet.add(wmsStorageReceiptInfo.getUnit());
            materialIds.add(wmsStorageReceiptInfo.getMaterialId());
        });
        Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<WmsStorageReceiptInfoVO> wmsStorageReceiptInfoVOS = BeanUtil.copyToList(list, WmsStorageReceiptInfoVO.class);
        wmsStorageReceiptInfoVOS.forEach(wmsStorageReceiptInfoVO -> {
            wmsStorageReceiptInfoVO.setUnitName(dictMap.get(wmsStorageReceiptInfoVO.getUnit()));
            wmsStorageReceiptInfoVO.setSpecification(materialMap.get(wmsStorageReceiptInfoVO.getMaterialId()).getSpecifications());
        });
        wmsStorageReceiptVO.setWarehouseName(officeMap.get(wmsStorageReceiptVO.getWarehouseId()));
        wmsStorageReceiptVO.setTypeName(dictMap.get(wmsStorageReceiptVO.getType()));
        wmsStorageReceiptVO.setList(wmsStorageReceiptInfoVOS);
        return wmsStorageReceiptVO;
    }

    @Override
    public String createOrder(WmsStorageReceiptDTO dto) {
        //1.校验数据是否存在
        Assert.isTrue(Objects.nonNull(dto), "入库凭证信息为空");
        //2.定义数据模型
        WmsStorageReceipt model = new WmsStorageReceipt();
        BeanUtil.copyProperties(dto, model);
        //3.给当前入库单生成编码
        model.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_STORAGE_RECEIPT, WmsCodeUtil.NUM_BIT_4, currentMaxCode()));
        model.setStatus(WmsManagerConstant.OrderStateEnum.ZERO.getCode());
        //4.保存入库单信息
        save(model);
        //5.保存入库单明细信息
        if (Objects.nonNull(dto.getList())) {
            List<WmsInventoryDTO> infoList = Lists.newArrayList();
            List<WmsStorageReceiptInfo> receiptInfoList = Lists.newArrayList();
            Long caseNo = getCaseNo();
            List<WmsStorageReceiptInfoDTO> list = dto.getList();
            List<String> caseNos = Lists.newArrayList();
            for (WmsStorageReceiptInfoDTO info : list) {
                if (Objects.isNull(info.getCaseNo()) || "".equals(info.getCaseNo())) {
                    //生成箱号
                    info.setCaseNo(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_CASE_NO, WmsCodeUtil.NUM_BIT_4, caseNo));
                    caseNo++;
                }
                WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                wmsInventoryDTO.setWarehouseId(dto.getWarehouseId());
                wmsInventoryDTO.setWarehouseName(dto.getWarehouseName());
                BeanUtil.copyProperties(info, wmsInventoryDTO);
                WmsStorageReceiptInfo wmsStorageReceiptInfo = new WmsStorageReceiptInfo();
                wmsStorageReceiptInfo.setStorageReceiptId(model.getId());
                BeanUtil.copyProperties(info, wmsStorageReceiptInfo);
                caseNos.add(wmsInventoryDTO.getCaseNo());
                infoList.add(wmsInventoryDTO);
                receiptInfoList.add(wmsStorageReceiptInfo);
            }
            long count = infoList.stream().distinct().count();
            Assert.isFalse(count < infoList.size(), "箱号重复，请修改");
            //增加库存信息
            wmsInventoryService.addInfo(infoList);
            //保存明细信息
            wmsStorageReceiptInfoService.saveBatch(receiptInfoList);
        }
        affirm(model.getId());
        //确认
        return model.getCode();
    }

    @Override
    public Boolean excelImportData(PmExcelDTO dto) throws IOException {
        //定义读取excel数据的集合
        List<WmsStorageReceiptExcelDTO> excelList = Lists.newArrayList();
        EasyExcel.read(dto.getFile().getInputStream(), WmsStorageReceiptExcelDTO.class, new PageReadListener<WmsStorageReceiptExcelDTO>(dataList -> {
            dataList.forEach(data -> {
                excelList.add(data);
            });
        })).sheet().doRead();
        //处理数据
        Set<String> officeNames = Sets.newHashSet();
        Set<String> dictName = Sets.newHashSet();
        List<String> materialCodes = Lists.newArrayList();
        excelList.forEach(excelDTO -> {
            officeNames.add(excelDTO.getWarehouseName());
            officeNames.add(excelDTO.getShelfName());
            dictName.add(excelDTO.getUnitName());
            dictName.add(excelDTO.getType());
            materialCodes.add(excelDTO.getMaterialCode());
        });
        System.out.println("单位：" + dictName.size());
        Map<String, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByName(officeNames), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<String, EqpSpareVO> spareMap = FeignOptional.of(() -> remoteSpareFeignApi.listByCode(materialCodes), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<String, Long> dictIdMap = FeignOptional.of(() -> remoteDictFeignApi.listByNameCh(dictName), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if (CollUtil.isNotEmpty(excelList)) {
            Map<String, List<WmsStorageReceiptExcelDTO>> map = excelList.stream().collect(Collectors.groupingBy(WmsStorageReceiptExcelDTO::getWarehouseName));
            List<WmsStorageReceiptDTO> wmsStorageReceiptList = Lists.newArrayList();
            for (Map.Entry<String, List<WmsStorageReceiptExcelDTO>> list : map.entrySet()) {
                WmsStorageReceiptDTO receiptDTO = new WmsStorageReceiptDTO();
                receiptDTO.setWarehouseName(list.getKey());
                List<WmsStorageReceiptInfoDTO> infoDTOList = Lists.newArrayList();
                if (Objects.nonNull(officeMap.get(list.getKey()))) {
                    receiptDTO.setWarehouseId(officeMap.get(list.getKey()).getId());
                }
                receiptDTO.setStatus(WmsManagerConstant.OrderStateEnum.THREE.getCode());
                for (WmsStorageReceiptExcelDTO wmsStorageReceiptExcelDTO : list.getValue()) {
                    WmsStorageReceiptInfoDTO wmsStorageReceiptInfo = new WmsStorageReceiptInfoDTO();
                    EqpSpareVO eqpSpareVO = spareMap.get(wmsStorageReceiptExcelDTO.getMaterialCode());
                    SysOfficeVO sysOfficeVO = officeMap.get(wmsStorageReceiptExcelDTO.getShelfName());
                    wmsStorageReceiptInfo.setMaterialCode(eqpSpareVO.getCode());
                    wmsStorageReceiptInfo.setMaterialId(eqpSpareVO.getId());
                    wmsStorageReceiptInfo.setMaterialName(eqpSpareVO.getName());
                    wmsStorageReceiptInfo.setShelfId(sysOfficeVO.getId());
                    wmsStorageReceiptInfo.setShelfCode(sysOfficeVO.getOfficeCode());
                    wmsStorageReceiptInfo.setShelfName(sysOfficeVO.getOfficeName());
                    wmsStorageReceiptInfo.setQuantity(wmsStorageReceiptExcelDTO.getQuantity());
                    wmsStorageReceiptInfo.setUnit(dictIdMap.get(wmsStorageReceiptExcelDTO.getUnitName()));
                    wmsStorageReceiptInfo.setBatch(wmsStorageReceiptExcelDTO.getBatchNo());
                    if (Objects.isNull(wmsStorageReceiptInfo.getCaseNo())) {
                        wmsStorageReceiptInfo.setBatch("0");
                    }
                    wmsStorageReceiptInfo.setCaseNo(wmsStorageReceiptExcelDTO.getCaseNo());
                    infoDTOList.add(wmsStorageReceiptInfo);
                }
                receiptDTO.setType(dictIdMap.get(list.getValue().get(0).getType()));
                receiptDTO.setList(infoDTOList);
                wmsStorageReceiptList.add(receiptDTO);
            }
            if (CollUtil.isNotEmpty(wmsStorageReceiptList)) {
                //获取当前最大的code
                Long aLong = currentMaxCode();
                Long caseNo = getCaseNo();
                //获取入库单编码
                //处理数据生成入库单信息
                List<WmsInventory> wmsInventoryList = Lists.newArrayList();
                for (WmsStorageReceiptDTO receiptDTO : wmsStorageReceiptList) {
                    WmsStorageReceipt receipt = new WmsStorageReceipt();
                    BeanUtil.copyProperties(receiptDTO, receipt);
                    receipt.setAffirmName(SecurityUtils.getUser().getName());
                    receipt.setAffirmTime(LocalDateTime.now());
                    //生成入库单号
                    //3.给当前入库单生成编码
                    receipt.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_STORAGE_RECEIPT, WmsCodeUtil.NUM_BIT_4, aLong));
                    aLong++;
                    save(receipt);
                    //保存明细信息并生成库存信息
                    if (CollUtil.isNotEmpty(receiptDTO.getList())) {
                        List<WmsStorageReceiptInfo> receiptInfoList = BeanUtil.copyToList(receiptDTO.getList(), WmsStorageReceiptInfo.class);

                        for (WmsStorageReceiptInfo info : receiptInfoList) {
                            info.setStorageReceiptId(receipt.getId());
//                            if (Objects.isNull(info.getCaseNo()) || "".equals(info.getCaseNo())) {
//                                //生成箱号
//                                info.setCaseNo(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_CASE_NO, WmsCodeUtil.NUM_BIT_4, caseNo));
//                                System.out.println("caseNo:" + WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_CASE_NO, WmsCodeUtil.NUM_BIT_4, caseNo));
//                                caseNo++;
//                                System.out.println(caseNo);
//                            }
                            info.setCaseNo(info.getMaterialCode());
                            //生成库存信息
                            WmsInventory wmsInventory = new WmsInventory();
                            BeanUtil.copyProperties(info, wmsInventory);
                            wmsInventory.setUnit(info.getUnit());
                            wmsInventory.setWarehouseId(Math.toIntExact(receipt.getWarehouseId()));
                            wmsInventory.setWarehouseName(receiptDTO.getWarehouseName());
                            wmsInventory.setFreezeQuantity(BigDecimal.ZERO);
                            wmsInventory.setLockQuantity(BigDecimal.ZERO);
                            wmsInventory.setStockQuantity(BigDecimal.ZERO);
                            wmsInventory.setAvailableQuantity(info.getQuantity());
                            wmsInventory.setTotalQuantity(info.getQuantity());
                            wmsInventory.setBatch("0");
                            wmsInventoryList.add(wmsInventory);
                        }
                        wmsStorageReceiptInfoService.saveBatch(receiptInfoList);

                    }

                }
                System.out.println("库存:" + wmsInventoryList.size());
//                保存库存信息
                wmsInventoryService.saveBatch(wmsInventoryList);
            }
            log.info(String.valueOf(wmsStorageReceiptList.size()));
        }
        return true;
    }

    /**
     * 冲销出库
     *
     * @param id
     * @return
     */
    @Override
    public Boolean chargeAgainstById(Long id) {
        WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO = chargeAgainst(id, SecurityUtils.getUser().getName());
        WmsStorageReceiptVO wmsStorageReceiptVO = get(id);
        //查询该入库单是否在生产入库单中存在，如果存在,则创建对应的出库单
        Boolean aBoolean = FeignOptional.of(() -> remoteCompleteStorageFeignApi.getCompleteStorageByCode(wmsStorageReceiptVO.getCode()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        if (aBoolean) {
            //在pms中创建对应的出库单
            wmsDeliveryReceiptDTO.setChargeAgainstCode(wmsStorageReceiptVO.getCode());

        }
        return true;
    }

    @Override
    public WmsDeliveryReceiptDTO chargeAgainstByCode(String code, String userName) {
        WmsStorageReceipt wmsStorageReceipt = getOne(new LambdaQueryWrapper<WmsStorageReceipt>().eq(WmsStorageReceipt::getCode, code));
        Assert.isTrue(Objects.nonNull(wmsStorageReceipt), "单据不存在");
        return chargeAgainst(wmsStorageReceipt.getId(), userName);
    }

    @Override
    public Boolean linkErp(Long id) {
        //先获取入库单详情
        WmsStorageReceiptVO wmsStorageReceiptVO = get(id);
        //判断是否是已确认状态
        if (!wmsStorageReceiptVO.getStatus().equals(OaConstant.NUM_1)) {
            throw new BusinessException("只有已确认状态才可以同步");
        }
        //仓库编码
        String sourceCode = FeignOptional.of(() -> remoteOfficeFeign.getByIdSysOfficeInfo(wmsStorageReceiptVO.getWarehouseId().longValue()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //类型为生产完工入库
        if (wmsStorageReceiptVO.getTypeName().equals(WmsManagerConstant.RECEIPT_TYPE_1)) {
            ErpCommonSaveDTO<ErpSpinStockSaveDetailDTO> erpCommonSaveDTO = new ErpCommonSaveDTO<>();
            erpCommonSaveDTO.setFdate(wmsStorageReceiptVO.getCreateTime().toLocalDate());
            erpCommonSaveDTO.setFmesbillid(wmsStorageReceiptVO.getId());
            erpCommonSaveDTO.setFmesbillno(wmsStorageReceiptVO.getCode());
            //引入详细信息
            List<ErpSpinStockSaveDetailDTO> erpCommonSaveDetails = Lists.newArrayList();
            if (CollUtil.isNotEmpty(wmsStorageReceiptVO.getList())) {
                for (WmsStorageReceiptInfoVO tmp : wmsStorageReceiptVO.getList()) {
                    ErpSpinStockSaveDetailDTO erpCommonSaveDetail = new ErpSpinStockSaveDetailDTO();
                    erpCommonSaveDetail.setFmesentryid(tmp.getId().toString());
                    erpCommonSaveDetail.setFmatno(tmp.getMaterialCode());
                    erpCommonSaveDetail.setFmustqty(tmp.getQuantity());
                    erpCommonSaveDetail.setFrealqty(tmp.getQuantity());
                    erpCommonSaveDetail.setFlot(tmp.getBatch());
                    erpCommonSaveDetail.setFstockno(sourceCode);
                    if (Objects.isNull(tmp.getAssociatedNo())) {
                        throw new BusinessException("没有关联生产订单");
                    }
                    PmsProduceOrderVO pmsProduceOrderVO = FeignOptional.of(() -> remotePmsFeignApi.allCode(tmp.getAssociatedNo()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
                    //拿到epr关联的code
                    SysErpOfficeVO sysErpOfficeVO = FeignOptional.of(() -> remoteOfficeFeign.getErpCodeByOfficeId(pmsProduceOrderVO.getProductionLineId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    erpCommonSaveDetail.setFdeptno(sysErpOfficeVO.getErpCode());
                    erpCommonSaveDetails.add(erpCommonSaveDetail);
                }
            }
            erpCommonSaveDTO.setFentry(erpCommonSaveDetails);
            String listR = remoteErpFeignApi.saveSpinStock(erpCommonSaveDTO);
            //判断是否保存成功
            JSONObject jsonObject = JSONObject.parseObject(listR);
            if (jsonObject.getBoolean(WmsManagerConstant.JSON_SUCCESS)) {
                WmsStorageReceipt wmsStorageReceipt = new WmsStorageReceipt();
                wmsStorageReceipt.setId(id);
                wmsStorageReceipt.setStatus(OaConstant.NUM_2);
                updateById(wmsStorageReceipt);
            } else {
                String error = "同步失败-" + jsonObject.getString(WmsManagerConstant.JSON_MESSAGE);
                log.error(error);
                throw new BusinessException(error);
            }
        }
        return true;
    }

    @Override
    public Boolean purchaseLinkErp(Long id) {
        //先获取入库单详情
        WmsStorageReceiptVO wmsStorageReceiptVO = get(id);
        //判断是否是已确认状态
        if (!wmsStorageReceiptVO.getStatus().equals(OaConstant.NUM_1)) {
            throw new BusinessException("只有已确认状态才可以同步");
        }
        //仓库编码
        String sourceCode = FeignOptional.of(() -> remoteOfficeFeign.getByIdSysOfficeInfo(wmsStorageReceiptVO.getWarehouseId().longValue()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //类型为采购入库
        if (wmsStorageReceiptVO.getTypeName().equals(WmsManagerConstant.RECEIPT_TYPE_1)) {
            ErpStockSaveDTO erpCommonSaveDTO = new ErpStockSaveDTO();
            erpCommonSaveDTO.setFdate(wmsStorageReceiptVO.getCreateTime().toLocalDate());
            erpCommonSaveDTO.setFmesbillid(wmsStorageReceiptVO.getId());
            erpCommonSaveDTO.setFmesbillno(wmsStorageReceiptVO.getCode());
            //引入详细信息
            List<ErpStockSaveDetailDTO> erpCommonSaveDetails = Lists.newArrayList();
            if (CollUtil.isNotEmpty(wmsStorageReceiptVO.getList())) {
                for (WmsStorageReceiptInfoVO tmp : wmsStorageReceiptVO.getList()) {
                    ErpStockSaveDetailDTO erpCommonSaveDetail = new ErpStockSaveDetailDTO();
                    erpCommonSaveDetail.setFmesentryid(tmp.getId().toString());
                    erpCommonSaveDetail.setFmatno(tmp.getMaterialCode());
                    erpCommonSaveDetail.setFmustqty(tmp.getQuantity());
                    erpCommonSaveDetail.setFrealqty(tmp.getQuantity());
                    erpCommonSaveDetail.setFlot(tmp.getBatch());
                    erpCommonSaveDetail.setFstockno(sourceCode);
                    erpCommonSaveDetails.add(erpCommonSaveDetail);
                }
            }
            erpCommonSaveDTO.setFentry(erpCommonSaveDetails);
            String listR = remoteErpFeignApi.saveInStock(erpCommonSaveDTO);
            //判断是否保存成功
            JSONObject jsonObject = JSONObject.parseObject(listR);
            if (jsonObject.getBoolean(WmsManagerConstant.JSON_SUCCESS)) {
                WmsStorageReceipt wmsStorageReceipt = new WmsStorageReceipt();
                wmsStorageReceipt.setId(id);
                wmsStorageReceipt.setStatus(OaConstant.NUM_2);
                updateById(wmsStorageReceipt);
            } else {
                String error = "同步失败-" + jsonObject.getString(WmsManagerConstant.JSON_MESSAGE);
                log.error(error);
                throw new BusinessException(error);
            }
        }
        return true;
    }

    @Override
    public Boolean deleteData() {
        //备品备件库、包装材料库、辅助材料库 这三个仓库的入库单及库存删除数据
        log.info("开始时间：" + LocalDateTime.now());
        Set<String> names = new HashSet<>();
        names.add("备品备件库");
        names.add("包装材料库");
        names.add("辅助材料库-光伏");
        //获取仓库id
        Map<String, SysOfficeVO> map = FeignOptional.of(() -> remoteOfficeFeign.listByName(names), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<Long> ids = new HashSet<>();
        map.values().forEach(office -> ids.add(Long.valueOf(office.getId())));
        //删除入库单数据
        List<WmsStorageReceipt> list = list(new LambdaQueryWrapper<WmsStorageReceipt>().in(WmsStorageReceipt::getWarehouseId, ids));
//        list.forEach( wmsStorageReceipt -> {
//            wmsStorageReceipt.setCreateBy(System.currentTimeMillis());
//            wmsStorageReceipt.setDelFlag(1);
//        });
        //明细数据
        Set<Long> receiptIdS = list.stream().map(WmsStorageReceipt::getId).collect(Collectors.toSet());
        List<WmsStorageReceiptInfo> infos = wmsStorageReceiptInfoService.list(new LambdaQueryWrapper<WmsStorageReceiptInfo>().in(WmsStorageReceiptInfo::getStorageReceiptId, receiptIdS));
//        infos.forEach( info ->{
//            info.setCreateBy(System.currentTimeMillis());
//            info.setDelFlag(1);
//        });
        List<Long> infoIds = infos.stream().map(WmsStorageReceiptInfo::getId).collect(Collectors.toList());
        List<Long> listIds = list.stream().map(WmsStorageReceipt::getId).collect(Collectors.toList());
        boolean b = removeByIds(listIds);
        boolean b1 = wmsStorageReceiptInfoService.removeByIds(infoIds);
        log.info("软删入库单数据:" + list.size() + b);
        log.info("软删入库单明细数据:" + infos.size() + b1);
        log.info("结束时间：" + LocalDateTime.now());
        return true;
    }

    @Override
    public WmsStorageFlagInfoVO storageFlagInfo(WmsStorageReceiptFlagInfoDTO dto) {
        WmsStorageFlagInfoVO flagInfoVO = new WmsStorageFlagInfoVO();
        //1.通过判断参数中是否存在物料编码判断新老产线
        if (Objects.isNull(dto.getMaterialCode())) {
            //1.1判断箱号在智慧平台是否存在
            Boolean flag = FeignOptional.of(() -> remotePmsFeignApi.getCaseNo(dto.getCaseNo()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            if (flag) {
                flagInfoVO.setCaseNoFlag(1);
            }
        }
        //2.箱号是否关联了入库单
        WmsStorageReceiptInfo receiptInfo = wmsStorageReceiptInfoService.getOne(new LambdaQueryWrapper<WmsStorageReceiptInfo>().eq(WmsStorageReceiptInfo::getCaseNo, dto.getCaseNo()).last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(receiptInfo)) {
            flagInfoVO.setRelevancyFlag(1);
            //2.1 箱号关联的入库单状态是否为已创建
            WmsStorageReceipt receipt = getOne(new LambdaQueryWrapper<WmsStorageReceipt>().eq(WmsStorageReceipt::getId, receiptInfo.getStorageReceiptId()));
            if (WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(receipt.getStatus())) {
                flagInfoVO.setOrderStateCreateFlag(1);
            } else if (WmsManagerConstant.OrderStateEnum.ONE.getCode().equals(receipt.getStatus()) || WmsManagerConstant.OrderStateEnum.THREE.getCode().equals(receipt.getStatus())) {
                flagInfoVO.setOrderStateAffirmFlag(1);
            }
        }
        return flagInfoVO;
    }

    @Override
    public Page<WmsSalesDeliveryVO> salesDeliveryPage(WmsDeliveryOrderDTO dto) {
        List<WmsSalesDeliveryVO> results = Lists.newArrayList();
        //如果缓存中没有值则重新获取token
        String srmToken = stringRedisTemplate.opsForValue().get("SrmOauthToken::SimpleKey []");
        if(StrUtil.isBlank(srmToken)){
            srmToken = remoteSrmFeignApi.getOauthToken();
        }
        //获取数据
        MultiValueMap<String,String> headers = new LinkedMultiValueMap<>();
        SrmOaWorkFlowDTO param = new SrmOaWorkFlowDTO();
        String salesOrder = remoteSrmFeignApi.querySalesOrder(headers, param);
        List<WmsSalesDeliveryTransferVO> wmsSalesDeliveryVOS = JSONObject.parseObject(salesOrder).getJSONArray("content").getJSONObject(0).getJSONArray("asnLineExpList").toJavaList(WmsSalesDeliveryTransferVO.class);
        //存相同物料的caseNo
        Map<String,Integer> caseNoMap = Maps.newHashMap();
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        wmsSalesDeliveryVOS.forEach(tmp->{
            WmsSalesDeliveryVO result = new WmsSalesDeliveryVO();
            EqpSpareVO eqpSpareVO = FeignOptional.of(() -> remoteSpareFeignApi.getByCode(tmp.getEsItemCode()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notNull(eqpSpareVO,"系统中不存在该物料");
            //物料
            result.setMaterialCode(eqpSpareVO.getCode());
            result.setMaterialName(eqpSpareVO.getName());
            result.setMaterialId(eqpSpareVO.getId());
            result.setSpecifications(eqpSpareVO.getSpecifications());
            //箱号
            if(eqpSpareVO.getCode().startsWith("P1.W.P")){
                //箱号规则   六位日期-六位流水()
                Integer caseNo = caseNoMap.getOrDefault(eqpSpareVO.getCode(), null);
                if(caseNo == null) {
                    WmsStorageReceiptInfo caseNoObj = wmsStorageReceiptInfoService.getOne(new LambdaQueryWrapper<WmsStorageReceiptInfo>()
                            .select(WmsStorageReceiptInfo::getCaseNo)
                            .eq(WmsStorageReceiptInfo::getMaterialCode,eqpSpareVO.getCode())
                            .likeRight(WmsStorageReceiptInfo::getCaseNo, yyyyMMdd + "-")
                            .orderByDesc(WmsStorageReceiptInfo::getCaseNo)
                            .last("LIMIT 1"));
                    if(Objects.nonNull(caseNoObj)){
                        String[] split = caseNoObj.getCaseNo().split("-");
                        caseNo = Integer.parseInt(split[1]);
                        caseNo = caseNo + 1;
                    }else {
                        caseNo = 0;
                    }
                }else {
                    caseNo = caseNo + 1;
                }
                String caseNoStr = yyyyMMdd + "-" + String.format("%06d", caseNo);
                caseNoMap.put(eqpSpareVO.getCode(),caseNo);
                result.setCaseNo(caseNoStr);
            }
            //数量
            result.setNum(BigDecimalUtil.subtract(tmp.getShipQuantity(),tmp.getReceiveQuantity(),3));
            //关联单据
            result.setAssociatedNo(tmp.getAsnLineNum().toString());
            //单位
//            result.setUnitName();
//            result.setUnitId();
            results.add(result);
        });
        Page<WmsSalesDeliveryVO> page = LogicalPageUtil.page(results, dto.getCurrent(), dto.getSize());
        return page;
    }

    @Override
    public String produceOldLineAdd(WmsStorageReceiptDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_STORAGE_RECEIPT);
        lock.lock();
        String code;
        try {
            //1.校验数据是否存在
            Assert.isTrue(Objects.nonNull(dto), "入库凭证信息为空");
            //2.定义数据模型
            WmsStorageReceipt model = new WmsStorageReceipt();
            BeanUtil.copyProperties(dto, model);
            //3.给当前入库单生成编码
            code = WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_STORAGE_RECEIPT, WmsCodeUtil.NUM_BIT_4, currentMaxCode());
            model.setCode(code);
            model.setStatus(WmsManagerConstant.OrderStateEnum.ZERO.getCode());
            //获取完工入库类型
            SysDictVO dictVO = FeignOptional.of(()->remoteDictFeignApi.getByCode(WmsManagerConstant.Code_ZERO, WmsManagerConstant.WAREHOUSING_TYPE),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            model.setType(dictVO.getId());
            //4.保存入库单信息
            save(model);
            //5.保存入库单明细信息
            if (Objects.nonNull(dto.getList())) {
                List<WmsInventoryDTO> infoList = Lists.newArrayList();
                List<WmsStorageReceiptInfo> receiptInfoList = Lists.newArrayList();
                Long caseNo = getCaseNo();
                List<WmsStorageReceiptInfoDTO> list = dto.getList();
                List<String> caseNos = Lists.newArrayList();
                List<String> materialCodes = Lists.newArrayList();
                for (WmsStorageReceiptInfoDTO info : list) {
                    if (Objects.isNull(info.getCaseNo()) || "".equals(info.getCaseNo())) {
                        info.setCaseNo(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_CASE_NO, WmsCodeUtil.NUM_BIT_4, caseNo));
                        caseNo++;
                    }
                    WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                    wmsInventoryDTO.setWarehouseId(dto.getWarehouseId());
                    wmsInventoryDTO.setWarehouseName(dto.getWarehouseName());
                    BeanUtil.copyProperties(info, wmsInventoryDTO);
                    WmsStorageReceiptInfo wmsStorageReceiptInfo = new WmsStorageReceiptInfo();
                    wmsStorageReceiptInfo.setStorageReceiptId(model.getId());
                    BeanUtil.copyProperties(info, wmsStorageReceiptInfo);
                    caseNos.add(wmsInventoryDTO.getCaseNo());
                    materialCodes.add(wmsInventoryDTO.getMaterialCode());
                    infoList.add(wmsInventoryDTO);
                    receiptInfoList.add(wmsStorageReceiptInfo);
                }
                //根据物料编码获取物料信息
                Map<String, EqpSpareVO> materialMap= FeignOptional.of(() -> remoteSpareFeignApi.listByCode(materialCodes), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                receiptInfoList.forEach( info->{
                    EqpSpareVO eqpSpareVO = materialMap.get(info.getMaterialCode());
                    Assert.isTrue(Objects.nonNull(eqpSpareVO),info.getMaterialCode()+"在物料清单中不存在，请检查");
                    info.setMaterialId(eqpSpareVO.getId());
                    info.setMaterialName(eqpSpareVO.getName());
                    info.setUnit(eqpSpareVO.getUnitId());
                });
                infoList.forEach( info->{
                    EqpSpareVO eqpSpareVO = materialMap.get(info.getMaterialCode());
                    Assert.isTrue(Objects.nonNull(eqpSpareVO),info.getMaterialCode()+"在物料清单中不存在，请检查");
                    info.setMaterialId(eqpSpareVO.getId().toString());
                    info.setMaterialName(eqpSpareVO.getName());
                    info.setUnit(eqpSpareVO.getUnitId());
                });
                long count = infoList.stream().distinct().count();
                Assert.isFalse(count < infoList.size(), "箱号重复，请修改");
                //增加库存信息
                wmsInventoryService.addInfo(infoList);
                //保存明细信息
                wmsStorageReceiptInfoService.saveBatch(receiptInfoList);
            }
        } finally {
            lock.unlock();
        }
        return code;
    }

    @Override
    public String inStorageCodeRepeat(LocalDate current) {
        Long aLong = currentMaxCode();
        return Objects.isNull(aLong) ? String.valueOf(1) : String.valueOf(aLong);
    }

    WmsDeliveryReceiptDTO chargeAgainst(Long id, String userName) {
        RLock lock = redissonClient.getLock(LockConstant.WMS_CHARGE_AGAINST);
        lock.lock();
        WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO1 = new WmsDeliveryReceiptDTO();
        try {
            // 获取当前入库单明细
            WmsStorageReceiptVO wmsStorageReceiptVO = get(id);
            //校验状态
            Assert.isTrue(WmsManagerConstant.OrderStateEnum.ONE.getCode().equals(wmsStorageReceiptVO.getStatus()), "当前状态不可冲销出库");
            List<WmsStorageReceiptInfoVO> list = wmsStorageReceiptVO.getList();
            if (CollUtil.isNotEmpty(list)) {
                //获取出库类型
                SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(WmsManagerConstant.CODE_FOUR, WmsManagerConstant.OUTBOUND_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                // 获取库存中物料的库存信息
                Set<Long> materialId = list.stream().map(WmsStorageReceiptInfoVO::getMaterialId).collect(Collectors.toSet());
                Map<Long, List<WmsInventory>> inventoryMap = wmsInventoryService.inventoryMap(materialId);
                //生成出库单
                WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO = new WmsDeliveryReceiptDTO();
                wmsDeliveryReceiptDTO.setType(sysDictVO.getId());
                wmsDeliveryReceiptDTO.setWarehouseId(wmsStorageReceiptVO.getWarehouseId());
                List<WmsDeliveryReceiptInfoDTO> infoDTOList = Lists.newArrayList();
                List<WmsInventory> wmsInventoryList = Lists.newArrayList();
                for (WmsStorageReceiptInfoVO info : list) {
                    //处理库存数据
                    List<WmsInventory> inventoryList = inventoryMap.get(info.getMaterialId());
                    Assert.isTrue(CollUtil.isNotEmpty(inventoryList), info.getMaterialName() + "库存不足,冲销失败");
                    BigDecimal quantity = info.getQuantity();
                    for (WmsInventory inventory : inventoryList) {
                        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
                            break;
                        }
                        WmsInventory wmsInventory = new WmsInventory();
                        WmsDeliveryReceiptInfoDTO wmsDeliveryReceiptInfo = new WmsDeliveryReceiptInfoDTO();
                        BeanUtil.copyProperties(inventory, wmsInventory);
                        wmsDeliveryReceiptInfo.setInventoryId(inventory.getId());
                        wmsDeliveryReceiptInfo.setMaterialId(inventory.getMaterialId());
                        if (quantity.compareTo(inventory.getAvailableQuantity()) <= 0) {
                            wmsInventory.setAvailableQuantity(wmsInventory.getAvailableQuantity().subtract(quantity));
                            wmsDeliveryReceiptInfo.setQuantity(quantity);
                            quantity = BigDecimal.ZERO;
                        } else {
                            quantity = quantity.subtract(inventory.getAvailableQuantity());
                            wmsInventory.setAvailableQuantity(BigDecimal.ZERO);
                            wmsDeliveryReceiptInfo.setQuantity(inventory.getAvailableQuantity());
                        }
                        wmsInventoryList.add(wmsInventory);
                        infoDTOList.add(wmsDeliveryReceiptInfo);
                    }
                }
                wmsDeliveryReceiptDTO.setList(infoDTOList);
                //生成出库单并自动确认
                wmsDeliveryReceiptDTO1 = wmsDeliveryReceiptService.chargeAgainst(wmsDeliveryReceiptDTO, userName);
                //扣除库存
                wmsInventoryService.deduct(wmsInventoryList);
                //修改入库单状态为已冲销
                WmsStorageReceipt receipt = getById(id);
                receipt.setStatus(WmsManagerConstant.OrderStateEnum.FIVE.getCode());
                updateById(receipt);
            }
        } finally {
            lock.unlock();
        }
        return wmsDeliveryReceiptDTO1;
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
        WmsStorageReceipt maxCodeModel = getOne(new LambdaQueryWrapper<WmsStorageReceipt>().between(WmsStorageReceipt::getCreateTime, min, max).orderByDesc(WmsStorageReceipt::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(WmsCodeUtil.PREFIX_WMS_STORAGE_RECEIPT.length() + 10);
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }

    private Long getCaseNo() {
        // 定义返回值
        Long currentMax = 0L;
        //获取规则箱号
        LocalDateTime max = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        WmsStorageReceiptInfo maxCodeModel = wmsStorageReceiptInfoService.getOne(new LambdaQueryWrapper<WmsStorageReceiptInfo>().between(WmsStorageReceiptInfo::getCreateTime, min, max).orderByDesc(WmsStorageReceiptInfo::getCaseNo).last(MpConstant.STR_LIMIT_1));
//        WmsStorageReceiptInfo maxCodeModel = wmsStorageReceiptInfoService.getOne(new LambdaQueryWrapper<WmsStorageReceiptInfo>()
//                .like(WmsStorageReceiptInfo::getCaseNo, code)
//                .eq(WmsStorageReceiptInfo::getStorageReceiptId, id)
//                .orderByDesc(WmsStorageReceiptInfo::getCaseNo)
//                .last(MpConstant.STR_LIMIT_1));
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCaseNo().substring(WmsCodeUtil.PREFIX_CASE_NO.length() + WmsCodeUtil.NUM_BIT_12);
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




