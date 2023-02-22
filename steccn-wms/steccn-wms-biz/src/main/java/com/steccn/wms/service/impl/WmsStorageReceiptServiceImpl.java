package com.steccn.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.dto.PmsCompleteStorageDTO;
import com.steccn.pms.dto.WmsStorageReceiptExcelDTO;
import com.steccn.pms.feign.RemoteCompleteStorageFeignApi;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.WmsInventory;
import com.steccn.wms.domain.WmsStorageReceipt;
import com.steccn.wms.domain.WmsStorageReceiptInfo;
import com.steccn.wms.dto.WmsInventoryDTO;
import com.steccn.wms.dto.WmsStorageReceiptDTO;
import com.steccn.wms.dto.WmsStorageReceiptInfoDTO;
import com.steccn.wms.dto.WmsStorageReceiptPageDTO;
import com.steccn.wms.mapper.WmsStorageReceiptMapper;
import com.steccn.wms.service.WmsInventoryService;
import com.steccn.wms.service.WmsStorageReceiptInfoService;
import com.steccn.wms.service.WmsStorageReceiptService;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.WmsStorageReceiptInfoVO;
import com.steccn.wms.vo.WmsStorageReceiptVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author W10
 * @description 针对表【wms_storage_receipt】的数据库操作Service实现
 * @createDate 2022-12-09 11:18:09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WmsStorageReceiptServiceImpl extends ServiceImpl<WmsStorageReceiptMapper, WmsStorageReceipt>
        implements WmsStorageReceiptService {

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final RedissonClient redissonClient;

    private final WmsInventoryService wmsInventoryService;

    private final WmsStorageReceiptInfoService wmsStorageReceiptInfoService;

    private final RemoteCompleteStorageFeignApi remoteCompleteStorageFeignApi;

    private final RemoteSpareFeignApi remoteSpareFeignApi;

    @Override
    public Page<WmsStorageReceiptVO> pageList(WmsStorageReceiptPageDTO dto) {
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
            Map<Integer, String> officeMap = FeignOptional.of(()->remoteOfficeFeign.listByIdCh(officeSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Map<Long, String> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByIdCh(typeSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
        } finally {
            lock.unlock();
        }
        return true;
    }

    @Override
    @Transactional(readOnly = false)
    public Boolean edit(WmsStorageReceiptDTO dto) {
        RLock lock = redissonClient.getLock(LockConstant.TABLE_WMS_STORAGE_RECEIPT);
        lock.lock();
        try {
            //1.校验数据是否存在
            Assert.isTrue(Objects.nonNull(dto), "入库凭证信息为空");
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
                LambdaQueryWrapper<WmsStorageReceiptInfo> wrapper = new LambdaQueryWrapper<WmsStorageReceiptInfo>()
                        .eq(WmsStorageReceiptInfo::getStorageReceiptId, model.getId())
                        .notIn(CollUtil.isNotEmpty(ids), WmsStorageReceiptInfo::getId, ids);
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
        Assert.isTrue(WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(receipt.getStatus())
                , "当前状态不可确认");
        //3.确认(如果来自生产订单的数据:0)
        if (WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(receipt.getSourceType())) {
            //变更pms生产订单状态
            PmsCompleteStorageDTO pmsCompleteStorageDTO = new PmsCompleteStorageDTO();
            pmsCompleteStorageDTO.setCode(receipt.getCode());
            pmsCompleteStorageDTO.setConfirmName(SecurityUtils.getUser().getName());
            remoteCompleteStorageFeignApi.completeStorageReviseStatue(pmsCompleteStorageDTO);
        }
        //3.1获取该单据的信息
        List<WmsStorageReceiptInfo> list = wmsStorageReceiptInfoService.list(new LambdaQueryWrapper<WmsStorageReceiptInfo>()
                .eq(WmsStorageReceiptInfo::getStorageReceiptId, id));
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
        Assert.isTrue(WmsManagerConstant.OrderStateEnum.ZERO.getCode().equals(order.getStatus())
                , "当前状态不可删除");
        //修改状态为已删除
        order.setStatus(WmsManagerConstant.OrderStateEnum.FOUR.getCode());
        //获取订单的明细
        List<WmsStorageReceiptInfo> list = wmsStorageReceiptInfoService.list(new LambdaQueryWrapper<WmsStorageReceiptInfo>()
                .eq(WmsStorageReceiptInfo::getStorageReceiptId, id));
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
        List<WmsStorageReceiptInfo> list = wmsStorageReceiptInfoService.list(new LambdaQueryWrapper<WmsStorageReceiptInfo>()
                .eq(WmsStorageReceiptInfo::getStorageReceiptId, id));
        //处理数据
        WmsStorageReceiptVO wmsStorageReceiptVO = new WmsStorageReceiptVO();
        BeanUtil.copyProperties(order, wmsStorageReceiptVO);
        Set<Long> dictSet = Sets.newHashSet();
        Set<Integer> officeSet = Sets.newHashSet();
        dictSet.add(wmsStorageReceiptVO.getType());
        officeSet.add(wmsStorageReceiptVO.getWarehouseId());
        //处理明细数据
        list.forEach(wmsStorageReceiptInfo -> {
            dictSet.add(wmsStorageReceiptInfo.getUnit());
        });
        Map<Integer, String> officeMap = FeignOptional.of(()->remoteOfficeFeign.listByIdCh(officeSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, String> dictMap = FeignOptional.of(()->remoteDictFeignApi.listByIdCh(dictSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<WmsStorageReceiptInfoVO> wmsStorageReceiptInfoVOS = BeanUtil.copyToList(list, WmsStorageReceiptInfoVO.class);
        wmsStorageReceiptInfoVOS.forEach(wmsStorageReceiptInfoVO -> {
            wmsStorageReceiptInfoVO.setUnitName(dictMap.get(wmsStorageReceiptInfoVO.getUnit()));
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
        EasyExcel.read(dto.getFile().getInputStream(), WmsStorageReceiptExcelDTO.class, new PageReadListener<WmsStorageReceiptExcelDTO>(
                dataList -> {
                    dataList.forEach(data -> {
                        excelList.add(data);
                    });
                }
        )).sheet().doRead();
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
        System.out.println("单位："+dictName.size());
        Map<String, SysOfficeVO> officeMap = FeignOptional.of(()->remoteOfficeFeign.listByName(officeNames),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<String, EqpSpareVO> spareMap = FeignOptional.of(()->remoteSpareFeignApi.listByCode(materialCodes),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<String, Long> dictIdMap = FeignOptional.of(()->remoteDictFeignApi.listByNameCh(dictName),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
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
                    if (Objects.isNull(wmsStorageReceiptInfo.getCaseNo())){
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
                            if (Objects.isNull(info.getCaseNo()) || "".equals(info.getCaseNo())) {
                                //生成箱号
                                info.setCaseNo(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_CASE_NO, WmsCodeUtil.NUM_BIT_4, caseNo));
                                System.out.println("caseNo:"+WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_CASE_NO, WmsCodeUtil.NUM_BIT_4, caseNo));
                                caseNo++;
                                System.out.println(caseNo);
                            }
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
                System.out.println("库存:"+wmsInventoryList.size());
//                保存库存信息
                        wmsInventoryService.saveBatch(wmsInventoryList);
            }
            log.info(String.valueOf(wmsStorageReceiptList.size()));
        }
        return true;
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
        WmsStorageReceipt maxCodeModel = getOne(new LambdaQueryWrapper<WmsStorageReceipt>()
                .between(WmsStorageReceipt::getCreateTime, min, max)
                .orderByDesc(WmsStorageReceipt::getCode)
                .last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(WmsCodeUtil.PREFIX_WMS_STORAGE_RECEIPT.length() + WmsCodeUtil.NUM_BIT_12);
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
        WmsStorageReceiptInfo maxCodeModel = wmsStorageReceiptInfoService.getOne(new LambdaQueryWrapper<WmsStorageReceiptInfo>()
                .between(WmsStorageReceiptInfo::getCreateTime, min, max)
                .orderByDesc(WmsStorageReceiptInfo::getCaseNo)
                .last(MpConstant.STR_LIMIT_1));
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




