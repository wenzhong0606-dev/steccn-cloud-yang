package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chenhuan
 * @date 2022/12/9 9:31
 */
@Data
public class QtCheckTaskDetailItemMaterialVO {
    private String itemName;

    private String checkResult;

    private Long materialId;

    private LocalDateTime classTime;

    private Integer classId;

    /**
     * 产线
     */
    private QtBaseIdNameDTO productLine;
}
