package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.fastjson.JSONArray;
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
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.dto.QtSeriesDataDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.R;
import com.steccn.mes.dto.*;
import com.steccn.mes.feign.RemoteErpFeignApi;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.dto.*;
import com.steccn.platform.feign.*;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.*;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.dto.PmsProducePlanNewExcelDTO;
import com.steccn.pms.dto.WmsStorageReceiptExcelDTO;
import com.steccn.pms.util.UnitConvertUtil;
import com.steccn.pms.vo.*;
import com.steccn.pms.feign.RemoteCompleteStorageFeignApi;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.pms.vo.MesClassificationVO;
import com.steccn.wms.config.SrmConfig;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.*;
import com.steccn.wms.dto.*;
import com.steccn.wms.mapper.WmsDeliveryReceiptMapper;
import com.steccn.wms.mapper.WmsStorageReceiptMapper;
import com.steccn.wms.service.*;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
    private final SrmConfig srmConfig;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final RedissonClient redissonClient;

    private final WmsInventoryService wmsInventoryService;

    private final WmsStorageReceiptInfoService wmsStorageReceiptInfoService;

    private final RemoteCompleteStorageFeignApi remoteCompleteStorageFeignApi;

    private final RemoteSpareFeignApi remoteSpareFeignApi;

    private final WmsDeliveryReceiptService wmsDeliveryReceiptService;
    private final WmsDeliveryReceiptMapper wmsDeliveryReceiptMapper;

    private final RemoteErpFeignApi remoteErpFeignApi;

    private final RemotePmsFeignApi remotePmsFeignApi;

    private final WmsStorageReceiptMapper wmsStorageReceiptMapper;

    private final RemoteSrmFeignApi remoteSrmFeignApi;

    private final StringRedisTemplate stringRedisTemplate;

    private final WmsInventoryRecordService wmsInventoryRecordService;

    private final RemoteMessFeignApi remoteMessFeignApi;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;

    private final RemoteSpareUnitConvertFeignApi remoteSpareUnitConvertFeignApi;

    private final RemoteSalesFeignApi remoteSalesFeignApi;

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
                officeSet.add(receipt.getShelfId());
            });
            Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(officeSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(typeSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            records.forEach(receipt -> {
                receipt.setTypeName(dictMap.get(receipt.getType()));
                SysOfficeVO whSysOffice = officeMap.getOrDefault(receipt.getWarehouseId(), new SysOfficeVO());
                receipt.setWarehouseName(whSysOffice.getOfficeName());
                receipt.setWarehouseCode(whSysOffice.getOfficeCode());
                SysOfficeVO shelfSysOffice = officeMap.getOrDefault(receipt.getShelfId(), new SysOfficeVO());
                receipt.setShelfName(shelfSysOffice.getOfficeName());
                receipt.setShelfCode(shelfSysOffice.getOfficeCode());
            });
        }
        //返回数据
        return page;
    }

    @Override
    @Transactional(readOnly = false)
    public Boolean add(WmsStorageReceiptDTO dto) {
        addReturnId(dto);
        return true;
    }

    /**
     * 返回id的新增方法
     * @param dto
     * @return
     */
    @Transactional(readOnly = false)
    public Long addReturnId(WmsStorageReceiptDTO dto){
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_STORAGE_RECEIPT);
        lock.lock();
        Long id = null;
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
                //采购入库存相同物料的caseNo
                Map<String,Integer> caseNoMap = Maps.newHashMap();
                String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
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
                    //如果是采购入库则生成箱号
                    if(dto.getType().equals(1600405739121573888L)&&dto.getIsSRMSys()!=null&&dto.getIsSRMSys().equals(OaConstant.NUM_2)){
                        EqpSpareVO eqpSpareVO = FeignOptional.of(() -> remoteSpareFeignApi.getByCode(info.getMaterialCode()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                        //箱号
                        if(eqpSpareVO.getCode().startsWith("P1.W.P")){
                            //箱号规则   六位日期-六位流水()
                            Integer caseNo1 = caseNoMap.getOrDefault(eqpSpareVO.getCode(), null);
                            if(caseNo1 == null) {
                                WmsStorageReceiptInfo caseNoObj = wmsStorageReceiptInfoService.getOne(new LambdaQueryWrapper<WmsStorageReceiptInfo>()
                                        .select(WmsStorageReceiptInfo::getCaseNo)
                                        .eq(WmsStorageReceiptInfo::getMaterialCode,eqpSpareVO.getCode())
                                        .likeRight(WmsStorageReceiptInfo::getCaseNo, yyyyMMdd + "-")
                                        .orderByDesc(WmsStorageReceiptInfo::getCaseNo)
                                        .last("LIMIT 1"));
                                if(Objects.nonNull(caseNoObj)){
                                    String[] split = caseNoObj.getCaseNo().split("-");
                                    caseNo1 = Integer.parseInt(split[1]);
                                    caseNo1 = caseNo1 + 1;
                                }else {
                                    caseNo1 = 0;
                                }
                            }else {
                                caseNo1 = caseNo1 + 1;
                            }
                            String caseNoStr = yyyyMMdd + "-" + String.format("%06d", caseNo1);
                            caseNoMap.put(eqpSpareVO.getCode(),caseNo1);
                            info.setCaseNo(caseNoStr);
                        }

                    } else if (Objects.isNull(info.getCaseNo()) || "".equals(info.getCaseNo())) {
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
            //6.如果是采购单且来源是采购地磅系统,则自动确认
            if(dto.getType().equals(1600405739121573888L)&&dto.getIsSRMSys()!=null&&dto.getIsSRMSys().equals(OaConstant.NUM_1)){
                dto.setId(model.getId());
                wmsStorageReceiptMapper.salesAutoConfirm(dto);
            }
            //6.如果是采购单且来源是采购系统,则自动确认
            if(dto.getType().equals(1600405739121573888L)&&dto.getIsSRMSys()!=null&&dto.getIsSRMSys().equals(OaConstant.NUM_2)){
                affirm(model.getId(),SecurityUtils.getUser().getName());
            }

            id = model.getId();
        } finally {
            lock.unlock();
        }
        return id;
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
                    material.setErpLineId(tmp.getErpLineId());
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
            receiptDTO.setErpReceiptId(dto.getErpReceiptId());
            receiptDTO.setErpReceiptCode(dto.getErpReceiptCode());
            receiptDTO.setSourceType(OaConstant.NUM_1);
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
                    wmsInventoryDTO.setCreateBy(dto.getCreateBy());
                    wmsInventoryDTO.setCreateName(dto.getCreateName());
                    wmsInventoryDTO.setUpdateBy(dto.getUpdateBy());
                    wmsInventoryDTO.setUpdateName(dto.getUpdateName());
                    WmsStorageReceiptInfo wmsStorageReceiptInfo = new WmsStorageReceiptInfo();
                    wmsStorageReceiptInfo.setStorageReceiptId(model.getId());
                    BeanUtil.copyProperties(info, wmsStorageReceiptInfo);
                    caseNos.add(wmsInventoryDTO.getCaseNo());
                    infoList.add(wmsInventoryDTO);
                    receiptInfoList.add(wmsStorageReceiptInfo);
                }
                long count = infoList.stream().distinct().count();
                Assert.isFalse(count < infoList.size(), "箱号重复，请修改");
                //生成库存变化
                wmsInventoryService.addInfo(infoList,model.getCode(),dto.getWarehouseId(), dto.getType());

//                //增加库存信息
//                wmsInventoryService.addInfo(infoList);
                //保存明细信息
                wmsStorageReceiptInfoService.saveBatch(receiptInfoList);
            }
            //如果是销售退货单,则自动确认
            if (dto.getType().equals(1600405825377435648L)){
                affirm(model.getId(),SecurityUtils.getUser().getName());
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
//                    if (receiptType == 1) {
//                        PmsProduceOrderVO pmsProduceOrderVO = FeignOptional.of(() -> remotePmsFeignApi.allCode(info.getAssociatedNo()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
//                        if (pmsProduceOrderVO.getId() == null) {
//                            throw new BusinessException("关联的生产订单不存在");
//                        }
//                        List<PmsWorkOrderBatchVO> pmsWorkOrderBatchVOS = FeignOptional.of(() -> remotePmsFeignApi.getAllBatchByOrderId(pmsProduceOrderVO.getId()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
//                        if (CollUtil.isEmpty(pmsWorkOrderBatchVOS)) {
//                            throw new BusinessException("该订单下没有批次信息");
//                        }
//                        if (Objects.isNull(pmsWorkOrderBatchVOS.stream().filter(v -> v.getCode().equals(info.getBatch())).findFirst().orElse(null))) {
//                            throw new BusinessException("与该订单下的批次信息不符合");
//                        }
//                    }
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
            }else {
                LambdaQueryWrapper<WmsStorageReceiptInfo> wrapper = new LambdaQueryWrapper<WmsStorageReceiptInfo>().eq(WmsStorageReceiptInfo::getStorageReceiptId, model.getId());
                //获取需要删除的数据
                List<WmsStorageReceiptInfo> list = wmsStorageReceiptInfoService.list(wrapper);
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
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    @Transactional(readOnly = false)
    public Boolean affirm(Long id,String userName) {
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
            //生成库存流水记录
            //获取库存变动类型
            SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.get(receipt.getType()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            String type = WmsManagerConstant.variableTypeEnum.getTypeByName(sysDictVO.getName());
            if (Objects.isNull(type)){
                type = WmsManagerConstant.variableTypeEnum.getTypeByValue(sysDictVO.getName());
            }
            if (CollUtil.isNotEmpty(inventoryDTOList)) {
                List<String> caseNoList = Lists.newArrayList();
                inventoryDTOList.forEach(wmsInventoryDTO -> {
                    caseNoList.add(wmsInventoryDTO.getCaseNo());
                });
                WmsInventory wmsInventory = new WmsInventory();
                List<WmsInventoryRecordDTO> wmsInventoryRecords = Lists.newArrayList();
                Map<String, WmsInventory> inventoryByCaseNo = wmsInventoryService.getInventoryByCaseNo(caseNoList);
                String finalType = type;
                SysDictVO dictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(finalType, WmsManagerConstant.VARIATION_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    inventoryDTOList.forEach(info -> {
                        WmsInventory orDefault = inventoryByCaseNo.getOrDefault(info.getCaseNo(), wmsInventory);
                        WmsInventoryRecordDTO wmsInventoryRecord = new WmsInventoryRecordDTO();
                        wmsInventoryRecord.setMaterialId(info.getMaterialId());
                        wmsInventoryRecord.setType(dictVO.getId());
                        wmsInventoryRecord.setCode(receipt.getCode());
                        wmsInventoryRecord.setMaterialName(info.getMaterialName());
                        wmsInventoryRecord.setShelfId(orDefault.getShelfId());
                        wmsInventoryRecord.setWarehouseId(orDefault.getWarehouseId());
                        //变动数量
                        wmsInventoryRecord.setVariableQuantity(info.getQuantity());
                        //变动前
                        wmsInventoryRecord.setFrontAvailableQuantity(orDefault.getAvailableQuantity());
                        wmsInventoryRecord.setFrontStockQuantity(orDefault.getStockQuantity());
                        wmsInventoryRecord.setFrontLockQuantity(orDefault.getLockQuantity());
                        wmsInventoryRecord.setFrontTotalQuantity(orDefault.getTotalQuantity());
                        //变动后
                        wmsInventoryRecord.setBackAvailableQuantity(orDefault.getAvailableQuantity().add(info.getQuantity()));
                        wmsInventoryRecord.setBackStockQuantity(orDefault.getStockQuantity().subtract(info.getQuantity()));
                        wmsInventoryRecord.setBackTotalQuantity(orDefault.getTotalQuantity().add(info.getQuantity()));
                        wmsInventoryRecord.setBackLockQuantity(orDefault.getLockQuantity());
                        if (Objects.isNull(wmsInventoryRecord.getWarehouseId())) {
                            wmsInventoryRecord.setWarehouseId(Math.toIntExact(receipt.getWarehouseId()));
                            wmsInventoryRecord.setShelfId(info.getShelfId());
                        }
                        wmsInventoryRecords.add(wmsInventoryRecord);
                    });
                    //生成库存流水记录
                    List<WmsInventoryRecord> recordList = BeanUtil.copyToList(wmsInventoryRecords, WmsInventoryRecord.class);
                    wmsInventoryRecordService.saveBatch(recordList);
                    Boolean aBoolean = wmsInventoryService.affirm(inventoryDTOList);
                }
            }
        //4.修改确认单状态
        receipt.setAffirmName(userName);
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
            if(count < infoList.size()){
                Set<String> duplicateCaseNos = infoList.stream()
                        .filter(Objects::nonNull)
                        .map(WmsInventoryDTO::getCaseNo) // 只要在实例上用就没问题
                        .filter(Objects::nonNull)
                        .collect(Collectors.groupingBy(v -> v, Collectors.counting()))
                        .entrySet().stream()
                        .filter(entry -> entry.getValue() > 1)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());
                Assert.isFalse(count < infoList.size(), "箱号"+duplicateCaseNos.toString()+"重复，请修改");
            }
            //增加库存信息
            wmsInventoryService.addInfo(infoList);
            //保存明细信息
            wmsStorageReceiptInfoService.saveBatch(receiptInfoList);
        }
        if (Objects.isNull(dto.getCreateName())){
            dto.setCreateName(SecurityUtils.getUser().getName());
        }
        affirm(model.getId(),dto.getCreateName());
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
//               保存库存信息
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
        chargeAgainst(id, SecurityUtils.getUser().getName());
        WmsStorageReceiptVO wmsStorageReceiptVO = get(id);
        //查询该入库单是否在生产入库单中存在
        Boolean aBoolean = FeignOptional.of(() -> remoteCompleteStorageFeignApi.getCompleteStorageByCode(wmsStorageReceiptVO.getCode()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
        if (aBoolean) {
            //在pms对入库中对应的箱状态进行修改
            PmsCompleteStorageDTO pmsCompleteStorageDTO = new PmsCompleteStorageDTO();
            pmsCompleteStorageDTO.setCode(wmsStorageReceiptVO.getCode());
            pmsCompleteStorageDTO.setConfirmName(SecurityUtils.getUser().getName());
            FeignOptional.of(() -> remoteCompleteStorageFeignApi.completeStorageStatue(pmsCompleteStorageDTO), FeignConstant.STECCN_PMS_SERVICE_NAME);
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
        //获取erp中的code
        Map<String, List<SysConfigurationDTO>> erpWarehouse = FeignOptional.of(() -> remoteSysConfigurationFeignApi.confList(new TupleDTO<>("ERP_WAREHOUSE", Sets.newHashSet(sourceCode))), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if(CollUtil.isEmpty(erpWarehouse)){
            throw new BusinessException("erp中没有对应的仓库");
        }
        String erpStockCode = erpWarehouse.get(sourceCode).get(0).getRefContent();
        //类型为生产完工入库
        if (wmsStorageReceiptVO.getTypeName().equals(WmsManagerConstant.RECEIPT_TYPE_1)) {
            ErpCommonSaveDTO<ErpSpinStockSaveDetailDTO> erpCommonSaveDTO = new ErpCommonSaveDTO<>();
            erpCommonSaveDTO.setFdate(wmsStorageReceiptVO.getCreateTime().toLocalDate());
            erpCommonSaveDTO.setFmesbillid(wmsStorageReceiptVO.getId());
            erpCommonSaveDTO.setFmesbillno(wmsStorageReceiptVO.getCode());
            //引入详细信息
            List<ErpSpinStockSaveDetailDTO> erpCommonSaveDetails = Lists.newArrayList();
            //获取生产完工入库
            PmsCompleteStorageVO pmsCompleteStorageVO = FeignOptional.of(() -> remoteCompleteStorageFeignApi.getCompleteStorage(wmsStorageReceiptVO.getCode()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
            erpCommonSaveDTO.setFdate(pmsCompleteStorageVO.getInStorageTime().toLocalDate());
            //拿到epr关联的code
            String erpDepCode = "";
            if(!Objects.isNull(pmsCompleteStorageVO.getProductLineId())){
                String officeName = FeignOptional.of(() -> remoteOfficeFeign.getNameById(pmsCompleteStorageVO.getProductLineId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                erpDepCode = WmsManagerConstant.ErpProductionLineCodeEnum.getCodeByName(officeName);
            }
            if (CollUtil.isNotEmpty(wmsStorageReceiptVO.getList())) {
                Set<String> productLineCodes = wmsStorageReceiptVO.getList().stream().filter(v -> Objects.nonNull(v.getProductionLineName())).map(WmsStorageReceiptInfoVO::getProductionLineName).collect(Collectors.toSet());
                Map<String, SysOfficeVO> stringSysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listByCodeSet(productLineCodes), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                for (WmsStorageReceiptInfoVO tmp : wmsStorageReceiptVO.getList()) {
                    ErpSpinStockSaveDetailDTO erpCommonSaveDetail = new ErpSpinStockSaveDetailDTO();
                    erpCommonSaveDetail.setFmesentryid(tmp.getId().toString());
                    erpCommonSaveDetail.setFmatno(tmp.getMaterialCode());
                    erpCommonSaveDetail.setFmustqty(tmp.getQuantity());
                    erpCommonSaveDetail.setFrealqty(tmp.getQuantity());
                    //YWZ需求 批号为箱号
//                    erpCommonSaveDetail.setFlot(tmp.getCaseNo());
                    erpCommonSaveDetail.setFlot(WmsCodeUtil.getErpFlotCode(tmp.getMaterialName(),stringSysOfficeVOMap.get(tmp.getProductionLineName()).getParentCode(),String.valueOf(tmp.getQuantity().intValue())));
                    erpCommonSaveDetail.setFstockno(erpStockCode);
                    if (StrUtil.isBlank(tmp.getErpLineCode())) {
                        if(StrUtil.isBlank(erpDepCode)){
                            throw new BusinessException("没有erp对应的部门");
                        }else {
                            erpCommonSaveDetail.setFdeptno(erpDepCode);
                        }
                    }else {
                        erpCommonSaveDetail.setFdeptno(tmp.getErpLineCode());
                    }
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
                    erpCommonSaveDetail.setFlot(OaConstant.NO.toString());
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
        WmsStorageReceiptInfo receiptInfo =  wmsStorageReceiptInfoService.getByCaseNo(dto.getCaseNo());
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
            srmToken = JSONObject.parseObject(remoteSrmFeignApi.getOauthToken()).getString("access_token");;
        }
        //获取数据
        Map<String,String> headers = Maps.newHashMap();
        headers.put("Authorization",srmToken);
        SrmDeliveryDTO deliveryDTO = new SrmDeliveryDTO();
        deliveryDTO.setDateFrom(LocalDateTime.of(2000,1,1,0,0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        deliveryDTO.setDateTo(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        deliveryDTO.setDocCodes(com.google.common.collect.Lists.newArrayList(dto.getDeliveryOrderCode()));
        deliveryDTO.setPage(OaConstant.NO);
        deliveryDTO.setSize(10000);
        deliveryDTO.setExternalSystemCode("MES");
        SrmOaWorkFlowDTO param = new SrmOaWorkFlowDTO();
        param.setPayload(new cn.hutool.json.JSONObject(new SrmDeliveryBodyDTO(deliveryDTO)).toString());
//        String salesOrder = "";
//        String salesOrder = remoteSrmFeignApi.querySalesOrder(headers, param);
        String salesOrder = HttpUtil.createPost(srmConfig.getSrmIp()+"/hitf/v1/rest/invoke?namespace=SRM&serverCode=DELIVERY_NOTE_INTERFACE&interfaceCode=srm-interface.asn-export.pull").addHeaders(headers).body(new cn.hutool.json.JSONObject(param).toString()).execute().body();
        JSONObject jsonObject = JSONObject.parseObject(salesOrder).getJSONObject("payload");
        JSONArray jsonArray = jsonObject.getJSONArray("content");
        if(jsonArray.size() == 0){
            throw new BusinessException("收料单不存在");
        }
        String orderStatus = jsonArray.getJSONObject(0).getString("asnStatus");
        //校验单据状态
        if(!orderStatus.equals(WmsManagerConstant.SalesOrderStateEnum.SHIPPED.getCode())){
            if(orderStatus.equals(WmsManagerConstant.SalesOrderStateEnum.CLOSED.getCode())){
                throw new BusinessException("收料单已关闭");
            }else {
                throw new BusinessException("收料单不存在");
            }
        }
        List<WmsSalesDeliveryTransferVO> wmsSalesDeliveryVOS = jsonArray.getJSONObject(0).getJSONArray("asnLineExpList").toJavaList(WmsSalesDeliveryTransferVO.class);
        //获取供应商
        String esSupplierCode = jsonArray.getJSONObject(0).getString("esSupplierCode");
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
            result.setDeliveryOrderCode(dto.getDeliveryOrderCode());
            result.setEsSupplierCode(esSupplierCode);
            result.setEsFromPoNum(tmp.getEsPoNum());
            result.setEsFromPoLineNum(tmp.getEsPoLineNum());
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
            result.setQuantity(BigDecimalUtil.subtract(tmp.getShipQuantity(),tmp.getReceiveQuantity(),3));
            //关联单据
            result.setAssociatedNo(tmp.getAsnLineNum().toString());
            //单位
            if(StrUtil.isNotBlank(tmp.getEsUomCode())){
                SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(tmp.getEsUomCode(), "measure_unit"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                result.setUnitName(sysDictVO.getName());
                result.setUnitId(sysDictVO.getId());
            }else {
                result.setUnitName(eqpSpareVO.getUnit());
                result.setUnitId(eqpSpareVO.getUnitId());
            }
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
            if (Objects.isNull(dto.getType())) {
                SysDictVO dictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(WmsManagerConstant.Code_ZERO, WmsManagerConstant.WAREHOUSING_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                model.setType(dictVO.getId());
            }
            model.setComeTime(dto.getComeTime());
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
                    info.setMaterialId(eqpSpareVO.getId());
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
    @Transactional
    public String salesReceiptAdd(SalesStorageReceiptDTO dto) {
        dto.setIsSRMSys(OaConstant.NUM_2);
        dto.setSourceType(OaConstant.NUM_2);
        dto.setType(1600405739121573888L);
        //调用事务同步接口
        //如果缓存中没有值则重新获取token
        String srmToken = stringRedisTemplate.opsForValue().get("SrmOauthToken::SimpleKey []");
        if(StrUtil.isBlank(srmToken)){
            srmToken = JSONObject.parseObject(remoteSrmFeignApi.getOauthToken()).getString("access_token");;
        }
        //获取数据
        Map<String,String> headers = Maps.newHashMap();
        headers.put("Authorization",srmToken);
        SalesTransactionDTO param = new SalesTransactionDTO();
        //组头部
        SalesTransactionHeaderDTO paramHeader = new SalesTransactionHeaderDTO();
        paramHeader.setApplicationCode(srmConfig.getApplicationCode());
        paramHeader.setApplicationGroupCode(srmConfig.getApplicationGroupCode());
        paramHeader.setBatchNum(String.valueOf(LocalDateTime.now().toInstant(ZoneOffset.UTC).getEpochSecond()));
        paramHeader.setInterfaceCode(srmConfig.getInterfaceCode());
        paramHeader.setUserName("");
        paramHeader.setBatchCount(OaConstant.NUM_1.toString());
        paramHeader.setExternalSystemCode(srmConfig.getExternalSystemCode());
        param.setHeader(paramHeader);
        //组请求体
        Long id = addReturnId(dto);
        SalesTransactionBodyDTO paramBody = new SalesTransactionBodyDTO();
        WmsStorageReceipt byId = getById(id);
        paramBody.setEsTrxNum(byId.getCode());
        paramBody.setTrxYear(Long.parseLong(String.valueOf(LocalDate.now().getYear())));
        paramBody.setEsOuCode("CO00000001");
        paramBody.setSourceCode("MES");
        String localDateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        paramBody.setTrxDate(localDateStr);
        //请求体详情
        List<SalesTransactionBodyDetailDTO> bodyDetail = Lists.newArrayList();
        dto.getList().forEach(tmp->{
            SalesTransactionBodyDetailDTO bodyDetailTmp = new SalesTransactionBodyDetailDTO();
            if(StrUtil.isBlank(tmp.getAssociatedNo())){
                throw new BusinessException("关联单据不能为空");
            }
            bodyDetailTmp.setEsTrxLineNum(tmp.getAssociatedNo());
            bodyDetailTmp.setTrxDate(localDateStr);
            bodyDetailTmp.setEsTrxTypeCode("ERP01");
            bodyDetailTmp.setQuantity(tmp.getQuantity());
            bodyDetailTmp.setStockType("OWN_STOCK");
            bodyDetailTmp.setEsOuCode("CO00000001");
            bodyDetailTmp.setEsSupplierNum(dto.getEsSupplierCode());
            bodyDetailTmp.setCurrencyCode("CNY");
            Assert.notBlank(tmp.getEsFromPoNum(),"外部系统订单头不能为空");
            bodyDetailTmp.setEsFromPoNum(tmp.getEsFromPoNum());
            Assert.notBlank(tmp.getEsFromPoLineNum(),"外部系统订单行号不能为空");
            bodyDetailTmp.setEsFromPoLineNum(tmp.getEsFromPoLineNum());
            bodyDetail.add(bodyDetailTmp);
        });
        paramBody.setRcvTrxLineList(bodyDetail);
        param.setBody(com.google.common.collect.Lists.newArrayList(paramBody));
        String paramStr = new cn.hutool.json.JSONObject(param).toString();
        SrmOaWorkFlowDTO payload = new SrmOaWorkFlowDTO();
        payload.setPayload(paramStr);
        String result = HttpUtil.createPost(srmConfig.getSrmIp()+"/hitf/v1/rest/invoke?namespace=SRM&serverCode=DELIVERY_NOTE_INTERFACE&interfaceCode=srm-interface.rcv-trx-header.receiveRestRcvTrxHeaderData").addHeaders(headers).body(new cn.hutool.json.JSONObject(payload).toString()).execute().body();
        JSONObject payloadObj = JSONObject.parseObject(result).getJSONObject("payload");
        if(payloadObj.getString("responseStatus").equals("ERROR")){
            throw new BusinessException(payloadObj.getString("responseMessage"));
        }
        if(payloadObj.getJSONArray("restResponseDtlDTOList").getJSONObject(0).getString("responseStatus").equals("ERROR")){
            throw new BusinessException(payloadObj.getJSONArray("restResponseDtlDTOList").getJSONObject(0).getString("responseMessage"));
        }
        return byId.getCode();
    }

    @Override
    public Map<Long, String> rwLinkSupplier(Set<Long> rwIdSet) {
        Map<Long,String> resMap = Maps.newHashMap();
        //根据报工主键查询箱号
        if(CollUtil.isNotEmpty(rwIdSet)){
            List<WmsDeliveryReceipt> receiptList =  wmsDeliveryReceiptMapper.rwLinkCaseNo(rwIdSet);
            if(CollUtil.isNotEmpty(receiptList)){
                //根据箱号，查询供应商code,应该是一个箱号 对应一个供应商
                Set<String> caseNoSet = receiptList.stream().map(WmsDeliveryReceipt::getCode).collect(Collectors.toSet());
                List<WmsStorageReceipt> caseNoSupplierList = wmsStorageReceiptMapper.caseNoLinkSupplier(caseNoSet);
                if(CollUtil.isNotEmpty(caseNoSupplierList)){
                    Map<String,String> caseNoLinkSupplierMap = Maps.newHashMap();
                    caseNoSupplierList.forEach(ele -> caseNoLinkSupplierMap.put(ele.getCode(),ele.getEsSupplierCode()));
                    receiptList.forEach(ele ->{
                        String supplierCode = caseNoLinkSupplierMap.get(ele.getCode());
                        if(StrUtil.isNotBlank(supplierCode)){
                            resMap.put(ele.getLinkedId(),supplierCode);
                        }
                    });
                }
            }
        }
        return resMap;
    }


    @Override
    public Page<WmsReportWarehouseVO> warehouseWarrant(WmsReportWarehouseDTO dto) {
        //处理时间
        if (Objects.nonNull(dto.getDate())) {
            dto.setStartTime(LocalDateTime.of(dto.getDate(), LocalTime.MIN));
            dto.setEndTime(LocalDateTime.of(dto.getDate(), LocalTime.MAX));
        }
        List<WmsReportWarehouseVO> resList = Lists.newArrayList();
        List<WmsStorageReceipt> list = list(new LambdaQueryWrapper<WmsStorageReceipt>()
                .eq(Objects.nonNull(dto.getStatus()),WmsStorageReceipt::getStatus, dto.getStatus())
                .eq(Objects.nonNull(dto.getTypeId()),WmsStorageReceipt::getType, dto.getTypeId())
                .between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()),WmsStorageReceipt::getCreateTime,dto.getStartTime(),dto.getEndTime())
                .in(CollUtil.isNotEmpty(dto.getWarehouseIds()),WmsStorageReceipt::getWarehouseId,dto.getWarehouseIds())
                .notIn(WmsStorageReceipt::getStatus,WmsManagerConstant.OrderStateEnum.FIVE.getCode()));
        if (CollUtil.isNotEmpty(list)){
            Map<Long,WmsStorageReceipt> map = Maps.newHashMap();
            List<Long> typeIds = Lists.newArrayList();
            Set<Integer> officeIds = Sets.newHashSet();
            list.forEach( receipt->{
                map.put(receipt.getId(),receipt);
                typeIds.add(receipt.getType());
                officeIds.add(Math.toIntExact(receipt.getWarehouseId()));
            });
            Set<Long> ids = map.keySet();
            Map<Long, SysDictVO> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listById(typeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Integer, String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            List<WmsStorageReceiptInfo> infos = wmsStorageReceiptInfoService.list(new LambdaQueryWrapper<WmsStorageReceiptInfo>().in(WmsStorageReceiptInfo::getStorageReceiptId,ids));
            infos.forEach(info->{
                WmsReportWarehouseVO wmsReportWarehouseVO = new WmsReportWarehouseVO();
                BeanUtil.copyProperties(info,wmsReportWarehouseVO);
                WmsStorageReceipt wmsStorageReceipt = map.get(info.getStorageReceiptId());
                Long typeId = wmsStorageReceipt.getType();
                wmsReportWarehouseVO.setType(typeId);
                wmsReportWarehouseVO.setWarehouse(wmsStorageReceipt.getWarehouseId());
                wmsReportWarehouseVO.setWarehouseName(officeMap.get(Math.toIntExact(wmsStorageReceipt.getWarehouseId())));
                wmsReportWarehouseVO.setCode(wmsStorageReceipt.getCode());
                wmsReportWarehouseVO.setTypeName(dictMap.get(typeId).getName());
                wmsReportWarehouseVO.setStatus(wmsStorageReceipt.getStatus());
                wmsReportWarehouseVO.setAffirmName(wmsStorageReceipt.getAffirmName());
                wmsReportWarehouseVO.setAffirmTime(wmsStorageReceipt.getAffirmTime());
                wmsReportWarehouseVO.setCreateTime(wmsStorageReceipt.getCreateTime());
                wmsReportWarehouseVO.setCreateName(wmsStorageReceipt.getCreateName());
                resList.add(wmsReportWarehouseVO);
            });
        }
        return LogicalPageUtil.page(resList, dto.getCurrent(), dto.getSize());
    }

    @Override
    public EpChartGraphVO<List<String>, BigDecimal> warehousingClassification(WmsReportWarehouseDTO dto) {
        EpChartGraphVO<List<String>, BigDecimal>   chartVO =  new EpChartGraphVO<>();
        List<QtSeriesDataDTO> seriesDataVO = Lists.newArrayList();
        if (Objects.nonNull(dto.getDate())) {
            dto.setStartTime(LocalDateUtil.getMorning(dto.getDate()));
            dto.setEndTime(LocalDateUtil.getMorning(dto.getDate()).plusDays(1));
        }
        //获取入库单数据
//        List<WmsStorageReceipt> list = list(new LambdaQueryWrapper<WmsStorageReceipt>()
//                .between(Objects.nonNull(dto.getStartTime()) && Objects.nonNull(dto.getEndTime()),WmsStorageReceipt::getCreateTime,dto.getStartTime(),dto.getEndTime())
//                .in(CollUtil.isNotEmpty(dto.getWarehouseIds()),WmsStorageReceipt::getWarehouseId,dto.getWarehouseIds())
//                .notIn(WmsStorageReceipt::getStatus,WmsManagerConstant.OrderStateEnum.FIVE.getCode()));
        List<WmsStorageReceiptInfo> infos = this.baseMapper.warehousingClassification(dto.getWarehouseIds(),dto.getStartTime(),dto.getEndTime());
        if (CollUtil.isNotEmpty(infos)) {
//            Set<Long> ids = list.stream().map(WmsStorageReceipt::getId).collect(Collectors.toSet());
//            List<WmsStorageReceiptInfo> infos = wmsStorageReceiptInfoService.list(new LambdaQueryWrapper<WmsStorageReceiptInfo>().in(WmsStorageReceiptInfo::getStorageReceiptId,ids));
            List<WmsStorageReceiptInfoVO> wmsStorageReceiptInfoVOS = BeanUtil.copyToList(infos, WmsStorageReceiptInfoVO.class);
            List<Long> materialIds = infos.stream().map(WmsStorageReceiptInfo::getMaterialId).collect(Collectors.toList());
            Set<Long> typeIds = Sets.newHashSet();
            List<String> types = Lists.newArrayList();
            Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            materialMap.forEach((key, value) -> typeIds.add(value.getType()));
            //获取物料分类
            Map<Long, ClassificationVO> classificationMap = remoteMessFeignApi.spareClassification(typeIds);
            if (CollUtil.isNotEmpty(classificationMap)){
                classificationMap.forEach((key,value)-> types.add(value.getName()));
                wmsStorageReceiptInfoVOS.forEach(infoVO-> {
                    if (Objects.nonNull(materialMap.get(infoVO.getMaterialId()).getType())) {
                        infoVO.setClassificationName(classificationMap.get( materialMap.get(infoVO.getMaterialId()).getType()).getName());
                    }
                });
                Map<String, List<WmsStorageReceiptInfoVO>> receiptMap = wmsStorageReceiptInfoVOS.stream()
                        .filter(vo -> vo.getClassificationName() != null)
                        .collect(Collectors.groupingBy(WmsStorageReceiptInfoVO::getClassificationName));
                QtSeriesDataDTO<String,BigDecimal> qtSeriesDataDTO = new QtSeriesDataDTO<>();
                List<BigDecimal> data = Lists.newArrayList();
                qtSeriesDataDTO.setType(WmsManagerConstant.BAR);
                types.forEach(type->{
                    data.add(receiptMap.get(type).stream().map(WmsStorageReceiptInfoVO::getQuantity).reduce(BigDecimal.ZERO,BigDecimalUtil::add));
                });
                qtSeriesDataDTO.setName(WmsManagerConstant.MATERIAL);
                qtSeriesDataDTO.setData(data);
                seriesDataVO.add(qtSeriesDataDTO);
            }
            chartVO.setXData(types);
            chartVO.setSeriesDataVO(seriesDataVO);
        }
        return chartVO;
    }

    @Override
    public BigDecimal totalQuantity(WmsReportWarehouseDTO dto) {
        BigDecimal quantity = null;
       List<WmsStorageReceiptInfo> infos = this.baseMapper.warehousingClassification(dto.getWarehouseIds(),dto.getStartTime(),dto.getEndTime());
       if (CollUtil.isNotEmpty(infos)) {
           quantity = infos.stream().map(WmsStorageReceiptInfo::getQuantity).reduce(BigDecimal.ZERO, BigDecimalUtil::add);
       }
        return quantity;
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
//                // 获取库存中物料的库存信息
//                Set<Long> materialId = list.stream().map(WmsStorageReceiptInfoVO::getMaterialId).collect(Collectors.toSet());
//                Map<Long, List<WmsInventory>> inventoryMap = wmsInventoryService.inventoryMap(materialId);
                //根据箱号获取库存中的物料信息
                Set<String> caseNos = list.stream().map(WmsStorageReceiptInfoVO::getCaseNo).collect(Collectors.toSet());
                Map<String, WmsInventory> inventoryMap =  wmsInventoryService.inventoryMapByCaseNos(caseNos);
                // 获取库存中物料的库存信息
//                Set<Long> materialId = list.stream().map(WmsStorageReceiptInfoVO::getMaterialId).collect(Collectors.toSet());
//                Map<Long, List<WmsInventory>> inventoryMap = wmsInventoryService.inventoryMap(materialId);
                //获取库存变化类型
                SysDictVO incomingType = FeignOptional.of(() -> remoteDictFeignApi.getByCode(WmsManagerConstant.variableTypeEnum.TYPE_15.getType(), WmsManagerConstant.VARIATION_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                //生成出库单
                WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO = new WmsDeliveryReceiptDTO();
                wmsDeliveryReceiptDTO.setType(sysDictVO.getId());
                wmsDeliveryReceiptDTO.setWarehouseId(wmsStorageReceiptVO.getWarehouseId());
                List<WmsDeliveryReceiptInfoDTO> infoDTOList = Lists.newArrayList();
                List<WmsInventory> wmsInventoryList = Lists.newArrayList();
                List<WmsInventoryRecord> wmsInventoryRecordList = Lists.newArrayList();
                for (WmsStorageReceiptInfoVO info : list) {
                    //处理库存数据
//                    List<WmsInventory> inventoryList = inventoryMap.get(info.getMaterialId());
                    WmsInventory inventory = inventoryMap.get(info.getCaseNo());
                    BigDecimal quantity = info.getQuantity();
                    Assert.isTrue(Objects.nonNull(inventory) && quantity.compareTo(inventory.getAvailableQuantity()) <= 0, info.getMaterialName() + "库存不足,冲销失败");
//                    //过滤可用库存为O的数据
//                  inventoryList = inventoryList.stream()
//                            .filter(inventory -> inventory.getAvailableQuantity().compareTo(BigDecimal.ZERO) > 0)
//                            .collect(Collectors.toList());
                    WmsInventoryRecord wmsInventoryRecord = new WmsInventoryRecord();
                    wmsInventoryRecord.setMaterialId(inventory.getMaterialId());
                    wmsInventoryRecord.setMaterialName(inventory.getMaterialName());
                    wmsInventoryRecord.setType(incomingType.getId());
                    wmsInventoryRecord.setCode(wmsStorageReceiptVO.getCode());
                    wmsInventoryRecord.setWarehouseId(inventory.getWarehouseId());
                    wmsInventoryRecord.setShelfId(inventory.getShelfId());
                    //变动前
                    //变动前
                    wmsInventoryRecord.setFrontAvailableQuantity(inventory.getAvailableQuantity());
                    wmsInventoryRecord.setFrontStockQuantity(inventory.getStockQuantity());
                    wmsInventoryRecord.setFrontLockQuantity(inventory.getLockQuantity());
                    wmsInventoryRecord.setFrontTotalQuantity(inventory.getTotalQuantity());

                    WmsInventory wmsInventory = new WmsInventory();
                    WmsDeliveryReceiptInfoDTO wmsDeliveryReceiptInfo = new WmsDeliveryReceiptInfoDTO();
                    BeanUtil.copyProperties(inventory, wmsInventory);
                    wmsDeliveryReceiptInfo.setInventoryId(inventory.getId());
                    wmsDeliveryReceiptInfo.setMaterialId(inventory.getMaterialId());
                    wmsDeliveryReceiptInfo.setAssociatedNo(wmsStorageReceiptVO.getCode());
                    if (quantity.compareTo(inventory.getAvailableQuantity()) <= 0) {
                        //变动数量
                        wmsInventoryRecord.setVariableQuantity(quantity);
                        //变动后
                        wmsInventoryRecord.setBackAvailableQuantity(wmsInventory.getAvailableQuantity().subtract(quantity));
                        wmsInventoryRecord.setBackStockQuantity(inventory.getStockQuantity());
                        wmsInventoryRecord.setBackTotalQuantity(wmsInventory.getTotalQuantity().subtract(quantity));
                        wmsInventoryRecord.setBackLockQuantity(inventory.getLockQuantity());

                        wmsInventory.setAvailableQuantity(wmsInventory.getAvailableQuantity().subtract(quantity));
                        wmsInventory.setTotalQuantity(wmsInventory.getTotalQuantity().subtract(quantity));
                        wmsDeliveryReceiptInfo.setQuantity(quantity);
                        quantity = BigDecimal.ZERO;
                    } else {
                        //变动数量
                        wmsInventoryRecord.setVariableQuantity(inventory.getAvailableQuantity());
                        //变动后
                        wmsInventoryRecord.setBackAvailableQuantity(BigDecimal.ZERO);
                        wmsInventoryRecord.setBackStockQuantity(inventory.getStockQuantity());
                        wmsInventoryRecord.setBackTotalQuantity(wmsInventory.getTotalQuantity().subtract(inventory.getAvailableQuantity()));
                        wmsInventoryRecord.setBackLockQuantity(inventory.getLockQuantity());
                        quantity = quantity.subtract(inventory.getAvailableQuantity());
                        wmsInventory.setTotalQuantity(wmsInventory.getTotalQuantity().subtract(inventory.getAvailableQuantity()));
                        wmsInventory.setAvailableQuantity(BigDecimal.ZERO);
                        wmsDeliveryReceiptInfo.setQuantity(inventory.getAvailableQuantity());
                    }
                    if (wmsInventoryRecord.getVariableQuantity().compareTo(BigDecimal.ZERO)>0) {
                        wmsInventoryRecordList.add(wmsInventoryRecord);
                    }
                    wmsInventoryList.add(wmsInventory);
                    infoDTOList.add(wmsDeliveryReceiptInfo);
                }
//                    for (WmsInventory inventory : inventoryList) {
//                        if (quantity.compareTo(BigDecimal.ZERO) == 0) {
//                            break;
//                        }
//                        //库存变动记录
//                        WmsInventoryRecord wmsInventoryRecord = new WmsInventoryRecord();
//                        wmsInventoryRecord.setMaterialId(inventory.getMaterialId());
//                        wmsInventoryRecord.setMaterialName(inventory.getMaterialName());
//                        wmsInventoryRecord.setType(incomingType.getId());
//                        wmsInventoryRecord.setCode(wmsStorageReceiptVO.getCode());
//                        wmsInventoryRecord.setWarehouseId(inventory.getWarehouseId());
//                        wmsInventoryRecord.setShelfId(inventory.getShelfId());
//                        //变动前
//                        //变动前
//                        wmsInventoryRecord.setFrontAvailableQuantity(inventory.getAvailableQuantity());
//                        wmsInventoryRecord.setFrontStockQuantity(inventory.getStockQuantity());
//                        wmsInventoryRecord.setFrontLockQuantity(inventory.getLockQuantity());
//                        wmsInventoryRecord.setFrontTotalQuantity(inventory.getTotalQuantity());
//
//                        WmsInventory wmsInventory = new WmsInventory();
//                        WmsDeliveryReceiptInfoDTO wmsDeliveryReceiptInfo = new WmsDeliveryReceiptInfoDTO();
//                        BeanUtil.copyProperties(inventory, wmsInventory);
//                        wmsDeliveryReceiptInfo.setInventoryId(inventory.getId());
//                        wmsDeliveryReceiptInfo.setMaterialId(inventory.getMaterialId());
//                        if (quantity.compareTo(inventory.getAvailableQuantity()) <= 0) {
//                            //变动数量
//                            wmsInventoryRecord.setVariableQuantity(quantity);
//                            //变动后
//                            wmsInventoryRecord.setBackAvailableQuantity(wmsInventory.getAvailableQuantity().subtract(quantity));
//                            wmsInventoryRecord.setBackStockQuantity(inventory.getStockQuantity());
//                            wmsInventoryRecord.setBackTotalQuantity(wmsInventory.getTotalQuantity().subtract(quantity));
//                            wmsInventoryRecord.setBackLockQuantity(inventory.getLockQuantity());
//
//                            wmsInventory.setAvailableQuantity(wmsInventory.getAvailableQuantity().subtract(quantity));
//                            wmsInventory.setTotalQuantity(wmsInventory.getTotalQuantity().subtract(quantity));
//                            wmsDeliveryReceiptInfo.setQuantity(quantity);
//                            quantity = BigDecimal.ZERO;
//                        } else {
//                            //变动数量
//                            wmsInventoryRecord.setVariableQuantity(inventory.getAvailableQuantity());
//                            //变动后
//                            wmsInventoryRecord.setBackAvailableQuantity(BigDecimal.ZERO);
//                            wmsInventoryRecord.setBackStockQuantity(inventory.getStockQuantity());
//                            wmsInventoryRecord.setBackTotalQuantity(wmsInventory.getTotalQuantity().subtract(inventory.getAvailableQuantity()));
//                            wmsInventoryRecord.setBackLockQuantity(inventory.getLockQuantity());
//                            quantity = quantity.subtract(inventory.getAvailableQuantity());
//                            wmsInventory.setTotalQuantity(wmsInventory.getTotalQuantity().subtract(inventory.getAvailableQuantity()));
//                            wmsInventory.setAvailableQuantity(BigDecimal.ZERO);
//                            wmsDeliveryReceiptInfo.setQuantity(inventory.getAvailableQuantity());
//                        }
//                        if (wmsInventoryRecord.getVariableQuantity().compareTo(BigDecimal.ZERO)>0) {
//                            wmsInventoryRecordList.add(wmsInventoryRecord);
//                        }
//                        wmsInventoryList.add(wmsInventory);
//                        infoDTOList.add(wmsDeliveryReceiptInfo);
//                    }
                wmsDeliveryReceiptDTO.setList(infoDTOList);
                //生成出库单并自动确认
                wmsDeliveryReceiptService.createAndAffirm(wmsDeliveryReceiptDTO, userName);
                //生成库存变化流水记录
                wmsInventoryRecordService.saveBatch(wmsInventoryRecordList);
                //扣除库存
                wmsInventoryService.deduct(wmsInventoryList);
                }
            //修改入库单状态为已冲销
            WmsStorageReceipt receipt = getById(id);
            receipt.setStatus(WmsManagerConstant.OrderStateEnum.FIVE.getCode());
            receipt.setChargeName(userName);
            receipt.setChargeTime(LocalDateTime.now());
            updateById(receipt);
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
    @Override
    public Boolean chargeAgainstAssociated(List<BatchChargeAgainstDTO> dtoList) {
        //获取出库单数据
        List<WmsDeliveryReceipt> wmsDeliveryReceipts = this.baseMapper.listByAssociatedNo(dtoList);
        if (CollUtil.isNotEmpty(wmsDeliveryReceipts)){
            //进行冲销
            for (WmsDeliveryReceipt wmsDeliveryReceipt : wmsDeliveryReceipts) {
                chargeAgainstDelivery(wmsDeliveryReceipt.getId(),dtoList.get(0).getUserName());
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateBatchShelf(WmsUpdateBatchShelfDTO dto) {
        WmsStorageReceipt one = getOne(new LambdaQueryWrapper<WmsStorageReceipt>().eq(WmsStorageReceipt::getId, dto.getId()).last(MpConstant.STR_LIMIT_1));
        Assert.isTrue(Objects.nonNull(one) && WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(one.getStatus()),"当前状态不能修改");
        //获取单据明细信息
        List<WmsStorageReceiptInfo> list = wmsStorageReceiptInfoService.list(new LambdaQueryWrapper<WmsStorageReceiptInfo>().eq(WmsStorageReceiptInfo::getStorageReceiptId, dto.getId()));
        WmsStorageReceiptDTO wmsStorageReceiptDTO = new WmsStorageReceiptDTO();
        BeanUtil.copyProperties(one,wmsStorageReceiptDTO);
        Map<Integer, SysOfficeVO> integerSysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(dto.getShelfId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if (CollUtil.isNotEmpty(list)){
            List<WmsStorageReceiptInfoDTO> receiptInfoDTOS = Lists.newArrayList();
            list.forEach( wmsStorageReceiptInfo -> {
                WmsStorageReceiptInfoDTO infoDTO = new WmsStorageReceiptInfoDTO();
                BeanUtil.copyProperties(wmsStorageReceiptInfo,infoDTO);
                SysOfficeVO sysOfficeVO = integerSysOfficeVOMap.get(dto.getShelfId());
                infoDTO.setShelfCode(sysOfficeVO.getOfficeCode());
                infoDTO.setShelfId(sysOfficeVO.getId());
                infoDTO.setShelfName(sysOfficeVO.getOfficeName());
                receiptInfoDTOS.add(infoDTO);
            });
            wmsStorageReceiptDTO.setList(receiptInfoDTOS);
        }
        return edit(wmsStorageReceiptDTO);
    }

    /**
     * 对出库单进行冲销
     */
    @Override
    public Boolean chargeAgainstDelivery(Long deliveryId, String userName){
        WmsDeliveryReceipt wmsDeliveryReceipt =wmsDeliveryReceiptService.getById(deliveryId);
        Assert.notNull(wmsDeliveryReceipt,"单据不存在");
        return chargeAgainstStorage(deliveryId,userName);
    }

    @Override
    public BigDecimal totalQuantityM(WmsReportWarehouseDTO dto) {
        AtomicReference<BigDecimal> totalM = new AtomicReference<>(BigDecimal.ZERO);
        //获取当前仓库下的货架
        SysOfficeParentDTO officeParentDTO = new SysOfficeParentDTO();
        Set<Integer> ids = dto.getWarehouseIds().stream().mapToInt(Long::intValue)
                .boxed()
                .collect(Collectors.toSet());
        officeParentDTO.setParentIds(ids);
        officeParentDTO.setOfficeType(WmsManagerConstant.CODE_FIVE);
        List<SysOfficeVO> officeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentIdAndType(officeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Set<Integer> shelfIds = officeVOS.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        List<WmsStorageReceiptInfo> infos = this.baseMapper.warehousingClassificationByShelfIds(shelfIds,dto.getStartTime(),dto.getEndTime());
        if (CollUtil.isNotEmpty(infos)) {
            //单位换算
            Set<Long> materialIdSet = infos.stream().map(WmsStorageReceiptInfo::getMaterialId).collect(Collectors.toSet());
            //获取转换单位
            SysDictVO unitPiece = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_PIECE, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            SysDictVO unitSm = FeignOptional.of(() -> remoteDictFeignApi.getByCode(PmManagerConstant.MEASURE_UNIT_SM, SysDictConstant.TYPE_MEASURE_UNIT), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, List<SysSpareUnitConversionVO>> materialConvertMap = FeignOptional.of(() -> remoteSpareUnitConvertFeignApi.convert(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //单位转换
            infos.forEach(inventory -> {
                totalM.set(BigDecimalUtil.add(totalM.get(), UnitConvertUtil.convertMaterial(materialConvertMap.get(inventory.getMaterialId()), unitPiece.getId(), unitSm.getId(), inventory.getQuantity(), 4)));
            });
        }
        return totalM.get();
    }

    @Override
    public WmsSalesReturnsFlagVO salesReturnsFlag(String number) {
        WmsSalesReturnsFlagVO wmsSalesReturnsFlagVO = new WmsSalesReturnsFlagVO();
        //调用物流单明细查询接口
        SalesReturnOrderDTO dto = new SalesReturnOrderDTO();
        dto.setServer_code(WmsManagerConstant.SERVER_CODE2);
        dto.setCode(number);
        String data = remoteSalesFeignApi.queryReturnOrder(dto);
        JSONObject jsonObject = JSONObject.parseObject(data);
        try {
            SalesLogisticsVO logisticsVOS = jsonObject.getJSONObject("result").getObject("mdMappings",SalesLogisticsVO.class);
            if (Objects.nonNull(logisticsVOS)){
                wmsSalesReturnsFlagVO.setExists(WmsManagerConstant.YesOrNo.YES.getCode());
                SaleLogisticsDataVO saleLogisticsDataVO = logisticsVOS.getList_data().get(0);
                wmsSalesReturnsFlagVO.setMaterialName(saleLogisticsDataVO.getProduct_name());
                EqpSpareVO eqpSpareVO = FeignOptional.of(() -> remoteSpareFeignApi.getByCode(saleLogisticsDataVO.getProduct_code()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                wmsSalesReturnsFlagVO.setMaterialId(eqpSpareVO.getId());
                wmsSalesReturnsFlagVO.setQty(saleLogisticsDataVO.getQty());
                String state = logisticsVOS.getState();
                if (WmsManagerConstant.LogisticsBillState.RETURNED.getName().equals(state) ||
                        WmsManagerConstant.LogisticsBillState.REISSUED.getName().equals(state)){
                    wmsSalesReturnsFlagVO.setReturnedState(WmsManagerConstant.YesOrNo.YES.getCode());
                }
            }
        } catch (Exception e) {
            throw new BusinessException("物流单不存在");
        }
        return wmsSalesReturnsFlagVO;
    }

    @Override
    public  List<WmsStorageReceiptInfo> intervalData(LocalDateTime startTime, LocalDateTime endTime,Set<Long> materialIds) {
        return  this.baseMapper.intervalData(startTime,endTime,materialIds);
    }

    @Override
    public List<WmsStorageReceiptInfoVO> intervalDataVO(LocalDateTime morningStartTime, LocalDateTime nightStartTime) {
        return  this.baseMapper.intervalDataVO(morningStartTime,nightStartTime);
    }

    @Override
    public String salesReturns(WmsSalesReturnsDTO wmsSalesReturnsDTO) {

        WmsStorageReceiptDTO wmsStorageReceiptDTO = new WmsStorageReceiptDTO();
        wmsStorageReceiptDTO.setSourceCode(wmsStorageReceiptDTO.getSourceCode());
        wmsStorageReceiptDTO.setComeTime(LocalDateTime.now());
        wmsStorageReceiptDTO.setCreateName(SecurityUtils.getUser().getName());
        //获取创建入库单的类型
        SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(WmsManagerConstant.CODE_THREE, WmsManagerConstant.WAREHOUSING_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        wmsStorageReceiptDTO.setType(sysDictVO.getId());
        wmsStorageReceiptDTO.setWarehouseId(wmsSalesReturnsDTO.getWarehouseId());
        wmsStorageReceiptDTO.setList(wmsSalesReturnsDTO.getList());
        //创建入库单
        String order = addReturnCode(wmsStorageReceiptDTO);
        //销售同步
        SalesReturnOrderSyncDTO salesReturnOrderSyncDTO = new SalesReturnOrderSyncDTO();
        salesReturnOrderSyncDTO.setStock_order(order);
        salesReturnOrderSyncDTO.setServer_code(WmsManagerConstant.SERVER_CODE2);
        salesReturnOrderSyncDTO.setLogistics_code(wmsSalesReturnsDTO.getSourceCode());
        remoteSalesFeignApi.syncReturnOrder(salesReturnOrderSyncDTO);
        return order;
    }

    @Override
    public List<WmsInventoryDaySheetVO> daySheetData(WmsInventoryDaySheetDTO dto) {
        return this.baseMapper.daySheetData(dto);

    }

    @Override
    public List<WmsInventoryDaySheetCaseVO> daySheetCaseList(WmsInventoryDaySheetDTO dto) {
        return this.baseMapper.daySheetCaseList(dto);
    }

    @Override
    public Boolean deleteInfo(WmsStorageReceiptDTO dto) {

        if (CollUtil.isNotEmpty(dto.getList())) {

            List<WmsInventoryDTO> deleteList = Lists.newArrayList();
            Set<Long> ids = Sets.newHashSet();
            Set<String> caseNos = Sets.newHashSet();
            dto.getList().forEach(info -> {
                ids.add(info.getId());
                WmsInventoryDTO wmsInventoryDTO = new WmsInventoryDTO();
                BeanUtil.copyProperties(info, wmsInventoryDTO);
                wmsInventoryDTO.setOriginalCaseNo(info.getCaseNo());
                wmsInventoryDTO.setOriginalQuantity(info.getQuantity());
                deleteList.add(wmsInventoryDTO);
                caseNos.add(info.getCaseNo());
            });
            //还原之前的库存信息
            wmsInventoryService.reduceInfoByCaseNoList(deleteList);
            wmsStorageReceiptInfoService.removeByIds(ids);
            WmsStorageReceipt order = getById(dto.getId());
            if (Objects.nonNull(order)){
                SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode("0", SysDictConstant.TYPE_WAREHOUSING_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

                if (order.getType().equals(sysDictVO.getId())){
                    //如果为生产完工入库，删除生产完工入库中的信息
                    Boolean aBoolean = FeignOptional.of(() -> remoteCompleteStorageFeignApi.deleteInfoByCaseNo(caseNos), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                }
            }
        }
        return true;
    }

    @Override
    public WmsCodeInfoVO codeInfo(WmsStorageReceiptFlagInfoDTO dto) {
        //新线：仓储存在->返回仓储相关信息 不存在->获取生产订单相关信息
        //老线：存在->返回仓储相关信息
        WmsCodeInfoVO codeInfoVO = new WmsCodeInfoVO();
        WmsInventory inventory = wmsInventoryService.getOne(new LambdaQueryWrapper<WmsInventory>().eq(WmsInventory::getCaseNo, dto.getCaseNo()).orderByDesc(WmsInventory::getUpdateTime).last(MpConstant.STR_LIMIT_1));
        if (Objects.isNull(inventory)) {
            //通过判断参数中是否存在物料编码判断新老产线
            if (Objects.isNull(dto.getMaterialCode())) {
                //新线 （获取生产订单相关信息）
                PmsCaseNoInfoVO pmsCaseNoInfoVO = FeignOptional.of(() -> remotePmsFeignApi.getCaseNoInfo(dto.getCaseNo()), FeignConstant.STECCN_PMS_SERVICE_NAME).get();
                BeanUtil.copyProperties(pmsCaseNoInfoVO,codeInfoVO);
            }else {
                codeInfoVO.setMaterialCode(dto.getMaterialCode());
                codeInfoVO.setCaseNo(dto.getCaseNo());
                codeInfoVO.setQuantity(dto.getQuantity());
            }
            //处理物料
            if (Objects.nonNull(codeInfoVO.getMaterialId())) {
                EqpSpareVO eqpSpareVO = FeignOptional.of(() -> remoteSpareFeignApi.getById(codeInfoVO.getMaterialId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                codeInfoVO.setMaterialName(eqpSpareVO.getName());
                codeInfoVO.setMaterialCode(eqpSpareVO.getCode());
                codeInfoVO.setUnitId(eqpSpareVO.getUnitId());
            }else {
                //根据code 获取物料id名称
                EqpSpareVO eqpSpareVO = FeignOptional.of(() -> remoteSpareFeignApi.getByCode(codeInfoVO.getMaterialCode()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                codeInfoVO.setMaterialId(eqpSpareVO.getId());
                codeInfoVO.setMaterialName(eqpSpareVO.getName());
                codeInfoVO.setUnitId(eqpSpareVO.getUnitId());
            }
            //处理仓库
            if (Objects.nonNull(codeInfoVO.getWarehouseId())) {
                Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(Sets.newHashSet(Math.toIntExact(codeInfoVO.getMaterialId()))), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                codeInfoVO.setWarehouseName(officeMap.get(codeInfoVO.getWarehouseId()).getOfficeName());
                codeInfoVO.setWarehouseCode(officeMap.get(codeInfoVO.getWarehouseId()).getOfficeCode());
            } else if (Objects.nonNull(codeInfoVO.getWarehouseCode())){
                //根据code 获取物料id名称
                SysOfficeVO sysOfficeVO = FeignOptional.of(() -> remoteOfficeFeign.getByCode(codeInfoVO.getWarehouseCode()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                codeInfoVO.setWarehouseId(sysOfficeVO.getId());
                codeInfoVO.setWarehouseName(sysOfficeVO.getOfficeName());
            }
        }else {
            BeanUtil.copyProperties(inventory, codeInfoVO);
            codeInfoVO.setBatchNo(inventory.getBatch());
            codeInfoVO.setUnitId(inventory.getUnit());
            codeInfoVO.setShelfName(FeignOptional.of(() -> remoteOfficeFeign.getNameById(inventory.getShelfId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get());
        }
        return codeInfoVO;
    }

    @Override
    public Integer unAffirmByShelf(Set<Long> shelfIds) {
      return  this.baseMapper.unAffirmByShelf(shelfIds);
    }

    //根据仓库id批量确认入参时间之前的未确认的入库单
    @Override
    public Boolean batchConfirmation(TupleDTO<LocalDateTime, Long> tupleDTO) {
        List<WmsStorageReceipt> list = list(new LambdaQueryWrapper<WmsStorageReceipt>().eq(WmsStorageReceipt::getWarehouseId, tupleDTO.getField2())
                .lt(WmsStorageReceipt::getCreateTime, tupleDTO.getField1())
                .eq(WmsStorageReceipt::getStatus, WmsManagerConstant.OrderStateEnum.ZERO.getCode()));
        if (CollUtil.isNotEmpty(list)){
            for (WmsStorageReceipt wmsStorageReceipt : list) {
                System.out.println("开始"+wmsStorageReceipt.getId());
                affirm(wmsStorageReceipt.getId(),SecurityUtils.getUser().getName());
                System.out.println("成功:"+wmsStorageReceipt.getId());
            }
        }

        return true;
    }

    @Override
    public Boolean batchAffirm(WmsStorageReceiptBatchAffirmDTO dto) {
        if (CollUtil.isNotEmpty(dto.getIdList())){
            dto.getIdList().forEach(id->{
                WmsUpdateBatchShelfDTO wmsUpdateBatchShelfDTO = new WmsUpdateBatchShelfDTO();
                wmsUpdateBatchShelfDTO.setShelfCode(dto.getShelfCode());
                wmsUpdateBatchShelfDTO.setShelfName(dto.getShelfName());
                wmsUpdateBatchShelfDTO.setShelfId(dto.getShelfId());
                wmsUpdateBatchShelfDTO.setId(id);
                updateBatchShelf(wmsUpdateBatchShelfDTO);
                affirm(id,SecurityUtils.getUser().getName());
            });
        }
        return Boolean.TRUE;
    }

    @Override
    public BigDecimal monthSum(Set<Long> warehouseIds) {
        LocalDate today = LocalDate.now();
        // 获取当月的第一天
        LocalDate firstDay = today.with(TemporalAdjusters.firstDayOfMonth());
        // 获取当月的最后一天
        LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
        BigDecimal bigDecimal = this.baseMapper.monthSum(firstDay, lastDay, warehouseIds);
        if (Objects.isNull(bigDecimal)){
            return BigDecimal.ZERO;
        }
        return bigDecimal;
    }

    @Override
    public BigDecimal damagedInventory(Set<Long> warehouseIds) {
        LocalDate today = LocalDate.now();
        // 获取当月的第一天
        LocalDate firstDay = today.with(TemporalAdjusters.firstDayOfMonth());
        // 获取当月的最后一天
        LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
        BigDecimal bigDecimal = this.baseMapper.damagedInventory(firstDay, lastDay, warehouseIds);
        if (Objects.isNull(bigDecimal)){
            return BigDecimal.ZERO;
        }
        return bigDecimal;
    }

    @Override
    public BigDecimal salesTotalQuantity(Set<Long> warehouseIds, LocalDate firstDay, LocalDate result) {
        BigDecimal bigDecimal = this.baseMapper.salesTotalQuantity(warehouseIds, firstDay, result);
        if (Objects.isNull(bigDecimal)) {
            return BigDecimal.ZERO;
        }
        return bigDecimal;
    }

    @Override
    public Map<Long, BigDecimal>   intervalDataQuantity(LocalDateTime startTime, LocalDateTime endTime, Set<Long> materialIds, Long warehouseId) {
        Map<Long, BigDecimal> map = new HashMap<>();
        List<WmsStorageReceiptInfo> infos = this.baseMapper.intervalDataQuantity(startTime, endTime, materialIds, warehouseId);
        infos.forEach(info->{
            map.put(info.getMaterialId(),info.getQuantity());
        });
        return  map;
    }

    @Override
    public Map<Long, Integer> intervalOrderQuantity(LocalDateTime startTime, LocalDateTime endTime, Set<Long> materialIds, Long warehouseId) {
        //获取对应的入库单的单数
        List<WmsDeliveryReceiptInfoVO> wmsDeliveryReceiptInfoVOS =
                this.baseMapper.intervalOrderQuantity(startTime, endTime, materialIds, warehouseId);
        Map<Long, Integer> map = new HashMap<>();
        if (CollUtil.isNotEmpty(wmsDeliveryReceiptInfoVOS)){
            wmsDeliveryReceiptInfoVOS.forEach(
                    e-> map.put(e.getMaterialId(),e.getCount())
            );
        }
        return map;
    }

    @Override
    public Boolean deleteDetail(Long id) {
        WmsStorageReceiptInfo byId = wmsStorageReceiptInfoService.getById(id);
        WmsStorageReceipt receipt = getById(byId.getStorageReceiptId());
        if(Objects.isNull(receipt)||!receipt.getStatus().equals(OaConstant.NO)){
            throw new BusinessException("必须是已创建状态的入库单");
        }
        //修改库存
        WmsInventory one = wmsInventoryService.getOne(new LambdaQueryWrapper<WmsInventory>()
                .eq(WmsInventory::getCaseNo, byId.getCaseNo())
                .eq(WmsInventory::getMaterialId, byId.getMaterialId())
                .last("LIMIT 1"));
        if(Objects.nonNull(one)){
            WmsInventory model = new WmsInventory();
            BeanUtil.copyProperties(one,model);
            model.setStockQuantity(BigDecimalUtil.subtract(model.getStockQuantity(),byId.getQuantity()));
            if(model.getAvailableQuantity().compareTo(BigDecimal.ZERO)==0
                    &&model.getDetectionQuantity().compareTo(BigDecimal.ZERO)==0
                    &&model.getTotalQuantity().compareTo(BigDecimal.ZERO)==0
                    &&model.getStockQuantity().compareTo(BigDecimal.ZERO)==0
                    &&model.getFreezeQuantity().compareTo(BigDecimal.ZERO)==0
                    &&model.getLockQuantity().compareTo(BigDecimal.ZERO)==0){
                wmsInventoryService.removeById(one.getId());
            }else {
                wmsInventoryService.updateById(model);
            }
        }
        wmsStorageReceiptInfoService.removeById(id);
        return true;
    }

    @Override
    public void download(HttpServletResponse response) throws IOException{
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        //防止中文乱码
        String fileName = URLEncoder.encode("生产月计划模板", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), WmsStorageReceiptExcelDTO.class).sheet("模板").doWrite(new ArrayList<>());
    }

    /**
     * 冲销
     * @return
     */
    private Boolean chargeAgainstStorage(Long id,String userName) {
        RLock lock = redissonClient.getLock(LockConstant.WMS_CHARGE_AGAINST);
        lock.lock();
        try {
            // 获取当前出库单明细
            WmsDeliveryReceiptVO wmsDeliveryReceiptVO = wmsDeliveryReceiptService.get(id);
            //校验状态
            Assert.isTrue(WmsManagerConstant.OrderStateEnum.ONE.getCode().toString().equals(wmsDeliveryReceiptVO.getStatus()), "当前状态不可冲销入库");
            List<WmsDeliveryReceiptInfoVO> list = wmsDeliveryReceiptVO.getList();
            if (CollUtil.isNotEmpty(list)) {
                //获取创建入库单的类型
                SysDictVO sysDictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode(WmsManagerConstant.CODE_TWO, WmsManagerConstant.WAREHOUSING_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                //获取库存变化类型
                SysDictVO incomingType = FeignOptional.of(() -> remoteDictFeignApi.getByCode(WmsManagerConstant.variableTypeEnum.TYPE_15.getType(), WmsManagerConstant.VARIATION_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                //生成入库单
                WmsStorageReceiptDTO wmsStorageReceiptDTO = new WmsStorageReceiptDTO();
                wmsStorageReceiptDTO.setType(sysDictVO.getId());
                wmsStorageReceiptDTO.setWarehouseId(wmsDeliveryReceiptVO.getWarehouseId());
                wmsStorageReceiptDTO.setCreateName(userName);
                List<WmsStorageReceiptInfoDTO> infoDTOList = Lists.newArrayList();
                for (WmsDeliveryReceiptInfoVO wmsDeliveryReceiptInfoVO : list) {
                    WmsStorageReceiptInfoDTO wmsStorageReceiptInfoDTO = new WmsStorageReceiptInfoDTO();
                    wmsStorageReceiptInfoDTO.setAssociatedNo(wmsDeliveryReceiptVO.getCode());
                    BeanUtil.copyProperties(wmsDeliveryReceiptInfoVO,wmsStorageReceiptInfoDTO);
                    wmsStorageReceiptInfoDTO.setQuantity(wmsDeliveryReceiptInfoVO.getQuantity());
                }
                wmsStorageReceiptDTO.setList(infoDTOList);
                //生成入库单并自动确认
                createOrder(wmsStorageReceiptDTO);
                //修改出库单状态为已冲销
                WmsDeliveryReceipt receipt = wmsDeliveryReceiptService.getById(id);
                receipt.setStatus(WmsManagerConstant.OrderStateEnum.FIVE.getCode());
                wmsDeliveryReceiptService.updateById(receipt);
            }
        } finally {
            lock.unlock();
        }
        return Boolean.TRUE;
    }

}




