package com.steccn.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.wms.domain.WmsCheckOrder;
import com.steccn.wms.domain.WmsCheckOrderInfo;
import com.steccn.wms.dto.WmsCheckOrderDTO;
import com.steccn.wms.mapper.WmsCheckOrderMapper;
import com.steccn.wms.service.WmsCheckOrderInfoService;
import com.steccn.wms.service.WmsCheckOrderService;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.WmsCheckOrderInfoVO;
import com.steccn.wms.vo.WmsCheckOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
            List<Long> typeIds = new ArrayList<>();
            typeIds.add(vo.getType());
            R<Map<Long, SysDictVO>> sysDictVO = remoteDictFeignApi.listById(typeIds);
            vo.setTypeName(sysDictVO.getData().get(vo.getType()).getName());
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
            List<Long> typeIds = new ArrayList<>();
            typeIds.add(vo.getType());
            R<Map<Long, SysDictVO>> sysDictVO = remoteDictFeignApi.listById(typeIds);
            vo.setTypeName(sysDictVO.getData().get(vo.getType()).getName());
        }
        return  page.setRecords(list);
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




