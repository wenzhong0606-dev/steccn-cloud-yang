package com.steccn.pms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import lombok.Data;

/**
 * 打印记录流水信息
 * @TableName pms_barcode_print_record
 */
@TableName(value ="pms_barcode_print_record")
@Data
public class PmsBarcodePrintRecord implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 班次id
     */
    private Long classesId;

    /**
     * 班组id
     */
    private Long classGroupId;


    /**
     * 产线
     */
    private Long produceLineId;

    /**
     * 生产订单id
     */
    private Long produceOrderId;

    /**
     * 打印日期
     */
    private LocalDate printDate;

    /**
     * 流水号
     */
    private Integer serialNum;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人id
     */
    private Long updateBy;

    /**
     * 更新人名称
     */
    private String updateName;

    /**
     * 更新人时间
     */
    private Date updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}