package com.steccn.platform.dto;

import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author chenhuan
 * @date 2022/11/2 17:24
 */
@Data
public class SysStationWarehouseDTO extends BasePageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(groups = {UpdateGroup.class}, message = "id不能为空")
    private Long id;

    @NotNull(groups = {UpdateGroup.class}, message = "货架不能为空")
    @ApiModelProperty("货架id")
    private Integer shelfId;


    @ApiModelProperty("组织架构id")
    private Long organId;

    @ApiModelProperty("组织架构类型")
    private Integer organType;

    @NotNull(groups = {UpdateGroup.class}, message = "仓库不能为空")
    @ApiModelProperty("仓库id")
    private Integer warehouseId;

    @ApiModelProperty("备注")
    private String note;
}
