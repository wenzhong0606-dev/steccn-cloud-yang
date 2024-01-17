package com.steccn.pms.dto;


import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RawMaterialReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;



    /**
     * 开始时间
     */
    private LocalDate startDate;

    /**
     * 结束时间
     */
    private LocalDate endDate;

    /**
     * 物料名称
     */
    private List<String> materialName;


    /**
     * 仓库id
     */
    private List<Integer> warehouseIds;


    /**
     * 物料id
     */
    private List<Long> materialIds;

}
