package com.steccn.pms.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @desc 返洗不良日报表
 * @date 2023/10/16
 */
@Data
public class RepaireBatchListVO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 批次code
     */
    private String batchNo;

    /**
     * 生产订单id
     */
    private Long produceOrderId;

    /**
     * 生产订单编码
     */
    private String produceOrderNo;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 规格
     */
    private String specifications;

    /**
     * 班次日期
     */
    private LocalDate classDate;

    /**
     * 产线
     */
    private Integer productLine;

    /**
     * 产线名称
     */
    private String productLineName;

    /**
     * 班次主键
     */
    private Integer classId;

    /**
     * 班次名称
     */
    private String className;

    /**
     * 班组主键
     */
    private Integer classGroupId;

    /**
     * 班组名称
     */
    private String classGroupName;

    /**
     * 物料主键
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 耗用数-片
     */
    private BigDecimal useCountPiece;

    /**
     * 耗用数-平方米
     */
    private BigDecimal useCountSquare;

    /**
     * 缺陷不良- 数值列
     */
    private List<BigDecimal> valueList;

    /**
     * 不良总数-合计
     */
    private BigDecimal defectSum;

    /**
     * 不良率
     */
    private BigDecimal defectRate;

}
