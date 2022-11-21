package com.steccn.platform.vo;

import lombok.Data;

/**
 * @author chenhuan
 * @date 2022/11/4 18:40
 */
@Data
public class SysStationWarehouseVO {

    private Long id;

    private String warehouseName;

    private String shelfName;

    private String shelfCode;

    /**
     * 仓库id
     */
    private Integer warehouseId;

    /**
     * 货架id
     */
    private Integer shelfId;

    private String note;

    /**
     * 工位id
     */
    private Integer stationId;
}
