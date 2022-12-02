package com.steccn.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.common.constant.MpConstant;
import com.steccn.wms.domain.WmsCheckOrder;
import com.steccn.wms.domain.WmsCheckOrderInfo;
import com.steccn.wms.dto.WmsCheckOrderDTO;
import com.steccn.wms.mapper.WmsCheckOrderMapper;
import com.steccn.wms.service.WmsCheckOrderInfoService;
import com.steccn.wms.service.WmsCheckOrderService;
import com.steccn.wms.util.WmsCodeUtil;
import com.steccn.wms.vo.WmsCheckOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

    private final WmsCheckOrderInfoService wmsCheckOrderInfoService;
    /***
     * 获取盘点管理列表
     * @param dto
     * @return
     */
    @Override
    public Page<WmsCheckOrderVO> getCheckOrderList(WmsCheckOrderDTO dto) {
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        List<WmsCheckOrderVO> list = baseMapper.getCheckOrderList(page,  dto);
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




