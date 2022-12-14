package com.steccn.ems.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 仪表台账
 * @TableName ep_instrument_account
 */
@TableName(value ="ep_instrument_account")
@Data
public class EpInstrumentAccount implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 所属分区节点
     */
    private Long partitionNode;

    /**
     * 仪表类型
     */
    private Integer meterType;

    /**
     * 计量单位
     */
    private Long measureUnit;

    /**
     * 0实体远传仪表、1实体非远传仪表、2虚拟仪表
     */
    private Integer entityTable;

    /**
     * 远传表映射编码
     */
    private String distantMapCode;

    /**
     * 仪表名称
     */
    private String meterName;

    /**
     * 安装位置编码(电表类型时取自数据字典)
     */
    private Long site;

    /**
     * 安装位置名称
     */
    private String siteName;

    /**
     * 资产编码
     */
    private String assetsCode;

    /**
     * 备注
     */
    private String note;

    /**
     * 0停用 1启用
     */
    private Integer status;

    /**
     * 是否删除 0 否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 是否可见 0 否 1 是
     */
    private Integer showFlag;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人名称
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新人名称
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /**
     * 更新人时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}