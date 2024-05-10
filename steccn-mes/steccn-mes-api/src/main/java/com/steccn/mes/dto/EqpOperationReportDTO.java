package com.steccn.mes.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.util.List;

/**
 * 设备运行报表
 *
 * @author AndrewLuo
 * @date 2024/05/06
 */
@Data
public class EqpOperationReportDTO extends BasePageDTO {
    /**
     * 设备编码
     */
    private String eqpCode;

    /**
     * 设备名称
     */
    private String eqpName;

    /**
     * 部门编码
     */
    private String departmentCode;

    /**
     * 分类编码
     */
    private String clsCode;

    /**
     * 状态
     */
    private List<Integer> state;
}
