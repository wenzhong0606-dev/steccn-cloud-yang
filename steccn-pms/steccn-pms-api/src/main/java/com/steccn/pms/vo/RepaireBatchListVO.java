package com.steccn.pms.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    private Integer classId;

    private String className;

    private Integer classGroupId;

    private String classGroupName;

    private Long materialId;

    private String materialName;

    private BigDecimal useCountPiece;

    private BigDecimal useCountSquare;

}
