package com.steccn.data.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 运行状态VO
 *
 * @author AndrewLuo
 * @date 2023/09/05ss
 */
@Data
public class OperationStatusVO implements Serializable {
    /**
     * 停机次数
     */
    private Long stopNum;
    /**
     * 停机时长
     */
    private String stopTime;
    /**
     * 运行记录
     */
    private List<OperationStateVO> state;
}
