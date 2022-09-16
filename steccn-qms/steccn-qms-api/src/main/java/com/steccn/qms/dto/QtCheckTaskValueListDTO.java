package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * value下listDTO
 *
 * @author AndrewLuo
 * @date 2022/09/16
 */
@Data
public class QtCheckTaskValueListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 分组id
     */
    private Integer groupId;

    /**
     * 分组下点检项
     */
    private List<QtCheckTaskCheckGroupDTO> checkGroup;
}
