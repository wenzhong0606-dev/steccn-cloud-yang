package com.steccn.qms.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 回显缺陷项
 *
 * @author AndrewLuo
 * @date 2022/09/13
 */
@Data
public class QtCheckTaskDefectVO implements Serializable {

    private static final long serialVersionUID = 1L;

    Map<Integer, QtTaskDefectVO> defects;

}
