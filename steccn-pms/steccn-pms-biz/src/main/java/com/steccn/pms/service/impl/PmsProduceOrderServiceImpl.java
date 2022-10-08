package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.constant.MpConstant;
import com.steccn.pms.domain.PmBillOfMaterial;
import com.steccn.pms.domain.PmsProduceOrder;
import com.steccn.pms.dto.PmsProduceOrderDTO;
import com.steccn.pms.service.PmsProduceOrderService;
import com.steccn.pms.mapper.PmsProduceOrderMapper;
import com.steccn.pms.util.PmsCodeUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
* @author yequan
* @description 针对表【pms_produce_order(生产订单)】的数据库操作Service实现
* @createDate 2022-09-28 15:18:51
*/
@Service
public class PmsProduceOrderServiceImpl extends ServiceImpl<PmsProduceOrderMapper, PmsProduceOrder>
    implements PmsProduceOrderService{

    /**
     * 新增生产订单信息
     * @param dto
     * @return
     */
    @Override
    @Transactional(readOnly = false)
    public Boolean add(PmsProduceOrderDTO dto) {
        if (Objects.isNull(dto)){
            throw new BusinessException("订单信息为空");
        }
        PmsProduceOrder model = new PmsProduceOrder();
        BeanUtil.copyProperties(dto,model);
        //给当前订单生成编码
        model.setCode(PmsCodeUtil.getProduceOrderCode(PmsCodeUtil.PREFIX_PMS_PRODUCE_ORDER,PmsCodeUtil.NUM_BIT_4,currentMaxCode()));
        return null;
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
        PmsProduceOrder maxCodeModel = getOne(new LambdaQueryWrapper<PmsProduceOrder>().orderByDesc(PmsProduceOrder::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(PmsCodeUtil.PREFIX_PMS_PRODUCE_ORDER.length()+PmsCodeUtil.NUM_BIT_8);
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




