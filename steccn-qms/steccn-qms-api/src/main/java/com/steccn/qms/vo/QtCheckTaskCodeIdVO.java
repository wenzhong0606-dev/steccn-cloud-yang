package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 检验任务id和codeVO
 *
 * @author AndrewLuo
 * @date 2023/03/24
 */
@Data
public class QtCheckTaskCodeIdVO implements Serializable {
    private Long id;
    private String code;
}
