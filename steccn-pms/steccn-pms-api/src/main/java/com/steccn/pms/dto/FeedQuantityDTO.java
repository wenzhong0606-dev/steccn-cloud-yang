package com.steccn.pms.dto;

import com.google.common.base.Objects;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 投料信息DTO
 *
 * @author Mongknife
 * @date 2022/11/11
 */
@ApiModel("投料信息DTO")
@Getter
@Setter
public class FeedQuantityDTO {

    @ApiModelProperty("订单id")
    private Long produceOrderId;

    @ApiModelProperty("工序id")
    private Long processId;

    @ApiModelProperty("物料id")
    private Long materialId;

    @ApiModelProperty("数量")
    private BigDecimal quantity;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o instanceof FeedQuantityDTO){
            FeedQuantityDTO that = (FeedQuantityDTO) o;
            return Objects.equal(produceOrderId, that.produceOrderId) && Objects.equal(processId, that.processId) && Objects.equal(materialId, that.materialId) ;
        }
       return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(produceOrderId, processId, materialId);
    }
}
