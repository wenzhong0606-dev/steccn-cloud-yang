package com.steccn.canal.domain;

import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class JsSysSpareUnitConversion implements Serializable {


    @Column(name = "id")
    private Long id;

    @Column(name = "spare_id")
    private String spareId;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "conversion_quantity")
    private BigDecimal conversionQuantity;


    @Column(name = "conversion_unit_id")
    private Long conversionUnitId;


}
