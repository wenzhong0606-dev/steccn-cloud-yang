package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * 上架策略
 * @TableName wms_shelf_up_strategy
 */
@Data
public class WmsShelfUpStrategyDTO extends BasePageDTO implements Serializable {

    private String keyWord;//条件查询 物料编码|物料名称
    private static final long serialVersionUID = 1L;
}