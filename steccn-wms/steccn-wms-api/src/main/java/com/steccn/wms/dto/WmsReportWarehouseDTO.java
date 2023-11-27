package com.steccn.wms.dto;


import com.steccn.common.dto.BasePageDTO;
import com.steccn.pms.vo.PassRateDateVo;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 仓储驾驶仓单据DTO
 *
 * @author 孟乐
 * @date 2023年11月23日
 */

@Data
@ApiModel("仓储驾驶仓单据DTO")
public class WmsReportWarehouseDTO extends BasePageDTO implements Serializable {

    /**
     * 日期
     */
    private LocalDate  date;

    /**
     * 仓库id
     */

    private  Long warehouseId;

    /**
     * 类型id
     */
    private Long typeId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
