package com.steccn.ems.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 仪表台账新增编辑dto
 */
@Data
public class EpInstrumentAccountDTO implements Serializable {
    /**
     * 主键id
     */
    private Integer id;

    /**
     * 所属分区节点
     */
    @NotNull(message = "所属分区节点不能为空")
    private Long partitionNode;

    /**
     * 仪表类型
     */
    @NotNull(message = "仪表类型不能为空")
    private Integer meterType;

    /**
     * 计量单位
     */
    @NotNull(message = "计量单位不能为空")
    private Long measureUnit;

    /**
     * 0实体远传仪表、1实体非远传仪表、2虚拟仪表
     */
    @NotNull(message = "是否实体表不能为空")
    private Integer entityTable;

    /**
     * 远传表映射编码
     */
    @NotNull(message = "远传表映射编码不能为空")
    @Length(max=20,message = "远传表映射编码长度不能超过20个文字")
    private String distantMapCode;

    /**
     * 仪表名称
     */
    @NotBlank(message = "仪表名称不能为空")
    @Length(max=20,message = "仪表名称长度不能超过20个文字")
    private String meterName;

    /**
     * 安装位置
     */
    private Long site;

    /**
     * 资产编码
     */
    @Length(max = 100,message = "资产编码长度不能超过100个文字")
    private String assetsCode;

    /**
     * 备注
     */
    @Length(max = 100,message = "备注长度不能超过100个文字")
    private String note;

    /**
     * 0停用 1启用
     */
    private Integer status;
}
