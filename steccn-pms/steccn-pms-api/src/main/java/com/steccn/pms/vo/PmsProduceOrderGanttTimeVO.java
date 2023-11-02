package com.steccn.pms.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 甘特图时间戳
 *
 * @author AndrewLuo
 * @date 2023/11/02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PmsProduceOrderGanttTimeVO {
    private Long start;
    private Long end;
}
