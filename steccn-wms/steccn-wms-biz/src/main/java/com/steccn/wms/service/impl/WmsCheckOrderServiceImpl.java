package com.steccn.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.filter.IFilterConfig;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.constant.SysDictConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.vo.WmsStockVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.*;
import com.steccn.wms.dto.*;
import com.steccn.wms.mapper.*;
import com.steccn.wms.service.*;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.WmsCheckAreaVO;
import com.steccn.wms.vo.WmsCheckOrderAreaVO;
import com.steccn.wms.vo.WmsCheckOrderInfoVO;
import com.steccn.wms.vo.WmsCheckOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.function.Tuple3;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author W10
 * @description 针对表【wms_check_order(盘点单)】的数据库操作Service实现
 * @createDate 2022-11-30 09:45:17
 */
@Service
@RequiredArgsConstructor
public class WmsCheckOrderServiceImpl extends ServiceImpl<WmsCheckOrderMapper, WmsCheckOrder>
        implements WmsCheckOrderService {
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final WmsCheckOrderInfoService wmsCheckOrderInfoService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final WmsInventoryMapper wmsInventoryMapper;
    private final WmsDeliveryReceiptMapper wmsDeliveryReceiptMapper;
    private final WmsDeliveryReceiptInfoMapper wmsDeliveryReceiptInfoMapper;
    private final WmsStorageReceiptMapper wmsStorageReceiptMapper;
    private final WmsStorageReceiptInfoMapper wmsStorageReceiptInfoMapper;
    private final WmsStorageReceiptService wmsStorageReceiptService;
    private final WmsDeliveryReceiptService wmsDeliveryReceiptService;
    private final WmsInventoryService wmsInventoryService;
    private final WmsCheckOrderScanInfoService wmsCheckOrderScanInfoService;
    private final WmsTransferSlipService wmsTransferSlipService;

    /***
     * 获取盘点管理列表
     * @param dto
     * @return
     */
    @Override
    public Page<WmsCheckOrderVO> getCheckOrderList(WmsCheckOrderDTO dto) {
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        if (Objects.nonNull(dto.getCreateTimeTem()) && dto.getCreateTimeTem().size() > 0) {
            dto.setBeginCreateTime(dto.getCreateTimeTem().get(0));
            dto.setEndCreateTime(dto.getCreateTimeTem().get(1));
        }
        if (Objects.nonNull(dto.getCheckTimeTem()) && dto.getCheckTimeTem().size() > 0) {
            dto.setBeginCheckTime(dto.getCheckTimeTem().get(0));
            dto.setEndCheckTime(dto.getCheckTimeTem().get(1));
        }
        List<WmsCheckOrderVO> list = baseMapper.getCheckOrderList(page, dto);
        for (WmsCheckOrderVO vo : list) {
            //盘点类型
            vo.setTypeName(typeName(vo.getType()));
            vo.setPostName(Objects.isNull(remoteOfficeFeign.sysOfficeInfo(vo.getPost()).getData()) ? null
                    : remoteOfficeFeign.sysOfficeInfo(vo.getPost()).getData().getOfficeName());
        }
        return page.setRecords(list);
    }

    /****
     * 新增或者编辑盘点管理
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addOrUpdateCheckOrder(WmsCheckOrder dto) {
        Integer result;
        if (Objects.isNull(dto.getId())) {
            dto.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_PMS_BATCH, WmsCodeUtil.NUM_BIT_4, currentMaxCode()));
            dto.setStatus(0);
            List<WmsCheckAreaVO> checkAreaList = JSON.parseArray(dto.getCheckArea().toString(), WmsCheckAreaVO.class);
            if (CollUtil.isNotEmpty(checkAreaList)) {
                Set<Long> shelfIds = Sets.newHashSet();
                //如果盘点范围为货架
                if (dto.getCheckScope().equals(1)) {
                    shelfIds.addAll(checkAreaList.stream().map(WmsCheckAreaVO::getId).collect(Collectors.toList()));
                } else if (dto.getCheckScope().equals(0)) {
                    //盘点范围为仓库，获取仓库下的货架(货架单选)
                    SysOfficeParentDTO officeParentDTO = new SysOfficeParentDTO();
                    officeParentDTO.setOfficeType("5");
                    officeParentDTO.setId(Math.toIntExact(checkAreaList.get(0).getId()));
                    List<SysOfficeVO> officeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    officeVOS.forEach(office -> shelfIds.add(Long.valueOf(office.getId())));
                }
                if (CollUtil.isNotEmpty(shelfIds)){
                    Integer integer = wmsStorageReceiptService.unAffirmByShelf(shelfIds);
                    Assert.isFalse(Objects.nonNull(integer) && integer > 0,"有入库单未确认，需要先去确认才可以开始盘点");
                    Integer deliveryQty = wmsDeliveryReceiptService.unAffirmByShelf(shelfIds);
                    Assert.isFalse(Objects.nonNull(deliveryQty) && deliveryQty > 0,"有出库单未确认，需要先去确认才可以开始盘点");
                    Integer transferSlipQty = wmsTransferSlipService.unAffirmByShelf(shelfIds);
                    Assert.isFalse(Objects.nonNull(transferSlipQty) && transferSlipQty > 0,"有调拨单未确认，需要先去确认才可以开始盘点");

                }
            }
            result = baseMapper.insert(dto);
        } else {
            result = baseMapper.updateById(dto);
        }
        return result > 0 ? dto.getCode() : "操作失败";
    }

    /***
     * 删除盘点管理
     * @param dto
     * @return
     */
    @Override
    public Boolean delCheckOrder(WmsCheckOrder dto) {
        WmsCheckOrder order = getById(dto.getId());
        order.setStatus(3);
        order.setDelFlag(1);
        Boolean result = updateById(order);
        QueryWrapper<WmsCheckOrderInfo> orderInfoQueryWrapper = new QueryWrapper<>();
        orderInfoQueryWrapper.eq("check_order_id", dto.getId());
        wmsCheckOrderInfoService.remove(orderInfoQueryWrapper);
        return result;
    }

    /***
     * 盘点管理详情
     * @param id
     * @return
     */
    @Override
    public Map getByIdCheckOrderDetail(Long id, Integer current, Integer size) {
        Map map = new HashMap();
        //获取盘点信息
        WmsCheckOrder wmsCheckOrder = getById(id);
        if (Objects.isNull(current)) current = 1;
        if (Objects.isNull(size)) size = 10;
        Page page = new Page<>(current, size);
        //获取明细信息
        QueryWrapper<WmsCheckOrderInfo> orderInfoQueryWrapper = new QueryWrapper<>();
        orderInfoQueryWrapper.eq("check_order_id", id).eq("del_flag", 0);
        orderInfoQueryWrapper.orderByDesc("create_time");
        Page resPage = wmsCheckOrderInfoService.page(page, orderInfoQueryWrapper);
        List<WmsCheckOrderInfoVO> infoVOList = new ArrayList<>();
        resPage.getRecords().forEach(vo -> {
            WmsCheckOrderInfoVO wmsCheckOrderInfoVO = new WmsCheckOrderInfoVO();
            BeanUtils.copyProperties((WmsCheckOrderInfo) vo, wmsCheckOrderInfoVO);
            wmsCheckOrderInfoVO.setShelf(((WmsCheckOrderInfo) vo).getShelf().toString());
            wmsCheckOrderInfoVO.setWarehouse(((WmsCheckOrderInfo) vo).getWarehouse().toString());
            //仓库
            if (Objects.nonNull(wmsCheckOrderInfoVO.getWarehouse())) {
                R<SysOfficeVO> warehouseInfo = remoteOfficeFeign.shelfWarehouseInfo(Integer.valueOf(wmsCheckOrderInfoVO.getWarehouse()));
                wmsCheckOrderInfoVO.setWarehouseName(warehouseInfo.getData().getOfficeName());
            }
            //货架
            if (Objects.nonNull(wmsCheckOrderInfoVO.getShelf())) {
                R<SysOfficeVO> shelfInfo = remoteOfficeFeign.shelfWarehouseInfo(Integer.valueOf(wmsCheckOrderInfoVO.getShelf()));
                wmsCheckOrderInfoVO.setShelfName(shelfInfo.getData().getOfficeName());
            }
            //物料
            if (Objects.nonNull(wmsCheckOrderInfoVO.getMaterialId())) {
                WmsCheckOrderInfoVO wmsCheckOrderInfoVOTem = getWmsCheckOrderInfoVOInfo(wmsCheckOrderInfoVO.getMaterialId());
                wmsCheckOrderInfoVO.setMaterialName(wmsCheckOrderInfoVOTem.getMaterialName());
                wmsCheckOrderInfoVO.setMaterialCode(wmsCheckOrderInfoVOTem.getMaterialCode());
            }
            infoVOList.add(wmsCheckOrderInfoVO);
        });
        resPage.setRecords(infoVOList);
        map.put("wmsCheckOrder", wmsCheckOrder);
        map.put("wmsCheckOrderInfo", resPage);
        return map;
    }

    /****
     * 盘点差异调整
     * @param id
     * @return
     */
    @Override
    public String varianceAdjustmentCheckOrder(Long id) {
        //获取盘点信息
        WmsCheckOrder wmsCheckOrder = getById(id);
        if (Objects.isNull(wmsCheckOrder)) return "未查询到盘点单信息";
//        Page<WmsCheckOrderInfoVO> orderInfoVOPage = getNowCheckOrderItemStockList(wmsCheckOrder.getCheckArea(), wmsCheckOrder.getCheckScope(),
//                id, 1, 10000, null);
        List<WmsCheckOrderInfo> list = wmsCheckOrderInfoService.list(new LambdaQueryWrapper<WmsCheckOrderInfo>().eq(WmsCheckOrderInfo::getCheckOrderId, id));
        if (list.size() == 0) return "盘点库存不存在，无法调整";
        if (wmsCheckOrder.getType().equals(1773263209901236224L)) {
            if (list.stream().filter(m -> Objects.isNull(m.getStatus())).findAny().isPresent())
                return "盘点未完成";
        }
        //差额为正数，则盘盈；盘盈生成对应的入库单，类型为盘点入库，关联单据为盘点单号，入库单自动确认
        List<WmsCheckOrderInfo> surplusList = list.stream().filter(wmsCheckOrderInfoVO -> Objects.nonNull(wmsCheckOrderInfoVO.getDifference())).filter(WmsCheckOrderInfoVO ->
                WmsCheckOrderInfoVO.getDifference().compareTo(BigDecimal.ZERO) == 1).collect(Collectors.toList());
        Map<Integer, List<WmsCheckOrderInfo>> warehouseSurplusList = surplusList.stream().collect(Collectors.groupingBy(WmsCheckOrderInfo::getWarehouse));
//        Map<String, List<WmsCheckOrderInfoVO>> warehouseSurplusList = surplusList.stream().collect(Collectors.groupingBy(t -> t.getWarehouse()));
        List<WmsInventory> wmsInventoryList = Lists.newArrayList();
        for (Map.Entry<Integer, List<WmsCheckOrderInfo>> surplusEntry : warehouseSurplusList.entrySet()) {
            //保存入库单
            WmsStorageReceipt wmsStorageReceipt = new WmsStorageReceipt();
            wmsStorageReceipt.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_STORAGE_RECEIPT, WmsCodeUtil.NUM_BIT_4, getStorageReceiptCurrentMaxCode()));
            wmsStorageReceipt.setType(wmsCheckOrder.getType());
            wmsStorageReceipt.setStatus(WmsManagerConstant.OrderStateEnum.ONE.getCode());
            wmsStorageReceipt.setWarehouseId(Long.valueOf(surplusEntry.getKey()));
            wmsStorageReceipt.setAffirmTime(LocalDateTime.now());
            wmsStorageReceipt.setAffirmName(SecurityUtils.getUser().getName());
            wmsStorageReceipt.setNote(wmsCheckOrder.getNote());
            wmsStorageReceiptMapper.insert(wmsStorageReceipt);
            SysOfficeVO warehouseInfo = FeignOptional.of(() -> remoteOfficeFeign.shelfWarehouseInfo(surplusEntry.getKey()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //保存入库明细

            for (WmsCheckOrderInfo vo : surplusEntry.getValue()) {
                WmsStorageReceiptInfo wmsStorageReceiptInfo = new WmsStorageReceiptInfo();
                BeanUtils.copyProperties(vo, wmsStorageReceiptInfo);
                wmsStorageReceiptInfo.setId(null);
                wmsStorageReceiptInfo.setStorageReceiptId(wmsStorageReceipt.getId());
                List<Long> materialIds = new ArrayList<>();
                materialIds.add(vo.getMaterialId());
                R<Map<Long, EqpSpareVO>> materialInfo = remoteSpareFeignApi.listById(materialIds);
                if (materialInfo.getData().size() > 0)
                    wmsStorageReceiptInfo.setMaterialCode(materialInfo.getData().get(vo.getMaterialId()).getCode());
                R<SysOfficeVO> shelfInfo = remoteOfficeFeign.shelfWarehouseInfo(vo.getShelf());

                if (Objects.nonNull(shelfInfo.getData()))
                    wmsStorageReceiptInfo.setShelfCode(shelfInfo.getData().getOfficeCode());
                wmsStorageReceiptInfo.setQuantity(vo.getDifference());
                //获取库存信息
                LambdaQueryWrapper<WmsInventory> inventoryLambdaQueryWrapper = new LambdaQueryWrapper();
                inventoryLambdaQueryWrapper.eq(WmsInventory::getCaseNo, vo.getCaseNo()).eq(WmsInventory::getBatch, vo.getBatch()).eq(WmsInventory::getWarehouseId, vo.getWarehouse());
                WmsInventory wmsInventory = wmsInventoryService.getOne(inventoryLambdaQueryWrapper, false);
                wmsStorageReceiptInfo.setUnit(Objects.isNull(wmsInventory) ? null : wmsInventory.getUnit());
                wmsStorageReceiptInfoMapper.insert(wmsStorageReceiptInfo);
                //更新库存
                if (Objects.nonNull(wmsInventory)) {
                    wmsInventory.setTotalQuantity(wmsInventory.getTotalQuantity().add(vo.getDifference()));
                    wmsInventory.setBatch(vo.getBatch());
                    wmsInventory.setAvailableQuantity(wmsInventory.getAvailableQuantity().add(vo.getDifference()));
                    wmsInventoryList.add(wmsInventory);
//                    wmsInventoryMapper.updateById(wmsInventory);
                } else {
                    WmsInventory inventory = new WmsInventory();
                    BeanUtils.copyProperties(vo, inventory);
                    inventory.setTotalQuantity(vo.getDifference());
                    inventory.setShelfId(vo.getShelf());
                    inventory.setShelfName(shelfInfo.getData().getOfficeName());
                    inventory.setShelfCode(shelfInfo.getData().getOfficeCode());
                    inventory.setWarehouseId(vo.getWarehouse());
                    inventory.setWarehouseName(warehouseInfo.getOfficeName());
                    inventory.setWarehouseCode(warehouseInfo.getOfficeCode());
                    inventory.setAvailableQuantity(vo.getDifference());
                    inventory.setBatch(vo.getBatch());
                    inventory.setUnit(vo.getUnitId());
                    wmsInventoryList.add(inventory);
//                    wmsInventoryMapper.insert(inventory);
                }
            }
        }

        //差额为负数，则盘亏；盘亏生成对应的出库单，类型为盘点出库，关联单据为盘点单号，出库单自动确认
        List<WmsCheckOrderInfo> lossList = list.stream().filter(wmsCheckOrderInfoVO -> Objects.nonNull(wmsCheckOrderInfoVO.getDifference())).filter(WmsCheckOrderInfo ->
                WmsCheckOrderInfo.getDifference().compareTo(BigDecimal.ZERO) == -1).collect(Collectors.toList());
        Map<Integer, List<WmsCheckOrderInfo>> warehouseLossList = lossList.stream().collect(Collectors.groupingBy(WmsCheckOrderInfo::getWarehouse));
//        Map<Integer, List<WmsCheckOrderInfo>> WmsCheckOrderInfo = lossList.stream().collect(Collectors.groupingBy(WmsCheckOrderInfo::getWarehouse));
//        Map<Integer, List<WmsCheckOrderInfoVO>> WmsCheckOrderInfo = lossList.stream().collect(Collectors.groupingBy(t -> t.getWarehouse()));
        for (Map.Entry<Integer, List<WmsCheckOrderInfo>> lossEntry : warehouseLossList.entrySet()) {
            //保存出库信息
            WmsDeliveryReceipt wmsDeliveryReceipt = new WmsDeliveryReceipt();
            wmsDeliveryReceipt.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_DELIVERY_RECEIPT, WmsCodeUtil.NUM_BIT_4, getDeliveryReceiptCurrentMaxCode()));
            wmsDeliveryReceipt.setType(wmsCheckOrder.getType());
            wmsDeliveryReceipt.setStatus(WmsManagerConstant.OrderStateEnum.ONE.getCode());
            wmsDeliveryReceipt.setWarehouseId(Long.valueOf(lossEntry.getKey()));
            wmsDeliveryReceipt.setAffirmTime(LocalDateTime.now());
            wmsDeliveryReceipt.setAffirmName(SecurityUtils.getUser().getName());
            wmsDeliveryReceiptMapper.insert(wmsDeliveryReceipt);
            for (WmsCheckOrderInfo vo : lossEntry.getValue()) {
                LambdaQueryWrapper<WmsInventory> inventoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
                inventoryLambdaQueryWrapper.eq(WmsInventory::getCaseNo, vo.getCaseNo()).eq(WmsInventory::getBatch, vo.getBatch()).eq(WmsInventory::getWarehouseId, vo.getWarehouse());
                WmsInventory wmsInventory = wmsInventoryService.getOne(inventoryLambdaQueryWrapper, false);
                //保存出库明细
                BigDecimal availableQuantity = vo.getDifference().abs();//数量
                WmsDeliveryReceiptInfo wmsDeliveryReceiptInfo = new WmsDeliveryReceiptInfo();
                wmsDeliveryReceiptInfo.setDeliveryReceiptId(wmsDeliveryReceipt.getId());
                wmsDeliveryReceiptInfo.setMaterialId(vo.getMaterialId());
                wmsDeliveryReceiptInfo.setInventoryId(Objects.isNull(wmsInventory) ? null : wmsInventory.getId());
                wmsDeliveryReceiptInfo.setQuantity(availableQuantity);
                wmsDeliveryReceiptInfoMapper.insert(wmsDeliveryReceiptInfo);
                //更新库存
                if (Objects.nonNull(wmsInventory)) {
                    wmsInventory.setTotalQuantity(wmsInventory.getTotalQuantity().subtract(vo.getDifference().abs()));
                    wmsInventory.setAvailableQuantity(wmsInventory.getAvailableQuantity().subtract(vo.getDifference().abs()));
                    if (wmsInventory.getAvailableQuantity().compareTo(BigDecimal.ZERO) < 0) {
                        wmsInventory.setAvailableQuantity(BigDecimal.ZERO);
                    }
                    if (wmsInventory.getTotalQuantity().compareTo(BigDecimal.ZERO) < 0) {
                        wmsInventory.setTotalQuantity(BigDecimal.ZERO);
                    }
                    wmsInventoryList.add(wmsInventory);
                }
            }
        }
        if (CollUtil.isNotEmpty(wmsInventoryList)) {
            List<Long> materialIds = wmsInventoryList.stream().map(WmsInventory::getMaterialId).collect(Collectors.toList());
            Map<Long, EqpSpareVO> longEqpSpareVOMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            wmsInventoryList.stream().forEach(inventory -> {
                EqpSpareVO eqpSpareVO = longEqpSpareVOMap.get(inventory.getMaterialId());
                inventory.setMaterialName(eqpSpareVO.getName());
                inventory.setMaterialCode(eqpSpareVO.getCode());
            });
        }
        //更新盘点单信息
        wmsCheckOrder.setStatus(4);
        updateById(wmsCheckOrder);
        return "差异调整完成";
    }

    @Override
    public Page<WmsCheckOrderVO> getApiCheckOrderList(Integer current, Integer size, Integer status, String beginTime,
                                                      String endTime, Integer post) {
        Page page = new Page<>(current, size);
        List<WmsCheckOrderVO> list = baseMapper.getApiCheckOrderList(page, status, beginTime, endTime, post);
        for (WmsCheckOrderVO vo : list) {
            //盘点类型
            vo.setTypeName(typeName(vo.getType()));
        }
        return page.setRecords(list);
    }

    /****
     * 手机端 获取盘点单明细列表
     * @param checkOrderId
     * @return
     */
    @Override
    public R<Page<WmsCheckOrderInfoVO>> getApiCheckOrderDetailList(Long checkOrderId, Integer current, Integer size, Integer status) {
        //获取盘点信息
        WmsCheckOrder wmsCheckOrder = getById(checkOrderId);
        if (Objects.isNull(wmsCheckOrder)) return R.failure("未查询到盘点信息");
        Object checkArea = wmsCheckOrder.getCheckArea();
        if (Objects.isNull(checkArea)) return R.failure("盘点信息区域为空，请联系管理员");
        //更新盘点单状态
        if (wmsCheckOrder.getStatus() == WmsManagerConstant.CheckOrderStateEnum.ZERO.getCode()) {
            wmsCheckOrder.setStatus(WmsManagerConstant.CheckOrderStateEnum.ONE.getCode());
            updateById(wmsCheckOrder);
        }
        return R.success(getNowCheckOrderItemStockList(checkArea, wmsCheckOrder.getCheckScope(),
                checkOrderId, current, size, status));
    }

    @Override
    public R<Boolean> adjNormalOrDifferenceCheckOrderItem(Map<String, Object> paramMap) {
        Boolean result = true;
        //效验
        Long checkOrderId = paramMap.containsKey("checkOrderId") ? (paramMap.get("checkOrderId") == null ? null :
                Long.parseLong(paramMap.get("checkOrderId").toString())) : null;
        if (Objects.isNull(checkOrderId)) return R.failure("盘点单主键必传");
        List<Map<String, Object>> wmsCheckOrderInfoList = paramMap.containsKey("wmsCheckOrderInfo") ? (List<Map<String,
                Object>>) paramMap.get("wmsCheckOrderInfo") : null;
        Boolean flag = true;
        for (Map<String, Object> item : wmsCheckOrderInfoList) {
            if (Objects.isNull(item.get("materialId")) || Objects.isNull(item.get("warehouse"))
                    || Objects.isNull(item.get("shelf")) || Objects.isNull(item.get("batch"))
                    || Objects.isNull(item.get("caseNo"))) flag = false;
        }
        if (!flag) return R.failure("参数不全，物料id、货架主键、仓库主键、批次、箱号不可为空");
        //获取盘点信息
        WmsCheckOrder wmsCheckOrder = getById(checkOrderId);
        if (Objects.isNull(wmsCheckOrder)) return R.success(false);
        //清除盘点信息
        wmsCheckOrderInfoService.remove(new LambdaQueryWrapper<WmsCheckOrderInfo>().eq(WmsCheckOrderInfo::getCheckOrderId, checkOrderId));
        for (Map<String, Object> orderInfo : wmsCheckOrderInfoList) {
            //获取明细
//            LambdaQueryWrapper<WmsCheckOrderInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//            lambdaQueryWrapper.eq(WmsCheckOrderInfo::getWarehouse,orderInfo.get("warehouse")).eq(WmsCheckOrderInfo::getShelf,orderInfo.get("shelf"))
//                    .eq(WmsCheckOrderInfo::getMaterialId,orderInfo.get("materialId")).eq(WmsCheckOrderInfo::getBatch,orderInfo.get("batch"))
//                    .eq(WmsCheckOrderInfo::getCaseNo,orderInfo.get("caseNo")).eq(WmsCheckOrderInfo::getCheckOrderId,checkOrderId);
//            WmsCheckOrderInfo wmsCheckOrderInfo = wmsCheckOrderInfoService.getOne(lambdaQueryWrapper,false);
            WmsCheckOrderInfo wmsCheckOrderInfoTem = new WmsCheckOrderInfo();
            BigDecimal resultNum = Objects.isNull(orderInfo.get("result")) ? new BigDecimal(orderInfo.get("quantity").toString())
                    : new BigDecimal(orderInfo.get("result").toString());
            BigDecimal difference = Objects.isNull(orderInfo.get("difference")) ? BigDecimal.ZERO : new BigDecimal(orderInfo.get("difference").toString());
            BigDecimal quantity = new BigDecimal(orderInfo.get("quantity").toString());//代表总数量
            String note = Objects.isNull(orderInfo.get("note")) ? null : orderInfo.get("note").toString();
//            if(Objects.isNull(wmsCheckOrderInfo)){
            wmsCheckOrderInfoTem.setResult(resultNum);
            wmsCheckOrderInfoTem.setDifference(difference);
            wmsCheckOrderInfoTem.setQuantity(quantity);
            wmsCheckOrderInfoTem.setCheckOrderId(checkOrderId);
            wmsCheckOrderInfoTem.setMaterialId(Long.parseLong(orderInfo.get("materialId").toString()));
            wmsCheckOrderInfoTem.setCaseNo(orderInfo.get("caseNo").toString());
            wmsCheckOrderInfoTem.setBatch(orderInfo.get("batch").toString());
            wmsCheckOrderInfoTem.setUnitId(Long.parseLong(orderInfo.get("unitId").toString()));
//                wmsCheckOrderInfoTem.setUnit(orderInfo.get("unit").toString());
            wmsCheckOrderInfoTem.setStatus(Objects.isNull(orderInfo.get("result")) ? WmsManagerConstant.CheckOrderInfoStateEnum.ZERO.getCode()
                    : WmsManagerConstant.CheckOrderInfoStateEnum.ONE.getCode());//原始数据设置未执行
            wmsCheckOrderInfoTem.setShelf(Integer.parseInt(orderInfo.get("shelf").toString()));
            wmsCheckOrderInfoTem.setWarehouse(Integer.parseInt(orderInfo.get("warehouse").toString()));
            wmsCheckOrderInfoTem.setCheckBy(SecurityUtils.getUser().getUserId());
            wmsCheckOrderInfoTem.setCheckName(SecurityUtils.getUser().getName());
            wmsCheckOrderInfoTem.setCheckTime(LocalDateTime.now());
            wmsCheckOrderInfoTem.setNote(note);
            result = wmsCheckOrderInfoService.save(wmsCheckOrderInfoTem);
//            }else{
//                wmsCheckOrderInfo.setResult(resultNum);
//                wmsCheckOrderInfo.setDifference(difference);
//                wmsCheckOrderInfo.setQuantity(quantity);
//                wmsCheckOrderInfo.setStatus(WmsManagerConstant.CheckOrderInfoStateEnum.ONE.getCode());
//                wmsCheckOrderInfo.setNote(note);
//                result = wmsCheckOrderInfoService.save(wmsCheckOrderInfo);
//            }
        }
        return R.success(result);
    }

    @Override
    public List<WmsCheckOrderAreaVO> areaCountList(Long checkOrderId) {
        //根据盘点单获取盘点范围的盘点货架
        WmsCheckOrder order = getById(checkOrderId);
        Assert.notNull(order, "盘点单不存在");
        List<WmsCheckAreaVO> checkAreaList = JSON.parseArray(order.getCheckArea().toString(), WmsCheckAreaVO.class);

        if (CollUtil.isNotEmpty(checkAreaList)) {
            Set<Long> shelfIds = Sets.newHashSet();
            //如果盘点范围为货架
            if (order.getCheckScope().equals(1)) {
                shelfIds.addAll(checkAreaList.stream().map(WmsCheckAreaVO::getId).collect(Collectors.toList()));
            } else if (order.getCheckScope().equals(0)) {
                //盘点范围为仓库，获取仓库下的货架(货架单选)
                SysOfficeParentDTO officeParentDTO = new SysOfficeParentDTO();
                officeParentDTO.setOfficeType("5");
                officeParentDTO.setId(Math.toIntExact(checkAreaList.get(0).getId()));
                List<SysOfficeVO> officeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                officeVOS.forEach(office -> shelfIds.add(Long.valueOf(office.getId())));
            }
            return wmsCheckOrderScanInfoService.areaCountList(checkOrderId, shelfIds);
        }
        return null;
    }

    //根据盘点区域获取盘点区域内的货架id
    private Set<Long> getCheckShelfIds(List<WmsCheckAreaVO> checkAreaList, WmsCheckOrder order) {
        if (CollUtil.isNotEmpty(checkAreaList)) {
            Set<Long> shelfIds = Sets.newHashSet();
            //如果盘点范围为货架
            if (order.getCheckScope().equals(1)) {
                shelfIds.addAll(checkAreaList.stream().map(WmsCheckAreaVO::getId).collect(Collectors.toList()));
            } else if (order.getCheckScope().equals(0)) {
                //盘点范围为仓库，获取仓库下的货架(货架单选)
                SysOfficeParentDTO officeParentDTO = new SysOfficeParentDTO();
                officeParentDTO.setOfficeType("5");
                officeParentDTO.setId(Math.toIntExact(checkAreaList.get(0).getId()));
                List<SysOfficeVO> officeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                officeVOS.forEach(office -> shelfIds.add(Long.valueOf(office.getId())));
            }
            return shelfIds;
        }
        return null;
    }

    @Override
    public Boolean affirmCheck(WmsCheckOrderDTO dto) {
        //确认盘点  对盘点扫描明细与库存数据进行比对，生成盘点结果
        //获取盘点单信息
        WmsCheckOrder order = getById(dto.getId());
        order.setStatus(1);
        Assert.notNull(order, "盘点单不存在");
        updateById(order);
        wmsCheckOrderInfoService.remove(new LambdaQueryWrapper<WmsCheckOrderInfo>().eq(WmsCheckOrderInfo::getCheckOrderId, order.getId()));
        //获取盘点单扫描信息
        List<WmsCheckOrderScanInfo> scanInfoList = wmsCheckOrderScanInfoService.list(new LambdaQueryWrapper<WmsCheckOrderScanInfo>()
                .eq(WmsCheckOrderScanInfo::getCheckOrderId, dto.getId()));
        //获取盘点区域库存信息
        List<WmsCheckAreaVO> checkAreaList = JSON.parseArray(order.getCheckArea().toString(), WmsCheckAreaVO.class);
            if (CollUtil.isNotEmpty(checkAreaList)) {
                Set<Long> shelfIds = Sets.newHashSet();
                //如果盘点范围为货架
                if (order.getCheckScope().equals(1)) {
                    //获取货架上的库存信息
                    shelfIds.addAll(checkAreaList.stream().map(WmsCheckAreaVO::getId).collect(Collectors.toList()));
                } else if (order.getCheckScope().equals(0)) {
                    //盘点范围为仓库，获取仓库下的货架(货架单选)
                    SysOfficeParentDTO officeParentDTO = new SysOfficeParentDTO();
                    officeParentDTO.setOfficeType("5");
                    officeParentDTO.setId(Math.toIntExact(checkAreaList.get(0).getId()));
                    List<SysOfficeVO> officeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeParentDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    officeVOS.forEach(office -> shelfIds.add(Long.valueOf(office.getId())));
                }
                //获取库存信息
                List<WmsInventory> inventoryList = wmsInventoryService.list(new LambdaQueryWrapper<WmsInventory>()
                        .in(WmsInventory::getShelfId, shelfIds));
                    List<WmsCheckOrderInfo> checkOrderInfos = Lists.newArrayList();
                    scanInfoList.forEach(scanInfo -> {
                        inventoryList.forEach(inventory -> {
                            if (inventory.getCaseNo().equals(scanInfo.getCaseNo())) {
                                //如果库存中的箱号与扫描明细中的箱号相对应，比较仓库货架批次是否一致
                                if (inventory.getBatch().equals(scanInfo.getBatch())
                                        && inventory.getWarehouseId().equals(scanInfo.getWarehouse())
                                        && inventory.getShelfId().equals(scanInfo.getShelf())) {
                                    //一致则对比数量，生成盘点明细
                                    WmsCheckOrderInfo wmsCheckOrderInfo = new WmsCheckOrderInfo();
                                    wmsCheckOrderInfo.setWarehouse(scanInfo.getWarehouse());
                                    wmsCheckOrderInfo.setShelf(scanInfo.getShelf());
                                    wmsCheckOrderInfo.setQuantity(scanInfo.getQuantity());
                                    wmsCheckOrderInfo.setBatch(scanInfo.getBatch());
                                    wmsCheckOrderInfo.setUnitId(scanInfo.getUnitId());
                                    wmsCheckOrderInfo.setCaseNo(scanInfo.getCaseNo());
                                    wmsCheckOrderInfo.setMaterialId(scanInfo.getMaterialId());
                                    wmsCheckOrderInfo.setCheckTime(LocalDateTime.now());
                                    wmsCheckOrderInfo.setCheckBy(SecurityUtils.getUser().getUserId());
                                    wmsCheckOrderInfo.setCheckName(SecurityUtils.getUser().getName());
                                    wmsCheckOrderInfo.setCheckOrderId(order.getId());
                                    wmsCheckOrderInfo.setDifference(scanInfo.getQuantity().subtract(inventory.getTotalQuantity()));
                                    wmsCheckOrderInfo.setResult(scanInfo.getQuantity());
                                    checkOrderInfos.add(wmsCheckOrderInfo);
                                } else {
                                    //如果箱号一致，对应的批次与仓库货架不同，生成两条盘点明细内容 （1.原库存中的库存信息减少  2.新的增加）
                                    //库存对应的盘点明细
                                    WmsCheckOrderInfo wmsCheckOrderInfo = new WmsCheckOrderInfo();
                                    wmsCheckOrderInfo.setWarehouse(inventory.getWarehouseId());
                                    wmsCheckOrderInfo.setQuantity(inventory.getTotalQuantity());
                                    wmsCheckOrderInfo.setResult(BigDecimal.ZERO);
                                    wmsCheckOrderInfo.setShelf(inventory.getShelfId());
                                    wmsCheckOrderInfo.setCaseNo(inventory.getCaseNo());
                                    wmsCheckOrderInfo.setBatch(inventory.getBatch());
                                    wmsCheckOrderInfo.setUnitId(inventory.getUnit());
                                    wmsCheckOrderInfo.setMaterialId(inventory.getMaterialId());
                                    wmsCheckOrderInfo.setCheckTime(LocalDateTime.now());
                                    wmsCheckOrderInfo.setCheckBy(SecurityUtils.getUser().getUserId());
                                    wmsCheckOrderInfo.setCheckName(SecurityUtils.getUser().getName());
                                    wmsCheckOrderInfo.setCheckOrderId(order.getId());
                                    //
                                    wmsCheckOrderInfo.setDifference(BigDecimal.ZERO.subtract(inventory.getTotalQuantity()));
                                    checkOrderInfos.add(wmsCheckOrderInfo);
                                    //扫描明细对应的盘点明细
                                    WmsCheckOrderInfo wmsCheckOrderInfo1 = new WmsCheckOrderInfo();
                                    wmsCheckOrderInfo1.setQuantity(BigDecimal.ZERO);
                                    wmsCheckOrderInfo1.setDifference(scanInfo.getQuantity().subtract(BigDecimal.ZERO));
                                    wmsCheckOrderInfo1.setResult(scanInfo.getQuantity());
                                    wmsCheckOrderInfo1.setWarehouse(scanInfo.getWarehouse());
                                    wmsCheckOrderInfo1.setShelf(scanInfo.getShelf());
                                    wmsCheckOrderInfo1.setBatch(scanInfo.getBatch());
                                    wmsCheckOrderInfo1.setCaseNo(scanInfo.getCaseNo());
                                    wmsCheckOrderInfo1.setUnitId(inventory.getUnit());
                                    wmsCheckOrderInfo1.setMaterialId(inventory.getMaterialId());
                                    wmsCheckOrderInfo1.setCheckTime(LocalDateTime.now());
                                    wmsCheckOrderInfo1.setCheckBy(SecurityUtils.getUser().getUserId());
                                    wmsCheckOrderInfo1.setCheckName(SecurityUtils.getUser().getName());
                                    wmsCheckOrderInfo1.setCheckOrderId(order.getId());
                                    checkOrderInfos.add(wmsCheckOrderInfo1);
                                }
                            }
                        });
                    });
                    //扫描列表中存在，盘点范围内的库存中不存在
                    List<WmsCheckOrderScanInfo> infoList = scanInfoList.stream()
                            .filter(scanItem ->
                                    inventoryList.stream()
                                            .noneMatch(inventoryItem -> inventoryItem.getCaseNo().equals(scanItem.getCaseNo())))
                            .collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(infoList)) {
                        //获取箱号对应的库存
                        List<WmsInventory> list = wmsInventoryService.list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getCaseNo, infoList.stream().map(WmsCheckOrderScanInfo::getCaseNo).collect(Collectors.toList())));
                        Map<String, List<WmsInventory>> inventoryMap = list.stream().collect(Collectors.groupingBy(WmsInventory::getCaseNo));
                        infoList.forEach(info -> {
                            List<WmsInventory> inventoryList1 = inventoryMap.get(info.getCaseNo());
                            if (CollUtil.isEmpty(inventoryList1)) {
                                WmsCheckOrderInfo wmsCheckOrderInfo = new WmsCheckOrderInfo();
                                wmsCheckOrderInfo.setWarehouse(info.getWarehouse());
                                wmsCheckOrderInfo.setQuantity(BigDecimal.ZERO);
                                wmsCheckOrderInfo.setResult(info.getQuantity());
                                wmsCheckOrderInfo.setShelf(info.getShelf());
                                wmsCheckOrderInfo.setBatch(info.getBatch());
                                wmsCheckOrderInfo.setCaseNo(info.getCaseNo());
                                wmsCheckOrderInfo.setUnitId(info.getUnitId());
                                wmsCheckOrderInfo.setMaterialId(info.getMaterialId());
                                wmsCheckOrderInfo.setCheckTime(LocalDateTime.now());
                                wmsCheckOrderInfo.setCheckBy(SecurityUtils.getUser().getUserId());
                                wmsCheckOrderInfo.setCheckName(SecurityUtils.getUser().getName());
                                wmsCheckOrderInfo.setCheckOrderId(order.getId());
                                wmsCheckOrderInfo.setDifference(info.getQuantity().subtract(BigDecimal.ZERO));
                                checkOrderInfos.add(wmsCheckOrderInfo);
                            }else {
                                WmsInventory wmsInventory = inventoryList1.get(0);
                                //原始库存记录出  新库存入
                                WmsCheckOrderInfo wmsCheckOrderInfo1 = new WmsCheckOrderInfo();
                                wmsCheckOrderInfo1.setWarehouse(wmsInventory.getWarehouseId());
                                wmsCheckOrderInfo1.setQuantity(wmsInventory.getTotalQuantity());
                                wmsCheckOrderInfo1.setResult(BigDecimal.ZERO);
                                wmsCheckOrderInfo1.setShelf(wmsInventory.getShelfId());
                                wmsCheckOrderInfo1.setBatch(wmsInventory.getBatch());
                                wmsCheckOrderInfo1.setCaseNo(wmsInventory.getCaseNo());
                                wmsCheckOrderInfo1.setUnitId(wmsInventory.getUnit());
                                wmsCheckOrderInfo1.setMaterialId(wmsInventory.getMaterialId());
                                wmsCheckOrderInfo1.setCheckTime(LocalDateTime.now());
                                wmsCheckOrderInfo1.setCheckBy(SecurityUtils.getUser().getUserId());
                                wmsCheckOrderInfo1.setCheckName(SecurityUtils.getUser().getName());
                                wmsCheckOrderInfo1.setCheckOrderId(order.getId());
                                wmsCheckOrderInfo1.setDifference(BigDecimal.ZERO.subtract(wmsInventory.getTotalQuantity()));
                                checkOrderInfos.add(wmsCheckOrderInfo1);
                                WmsCheckOrderInfo wmsCheckOrderInfo = new WmsCheckOrderInfo();
                                wmsCheckOrderInfo.setWarehouse(info.getWarehouse());
                                wmsCheckOrderInfo.setQuantity(BigDecimal.ZERO);
                                wmsCheckOrderInfo.setResult(info.getQuantity());
                                wmsCheckOrderInfo.setShelf(info.getShelf());
                                wmsCheckOrderInfo.setBatch(info.getBatch());
                                wmsCheckOrderInfo.setCaseNo(info.getCaseNo());
                                wmsCheckOrderInfo.setUnitId(info.getUnitId());
                                wmsCheckOrderInfo.setMaterialId(info.getMaterialId());
                                wmsCheckOrderInfo.setCheckTime(LocalDateTime.now());
                                wmsCheckOrderInfo.setCheckBy(SecurityUtils.getUser().getUserId());
                                wmsCheckOrderInfo.setCheckName(SecurityUtils.getUser().getName());
                                wmsCheckOrderInfo.setCheckOrderId(order.getId());
                                wmsCheckOrderInfo.setDifference(info.getQuantity().subtract(BigDecimal.ZERO));
                                checkOrderInfos.add(wmsCheckOrderInfo);

                            }
                        });
                    }
                    //盘点范围库存中存在，扫描列表中不存在
                    List<WmsInventory> inventoryInfoList = inventoryList.stream()
                            .filter(inventoryItem ->
                                    scanInfoList.stream()
                                            .noneMatch(scanItem -> scanItem.getCaseNo().equals(inventoryItem.getCaseNo())))
                            .collect(Collectors.toList());
                    if (CollUtil.isNotEmpty(inventoryInfoList)) {
                        inventoryInfoList.forEach(inventory -> {
                            WmsCheckOrderInfo wmsCheckOrderInfo = new WmsCheckOrderInfo();
                            wmsCheckOrderInfo.setDifference(BigDecimal.ZERO.subtract(inventory.getTotalQuantity()));
                            wmsCheckOrderInfo.setQuantity(inventory.getTotalQuantity());
                            wmsCheckOrderInfo.setResult(BigDecimal.ZERO);
                            wmsCheckOrderInfo.setCaseNo(inventory.getCaseNo());
                            wmsCheckOrderInfo.setWarehouse(inventory.getWarehouseId());
                            wmsCheckOrderInfo.setShelf(inventory.getShelfId());
                            wmsCheckOrderInfo.setResult(BigDecimal.ZERO);
                            wmsCheckOrderInfo.setBatch(inventory.getBatch());
                            wmsCheckOrderInfo.setUnitId(inventory.getUnit());
                            wmsCheckOrderInfo.setMaterialId(inventory.getMaterialId());
                            wmsCheckOrderInfo.setCheckTime(LocalDateTime.now());
                            wmsCheckOrderInfo.setCheckBy(SecurityUtils.getUser().getUserId());
                            wmsCheckOrderInfo.setCheckName(SecurityUtils.getUser().getName());
                            wmsCheckOrderInfo.setCheckOrderId(order.getId());
                            checkOrderInfos.add(wmsCheckOrderInfo);
                        });
                    }
                    if (CollUtil.isNotEmpty(checkOrderInfos)){
                        Set<Long> unitIds = checkOrderInfos.stream().map(WmsCheckOrderInfo::getUnitId).collect(Collectors.toSet());
                        Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(unitIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                        checkOrderInfos.stream().forEach(info-> info.setUnit(dictMap.get(info.getUnitId())));
                    }
                    wmsCheckOrderInfoService.saveBatch(checkOrderInfos);
            }
        return Boolean.TRUE;
    }

    @Override
    public Boolean newVarianceAdjustmentCheckOrder(Long id) {
        //获取盘点单
        WmsCheckOrder order = getById(id);
        Assert.notNull(order,"盘点单不存在");
        List<WmsCheckOrderInfo> list = wmsCheckOrderInfoService.list(new LambdaQueryWrapper<WmsCheckOrderInfo>().eq(WmsCheckOrderInfo::getCheckOrderId, id));
        if(CollUtil.isNotEmpty(list)){
            //差额为负数，则盘亏；盘亏生成对应的出库单，类型为盘点出库，关联单据为盘点单号，出库单自动确认
            List<WmsCheckOrderInfo> deliveryList = list.stream().filter(e -> e.getDifference().compareTo(BigDecimal.ZERO) < 0).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(deliveryList)){
                List<String> caseNos = deliveryList.stream().map(WmsCheckOrderInfo::getCaseNo).collect(Collectors.toList());
                SysDictVO dictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode("6", SysDictConstant.TYPE_OUTBOUND_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                List<WmsInventory> inventoryList = wmsInventoryService.list(new LambdaQueryWrapper<WmsInventory>().in(WmsInventory::getCaseNo, caseNos));
                Map<String, List<WmsInventory>> inventoryMap = inventoryList.stream().collect(Collectors.groupingBy(WmsInventory::getCaseNo));
                Map<Integer, List<WmsCheckOrderInfo>> deliveryMap = deliveryList.stream().collect(Collectors.groupingBy(WmsCheckOrderInfo::getWarehouse));
                deliveryMap.forEach((k,v)->{
                    WmsDeliveryReceiptDTO wmsDeliveryReceiptDTO = new WmsDeliveryReceiptDTO();
                    wmsDeliveryReceiptDTO.setType(dictVO.getId());
                    List<WmsDeliveryReceiptInfoDTO> deliveryReceiptInfoDTOList = Lists.newArrayList();
                    v.forEach(wmsCheckOrderInfo->{
                        List<WmsInventory> inventoryList1 = inventoryMap.get(wmsCheckOrderInfo.getCaseNo());
                        if (CollUtil.isNotEmpty(inventoryList1)){
                            WmsInventory wmsInventory = inventoryList1.get(0);
                            WmsDeliveryReceiptInfoDTO wmsDeliveryReceiptInfoDTO = new WmsDeliveryReceiptInfoDTO();
                            wmsDeliveryReceiptInfoDTO.setAssociatedNo(order.getCode());
                            wmsDeliveryReceiptInfoDTO.setBatch(wmsCheckOrderInfo.getBatch());
                            wmsDeliveryReceiptInfoDTO.setQuantity(wmsCheckOrderInfo.getDifference().abs());
                            wmsDeliveryReceiptInfoDTO.setCaseNo(wmsCheckOrderInfo.getCaseNo());
                            wmsDeliveryReceiptInfoDTO.setInventoryId(wmsInventory.getId());
                            wmsDeliveryReceiptInfoDTO.setMaterialId(wmsInventory.getMaterialId());
                            wmsDeliveryReceiptInfoDTO.setMaterialName(wmsInventory.getMaterialName());
                            wmsDeliveryReceiptInfoDTO.setMaterialCode(wmsInventory.getMaterialCode());
                            wmsDeliveryReceiptInfoDTO.setShelfId(Long.valueOf(wmsInventory.getShelfId()));
                            wmsDeliveryReceiptInfoDTO.setShelfName(wmsInventory.getShelfName());
                            wmsDeliveryReceiptInfoDTO.setUnit(wmsInventory.getUnit());
                            wmsDeliveryReceiptInfoDTO.setUnitId(wmsInventory.getUnit());
//                            wmsDeliveryReceiptInfoDTO.setUnitName(wmsInventory.getUpdateName());
                            deliveryReceiptInfoDTOList.add(wmsDeliveryReceiptInfoDTO);
                        }
                    });
                    wmsDeliveryReceiptDTO.setWarehouseId(k);
                    String warehouseName = FeignOptional.of(() -> remoteOfficeFeign.getNameById(k), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    wmsDeliveryReceiptDTO.setWarehouseName(warehouseName);
                    wmsDeliveryReceiptDTO.setList(deliveryReceiptInfoDTOList);
                    wmsDeliveryReceiptDTO.setAffirmFlag("1");
                    wmsDeliveryReceiptService.add(wmsDeliveryReceiptDTO);
                });
            }
            //差额为正数，则盘盈；盘盈生成对应的入库单，类型为盘点入库，关联单据为盘点单号，入库单自动确认
            List<WmsCheckOrderInfo> inStorageList = list.stream().filter(e -> e.getDifference().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(inStorageList)){
                SysDictVO dictVO = FeignOptional.of(() -> remoteDictFeignApi.getByCode("8", SysDictConstant.TYPE_WAREHOUSING_TYPE), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                Map<Integer, List<WmsCheckOrderInfo>> instorageMap = inStorageList.stream().collect(Collectors.groupingBy(WmsCheckOrderInfo::getWarehouse));
                instorageMap.forEach((k,v)->{
                    WmsStorageReceiptDTO wmsStorageReceiptDTO = new WmsStorageReceiptDTO();
                    wmsStorageReceiptDTO.setType(dictVO.getId());
                    List<WmsStorageReceiptInfoDTO> storageReceiptInfoList = Lists.newArrayList();
                    Set<Integer> officeIds = Sets.newHashSet();
                    List<Long> materialIds = Lists.newArrayList();
                    v.forEach(wmsCheckOrderInfo -> {
                        WmsStorageReceiptInfoDTO wmsStorageReceiptInfoDTO = new WmsStorageReceiptInfoDTO();
                        wmsStorageReceiptInfoDTO.setQuantity(wmsCheckOrderInfo.getDifference());
                        wmsStorageReceiptInfoDTO.setAssociatedNo(order.getCode());
                        wmsStorageReceiptInfoDTO.setCaseNo(wmsCheckOrderInfo.getCaseNo());
                        wmsStorageReceiptInfoDTO.setBatch(wmsCheckOrderInfo.getBatch());
                        wmsStorageReceiptInfoDTO.setUnit(wmsCheckOrderInfo.getUnitId());
                        wmsStorageReceiptInfoDTO.setUnitName(wmsCheckOrderInfo.getUnit());
                        wmsStorageReceiptInfoDTO.setShelfId(wmsCheckOrderInfo.getShelf());
                        wmsStorageReceiptInfoDTO.setMaterialId(wmsCheckOrderInfo.getMaterialId());
                        materialIds.add(wmsCheckOrderInfo.getMaterialId());
                        officeIds.add(wmsCheckOrderInfo.getShelf());
                        storageReceiptInfoList.add(wmsStorageReceiptInfoDTO);
                    });
                    officeIds.add(k);
                    Map<Integer, SysOfficeVO> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listById(officeIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    if (CollUtil.isNotEmpty(storageReceiptInfoList)){
                        Map<Long, EqpSpareVO> materialMap = FeignOptional.of(() -> remoteSpareFeignApi.listById(materialIds), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                        storageReceiptInfoList.forEach(wmsStorageReceiptInfoDTO -> {
                            SysOfficeVO shelf = officeMap.get(wmsStorageReceiptInfoDTO.getShelfId());
                            EqpSpareVO material = materialMap.get(wmsStorageReceiptInfoDTO.getMaterialId());
                            wmsStorageReceiptInfoDTO.setShelfName(shelf.getOfficeName());
                            wmsStorageReceiptInfoDTO.setShelfCode(shelf.getOfficeCode());
                            wmsStorageReceiptInfoDTO.setMaterialCode(material.getCode());
                            wmsStorageReceiptInfoDTO.setMaterialName(material.getName());
                        });
                    }
                    wmsStorageReceiptDTO.setWarehouseId(k);
                    wmsStorageReceiptDTO.setWarehouseName(officeMap.get(k).getOfficeName());
                    wmsStorageReceiptDTO.setList(storageReceiptInfoList);
                    wmsStorageReceiptService.createOrder(wmsStorageReceiptDTO);
                });
            }
        }
        order.setStatus(4);
        updateById(order);
        return Boolean.TRUE;
    }


    /*****
     * 得到当前盘点单关联库存信息
     * @param checkArea
     * @param checkScope
     * @param checkOrderId
     * @param current
     * @param size
     * @param status
     * @return
     */
    public Page<WmsCheckOrderInfoVO> getNowCheckOrderItemStockList(Object checkArea, Integer checkScope, Long checkOrderId,
                                                                   Integer current, Integer size, Integer status) {
        Set<String> shelfs = new HashSet<>();
        Set<Long> materials = new HashSet<>();
        //盘点区域
        List<WmsCheckAreaVO> checkAreaList = JSON.parseArray(checkArea.toString(), WmsCheckAreaVO.class);
        if (checkScope == WmsManagerConstant.CheckScope.WAREHOUSE.getCode()) {
            //根据仓库获取货架
            checkAreaList.forEach(vo -> {
                SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
                sysOfficeParentDTO.setId(vo.getId().intValue());
                R<List<SysOfficeVO>> listR = remoteOfficeFeign.listByParentId(sysOfficeParentDTO);
                if (Objects.nonNull(listR.getData())) {
                    listR.getData().forEach(a -> {
                        shelfs.add(a.getOfficeCode());
                    });
                }
            });
        }
        if (checkScope == WmsManagerConstant.CheckScope.SHELVES.getCode()) {
            //根据货架获取关联库存
            checkAreaList.forEach(vo -> {
                shelfs.add(remoteOfficeFeign.getByIdSysOfficeInfo(vo.getId()).getData());
            });
        }
        if (checkScope == WmsManagerConstant.CheckScope.MATERIALCLASSIFICATION.getCode()) {
            //根据分类查询物料
            Set<String> typeRelationMaterials = new HashSet<>();
            checkAreaList.forEach(vo -> {
                typeRelationMaterials.add(vo.getId().toString());
            });
            R<List<EqpSpareVO>> listR = remoteSpareFeignApi.getByTypeIdSpareList(typeRelationMaterials);
            listR.getData().forEach(vo -> {
                materials.add(vo.getId());
            });
        }
        if (checkScope == WmsManagerConstant.CheckScope.MATERIAL.getCode()) {
            //获取物料下关联库存
            checkAreaList.forEach(vo -> {
                materials.add(vo.getId());
            });
        }
        return getStockList(checkOrderId, getByShelfCodeOrMaterialsInventoryList(shelfs, materials), current, size, status);
    }

    /***
     * 根据货架编码/物料主键 获取最新库存列表
     * @param shelfs
     * @return
     */
    public List<WmsStockVO> getByShelfCodeOrMaterialsInventoryList(Set<String> shelfs, Set<Long> materials) {
        List<WmsStockVO> wmsStockNewList = wmsInventoryMapper.getByShelfCodeOrMaterialsInventoryList(shelfs, materials);
//        for (WmsStockVO vo:wmsStockNewList) {
//            vo.setUnit(typeName( vo.getUnitId()));
//        }
        return wmsStockNewList;
    }

    /****
     * 根据盘点单获取转换后满足条件库存列表
     * @param checkOrderId
     * @param wmsStockNewList
     * @return
     */
    public Page<WmsCheckOrderInfoVO> getStockList(Long checkOrderId, List<WmsStockVO> wmsStockNewList,
                                                  Integer current, Integer size, Integer status) {
        List<WmsCheckOrderInfoVO> orderInfoNowVoList = new ArrayList<>();
        WmsCheckOrderInfoVO wmsCheckOrderInfoVO = new WmsCheckOrderInfoVO();
        if (Objects.isNull(current)) current = 1;
        if (Objects.isNull(size)) current = 10;
        //查询当前盘点单明细
        QueryWrapper<WmsCheckOrderInfo> orderInfoQueryWrapper = new QueryWrapper<>();
        orderInfoQueryWrapper.eq("check_order_id", checkOrderId).eq("del_flag", 0);
        List<WmsCheckOrderInfo> orderInfoOldList = wmsCheckOrderInfoService.list(orderInfoQueryWrapper);
        for (WmsStockVO vo : wmsStockNewList) {
            if (!orderInfoOldList.stream().filter(m -> m.getWarehouse().equals(Integer.parseInt(vo.getWarehouse()))
                            && m.getShelf().equals(Integer.parseInt(vo.getShelf())) && m.getMaterialId().equals(vo.getMaterialId())
                            && m.getBatch().equals(vo.getBatchNo()) && m.getCaseNo().equals(vo.getCaseNo()))
                    .findAny().isPresent() && Objects.nonNull(vo.getShelf()) && Objects.nonNull(vo.getWarehouse())
                    && Objects.nonNull(vo.getMaterialId())) {
                wmsCheckOrderInfoVO = new WmsCheckOrderInfoVO();
                BeanUtils.copyProperties(vo, wmsCheckOrderInfoVO);
                wmsCheckOrderInfoVO.setBatch(vo.getBatchNo());
                wmsCheckOrderInfoVO.setQuantity(vo.getAvailableQuantity());
                if (Objects.nonNull(wmsCheckOrderInfoVO.getMaterialId()) && Objects.nonNull(wmsCheckOrderInfoVO.getShelf())
                        && Objects.nonNull(wmsCheckOrderInfoVO.getWarehouse()))
                    orderInfoNowVoList.add(wmsCheckOrderInfoVO);
            }
        }
        for (WmsCheckOrderInfo old : orderInfoOldList) {
            wmsCheckOrderInfoVO = new WmsCheckOrderInfoVO();
            BeanUtils.copyProperties(old, wmsCheckOrderInfoVO);
            //仓库
            R<SysOfficeVO> warehouseInfo = remoteOfficeFeign.shelfWarehouseInfo(old.getWarehouse());
            wmsCheckOrderInfoVO.setWarehouse(String.valueOf(old.getWarehouse()));
            wmsCheckOrderInfoVO.setWarehouseName(warehouseInfo.getData().getOfficeName());
            //货架
            R<SysOfficeVO> shelfInfo = remoteOfficeFeign.shelfWarehouseInfo(old.getShelf());
            wmsCheckOrderInfoVO.setShelf(String.valueOf(old.getShelf()));
            wmsCheckOrderInfoVO.setShelfName(shelfInfo.getData().getOfficeName());
            //物料
            WmsCheckOrderInfoVO wmsCheckOrderInfoVOTem = getWmsCheckOrderInfoVOInfo(wmsCheckOrderInfoVO.getMaterialId());
            wmsCheckOrderInfoVO.setMaterialName(wmsCheckOrderInfoVOTem.getMaterialName());
            wmsCheckOrderInfoVO.setMaterialCode(wmsCheckOrderInfoVOTem.getMaterialCode());
            LambdaQueryWrapper<WmsInventory> inventoryLambdaQueryWrapper = new LambdaQueryWrapper();
            inventoryLambdaQueryWrapper.eq(WmsInventory::getCaseNo, old.getCaseNo()).notIn(WmsInventory::getTotalQuantity, BigDecimal.ZERO).eq(WmsInventory::getBatch, old.getBatch());
            //库存
            WmsInventory wmsInventory = wmsInventoryService.getOne(inventoryLambdaQueryWrapper, false);
            wmsCheckOrderInfoVO.setTotalQuantity(Objects.nonNull(wmsInventory) ? wmsInventory.getTotalQuantity() : BigDecimal.ZERO);
            wmsCheckOrderInfoVO.setQuantity(Objects.nonNull(wmsInventory) ? wmsInventory.getAvailableQuantity() : BigDecimal.ZERO);
            orderInfoNowVoList.add(wmsCheckOrderInfoVO);
        }
        if (Objects.nonNull(status)) orderInfoNowVoList = orderInfoNowVoList.stream().filter(vo ->
                status == 1 ? vo.getStatus() == status : Objects.isNull(vo.getStatus())).collect(Collectors.toList());
        Page<WmsCheckOrderInfoVO> page = LogicalPageUtil.page(orderInfoNowVoList, current, size);
        Set<Long> unitIds = Sets.newHashSet();
        page.getRecords().forEach(info -> {
            unitIds.add(info.getUnitId());
        });
        Map<Long, String> unitMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(unitIds), FeignConstant.STECCN_WMS_SERVICE_NAME).get();
        page.getRecords().forEach(info -> {
            info.setUnit(unitMap.get(info.getUnitId()));
        });
        return page;
    }

    /****
     * 根据物料主键获取物料信息（名称|code）
     * @param materialId
     * @return
     */
    public WmsCheckOrderInfoVO getWmsCheckOrderInfoVOInfo(Long materialId) {
        WmsCheckOrderInfoVO wmsCheckOrderInfoVO = new WmsCheckOrderInfoVO();
        List<Long> materialIds = new ArrayList<>();
        materialIds.add(materialId);
        R<Map<Long, EqpSpareVO>> materialInfo = remoteSpareFeignApi.listById(materialIds);
        if (materialInfo.getData().size() > 0) {
            wmsCheckOrderInfoVO.setMaterialName(materialInfo.getData().get(materialId).getName());
            wmsCheckOrderInfoVO.setMaterialCode(materialInfo.getData().get(materialId).getCode());
        }
        return wmsCheckOrderInfoVO;
    }

    /***
     * 根据盘点单类型获取中文名称
     * @param type
     * @return
     */
    public String typeName(Long type) {
        List<Long> typeIds = new ArrayList<>();
        typeIds.add(type);
        R<Map<Long, SysDictVO>> sysDictVO = remoteDictFeignApi.listById(typeIds);
        return (sysDictVO.getData().get(type).getName());
    }

    private Long currentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        WmsCheckOrder maxCodeModel = baseMapper.getCurrentDayMaxCodeOrder();
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(WmsCodeUtil.NUM_BIT_12);
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }

    /**
     * 当前最大的code值
     * 入库凭证
     *
     * @return 数值类型，可能为null
     */
    private Long getStorageReceiptCurrentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        LocalDateTime max = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        WmsStorageReceipt maxCodeModel = wmsStorageReceiptMapper.selectOne(new LambdaQueryWrapper<WmsStorageReceipt>()
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

    /**
     * 当前最大的code值
     *
     * @return 数值类型，可能为null
     */
    private Long getDeliveryReceiptCurrentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        LocalDateTime max = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        LocalDateTime min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        WmsDeliveryReceipt maxCodeModel = wmsDeliveryReceiptMapper.selectOne(new LambdaQueryWrapper<WmsDeliveryReceipt>()
                .between(WmsDeliveryReceipt::getCreateTime, min, max)
                .orderByDesc(WmsDeliveryReceipt::getCode)
                .last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(WmsCodeUtil.PREFIX_WMS_DELIVERY_RECEIPT.length() + WmsCodeUtil.NUM_BIT_12);
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




