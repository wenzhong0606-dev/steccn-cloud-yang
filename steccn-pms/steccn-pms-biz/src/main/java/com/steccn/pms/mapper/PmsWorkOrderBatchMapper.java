package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsWorkOrderBatch;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.dto.PmsDefectDayOrderBasicDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【pms_work_order_batch(生产工单-批次信息)】的数据库操作Mapper
 * @createDate 2022-09-28 15:18:51
 * @Entity com.steccn.pms.domain.PmsWorkOrderBatch
 */
@Mapper
public interface PmsWorkOrderBatchMapper extends BaseMapper<PmsWorkOrderBatch> {

    /**
     * 不良日报表
     *
     * @param lineList 产线id集合
     * @param begin    开始时间
     * @param over     结束时间
     * @return
     */
    List<PmsDefectDayOrderBasicDTO> defectDay(@Param("lineList") List<Integer> lineList,
                                              @Param("begin") LocalDateTime begin,
                                              @Param("over") LocalDateTime over);

    /**
     * 生产报表甘特图
     *
     * @param lineList 产线id集合
     * @param begin    开始时间
     * @param over     结束时间
     * @return
     */
    List<PmsDefectDayOrderBasicDTO> produceOrderGanttChart(@Param("lineList") List<Integer> lineList,
                                              @Param("begin") LocalDateTime begin,
                                              @Param("over") LocalDateTime over);
}




