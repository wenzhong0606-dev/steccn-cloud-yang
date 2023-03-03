package com.steccn.canal.domain;

import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;


@Data
public class JsSysEqpSpare implements Serializable {

    @Column(name = "id")
    private String id;

    @Column(name = "code")
    private String code;

    @Column(name = "specifications")
    private String specifications;

    @Column(name = "length_unit_id")
    private Long lengthUnitId;

    @Column(name ="length_quantity")
    private BigDecimal lengthQuantity;

    @Column(name = "high_unit_id")
    private Long highUnitId;

    @Column(name = "high_quantity")
    private BigDecimal highQuantity;

    @Column(name = "wide_unit_id")
    private Long wideUnitId;

    @Column(name = "wide_quantity")
    private BigDecimal wideQuantity;

    @Column(name = "volume_quantity")
    private BigDecimal volumeQuantity;

    @Column(name = "volume_unit_id")
    private Long volumeUnitId;

    @Column(name = "weight_quantity")
    private BigDecimal weightQuantity;

    @Column(name = "weight_unit_id")
    private Long weightUnitId;

    @Column(name = "procurement_type_id")
    private Long procurementTypeId;

    @Column(name = "thick_quantity")
    private BigDecimal thickQuantity;

    @Column(name = "thick_unit_id")
    private Long thickUnitId;

    @Column(name = "unit")
    private String unit;

    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "state")
    private String state;

    @Column(name = "batch_type")
    private String batchType;

    @Column(name = "remarks")
    private String remarks;



}
