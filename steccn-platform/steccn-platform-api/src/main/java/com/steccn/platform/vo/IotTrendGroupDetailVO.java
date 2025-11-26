package com.steccn.platform.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/11/12
 */
@Data
public class IotTrendGroupDetailVO {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 名称
     */
    private Integer trendId;

    /**
     * 设备id
     */
    private String deviceId;

    /**
     * 测点id
     */
    private String pointId;

    /**
     * 排序
     */
    private Integer sort;
}
