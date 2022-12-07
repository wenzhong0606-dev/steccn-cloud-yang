package com.steccn.ems.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 子树传参
 *
 * @author AndrewLuo
 * @date 2022/12/07
 */
@Data
public class EpEnergyParamDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Long> partitionId;

    private Integer type;
}
