package com.steccn.wms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 库存流水记录表
 * @TableName wms_inventory_record
 */
@TableName(value ="wms_inventory_record")
@Data
public class WmsInventoryRecord implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 变动类型
     */
    private Long type;

    /**
     * 单据编号
     */
    private String code;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 仓库id
     */
    private Integer warehouseId;

    /**
     * 货架id
     */
    private Integer shelfId;

    /**
     * 变动数量
     */
    private BigDecimal variableQuantity;

    /**
     * 变动前总数量
     */
    private BigDecimal frontTotalQuantity;

    /**
     * 变动前可用数量
     */
    private BigDecimal frontAvailableQuantity;

    /**
     * 变动前锁定数量
     */
    private BigDecimal frontLockQuantity;

    /**
     * 变动前在途数量
     */
    private BigDecimal frontStockQuantity;

    /**
     * 变动后总数量
     */
    private BigDecimal backTotalQuantity;

    /**
     * 变动后可用数量
     */
    private BigDecimal backAvailableQuantity;

    /**
     * 变动后锁定数量
     */
    private BigDecimal backLockQuantity;

    /**
     * 变动后在途数量
     */
    private BigDecimal backStockQuantity;

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

    /**
     * 备注
     */
    private String note;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

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
        WmsInventoryRecord other = (WmsInventoryRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getType() == null ? other.getType() == null : this.getType().equals(other.getType()))
            && (this.getCode() == null ? other.getCode() == null : this.getCode().equals(other.getCode()))
            && (this.getMaterialId() == null ? other.getMaterialId() == null : this.getMaterialId().equals(other.getMaterialId()))
            && (this.getWarehouseId() == null ? other.getWarehouseId() == null : this.getWarehouseId().equals(other.getWarehouseId()))
            && (this.getShelfId() == null ? other.getShelfId() == null : this.getShelfId().equals(other.getShelfId()))
            && (this.getVariableQuantity() == null ? other.getVariableQuantity() == null : this.getVariableQuantity().equals(other.getVariableQuantity()))
            && (this.getFrontTotalQuantity() == null ? other.getFrontTotalQuantity() == null : this.getFrontTotalQuantity().equals(other.getFrontTotalQuantity()))
            && (this.getFrontAvailableQuantity() == null ? other.getFrontAvailableQuantity() == null : this.getFrontAvailableQuantity().equals(other.getFrontAvailableQuantity()))
            && (this.getFrontLockQuantity() == null ? other.getFrontLockQuantity() == null : this.getFrontLockQuantity().equals(other.getFrontLockQuantity()))
            && (this.getFrontStockQuantity() == null ? other.getFrontStockQuantity() == null : this.getFrontStockQuantity().equals(other.getFrontStockQuantity()))
            && (this.getBackTotalQuantity() == null ? other.getBackTotalQuantity() == null : this.getBackTotalQuantity().equals(other.getBackTotalQuantity()))
            && (this.getBackAvailableQuantity() == null ? other.getBackAvailableQuantity() == null : this.getBackAvailableQuantity().equals(other.getBackAvailableQuantity()))
            && (this.getBackLockQuantity() == null ? other.getBackLockQuantity() == null : this.getBackLockQuantity().equals(other.getBackLockQuantity()))
            && (this.getBackStockQuantity() == null ? other.getBackStockQuantity() == null : this.getBackStockQuantity().equals(other.getBackStockQuantity()))
            && (this.getCreateBy() == null ? other.getCreateBy() == null : this.getCreateBy().equals(other.getCreateBy()))
            && (this.getCreateName() == null ? other.getCreateName() == null : this.getCreateName().equals(other.getCreateName()))
            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
            && (this.getUpdateBy() == null ? other.getUpdateBy() == null : this.getUpdateBy().equals(other.getUpdateBy()))
            && (this.getUpdateName() == null ? other.getUpdateName() == null : this.getUpdateName().equals(other.getUpdateName()))
            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
            && (this.getDelFlag() == null ? other.getDelFlag() == null : this.getDelFlag().equals(other.getDelFlag()))
            && (this.getNote() == null ? other.getNote() == null : this.getNote().equals(other.getNote()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getType() == null) ? 0 : getType().hashCode());
        result = prime * result + ((getCode() == null) ? 0 : getCode().hashCode());
        result = prime * result + ((getMaterialId() == null) ? 0 : getMaterialId().hashCode());
        result = prime * result + ((getWarehouseId() == null) ? 0 : getWarehouseId().hashCode());
        result = prime * result + ((getShelfId() == null) ? 0 : getShelfId().hashCode());
        result = prime * result + ((getVariableQuantity() == null) ? 0 : getVariableQuantity().hashCode());
        result = prime * result + ((getFrontTotalQuantity() == null) ? 0 : getFrontTotalQuantity().hashCode());
        result = prime * result + ((getFrontAvailableQuantity() == null) ? 0 : getFrontAvailableQuantity().hashCode());
        result = prime * result + ((getFrontLockQuantity() == null) ? 0 : getFrontLockQuantity().hashCode());
        result = prime * result + ((getFrontStockQuantity() == null) ? 0 : getFrontStockQuantity().hashCode());
        result = prime * result + ((getBackTotalQuantity() == null) ? 0 : getBackTotalQuantity().hashCode());
        result = prime * result + ((getBackAvailableQuantity() == null) ? 0 : getBackAvailableQuantity().hashCode());
        result = prime * result + ((getBackLockQuantity() == null) ? 0 : getBackLockQuantity().hashCode());
        result = prime * result + ((getBackStockQuantity() == null) ? 0 : getBackStockQuantity().hashCode());
        result = prime * result + ((getCreateBy() == null) ? 0 : getCreateBy().hashCode());
        result = prime * result + ((getCreateName() == null) ? 0 : getCreateName().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateBy() == null) ? 0 : getUpdateBy().hashCode());
        result = prime * result + ((getUpdateName() == null) ? 0 : getUpdateName().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getDelFlag() == null) ? 0 : getDelFlag().hashCode());
        result = prime * result + ((getNote() == null) ? 0 : getNote().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", type=").append(type);
        sb.append(", code=").append(code);
        sb.append(", materialId=").append(materialId);
        sb.append(", warehouseId=").append(warehouseId);
        sb.append(", shelfId=").append(shelfId);
        sb.append(", variableQuantity=").append(variableQuantity);
        sb.append(", frontTotalQuantity=").append(frontTotalQuantity);
        sb.append(", frontAvailableQuantity=").append(frontAvailableQuantity);
        sb.append(", frontLockQuantity=").append(frontLockQuantity);
        sb.append(", frontStockQuantity=").append(frontStockQuantity);
        sb.append(", backTotalQuantity=").append(backTotalQuantity);
        sb.append(", backAvailableQuantity=").append(backAvailableQuantity);
        sb.append(", backLockQuantity=").append(backLockQuantity);
        sb.append(", backStockQuantity=").append(backStockQuantity);
        sb.append(", createBy=").append(createBy);
        sb.append(", createName=").append(createName);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateBy=").append(updateBy);
        sb.append(", updateName=").append(updateName);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", delFlag=").append(delFlag);
        sb.append(", note=").append(note);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}