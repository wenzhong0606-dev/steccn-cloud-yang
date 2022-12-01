package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * 下架策略
 */
@Data
public class WmsShelfDownStrategyDTO extends BasePageDTO implements Serializable {
    private String keyWord;//条件查询 物料编码|物料名称
    private static final long serialVersionUID = 1L;
}