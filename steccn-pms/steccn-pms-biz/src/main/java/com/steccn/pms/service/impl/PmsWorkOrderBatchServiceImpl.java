package com.steccn.pms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.domain.PmsWorkOrderBatch;
import com.steccn.pms.service.PmsWorkOrderBatchService;
import com.steccn.pms.mapper.PmsWorkOrderBatchMapper;
import com.steccn.pms.vo.WorkOrderBatchVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
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
}




