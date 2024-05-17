package com.steccn.wms.dto;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ApiModel("仓储统计日报表DTO")
public class WmsInventoryDaySheetDTO {

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private  Integer status;

    private Integer warehouseId;

    private Long type;

    private String classes;

    private String productionLineName;

    private String materialName;

    private List<String> caseNoList;

}
