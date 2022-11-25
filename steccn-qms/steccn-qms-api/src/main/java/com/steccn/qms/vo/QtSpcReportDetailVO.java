package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * spc任务明细表
 */
@Data
public class QtSpcReportDetailVO<T> implements Serializable {

    /**
     * 用来标记表的主键id
     */
    private Integer id;

    /**
     * 时间分片开始时间
     */
    private LocalDateTime startTime;

    /**
     * 时间分片结束时间
     */
    private LocalDateTime endTime;

    /**
     * 检验项id
     */
    private Integer checkItemId;

    /**
     * 检验项名称
     */
    private String  checkItemName;

    /**
     * 检验类型:来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7
     */
    private Integer checkType;

    /**
     * 计量单位
     */
    private String measureUnit;

    /**
     * 检验结果
     */
    private BigDecimal resultAverage;

    /**
     * 标准值
     */
    private BigDecimal standardValue;

    /**
     * 上限
     */
    private BigDecimal upLimit;

    /**
     * 下限
     */
    private BigDecimal downLimit;

//    /**
//     * 检验项详情
//     */
//   private QtSpcReportDetailTaskAllVO detailTaskAllVO;

//    /**
//     * 表头的字段及该字段下的数据
//     */
//    private Map<String, Object> details;

    /**
     * 模板3
     */
    private List<Object> taskDetails;
}
