package com.steccn.ems.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 仪表台账表分页明细
 */
@Data
public class EpInstrumentAccountVO implements Serializable {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 所属分区节点id
     */
    private Long partitionNode;

    /**
     * 所属分区节点名称
     */
    private String partitionNodeName;

    /**
     * 仪表类型
     */
    private Integer meterType;

    /**
     * 仪表类型名称
     */
    private String meterTypeName;

    /**
     * 计量单位
     */
    private Long measureUnit;

    /**
     * 计量单位名称
     */
    private String measureUnitName;

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
     * 安装位置数据字典id
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
    private LocalDateTime createTime;

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
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}
