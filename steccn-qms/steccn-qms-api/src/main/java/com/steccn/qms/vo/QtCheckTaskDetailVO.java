package com.steccn.qms.vo;

import com.steccn.qms.dto.QtBaseIdNameDTO;
import com.steccn.qms.dto.QtCheckTaskCopyDetailsDTO;
import com.steccn.qms.dto.QtCheckTaskDestoryInfoDTO;
import com.steccn.qms.dto.QtCheckTaskSimpleDTO;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 巡检详情页面
 *
 * @author AndrewLuo
 * @date 2022/09/09
 */
@Data
public class QtCheckTaskDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 单个物料
     */
    private QtCheckTaskListVO qtCheckTaskListVo;

    /**
     * 找出检验方案
     */

    /**
     *
     */
    private QtCheckPlanDetailsVO qtCheckPlanDetailsVO;


    /**
     * pc返回值
     */
    private List<QtCheckPlanGroupVO> checkGroup;

    /**
     * pc返回样本概况模块值
     */
    private List<QtCheckTaskSimpleDTO> sampleInfoList;

    /**
     * 设置落板概况模块
     */
    private List<QtCheckTaskDestoryInfoVO> qtCheckTaskDestoryInfo;

    /**
     * 落板备注
     */
    private String destoryInfoRemark;

    /**
     * 工位id
     */
    private Long stationId;

    /**
     * 工位Name
     */
    private String stationName;

    /**
     * 工位JSON
     */
    private QtBaseIdNameDTO station;



}
