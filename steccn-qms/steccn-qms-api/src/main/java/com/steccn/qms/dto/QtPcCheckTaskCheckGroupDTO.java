package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 提交任务下的groupDTO
 *
 * @author AndrewLuo
 * @date 2022/10/10
 */
@Data
public class QtPcCheckTaskCheckGroupDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * groupId
     */
    private Integer id;

    /**
     * 分组下的items
     */
    private List<QtPcCheckTaskCheckGroupItemDTO> items;
}
