package com.steccn.qms.vo;

import com.steccn.pms.vo.FurtherValueStrVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 损耗图表数
 *
 * @author AndrewLuo
 * @date 2023/04/20
 */
@Data
public class QtDayReportLossDataVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("类型  0正常元素  1合计 2总计 3小计")
    private Integer type;

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("班次id 0早班 1夜班")
    private Integer classId;

    @ApiModelProperty("班次名称")
    private String className;

    @ApiModelProperty("损耗名字")
    private String defectName;

    @ApiModelProperty("动态数据")
    private List<FurtherValueStrVO> module;
}
