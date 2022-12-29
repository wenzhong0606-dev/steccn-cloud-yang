package com.steccn.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.feign.RemoteMessFeignApi;
import com.steccn.pms.vo.WmsStockVO;
import com.steccn.wms.constant.WmsManagerConstant;
import com.steccn.wms.domain.*;
import com.steccn.wms.dto.WmsCheckOrderDTO;
import com.steccn.wms.mapper.*;
import com.steccn.wms.service.WmsCheckOrderInfoService;
import com.steccn.wms.service.WmsCheckOrderService;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.WmsCheckAreaVO;
import com.steccn.wms.vo.WmsCheckOrderInfoVO;
import com.steccn.wms.vo.WmsCheckOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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
    implements WmsCheckOrderService{
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final WmsCheckOrderInfoService wmsCheckOrderInfoService;
    private final RemoteOfficeFeign remoteOfficeFeign;
    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteMessFeignApi remoteMessFeignApi;

    private final WmsInventoryMapper wmsInventoryMapper;
    private final WmsDeliveryReceiptMapper wmsDeliveryReceiptMapper;
    private final WmsDeliveryReceiptInfoMapper wmsDeliveryReceiptInfoMapper;
    private final WmsStorageReceiptMapper wmsStorageReceiptMapper;
    private final WmsStorageReceiptInfoMapper wmsStorageReceiptInfoMapper;


    /***
     * 获取盘点管理列表
     * @param dto
     * @return
     */
    @Override
    public Page<WmsCheckOrderVO> getCheckOrderList(WmsCheckOrderDTO dto) {
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        if(Objects.nonNull(dto.getCreateTimeTem()) &&  dto.getCreateTimeTem().size() > 0){
            dto.setBeginCreateTime(dto.getCreateTimeTem().get(0));
            dto.setEndCreateTime(dto.getCreateTimeTem().get(1));
        }
        if(Objects.nonNull(dto.getCheckTimeTem()) && dto.getCheckTimeTem().size() > 0){
            dto.setBeginCheckTime(dto.getCheckTimeTem().get(0));
            dto.setEndCheckTime(dto.getCheckTimeTem().get(1));
        }
        List<WmsCheckOrderVO> list = baseMapper.getCheckOrderList(page,  dto);
        for (WmsCheckOrderVO vo:list) {
            //盘点类型
            vo.setTypeName(typeName(vo.getType()));
            vo.setPostName(Objects.isNull(remoteOfficeFeign.sysOfficeInfo(vo.getPost()).getData())?null
                    :remoteOfficeFeign.sysOfficeInfo(vo.getPost()).getData().getOfficeName());
        }
        return  page.setRecords(list);
    }

    /****
     * 新增或者编辑盘点管理
     * @param dto
     * @return
     */
    @Override
    public String addOrUpdateCheckOrder(WmsCheckOrder dto) {
        Integer result;
        if(Objects.isNull(dto.getId())){
            dto.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_PMS_BATCH, WmsCodeUtil.NUM_BIT_4, currentMaxCode()));
            dto.setStatus(0);
            result = baseMapper.insert(dto);
        }else{
            result = baseMapper.updateById(dto);
        }
        return result >0 ? dto.getCode():"操作失败";
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
        Boolean result =  updateById(order);
        QueryWrapper<WmsCheckOrderInfo>  orderInfoQueryWrapper = new QueryWrapper<>();
        orderInfoQueryWrapper.eq("check_order_id",dto.getId());
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
        if(Objects.isNull(current)) current =1;
        if(Objects.isNull(size)) size =10;
        Page page = new Page<>(current, size);
        //获取明细信息
        QueryWrapper<WmsCheckOrderInfo> orderInfoQueryWrapper = new QueryWrapper<>();
        orderInfoQueryWrapper.eq("check_order_id",id).eq("del_flag",0);
        orderInfoQueryWrapper.orderByDesc("create_time");
        Page resPage = wmsCheckOrderInfoService.page(page, orderInfoQueryWrapper);
        List<WmsCheckOrderInfoVO> infoVOList = new ArrayList<>();
        resPage.getRecords().forEach(vo->{
            WmsCheckOrderInfoVO wmsCheckOrderInfoVO = new WmsCheckOrderInfoVO();
            BeanUtils.copyProperties((WmsCheckOrderInfo)vo,wmsCheckOrderInfoVO);
            wmsCheckOrderInfoVO.setShelf(((WmsCheckOrderInfo) vo).getShelf().toString());
            wmsCheckOrderInfoVO.setWarehouse(((WmsCheckOrderInfo) vo).getWarehouse().toString());
            //仓库
            if(Objects.nonNull(wmsCheckOrderInfoVO.getWarehouse())){
                R<SysOfficeVO>  warehouseInfo = remoteOfficeFeign.shelfWarehouseInfo(Integer.parseInt(wmsCheckOrderInfoVO.getWarehouse()));
                wmsCheckOrderInfoVO.setWarehouseName(warehouseInfo.getData().getOfficeName());
            }
            //货架
            if(Objects.nonNull(wmsCheckOrderInfoVO.getShelf())){
                R<SysOfficeVO>  shelfInfo = remoteOfficeFeign.shelfWarehouseInfo(Integer.parseInt(wmsCheckOrderInfoVO.getShelf()));
                wmsCheckOrderInfoVO.setShelfName(shelfInfo.getData().getOfficeName());
            }
            //物料
            if(Objects.nonNull(wmsCheckOrderInfoVO.getShelf())){
                List<Long> materialIds = new ArrayList<>();
                materialIds.add(wmsCheckOrderInfoVO.getMaterialId());
                R<Map<Long, EqpSpareVO>> materialInfo = remoteSpareFeignApi.listById(materialIds);
                if(materialInfo.getData().size()>0) wmsCheckOrderInfoVO.setMaterialName(materialInfo.getData().get(wmsCheckOrderInfoVO.getMaterialId()).getName());
            }
            infoVOList.add(wmsCheckOrderInfoVO);
        });
        resPage.setRecords(infoVOList);
        map.put("wmsCheckOrder",wmsCheckOrder);
        map.put("wmsCheckOrderInfo",resPage);
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
        if(Objects.isNull(wmsCheckOrder)) return "未查询到盘点单信息";
        Page<WmsCheckOrderInfoVO> orderInfoVOPage = getNowCheckOrderItemStockList( wmsCheckOrder.getCheckArea(), wmsCheckOrder.getCheckScope(),
                id,1,10000,null);
        if(orderInfoVOPage.getRecords().size() == 0) return  "盘点库存不存在，无法调整";
        List<WmsCheckOrderInfoVO> orderInfoVOList = orderInfoVOPage.getRecords();
        if(orderInfoVOList.stream().filter(m-> Objects.isNull(m.getStatus())).findAny().isPresent()) return "盘点未完成";
        //差额为正数，则盘盈；盘盈生成对应的入库单，类型为盘点入库，关联单据为盘点单号，入库单自动确认
        List<WmsCheckOrderInfoVO> surplusList = orderInfoVOList.stream().filter(WmsCheckOrderInfoVO ->
                WmsCheckOrderInfoVO.getDifference().compareTo(BigDecimal.ZERO) == 1).collect(Collectors.toList());
        Map<String, List<WmsCheckOrderInfoVO>> warehouseSurplusList = surplusList.stream().collect(Collectors.groupingBy(t -> t.getWarehouse()));
        for (Map.Entry<String, List<WmsCheckOrderInfoVO>> surplusEntry : warehouseSurplusList.entrySet()) {
            //保存入库单
            WmsStorageReceipt wmsStorageReceipt = new WmsStorageReceipt();
            wmsStorageReceipt.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_STORAGE_RECEIPT, WmsCodeUtil.NUM_BIT_4, getStorageReceiptCurrentMaxCode()));
            wmsStorageReceipt.setType(wmsCheckOrder.getType());
            wmsStorageReceipt.setStatus(WmsManagerConstant.OrderStateEnum.ONE.getCode());
            wmsStorageReceipt.setWarehouseId(Long.parseLong(surplusEntry.getKey()));
            wmsStorageReceipt.setAffirmTime(LocalDateTime.now());
            wmsStorageReceipt.setAffirmName(SecurityUtils.getUser().getName());
            wmsStorageReceipt.setNote(wmsCheckOrder.getNote());
            wmsStorageReceiptMapper.insert(wmsStorageReceipt);
            //保存入库明细
            for (WmsCheckOrderInfoVO vo:surplusEntry.getValue()) {
                WmsStorageReceiptInfo wmsStorageReceiptInfo = new WmsStorageReceiptInfo();
                BeanUtils.copyProperties(vo,wmsStorageReceiptInfo);
                wmsStorageReceiptInfo.setStorageReceiptId(wmsStorageReceipt.getId());
                List<Long> materialIds = new ArrayList<>();
                materialIds.add(vo.getMaterialId());
                R<Map<Long, EqpSpareVO>> materialInfo = remoteSpareFeignApi.listById(materialIds);
                if(materialInfo.getData().size()>0) wmsStorageReceiptInfo.setMaterialCode(materialInfo.getData().get(vo.getMaterialId()).getCode());
                R<SysOfficeVO>  shelfInfo = remoteOfficeFeign.shelfWarehouseInfo(Integer.parseInt(vo.getShelf()));
                if(Objects.nonNull(shelfInfo.getData()))  wmsStorageReceiptInfo.setShelfCode(shelfInfo.getData().getOfficeCode());
                wmsStorageReceiptInfo.setQuantity(vo.getResult());
                //获取库存信息
                LambdaQueryWrapper<WmsInventory> inventoryLambdaQueryWrapper = new LambdaQueryWrapper();
                inventoryLambdaQueryWrapper.eq(WmsInventory::getCaseNo,vo.getCaseNo());
                WmsInventory wmsInventory = wmsInventoryMapper.selectOne(inventoryLambdaQueryWrapper);
                wmsStorageReceiptInfo.setUnit(Objects.isNull(wmsInventory)?null:wmsInventory.getUnit());
                wmsStorageReceiptInfoMapper.insert(wmsStorageReceiptInfo);
                //更新库存
                wmsInventory.setTotalQuantity(wmsInventory.getTotalQuantity().add(vo.getResult()));
                wmsInventory.setAvailableQuantity(wmsInventory.getAvailableQuantity().add(vo.getResult()));
                wmsInventoryMapper.updateById(wmsInventory);
            }
        }
        //差额为负数，则盘亏；盘亏生成对应的出库单，类型为盘点出库，关联单据为盘点单号，出库单自动确认
        List<WmsCheckOrderInfoVO> lossList = orderInfoVOList.stream().filter(WmsCheckOrderInfoVO ->
                WmsCheckOrderInfoVO.getDifference().compareTo(BigDecimal.ZERO) == -1).collect(Collectors.toList());
        Map<String, List<WmsCheckOrderInfoVO>> warehouseLossList = lossList.stream().collect(Collectors.groupingBy(t -> t.getWarehouse()));
        for (Map.Entry<String, List<WmsCheckOrderInfoVO>> lossEntry : warehouseLossList.entrySet()) {
            //保存出库信息
            WmsDeliveryReceipt wmsDeliveryReceipt = new WmsDeliveryReceipt();
            wmsDeliveryReceipt.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_WMS_DELIVERY_RECEIPT, WmsCodeUtil.NUM_BIT_4, getDeliveryReceiptCurrentMaxCode()));
            wmsDeliveryReceipt.setType(wmsCheckOrder.getType());
            wmsDeliveryReceipt.setStatus(WmsManagerConstant.OrderStateEnum.ONE.getCode());
            wmsDeliveryReceipt.setWarehouseId(Long.parseLong(lossEntry.getKey()));
            wmsDeliveryReceipt.setAffirmTime(LocalDateTime.now());
            wmsDeliveryReceipt.setAffirmName(SecurityUtils.getUser().getName());
            wmsDeliveryReceiptMapper.insert(wmsDeliveryReceipt);
            for (WmsCheckOrderInfoVO vo:lossEntry.getValue()) {
                LambdaQueryWrapper<WmsInventory> inventoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
                inventoryLambdaQueryWrapper.eq(WmsInventory::getCaseNo,vo.getCaseNo());
                WmsInventory wmsInventory = wmsInventoryMapper.selectOne(inventoryLambdaQueryWrapper);
                //保存出库明细
                BigDecimal availableQuantity = vo.getResult();//数量
                WmsDeliveryReceiptInfo wmsDeliveryReceiptInfo = new WmsDeliveryReceiptInfo();
                wmsDeliveryReceiptInfo.setDeliveryReceiptId(wmsDeliveryReceipt.getId());
                wmsDeliveryReceiptInfo.setMaterialId(vo.getMaterialId());
                wmsDeliveryReceiptInfo.setInventoryId(wmsInventory.getId());
                wmsDeliveryReceiptInfo.setQuantity(availableQuantity);
                wmsDeliveryReceiptInfoMapper.insert(wmsDeliveryReceiptInfo);
                //更新库存
                wmsInventory.setTotalQuantity(wmsInventory.getTotalQuantity().subtract(vo.getResult()));
                wmsInventory.setAvailableQuantity(wmsInventory.getAvailableQuantity().subtract(vo.getResult()));
                wmsInventoryMapper.updateById(wmsInventory);
            }
        }
        //更新盘点单信息
        wmsCheckOrder.setStatus(4);
        updateById(wmsCheckOrder);
        return "差异调整完成";
    }

    @Override
    public Page<WmsCheckOrderVO> getApiCheckOrderList(Integer current,Integer size,Integer status, String beginTime,
                                                      String endTime,Integer post) {
        Page page = new Page<>(current,size);
        List<WmsCheckOrderVO> list = baseMapper.getApiCheckOrderList(page,  status,beginTime,endTime,post);
        for (WmsCheckOrderVO vo:list) {
            //盘点类型
            vo.setTypeName(typeName(vo.getType()));
        }
        return  page.setRecords(list);
    }

    /****
     * 手机端 获取盘点单明细列表
     * @param checkOrderId
     * @return
     */
    @Override
    public R<Page<WmsCheckOrderInfoVO>> getApiCheckOrderDetailList(Long checkOrderId,Integer current, Integer size,Integer status) {
        //获取盘点信息
        WmsCheckOrder wmsCheckOrder = getById(checkOrderId);
        if(Objects.isNull(wmsCheckOrder)) return R.failure("未查询到盘点信息");
        Object checkArea = wmsCheckOrder.getCheckArea();
        if(Objects.isNull(checkArea)) return R.failure("盘点信息区域为空，请联系管理员");
        //更新盘点单状态
        if(wmsCheckOrder.getStatus() == WmsManagerConstant.CheckOrderStateEnum.ZERO.getCode()){
            wmsCheckOrder.setStatus(WmsManagerConstant.CheckOrderStateEnum.ONE.getCode());
            updateById(wmsCheckOrder);
        }
        return R.success(getNowCheckOrderItemStockList( checkArea, wmsCheckOrder.getCheckScope(),
                checkOrderId,current,size,status));
    }

    @Override
    public R<Boolean> adjNormalOrDifferenceCheckOrderItem( Map<String, Object> paramMap) {
        Boolean result = true;
        //效验
        Long checkOrderId = paramMap.containsKey("checkOrderId") ? (paramMap.get("checkOrderId") == null ? null :
                Long.parseLong(paramMap.get("checkOrderId").toString())) : null;
        if(Objects.isNull(checkOrderId)) return R.failure("盘点单主键必传");
        List<Map<String, Object>> wmsCheckOrderInfoList = paramMap.containsKey("wmsCheckOrderInfo") ? (List<Map<String,
                Object>>) paramMap.get("wmsCheckOrderInfo") : null;
        Boolean flag = true;
        for (Map<String, Object> item : wmsCheckOrderInfoList) {
            if(Objects.isNull(item.get("materialId")) || Objects.isNull(item.get("warehouse"))
                    || Objects.isNull(item.get("shelf")) ||  Objects.isNull(item.get("batch"))
                    || Objects.isNull(item.get("caseNo")))  flag = false;
        }
        if(!flag) return R.failure("参数不全，物料id、货架主键、仓库主键、批次、箱号不可为空");
        //获取盘点信息
        WmsCheckOrder wmsCheckOrder = getById(checkOrderId);
        if(Objects.isNull(wmsCheckOrder)) return  R.success(false) ;
        for (Map<String, Object> orderInfo : wmsCheckOrderInfoList) {
            //获取明细
            LambdaQueryWrapper<WmsCheckOrderInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(WmsCheckOrderInfo::getWarehouse,orderInfo.get("warehouse")).eq(WmsCheckOrderInfo::getShelf,orderInfo.get("shelf"))
                    .eq(WmsCheckOrderInfo::getMaterialId,orderInfo.get("materialId")).eq(WmsCheckOrderInfo::getBatch,orderInfo.get("batch"))
                    .eq(WmsCheckOrderInfo::getCaseNo,orderInfo.get("caseNo")).eq(WmsCheckOrderInfo::getCheckOrderId,checkOrderId);
            WmsCheckOrderInfo wmsCheckOrderInfo = wmsCheckOrderInfoService.getOne(lambdaQueryWrapper,false);
            WmsCheckOrderInfo wmsCheckOrderInfoTem = new WmsCheckOrderInfo();
            if(Objects.isNull(wmsCheckOrderInfo)){
                wmsCheckOrderInfoTem.setResult(Objects.isNull(orderInfo.get("result"))?new BigDecimal(orderInfo.get("quantity").toString())
                        :new BigDecimal(orderInfo.get("result").toString()));
                wmsCheckOrderInfoTem.setDifference(Objects.isNull(orderInfo.get("difference"))?BigDecimal.ZERO:new BigDecimal(orderInfo.get("difference").toString()));
                wmsCheckOrderInfoTem.setQuantity(new BigDecimal(orderInfo.get("quantity").toString()));
                wmsCheckOrderInfoTem.setCheckOrderId(checkOrderId);
                wmsCheckOrderInfoTem.setMaterialId(Long.parseLong(orderInfo.get("materialId").toString()));
                wmsCheckOrderInfoTem.setCaseNo(orderInfo.get("caseNo").toString());
                wmsCheckOrderInfoTem.setBatch(orderInfo.get("batch").toString());
                wmsCheckOrderInfoTem.setUnit(orderInfo.get("unit").toString());
                wmsCheckOrderInfoTem.setStatus(WmsManagerConstant.CheckOrderInfoStateEnum.ONE.getCode());
                wmsCheckOrderInfoTem.setShelf(Integer.parseInt(orderInfo.get("shelf").toString()));
                wmsCheckOrderInfoTem.setWarehouse(Integer.parseInt(orderInfo.get("warehouse").toString()));
                wmsCheckOrderInfoTem.setCheckBy( SecurityUtils.getUser().getUserId());
                wmsCheckOrderInfoTem.setCheckName( SecurityUtils.getUser().getName());
                wmsCheckOrderInfoTem.setCheckTime(LocalDateTime.now());
                wmsCheckOrderInfoTem.setNote(Objects.isNull(orderInfo.get("note"))?null:orderInfo.get("note").toString());
                result = wmsCheckOrderInfoService.save(wmsCheckOrderInfoTem);
            }else{
                wmsCheckOrderInfo.setStatus(WmsManagerConstant.CheckOrderInfoStateEnum.ONE.getCode());
                wmsCheckOrderInfo.setNote(Objects.isNull(orderInfo.get("note"))?null:orderInfo.get("note").toString());
                result = wmsCheckOrderInfoService.updateById(wmsCheckOrderInfo);
            }
        }
        return R.success(result);
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
    public  Page<WmsCheckOrderInfoVO>  getNowCheckOrderItemStockList(Object checkArea,Integer checkScope,Long checkOrderId,
                                                                     Integer current,Integer size,Integer status){
        Set<String> shelfs = new HashSet<>();
        Set<Long> materials = new HashSet<>();
        //盘点区域
        List<WmsCheckAreaVO> checkAreaList = JSON.parseArray(checkArea.toString(), WmsCheckAreaVO.class);
        if(checkScope == WmsManagerConstant.CheckScope.WAREHOUSE.getCode()){
            //根据仓库获取货架
            checkAreaList.forEach(vo->{
                SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
                sysOfficeParentDTO.setId(vo.getId().intValue());
                R<List<SysOfficeVO>> listR = remoteOfficeFeign.listByParentId(sysOfficeParentDTO);
                if(Objects.nonNull(listR.getData())){
                    listR.getData().forEach(a->{
                        shelfs.add(a.getOfficeCode());
                    });
                }
            });
        }
        if(checkScope == WmsManagerConstant.CheckScope.SHELVES.getCode()){
            //根据货架获取关联库存
            checkAreaList.forEach(vo->{
                shelfs.add(remoteOfficeFeign.getByIdSysOfficeInfo(vo.getId()).getMsg());
            });
        }
        if(checkScope == WmsManagerConstant.CheckScope.MATERIALCLASSIFICATION.getCode()){
            //根据分类查询物料
            Set<String> typeRelationMaterials = new HashSet<>();
            checkAreaList.forEach(vo->{
                typeRelationMaterials.add(vo.getId().toString());
            });
            R<List<EqpSpareVO>> listR = remoteSpareFeignApi.getByTypeIdSpareList(typeRelationMaterials);
            listR.getData().forEach(vo->{
                materials.add(vo.getId());
            });
        }
        if(checkScope == WmsManagerConstant.CheckScope.MATERIAL.getCode()){
            //获取物料下关联库存
            checkAreaList.forEach(vo->{
                materials.add(vo.getId());
            });
        }
      return  getStockList(checkOrderId,(checkScope == 0 || checkScope == 1)?getShelfNewStockList(shelfs)
              :getMaterialsNewStockList(materials),current, size,status);
    }


    /***
     * 根据物料获取最新库存列表
     * @param materials
     * @return
     */
    public List<WmsStockVO> getMaterialsNewStockList(Set<Long> materials){
        List<WmsStockVO> wmsStockNewList = new ArrayList<>();
        //根据物料获取关联库存
        Map<Long, List<WmsStockVO>>  stockList = remoteMessFeignApi.inventory(materials);
        for (Long key : stockList.keySet()) {
            wmsStockNewList.addAll(stockList.get(key));
        }
        return wmsStockNewList;
    }

    /***
     * 根据货架获取最新库存列表
     * @param shelfs
     * @return
     */
    public List<WmsStockVO> getShelfNewStockList(Set<String> shelfs){
        List<WmsStockVO> wmsStockNewList = new ArrayList<>();
        //根据货架获取关联库存
        Map<String, List<WmsStockVO>>  stockList = remoteMessFeignApi.shelfInventory(shelfs);
        for (String key : stockList.keySet()) {
            wmsStockNewList.addAll(stockList.get(key));
        }
        return wmsStockNewList;
    }
    /****
     * 根据盘点单获取转换后满足条件库存列表
     * @param checkOrderId
     * @param wmsStockNewList
     * @return
     */
    public  Page<WmsCheckOrderInfoVO> getStockList(Long checkOrderId,List<WmsStockVO> wmsStockNewList,
                                                   Integer current,Integer size,Integer status){
        List<WmsCheckOrderInfoVO> orderInfoNowVoList = new ArrayList<>();
        WmsCheckOrderInfoVO wmsCheckOrderInfoVO = new WmsCheckOrderInfoVO();
        if(Objects.isNull(current)) current =1;
        if(Objects.isNull(size)) current =10;
        //查询当前盘点单明细
        QueryWrapper<WmsCheckOrderInfo> orderInfoQueryWrapper = new QueryWrapper<>();
        orderInfoQueryWrapper.eq("check_order_id",checkOrderId).eq("del_flag",0);
        List<WmsCheckOrderInfo> orderInfoOldList = wmsCheckOrderInfoService.list(orderInfoQueryWrapper);
        for (WmsStockVO vo:wmsStockNewList) {
            if(!orderInfoOldList.stream().filter(m->m.getWarehouse().equals(vo.getWarehouse())
                    && m.getShelf().equals(vo.getShelf()) && m.getMaterialId().equals(vo.getMaterialId())
                    && m.getBatch().equals(vo.getBatchNo()) && m.getCaseNo().equals(vo.getCaseNo()))
                    .findAny().isPresent() && Objects.nonNull(vo.getShelf()) && Objects.nonNull(vo.getWarehouse())
                    && Objects.nonNull(vo.getMaterialId())){
                wmsCheckOrderInfoVO = new WmsCheckOrderInfoVO();
                BeanUtils.copyProperties(vo,wmsCheckOrderInfoVO);
                Set<String> codeSet = new HashSet<>();
                codeSet.add(vo.getWarehouse());
                codeSet.add(vo.getShelf());
                R<Map<String, SysOfficeVO>> sysOfficeVO = remoteOfficeFeign.listByCodeSet(codeSet);
                wmsCheckOrderInfoVO.setWarehouse(sysOfficeVO.getData().size()>0?Objects.isNull(sysOfficeVO.getData().get(vo.getWarehouse()))
                        ? null:sysOfficeVO.getData().get(vo.getWarehouse()).getId().toString():null);
                wmsCheckOrderInfoVO.setShelf(sysOfficeVO.getData().size()>0? Objects.isNull(sysOfficeVO.getData().get(vo.getShelf()))
                        ? null:sysOfficeVO.getData().get(vo.getShelf()).getId().toString():null);
                wmsCheckOrderInfoVO.setBatch(vo.getBatchNo());
                wmsCheckOrderInfoVO.setQuantity(vo.getAvailableQuantity());
                if(Objects.nonNull(wmsCheckOrderInfoVO.getMaterialId()) && Objects.nonNull(wmsCheckOrderInfoVO.getShelf())
                        && Objects.nonNull(wmsCheckOrderInfoVO.getWarehouse()))   orderInfoNowVoList.add(wmsCheckOrderInfoVO);
            }
        }
        for (WmsCheckOrderInfo old:orderInfoOldList) {
            wmsCheckOrderInfoVO = new WmsCheckOrderInfoVO();
            BeanUtils.copyProperties(old,wmsCheckOrderInfoVO);
            //仓库
            R<SysOfficeVO>  warehouseInfo = remoteOfficeFeign.shelfWarehouseInfo(old.getWarehouse());
            wmsCheckOrderInfoVO.setWarehouse(old.getWarehouse().toString());
            wmsCheckOrderInfoVO.setWarehouseName(warehouseInfo.getData().getOfficeName());
            //货架
            R<SysOfficeVO>  shelfInfo = remoteOfficeFeign.shelfWarehouseInfo(old.getShelf());
            wmsCheckOrderInfoVO.setShelf(old.getShelf().toString());
            wmsCheckOrderInfoVO.setShelfName(shelfInfo.getData().getOfficeName());
            orderInfoNowVoList.add(wmsCheckOrderInfoVO);
        }
        if(Objects.nonNull(status)) orderInfoNowVoList = orderInfoNowVoList.stream().filter(vo ->
                status ==1? vo.getStatus() == status :Objects.isNull(vo.getStatus())).collect(Collectors.toList());
        return LogicalPageUtil.page(orderInfoNowVoList, current, size);
    }


    /***
     * 根据盘点单类型获取中文名称
     * @param type
     * @return
     */
    public String typeName( Long type){
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




