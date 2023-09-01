package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 字典数据表
 * @TableName js_sys_dict_data
 */
@TableName(value ="js_sys_dict_data")
@Data
public class JsSysDictData implements Serializable {
    /**
     * 字典编码
     */
    @TableId
    private String dictCode;

    /**
     * 父级编号
     */
    private String parentCode;

    /**
     * 所有父级编号
     */
    private String parentCodes;

    /**
     * 本级排序号（升序）
     */
    private Integer treeSort;

    /**
     * 所有级别排序号
     */
    private String treeSorts;

    /**
     * 是否最末级
     */
    private String treeLeaf;

    /**
     * 层次级别
     */
    private Integer treeLevel;

    /**
     * 全节点名
     */
    private String treeNames;

    /**
     * 字典标签
     */
    private String dictLabel;

    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 字典类型
     */
    private String dictType;

    /**
     * 系统内置（1是 0否）
     */
    private String isSys;

    /**
     * 字典描述
     */
    private String description;

    /**
     * css样式（如：color:red)
     */
    private String cssStyle;

    /**
     * css类名（如：red）
     */
    private String cssClass;

    /**
     * 状态（0正常 1删除 2停用）
     */
    private String status;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 备注信息
     */
    private String remarks;

    /**
     * 租户名称
     */
    private String corpName;

    /**
     * 扩展 String 1
     */
    private String extendS1;

    /**
     * 扩展 String 2
     */
    private String extendS2;

    /**
     * 扩展 String 3
     */
    private String extendS3;

    /**
     * 扩展 String 4
     */
    private String extendS4;

    /**
     * 扩展 String 5
     */
    private String extendS5;

    /**
     * 扩展 String 6
     */
    private String extendS6;

    /**
     * 扩展 String 7
     */
    private String extendS7;

    /**
     * 扩展 String 8
     */
    private String extendS8;

    /**
     * 扩展 Integer 1
     */
    private Long extendI1;

    /**
     * 扩展 Integer 2
     */
    private Long extendI2;

    /**
     * 扩展 Integer 3
     */
    private Long extendI3;

    /**
     * 扩展 Integer 4
     */
    private Long extendI4;

    /**
     * 扩展 Float 1
     */
    private BigDecimal extendF1;

    /**
     * 扩展 Float 2
     */
    private BigDecimal extendF2;

    /**
     * 扩展 Float 3
     */
    private BigDecimal extendF3;

    /**
     * 扩展 Float 4
     */
    private BigDecimal extendF4;

    /**
     * 扩展 Date 1
     */
    private Date extendD1;

    /**
     * 扩展 Date 2
     */
    private Date extendD2;

    /**
     * 扩展 Date 3
     */
    private Date extendD3;

    /**
     * 扩展 Date 4
     */
    private Date extendD4;

    /**
     * 字典图标
     */
    private String dictIcon;

    /**
     * 扩展 JSON
     */
    private String extendJson;

    /**
     * 
     */
    private String corpCode;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}