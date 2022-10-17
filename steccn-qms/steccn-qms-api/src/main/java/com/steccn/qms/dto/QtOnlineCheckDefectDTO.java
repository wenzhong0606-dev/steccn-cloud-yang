package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/10/14
 */
@Data
public class QtOnlineCheckDefectDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer defectId;

    private Integer defectNumber;

    private String defectName;
}
