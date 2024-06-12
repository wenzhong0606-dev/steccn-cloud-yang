package com.steccn.wms.vo;

import io.swagger.models.auth.In;
import lombok.Data;

@Data
public class WmsCheckOrderAreaVO {

    private Integer shelf;

    private String shelfName;

    private Integer count;
}
