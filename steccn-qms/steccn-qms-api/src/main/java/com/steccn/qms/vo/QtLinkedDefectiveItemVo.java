package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/07
 */
@Data
public class QtLinkedDefectiveItemVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name;
}
