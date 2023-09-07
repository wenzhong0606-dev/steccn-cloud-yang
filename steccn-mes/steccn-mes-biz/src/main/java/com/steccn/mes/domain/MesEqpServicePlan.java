package com.steccn.mes.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName mes_eqp_service_plan
 */
@TableName(value ="mes_eqp_service_plan")
@Data
public class MesEqpServicePlan implements Serializable {
    /**
     * id
     */
    @TableId
    private String id;

    /**
     * 设备id
     */
    private String eqpId;

    /**
     * 部位
     */
    private String part;

    /**
     * 维修方式
     */
    private String maintainMethod;

    /**
     * 角色id
     */
    private String roleId;

    /**
     * 是否停机（0：是，1否）
     */
    private String isStop;

    /**
     * 周期
     */
    private Double cycle;

    /**
     * 周期单位
     */
    private String cycleUnit;

    /**
     * 工时
     */
    private Double hoursOfWork;

    /**
     * 工时单位
     */
    private String workUnits;

    /**
     * 生成任务提前量
     */
    private Double lead;

    /**
     * 生成任务提前量单位
     */
    private String leadUnit;

    /**
     * 允许延期时间
     */
    private Double delayTime;

    /**
     * 单位
     */
    private String delayUnit;

    /**
     * 参考资料（图片id）
     */
    private String fileId;

    /**
     * 维护人编码
     */
    private String accendant;

    /**
     * 维护时间
     */
    private Date maintenanceTime;

    /**
     * 执行岗位id
     */
    private String post;

    /**
     * 审批岗位
     */
    private String approvalPost;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}