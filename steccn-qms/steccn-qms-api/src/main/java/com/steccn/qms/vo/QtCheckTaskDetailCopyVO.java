package com.steccn.qms.vo;

import com.steccn.qms.dto.QtCheckTaskCopyDetailsDTO;
import com.steccn.qms.dto.QtCheckTaskDestoryInfoDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/17
 */
@Data
public class  QtCheckTaskDetailCopyVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 样本编号
     */
    private Integer num;

    /**
     * 分组
     */
    private List<QtCheckPlanGroupVO> checkGroup;

    /**
     * 样本详情
     */
    private QtCheckTaskCopyDetailsDTO copyDetails;

    /**
     * 落板记录
     */
    private List<QtCheckTaskDestoryInfoVO> qtCheckTaskDestoryInfo;


}
