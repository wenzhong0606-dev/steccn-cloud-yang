package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("工序报工-物料消息vo")
public class ProcessReportWorkMaterialConsumeVO implements Serializable {

    private Long materialId;

    private String materialName;


}
