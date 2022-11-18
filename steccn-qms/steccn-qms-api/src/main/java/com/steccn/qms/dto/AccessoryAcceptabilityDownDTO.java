package com.steccn.qms.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/18 15:33
 */
@Data
public class AccessoryAcceptabilityDownDTO {

    private Integer workshopId;

    private Integer compare;

    private LocalDate date;

    private List<Integer> workshopIdList;

    /**
     * 图表base64
     */
    private String base64;
}
