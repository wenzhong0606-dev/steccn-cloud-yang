package com.steccn.wms.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 销售系统入库单生成
 *
 * @author AndrewLuo
 * @date 2023/09/13
 */
@Data
public class WmsStorageReceiptSalesDTO {
    @NotNull(message = "仓库编码不能为空")
    private String warehouseCode;

    @NotNull(message = "操作人id不能为空")
    private Long operateId;

    @NotNull(message = "操作人名称不能为空")
    private String operateName;

    @Valid
    private List<WmsStorageSalesMaterialDTO> materials;

}
