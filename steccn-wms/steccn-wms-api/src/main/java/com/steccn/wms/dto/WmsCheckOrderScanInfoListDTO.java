package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

@Data
public class WmsCheckOrderScanInfoListDTO extends BasePageDTO {

    private Integer shelf;

    private Long checkOrderId;
}
