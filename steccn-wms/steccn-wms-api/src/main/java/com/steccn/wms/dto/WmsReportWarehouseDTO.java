package com.steccn.wms.dto;


import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.pms.vo.PassRateDateVo;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "日期不能为空")
    private LocalDate  date;

    /**
     * 仓库id
     */
    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "仓库不能为空")
    private Set<Long> warehouseIds;

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
