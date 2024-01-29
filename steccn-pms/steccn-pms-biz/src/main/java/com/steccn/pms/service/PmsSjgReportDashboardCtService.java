package com.steccn.pms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.QtLineChartBarDTO;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.pms.vo.SjgCountReportStopVO;
import com.steccn.pms.vo.SjgCountReportVO;
import com.steccn.pms.vo.SjgDashboardLineProduceVO;
import com.steccn.pms.vo.SjgMonthCountReportVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 深加工车间看板接口
 * @date 2023/9/5
 */
public interface PmsSjgReportDashboardCtService {

    /**
     * 产线产量
     *
     * @param workShopId 车间id
     * @param start      日期-开始
     * @param end        日期-结束
     * @param unit       单位
     * @param bit        位数
     * @return
     */
    QtLineChartBarDTO lineProduce(Integer workShopId, LocalDate start, LocalDate end, String unit, Integer bit);

    /**
     * 产线产量-表格
     *
     * @param workShopId 车间id
     * @param start      班次日期-开始
     * @param start      班次日期-结束
     * @param unit       单位
     * @param bit        位数
     * @return
     */
    SjgDashboardLineProduceVO lineProduceDetail(Integer workShopId, LocalDate start, LocalDate end, String unit, Integer bit);

    /**
     * 产线产量
     *
     * @param workShopId 车间id
     * @param start       日期
     * @param end       日期
     * @param unit       单位
     * @param bit        位数
     * @return
     */
    QtLineChartBarDTO lineProduceClassGroup(Integer workShopId, LocalDate start ,LocalDate end, String unit, Integer bit);

    /**
     * 产线产量-表格
     *
     * @param workShopId 车间id
     * @param start       日期
     * @param end       日期
     * @param unit       单位
     * @param bit        位数
     * @return
     */
    SjgDashboardLineProduceVO lineProduceClassGroupDetail(Integer workShopId, LocalDate start, LocalDate end, String unit, Integer bit);

    /**
     * 产线成品率
     * @param workShopId 车间id
     * @param start 开始
     * @param end 结束
     * @return
     */
    QtLineChartBarDTO linePassRate(Integer workShopId, LocalDate start, LocalDate end);

    /**
     * 产线成品率表格
     * @param workShopId
     * @param start
     * @param end
     * @return
     */
    SjgDashboardLineProduceVO linePassRateDetail(Integer workShopId, LocalDate start, LocalDate end);

    /**
     * 班组成品率
     * @param workShopId
     * @param start
     * @param end
     * @return
     */
    QtLineChartBarDTO lineGroupPassRate(Integer workShopId, LocalDate start, LocalDate end);

    /**
     * 班组成品率表格
     * @param workShopId
     * @param start
     * @param end
     * @return
     */
    SjgDashboardLineProduceVO lineGroupPassRateDetail(Integer workShopId, LocalDate start, LocalDate end);

    /**
     * 深加工统计报表
     * @param workShopId
     * @param start
     * @param end
     * @return
     */
    SjgCountReportVO countReport(Integer workShopId, LocalDate start, LocalDate end);

    /**
     * 深加工统计报表-停机信息
     *
     * @param lineId 产线主键
     * @param start  开始日期
     * @param end    结束日期
     * @return
     */
    List<SjgCountReportStopVO> countReportStop(Integer lineId, LocalDate start, LocalDate end);

    /**
     * 深加工月度统计报表
     *
     * @param type     类型 0表示产线 1表示车间
     * @param officeId 组织结构主键
     * @param start    开始时间
     * @param end      结束时间
     * @return
     */
    SjgMonthCountReportVO monthReport(Integer type, Integer officeId, LocalDate start, LocalDate end);
}
