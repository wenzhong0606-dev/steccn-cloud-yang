package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtCheckTaskListDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 检验类型Code
     * 来料检1/首检2/在线检3/巡检4/出货检5
     */
    private Integer checkTypeCode;

    /**
     * 任务状态 0待检验 1检验中 2已检验 3已取消 4暂存
     */
    private Integer status;

}
