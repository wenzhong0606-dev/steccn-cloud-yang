package com.steccn.canal.domain;

import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


@Data
public class JsSysEqpSpare implements Serializable {

    @Column(name = "ID")
    private String id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CODE")
    private String code;

        @Column(name = "SPECIFICATIONS")
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

    @Column(name = "UNIT")
    private String unit;

    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "STATE")
    private String state;

    @Column(name = "BATCH_TYPE")
    private String batchType;

    @Column(name = "REMARKS")
    private String remarks;



    /**
     * 创建者
     */
    @Column(name = "CREATE_BY")
    private String createBy;

    /**
     * 创建时间
     */
    @Column(name = "CREATE_DATE")
    private Date createDate;

    /**
     * 更新者
     */
    @Column(name = "UPDATE_BY")
    private String updateBy;

    /**
     * 更新时间
     */
    @Column(name = "UPDATE_DATE")
    private Date updateDate;


    @Column(name = "STATUS")
    private String status;

    @Column(name = "product_property")
    private Integer productProperty;

}
