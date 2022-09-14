package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/14
 */
@Data
public class QtDefectiveItemTreeVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String categoryName;

    private List<QtDefectiveItemVO> children;
}
