package com.steccn.platform.vo;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class SaleLogisticsDataVO {

    private String product_name;
    private String product_code;
    private String model_specs;
    private BigDecimal qty;

}
