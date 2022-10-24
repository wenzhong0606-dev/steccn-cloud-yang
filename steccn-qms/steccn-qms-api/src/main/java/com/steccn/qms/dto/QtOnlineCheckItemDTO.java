package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/10/14
 */
@Data
public class QtOnlineCheckItemDTO implements Serializable {
    /**
     * 检验项id
     */
    private Integer itemId;

    /**
     * 检验项name
     */
    private String itemName;

    /**
     * 分组id
     */
    private Integer groupId;

    private List<QtOnlineCheckCategoryDTO> category;
}
