package com.steccn.wms.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WmsReportStatisticalDTO {

    private LocalDate startDate;

    private LocalDate endDate;
}
