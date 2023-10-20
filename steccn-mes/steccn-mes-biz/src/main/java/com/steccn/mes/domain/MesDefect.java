package com.steccn.mes.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 缺陷管理表
 * @TableName mes_defect
 */
public class MesDefect implements Serializable {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 设备id
     */
    private String eqpId;

    /**
     * 编码
     */
    private String code;

    /**
     * 类型
     */
    private String type;

    /**
     * 检修作业计划id
     */
    private Long serviceId;

    /**
     * 0待确认 2维修中 3已关闭 5已完成
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
     * 抬头
     */
    private String title;

    /**
     * 设备名字
     */
    private String eqpName;

    /**
     * 与不合格绑定
     */
    private Long unQualifiedId;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 故障类型 0电气故障 1机械故障 2其他
     */
    private Integer faultType;

    /**
     * 发现时间
     */
    private Date findTime;

    /**
     * 工单id
     */
    private Long workOrderId;

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    public Long getId() {
        return id;
    }

    /**
     * 主键id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 设备id
     */
    public String getEqpId() {
        return eqpId;
    }

    /**
     * 设备id
     */
    public void setEqpId(String eqpId) {
        this.eqpId = eqpId;
    }

    /**
     * 编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 编码
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 类型
     */
    public String getType() {
        return type;
    }

    /**
     * 类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 检修作业计划id
     */
    public Long getServiceId() {
        return serviceId;
    }

    /**
     * 检修作业计划id
     */
    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * 0待确认 2维修中 3已关闭 5已完成
     */
    public String getStatus() {
        return status;
    }

    /**
     * 0待确认 2维修中 3已关闭 5已完成
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 创建者
     */
    public String getCreateBy() {
        return createBy;
    }

    /**
     * 创建者
     */
    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    /**
     * 创建时间
     */
    public Date getCreateDate() {
        return createDate;
    }

    /**
     * 创建时间
     */
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    /**
     * 更新者
     */
    public String getUpdateBy() {
        return updateBy;
    }

    /**
     * 更新者
     */
    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    /**
     * 更新时间
     */
    public Date getUpdateDate() {
        return updateDate;
    }

    /**
     * 更新时间
     */
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    /**
     * 备注信息
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * 备注信息
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * 抬头
     */
    public String getTitle() {
        return title;
    }

    /**
     * 抬头
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 设备名字
     */
    public String getEqpName() {
        return eqpName;
    }

    /**
     * 设备名字
     */
    public void setEqpName(String eqpName) {
        this.eqpName = eqpName;
    }

    /**
     * 与不合格绑定
     */
    public Long getUnQualifiedId() {
        return unQualifiedId;
    }

    /**
     * 与不合格绑定
     */
    public void setUnQualifiedId(Long unQualifiedId) {
        this.unQualifiedId = unQualifiedId;
    }

    /**
     * 等级
     */
    public Integer getLevel() {
        return level;
    }

    /**
     * 等级
     */
    public void setLevel(Integer level) {
        this.level = level;
    }

    /**
     * 故障类型 0电气故障 1机械故障 2其他
     */
    public Integer getFaultType() {
        return faultType;
    }

    /**
     * 故障类型 0电气故障 1机械故障 2其他
     */
    public void setFaultType(Integer faultType) {
        this.faultType = faultType;
    }

    /**
     * 发现时间
     */
    public Date getFindTime() {
        return findTime;
    }

    /**
     * 发现时间
     */
    public void setFindTime(Date findTime) {
        this.findTime = findTime;
    }

    /**
     * 工单id
     */
    public Long getWorkOrderId() {
        return workOrderId;
    }

    /**
     * 工单id
     */
    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        MesDefect other = (MesDefect) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getEqpId() == null ? other.getEqpId() == null : this.getEqpId().equals(other.getEqpId()))
            && (this.getCode() == null ? other.getCode() == null : this.getCode().equals(other.getCode()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getServiceId() == null ? other.getServiceId() == null : this.getServiceId().equals(other.getServiceId()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateDate() == null ? other.getCreateDate() == null : this.getCreateDate().equals(other.getCreateDate()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateDate() == null ? other.getUpdateDate() == null : this.getUpdateDate().equals(other.getUpdateDate()))
            && (this.getRemarks() == null ? other.getRemarks() == null : this.getRemarks().equals(other.getRemarks()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getEqpName() == null ? other.getEqpName() == null : this.getEqpName().equals(other.getEqpName()))
            && (this.getUnQualifiedId() == null ? other.getUnQualifiedId() == null : this.getUnQualifiedId().equals(other.getUnQualifiedId()))
            && (this.getLevel() == null ? other.getLevel() == null : this.getLevel().equals(other.getLevel()))
            && (this.getFaultType() == null ? other.getFaultType() == null : this.getFaultType().equals(other.getFaultType()))
            && (this.getFindTime() == null ? other.getFindTime() == null : this.getFindTime().equals(other.getFindTime()))
            && (this.getWorkOrderId() == null ? other.getWorkOrderId() == null : this.getWorkOrderId().equals(other.getWorkOrderId()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getEqpId() == null) ? 0 : getEqpId().hashCode());
        result = prime * result + ((getCode() == null) ? 0 : getCode().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getServiceId() == null) ? 0 : getServiceId().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateDate() == null) ? 0 : getCreateDate().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUpdateDate() == null) ? 0 : getUpdateDate().hashCode());
        result = prime * result + ((getRemarks() == null) ? 0 : getRemarks().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getEqpName() == null) ? 0 : getEqpName().hashCode());
        result = prime * result + ((getUnQualifiedId() == null) ? 0 : getUnQualifiedId().hashCode());
        result = prime * result + ((getLevel() == null) ? 0 : getLevel().hashCode());
        result = prime * result + ((getFaultType() == null) ? 0 : getFaultType().hashCode());
        result = prime * result + ((getFindTime() == null) ? 0 : getFindTime().hashCode());
        result = prime * result + ((getWorkOrderId() == null) ? 0 : getWorkOrderId().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", eqpId=").append(eqpId);
        sb.append(", code=").append(code);
        sb.append(", type=").append(type);
        sb.append(", serviceId=").append(serviceId);
        sb.append(", status=").append(status);
        sb.append(", createBy=").append(createBy);
        sb.append(", createDate=").append(createDate);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateDate=").append(updateDate);
        sb.append(", remarks=").append(remarks);
        sb.append(", title=").append(title);
        sb.append(", eqpName=").append(eqpName);
        sb.append(", unQualifiedId=").append(unQualifiedId);
        sb.append(", level=").append(level);
        sb.append(", faultType=").append(faultType);
        sb.append(", findTime=").append(findTime);
        sb.append(", workOrderId=").append(workOrderId);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}