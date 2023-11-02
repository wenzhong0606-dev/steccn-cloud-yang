package com.steccn.pms.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 甘特图行
 *
 * @author AndrewLuo
 * @date 2023/11/02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PmsProduceOrderGanttRowsVO {
    private String id;
    private String label;
}
