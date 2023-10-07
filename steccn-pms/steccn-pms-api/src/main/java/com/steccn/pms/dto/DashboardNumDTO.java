package com.steccn.pms.dto;

import com.steccn.pms.constant.PmManagerConstant;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @desc
 * @date 2023/9/22
 */
@Data
@ApiModel("看板产量")
public class DashboardNumDTO implements Serializable {

    /**
     * 车间
     */
    private Integer workShopId;

    /**
     * 产线
     */
    private List<Integer> lineIdList;

    /**
     * 开始日期
     */
    private LocalDate start;

    /**
     * 结束日期
     */
    private LocalDate end;

    /**
     * 类型 基片-0 深加工-1
     */
    private Integer type;

    /**
     * 保留位数
     */
    private Integer bit;

    /**
     * 单位
     */
    private String unit;


}
