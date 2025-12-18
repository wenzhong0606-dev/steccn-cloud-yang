package com.steccn.platform.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/12/15
 */
@Data
public class IotAlertRecordVO {
    /**
     * id
     */
    private Long id;

    /**
     * 名称
     */
    private Integer alertId;

    /**
     * 内容
     */
    private String content;

    /**
     * 报警人名称
     */
    private List<String> receiverName;

    /**
     * 报警时间
     */
    private LocalDateTime alertTime;
}
