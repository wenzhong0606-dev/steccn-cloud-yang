package com.steccn.qms.vo;

import com.steccn.pms.vo.FurtherValueStrVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 质量日报表-首检数据视图对象
 *
 * @author AndrewLuo
 * @date 2023/01/04
 */
@Data
public class QtDayReportFirstDataVO {

    /**
     * 任务id
     */
    private Long id;

    /**
     * 物料id
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 任务完成时间
     */
    private LocalDateTime finishedTime;

    /**
     * 检验结果
     */
    private String result;

    /**
     * 样本序号
     */
    private Integer copy;

    /**
     * 备注
     */
    private String note;

    /**
     * 班次
     */
    private String className;

    /**
     * 车间
     */
    private String productLine;

    /**
     * 数据module对象
     */
    private List<FurtherValueObjVO> module;
}
