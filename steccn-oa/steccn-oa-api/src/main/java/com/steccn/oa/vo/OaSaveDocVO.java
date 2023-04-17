package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 保存文档VO
 *
 * @author AndrewLuo
 * @date 2023/04/12
 */
@Data
public class OaSaveDocVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 文档id
     */
    private Integer docid;

    /**
     * 1成功 -1失败
     */
    private Integer status;
}
