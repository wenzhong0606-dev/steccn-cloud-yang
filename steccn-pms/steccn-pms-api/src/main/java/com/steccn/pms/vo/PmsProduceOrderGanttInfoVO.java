package com.steccn.pms.vo;

import lombok.Data;

/**
 * 甘特图具体信息VO
 *
 * @author AndrewLuo
 * @date 2023/11/02
 */
@Data
public class PmsProduceOrderGanttInfoVO {
    private Long id;
    private String label;
    private String rowId;
    private Integer status;
    private PmsProduceOrderGanttTimeVO time;
}
