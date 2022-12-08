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
import com.steccn.wms.domain.WmsCheckOrder;
import com.steccn.wms.domain.WmsCheckOrderInfo;
import com.steccn.wms.dto.WmsCheckOrderDTO;
import com.steccn.wms.dto.WmsCheckOrderInfoDTO;
import com.steccn.wms.mapper.WmsCheckOrderMapper;
import com.steccn.wms.service.WmsCheckOrderInfoService;
import com.steccn.wms.service.WmsCheckOrderService;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.WmsCheckAreaVO;
import com.steccn.wms.vo.WmsCheckOrderInfoVO;
import com.steccn.wms.vo.WmsCheckOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
        }
        return  page.setRecords(list);
    }

    /****
     * 新增或者编辑盘点管理
     * @param dto
     * @return
     */
    @Override
    public Boolean addOrUpdateCheckOrder(WmsCheckOrder dto) {
        Integer result;
        if(Objects.isNull(dto.getId())){
            dto.setCode(WmsCodeUtil.getProduceOrderCode(WmsCodeUtil.PREFIX_PMS_BATCH, WmsCodeUtil.NUM_BIT_4, currentMaxCode()));
            result = baseMapper.insert(dto);
        }else{
            result = baseMapper.updateById(dto);
        }
        return result >0 ? true:false;
    }

    /***
     * 删除盘点管理
     * @param dto
     * @return
     */
    @Override
    public Boolean delCheckOrder(WmsCheckOrder dto) {
        Boolean result =  removeById(dto);
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
    public WmsCheckOrderVO getByIdCheckOrderDetail(Long id) {
        WmsCheckOrderVO wmsCheckOrderVO = new WmsCheckOrderVO();
        //获取盘点信息
        WmsCheckOrder wmsCheckOrder = getById(id);
        BeanUtils.copyProperties(wmsCheckOrder,wmsCheckOrderVO);
        //获取明细信息
        QueryWrapper<WmsCheckOrderInfo> orderInfoQueryWrapper = new QueryWrapper<>();
        orderInfoQueryWrapper.eq("check_order_id",id).eq("del_flag",0);
        orderInfoQueryWrapper.orderByDesc("create_time");
        List<WmsCheckOrderInfo> infoList = wmsCheckOrderInfoService.list(orderInfoQueryWrapper);
        List<WmsCheckOrderInfoVO> infoVOList = new ArrayList<>();
        for (WmsCheckOrderInfo info:infoList) {
            WmsCheckOrderInfoVO wmsCheckOrderInfoVO = new WmsCheckOrderInfoVO();
            BeanUtils.copyProperties(info,wmsCheckOrderInfoVO);
            //仓库
            if(Objects.nonNull(info.getWarehouse())){
                R<SysOfficeVO>  warehouseInfo = remoteOfficeFeign.shelfWarehouse(info.getWarehouse());
                wmsCheckOrderInfoVO.setWarehouseName(warehouseInfo.getData().getOfficeName());
            }
            //货架
            if(Objects.nonNull(info.getShelf())){
                R<SysOfficeVO>  shelfInfo = remoteOfficeFeign.shelfWarehouse(info.getShelf());
                wmsCheckOrderInfoVO.setShelfName(shelfInfo.getData().getOfficeName());
            }
            //物料
            if(Objects.nonNull(info.getShelf())){
                List<Long> materialIds = new ArrayList<>();
                materialIds.add(info.getMaterialId());
                R<Map<Long, EqpSpareVO>> materialInfo = remoteSpareFeignApi.listById(materialIds);
                if(materialInfo.getData().size()>0) wmsCheckOrderInfoVO.setMaterialName(materialInfo.getData().get(info.getMaterialId()).getName());
            }
            infoVOList.add(wmsCheckOrderInfoVO);
        }
        wmsCheckOrderVO.setWmsCheckOrderInfoVOList(infoVOList);
        return wmsCheckOrderVO;
    }

    /****
     * 盘点差异调整
     * @param id
     * @return
     */
    @Override
    public Boolean varianceAdjustmentCheckOrder(Long id) {
        return null;
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
        Set<String> shelfs = new HashSet<>();
        Set<Long> materials = new HashSet<>();
        //获取盘点信息
        WmsCheckOrder wmsCheckOrder = getById(checkOrderId);
        if(Objects.isNull(wmsCheckOrder)) return R.failure("未查询到盘点信息");
        Object checkArea = wmsCheckOrder.getCheckArea();
        if(Objects.isNull(checkArea)) return R.failure("盘点信息区域为空，请联系管理员");
        //盘点区域
        List<WmsCheckAreaVO> checkAreaList = JSON.parseArray(checkArea.toString(), WmsCheckAreaVO.class);
        if(wmsCheckOrder.getCheckScope() == WmsManagerConstant.CheckScope.WAREHOUSE.getCode()){
            //根据仓库获取货架
            checkAreaList.forEach(vo->{
                SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
                sysOfficeParentDTO.setId(vo.getId().intValue());
                R<List<SysOfficeVO>> listR = remoteOfficeFeign.listByParentId(sysOfficeParentDTO);
                listR.getData().forEach(a->{
                    shelfs.add(a.getOfficeCode());
                });
            });
            return  R.success(getStockList(  checkOrderId,  getShelfNewStockList(shelfs),current, size,status));
        }
        if(wmsCheckOrder.getCheckScope() == WmsManagerConstant.CheckScope.SHELVES.getCode()){
            //根据货架获取关联库存
            checkAreaList.forEach(vo->{
                shelfs.add(remoteOfficeFeign.getByIdSysOfficeInfo(vo.getId()).getMsg());
            });
            return R.success(getStockList(  checkOrderId,  getShelfNewStockList(shelfs),current, size,status));
        }
        if(wmsCheckOrder.getCheckScope() == WmsManagerConstant.CheckScope.MATERIALCLASSIFICATION.getCode()){
            //根据分类查询物料
            Set<String> typeRelationMaterials = new HashSet<>();
            checkAreaList.forEach(vo->{
                typeRelationMaterials.add(vo.getId().toString());
            });
            R<List<EqpSpareVO>> listR = remoteSpareFeignApi.getByTypeIdSpareList(typeRelationMaterials);
            listR.getData().forEach(vo->{
                materials.add(vo.getId());
            });
            return  R.success(getStockList(  checkOrderId, getMaterialsNewStockList(materials),current, size,status));
        }
        if(wmsCheckOrder.getCheckScope() == WmsManagerConstant.CheckScope.MATERIAL.getCode()){
            //获取物料下关联库存
            checkAreaList.forEach(vo->{
                materials.add(vo.getId());
            });
            return R.success(getStockList(  checkOrderId, getMaterialsNewStockList(materials),current, size,status));
        }
        return R.success();
    }

    @Override
    public Boolean adjNormalOrDifferenceCheckOrderItem(WmsCheckOrderInfoDTO dto) {
        Boolean result = true;
        //获取盘点信息
        WmsCheckOrder wmsCheckOrder = getById(dto.getCheckOrderId());
        if(Objects.isNull(wmsCheckOrder)) return  false;
        //获取明细
        LambdaQueryWrapper<WmsCheckOrderInfo> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(WmsCheckOrderInfo::getWarehouse,dto.getWarehouse()).eq(WmsCheckOrderInfo::getShelf,dto.getShelf())
                .eq(WmsCheckOrderInfo::getMaterialId,dto.getMaterialId()).eq(WmsCheckOrderInfo::getBatch,dto.getBatch())
                .eq(WmsCheckOrderInfo::getCaseNo,dto.getCaseNo()).eq(WmsCheckOrderInfo::getCheckOrderId,dto.getCheckOrderId());
        WmsCheckOrderInfo wmsCheckOrderInfo = wmsCheckOrderInfoService.getOne(lambdaQueryWrapper,false);
        WmsCheckOrderInfo wmsCheckOrderInfoTem = new WmsCheckOrderInfo();
        if(Objects.isNull(wmsCheckOrderInfo)){
            BeanUtils.copyProperties(dto,wmsCheckOrderInfoTem);
            wmsCheckOrderInfoTem.setStatus(1);
            wmsCheckOrderInfoTem.setShelf(Integer.parseInt(dto.getShelf()));
            wmsCheckOrderInfoTem.setWarehouse(Integer.parseInt(dto.getWarehouse()));
            wmsCheckOrderInfoTem.setCheckBy( SecurityUtils.getUser().getUserId());
            wmsCheckOrderInfoTem.setCheckName( SecurityUtils.getUser().getName());
            wmsCheckOrderInfoTem.setCheckTime(LocalDateTime.now());
            result = wmsCheckOrderInfoService.save(wmsCheckOrderInfoTem);
        }else{
            wmsCheckOrderInfo.setStatus(1);
            wmsCheckOrderInfo.setCheckBy( SecurityUtils.getUser().getUserId());
            wmsCheckOrderInfo.setCheckName( SecurityUtils.getUser().getName());
            wmsCheckOrderInfo.setCheckTime(LocalDateTime.now());
            wmsCheckOrderInfo.setNote(dto.getNote());
            result = wmsCheckOrderInfoService.updateById(wmsCheckOrderInfo);
        }
        return result;
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
        WmsCheckOrder maxCodeModel = getOne(new LambdaQueryWrapper<WmsCheckOrder>().orderByDesc(WmsCheckOrder::getCode).last(MpConstant.STR_LIMIT_1));
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

}




