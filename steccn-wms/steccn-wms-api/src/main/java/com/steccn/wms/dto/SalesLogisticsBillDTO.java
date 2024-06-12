package com.steccn.wms.dto;


import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@ApiModel("销售物流单")
public class SalesLogisticsBillDTO extends BasePageDTO implements Serializable {

}
