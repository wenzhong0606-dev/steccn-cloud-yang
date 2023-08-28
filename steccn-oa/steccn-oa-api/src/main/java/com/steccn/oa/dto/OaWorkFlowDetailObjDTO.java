package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 详情数据对象DTO
 *
 * @author AndrewLuo
 * @date 2023/01/16
 */
@Data
public class OaWorkFlowDetailObjDTO implements Serializable {
    /**
     * 数据库表名称
     */
    private String tableDBName;

    /**
     * 详情数据
     */
    private List<OaWorkFlowDetailDataListDTO> workflowRequestTableRecords;
}
