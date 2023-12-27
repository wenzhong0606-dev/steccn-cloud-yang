package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsReportWorkHour;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.vo.SjgCountReportRowVO;
import com.steccn.pms.vo.SjgCountReportStopRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【pms_report_work_hour(报工工时信息)】的数据库操作Mapper
 * @createDate 2022-09-28 15:18:51
 * @Entity com.steccn.pms.domain.PmsReportWorkHour
 */
@Mapper
public interface PmsReportWorkHourMapper extends BaseMapper<PmsReportWorkHour> {

    /**
     * 根据产线和日期区间获取工序工位停机信息
     *
     * @param lineId 产线主键
     * @param start  开始日期
     * @param end    结束日期
     * @return
     */
    List<SjgCountReportStopRowVO> countReportStop(@Param("lineId") Integer lineId, @Param("start") LocalDate start,@Param("end") LocalDate end);
}




