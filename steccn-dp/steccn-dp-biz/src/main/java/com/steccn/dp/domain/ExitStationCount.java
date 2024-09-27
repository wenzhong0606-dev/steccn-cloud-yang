package com.steccn.dp.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 出站量统计
 * @TableName exit_station_count
 */
@TableName(value ="exit_station_count")
@Data
public class ExitStationCount implements Serializable {
    /**
     * 
     */
    @TableId
    private Long produceOrderId;

    /**
     * 
     */
    private Integer productionLineId;

    /**
     * 
     */
    private String code;

    /**
     * 
     */
    private BigDecimal valueCount;

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
        ExitStationCount other = (ExitStationCount) that;
        return (this.getProduceOrderId() == null ? other.getProduceOrderId() == null : this.getProduceOrderId().equals(other.getProduceOrderId()))
            && (this.getProductionLineId() == null ? other.getProductionLineId() == null : this.getProductionLineId().equals(other.getProductionLineId()))
            && (this.getCode() == null ? other.getCode() == null : this.getCode().equals(other.getCode()))
            && (this.getValueCount() == null ? other.getValueCount() == null : this.getValueCount().equals(other.getValueCount()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getProduceOrderId() == null) ? 0 : getProduceOrderId().hashCode());
        result = prime * result + ((getProductionLineId() == null) ? 0 : getProductionLineId().hashCode());
        result = prime * result + ((getCode() == null) ? 0 : getCode().hashCode());
        result = prime * result + ((getValueCount() == null) ? 0 : getValueCount().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", produceOrderId=").append(produceOrderId);
        sb.append(", productionLineId=").append(productionLineId);
        sb.append(", code=").append(code);
        sb.append(", valueCount=").append(valueCount);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}