package com.steccn.qms.dto;


import io.swagger.annotations.ApiModel;
import lombok.Data;


@ApiModel("抄送人dto")
@Data
public class QtCarbonCopyManDTO {

    /**
     * 抄送人
     */
    private Long userId;

    /**
     * 抄送人名称
     */
    private String userName;
}
