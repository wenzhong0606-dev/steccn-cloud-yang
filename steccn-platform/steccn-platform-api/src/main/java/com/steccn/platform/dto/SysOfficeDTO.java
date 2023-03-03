package com.steccn.platform.dto;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/6
 */
@Data
@ApiModel("机构数据dto")
public class SysOfficeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("机构名称")
    private String officeName;

    @ApiModelProperty("机构编码")
    private String officeCode;

    @ApiModelProperty("机构类型")
    private String officeType;


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
     * 机构代码
     */
    private String viewCode;


    /**
     * 机构全称   CUCC 公司表SUBCOMPANY_DESC
     */
    private String fullName;


    /**
     * 负责人
     */
    private String leader;

    /**
     * 办公电话
     */
    private String phone;

    /**
     * 联系地址
     */
    private String address;

    /**
     * 邮政编码
     */
    private String zipCode;

    /**
     * 电子邮箱
     */
    private String email;

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
    private LocalDateTime createDate;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateDate;

    /**
     * 备注信息
     */
    private String remarks;
}
