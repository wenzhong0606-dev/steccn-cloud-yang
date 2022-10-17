package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 在线检类型
 *
 * @author AndrewLuo
 * @date 2022/10/17
 */
@Data
public class QtOnlineCheckCategoryDTO implements Serializable {
    private String name;

    private Integer id;

    /**
     * 缺陷数量
     */
    private List<QtOnlineCheckDefectDTO> defect;
}
