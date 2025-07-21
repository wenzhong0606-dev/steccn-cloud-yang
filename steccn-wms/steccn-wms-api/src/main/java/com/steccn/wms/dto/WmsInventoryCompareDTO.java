package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/21
 */
@Data
public class WmsInventoryCompareDTO extends BasePageDTO {
    private String materialCode;
    @NotBlank(message = "仓库不能为空")
    private String warehouseCode;
}
