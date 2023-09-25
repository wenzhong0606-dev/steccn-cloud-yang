package com.steccn.platform.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * srm送货单DTO
 *
 * @author AndrewLuo
 * @date 2023/09/22
 */
@Data
public class SrmDeliveryDTO implements Serializable {
    private String dateFrom;
    private String dateTo;
    private List<String> docCodes;
    private Integer page;
    private Integer size;
    private String externalSystemCode;
}
