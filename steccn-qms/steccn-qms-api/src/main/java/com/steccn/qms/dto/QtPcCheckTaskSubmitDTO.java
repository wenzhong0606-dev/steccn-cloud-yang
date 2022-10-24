package com.steccn.qms.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * pc巡检任务值提交
 *
 * @author AndrewLuo
 * @date 2022/10/10
 */
@Data
public class QtPcCheckTaskSubmitDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private Long id;

    /**
     * 任务状态
     */
    private Integer status;

    /**
     * 合格数量
     */
    private Integer goodNum;

    /**
     * 不合格数量
     */
    private Integer badNum;

    /**
     * 结果
     */
    private String result;

    /**
     * 备注
     */
    private String note;

    /**
     * 缺陷数量
     */
    private Integer defectNum;

    /**
     * 检验数量
     */
    private Integer checkNum;

    /**
     * 分组
     */
    private List<QtPcCheckTaskCheckGroupDTO> checkGroup;

    /**
     * 在线检
     */
    private List<QtOnlineCheckItemDTO> onlineItem;

}
