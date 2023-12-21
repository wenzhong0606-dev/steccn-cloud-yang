package com.steccn.platform.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author yequan
 * @desc 标准产能的明细
 * @date 2023/12/20
 */
@Data
public class SysProductionCapacityInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @NotNull(message = "明细主键不能为空")
    private Integer id;

    /**
     * 车间
     */
    @NotNull(message = "车间不能为空")
    private Integer workshopId;

    /**
     * 产线
     */
    @NotNull(message = "产线不能为空")
    private Integer lineId;

    /**
     * 工位
     */
    @NotNull(message = "工位不能为空")
    private Integer stationId;

    /**
     * 产线基数
     */
    @NotNull(message = "产线基数不能为空")
    private Integer lineNum;
}
