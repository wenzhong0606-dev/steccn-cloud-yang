package com.steccn.ems.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.ems.vo.EpDayReportCountVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 能耗日统计报表
 * @date 2023/6/1
 */
public interface EpDayReportService {

    /**
     * 能耗日统计报表
     *
     * @param month 年月
     * @param flag  是否计入缓存
     * @return 结果
     */
    EpDayReportCountVO count(LocalDate month, boolean flag);

    /**
     * 能耗日报表数字孪生接口
     * @return
     */
    List<TupleDTO<String,String>> dataTwinCount();

    /**
     * 能耗日统计报表下载
     *
     * @param month 年月
     * @param flag  是否计入缓存
     * @return 结果
     */
    void countDownload(LocalDate month, boolean flag, HttpServletResponse response) throws IOException;

    Boolean countTask(LocalDate date);

    /**
     * 单耗日统计报表
     *
     * @param month 年月
     * @param flag  是否计入缓存
     * @return 结果
     */
    EpDayReportCountVO unitConsumeCount(LocalDate month, boolean flag);

    /**
     * 余热发电 - 电 & 水
     *
     * @param start          开始日期
     * @param end            结束日期
     * @param granularityInt 粒度
     * @return
     */
    List<CommonReportDTO> heatPowerElecWater(LocalDate start, LocalDate end, Integer granularityInt);


    /**
     * 光伏发电
     *
     * @param start          开始日期
     * @param end            结束日期
     * @param granularityInt 粒度
     * @return
     */
    List<CommonReportDTO> gfPowerList(LocalDate start, LocalDate end, Integer granularityInt);

    /**
     * 脱硫脱硝
     *
     * @param start          开始日期
     * @param end            结束日期
     * @param granularityInt 粒度
     * @return
     */
    List<CommonReportDTO> scList(LocalDate start, LocalDate end, Integer granularityInt);

}
