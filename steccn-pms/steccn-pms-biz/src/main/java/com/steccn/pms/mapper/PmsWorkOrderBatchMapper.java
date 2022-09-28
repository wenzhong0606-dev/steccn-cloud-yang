package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsWorkOrderBatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【pms_work_order_batch(生产工单-批次信息)】的数据库操作Mapper
* @createDate 2022-09-28 15:18:51
* @Entity com.steccn.pms.domain.PmsWorkOrderBatch
*/
@Mapper
public interface PmsWorkOrderBatchMapper extends BaseMapper<PmsWorkOrderBatch> {

}




