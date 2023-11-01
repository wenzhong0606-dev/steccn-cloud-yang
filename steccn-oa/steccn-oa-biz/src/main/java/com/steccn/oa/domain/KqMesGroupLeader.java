package com.steccn.oa.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 班组长信息
 * @TableName kq_mes_group_leader
 */
@TableName(value ="kq_mes_group_leader")
@Data
public class KqMesGroupLeader implements Serializable {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 车间id
     */
    private String workshopName;

    /**
     * 班组长名称
     */
    private String leaderName;

    /**
     * 班组名称
     */
    private String groupName;

    /**
     * 备注
     */
    private String note;

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