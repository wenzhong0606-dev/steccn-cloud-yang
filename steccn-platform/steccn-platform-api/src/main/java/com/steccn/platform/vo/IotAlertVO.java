package com.steccn.platform.vo;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/12/11
 */
@Data
public class IotAlertVO {
    /**
     * 修改
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 设备id
     */
    private String deviceId;

    /**
     * 测点id
     */
    private String pointId;

    /**
     * 聚合规则
     */
    private String aggregations;

    /**
     * 下限
     */
    private BigDecimal downValue;

    /**
     * 上限
     */
    private BigDecimal upValue;

    /**
     * 时间间隔
     */
    private Integer intervalSec;

    /**
     * 接收人
     */
    private List<Long> receiver;

    /**
     * 接收人姓名
     */
    private List<String> receiverName;
}
