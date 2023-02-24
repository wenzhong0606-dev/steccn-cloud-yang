package com.steccn.pms.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 配方详情VO
 *
 * @author AndrewLuo
 * @date 2023/02/16
 */
@Data
public class PmsRecipeVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否当前版本 0表示不是 1表示是
     */
    private Integer isCurrent;

    /**
     * 版本号
     */
    private String version;

    /**
     * 创建人姓名
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 版本历史
     */
    private List<PmsRecipeVerHisVO> verHistory;

    /**
     * 生料历史
     */
    private List<PmsRecipeRawVO> rawList;

    /**
     * 熟料占比
     */
    private BigDecimal clinkerRate;

    /**
     * 熟料用量
     */
    private BigDecimal clinkerQuantity;

    /**
     * 熟料单位
     */
    @JsonIgnore
    private Long clinkerUnit;

    /**
     * 配合料含水率
     */
    private BigDecimal batchWaterRate;

    /**
     * 加水量
     */
    private BigDecimal batchWaterQuantity;

    /**
     * 加水量单位
     */
    @JsonIgnore
    private Long batchWaterUnit;

    /**
     * 上偏差
     */
    private BigDecimal batchWaterUpper;

    /**
     * 下偏差
     */
    private BigDecimal batchWaterLower;

    /**
     * 备注
     */
    private String note;

    /**
     * 更新用量按钮权限
     * 0表示无 1表示有
     */
    private Integer buttonAmountAuth;

    /**
     * 更新用量按钮权限
     * 0表示无 1表示有
     */
    private Integer buttonRateAuth;
}
