package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsProduceOrder;
import com.steccn.pms.domain.PmsWorkOrderBatch;
import com.steccn.pms.dto.ProduceOrderBatchDto;
import com.steccn.pms.service.PmsWorkOrderBatchService;
import com.steccn.pms.mapper.PmsWorkOrderBatchMapper;
import com.steccn.pms.util.PmsCodeUtil;
import com.steccn.pms.vo.WorkOrderBatchVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【pms_work_order_batch(生产工单-批次信息)】的数据库操作Service实现
 * @createDate 2022-09-28 15:18:51
 */
@Service
public class PmsWorkOrderBatchServiceImpl extends ServiceImpl<PmsWorkOrderBatchMapper, PmsWorkOrderBatch>
        implements PmsWorkOrderBatchService {

    @Override
    public Map<Long, WorkOrderBatchVO> listById(Set<Long> idList) {
        Map<Long, WorkOrderBatchVO> resMap = Maps.newHashMap();
        if (CollUtil.isNotEmpty(idList)) {
            Collection<PmsWorkOrderBatch> collection = listByIds(idList);
            if (CollUtil.isNotEmpty(collection)) {
                for (PmsWorkOrderBatch pmsWorkOrderBatch : collection) {
                    WorkOrderBatchVO vo = new WorkOrderBatchVO();
                    BeanUtils.copyProperties(pmsWorkOrderBatch, vo);
                    resMap.put(vo.getId(), vo);
                }
            }
        }
        return resMap;
    }

    @Override
    public PmsWorkOrderBatch getByOrderId(Long orderId) {
        PmsWorkOrderBatch orderBatch = getOne(new LambdaQueryWrapper<PmsWorkOrderBatch>().eq(PmsWorkOrderBatch::getProduceOrderId, orderId).eq(PmsWorkOrderBatch::getCurrentBatchFlag, 1));
        return orderBatch;
    }



    @Override
    public Boolean add(ProduceOrderBatchDto dto) {
        //1.定义数据模型
        PmsWorkOrderBatch orderBatch = new PmsWorkOrderBatch();
        BeanUtil.copyProperties(dto,orderBatch);
        //2.给当前批次信息生成批次号
        //TODO 班次
        orderBatch.setCode(PmsCodeUtil.getBatchCode(PmsCodeUtil.PREFIX_PMS_BATCH, PmsCodeUtil.NUM_BIT_2, currentMaxCode(),"01"));
        orderBatch.setCurrentBatchFlag(PmsCodeUtil.NUM_BIT_1);
        //3.保存批次信息
        return save(orderBatch);
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
        PmsWorkOrderBatch maxCodeModel = getOne(new LambdaQueryWrapper<PmsWorkOrderBatch>().orderByDesc(PmsWorkOrderBatch::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(PmsCodeUtil.PREFIX_PMS_PRODUCE_ORDER.length() + PmsCodeUtil.NUM_BIT_8 +PmsCodeUtil.NUM_BIT_3);
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




