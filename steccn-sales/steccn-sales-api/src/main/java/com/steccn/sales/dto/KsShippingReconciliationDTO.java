package com.steccn.sales.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("销售发货")
public class KsShippingReconciliationDTO extends BasePageDTO implements Serializable {
}
