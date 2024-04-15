package com.steccn.dp.dto;


import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class DpWmsDataDTO {


    /**
     * 日期
     */
//    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "日期不能为空")
    private LocalDate date;

    /**
     * 仓库id
     */
//    @NotNull(groups = {InsertGroup.class, UpdateGroup.class}, message = "仓库不能为空")
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


    private Set<Long> materialIds;
}
