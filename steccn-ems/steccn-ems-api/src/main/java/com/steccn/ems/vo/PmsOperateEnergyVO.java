package com.steccn.ems.vo;

import com.steccn.common.vo.CommonBarLineVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 能耗
 * @date 2023/12/1
 */
@Data
public class PmsOperateEnergyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 天然气，重油，单耗，用电量，用水量
     */
    private BigDecimal totalQuantity;

    /**
     * 单位名称
     */
    private String unitName;

    /**
     * 折线图
     */
    private CommonBarLineVO commonBarLineVO;

}
