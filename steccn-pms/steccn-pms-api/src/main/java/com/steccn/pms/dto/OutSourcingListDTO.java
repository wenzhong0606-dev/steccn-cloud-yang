package com.steccn.pms.dto;

import com.steccn.pms.vo.FurtherValueStrVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yequan
 * @desc 外购原片不良日报表
 * @date 2023/11/3
 */
@Data
public class OutSourcingListDTO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 0-表示正常数据  1-表示小计（单规格） 2-表示合计（单供应商） 3-表示总计
     */
    private Integer type;

    /**
     * 报工主键
     */
    private Long reportWorkId;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 产线主键
     */
    private Integer productLineId;

    /**
     * 缺陷开始时间
     */
    private LocalDateTime startTime;

    /**
     * 缺陷结束时间
     */
    private LocalDateTime endTime;

    /**
     * 班次日期
     */
    private LocalDate classDate;

    /**
     * 班次主键
     */
    private Long classId;

    /**
     * 班次-int
     */
    private Integer classIdInt;

    /**
     * 班次名称
     */
    private String className;

    /**
     * 物料主键
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 工位主键
     */
    private Integer stationId;

    /**
     * 报工数量
     */
    private BigDecimal quantity;

    /**
     * 领用数-平方米
     */
    private BigDecimal quantitySq;

    /**
     * 供应商编码
     */
    private String supplierCode;

    /**
     * 供应商名称
     */
    private String supplierName;

    /**
     * 规格
     */
    private String specifications;

    /**
     * 缺陷数据
     */
    private List<Integer> defectNumList;

    private List<FurtherValueStrVO> module;

    /**
     * 缺陷数
     */
    private Integer defectNum;

    /**
     * 不良率
     */
    private BigDecimal defectRate;


}
