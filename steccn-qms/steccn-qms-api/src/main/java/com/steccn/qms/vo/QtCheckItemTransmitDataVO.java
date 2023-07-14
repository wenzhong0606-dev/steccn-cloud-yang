package com.steccn.qms.vo;

import com.steccn.pms.vo.FurtherValueStrVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 透光率视图数据对象
 * @date 2023/7/14
 */
@Data
@ApiModel("透光率视图数据对象")
public class QtCheckItemTransmitDataVO implements Serializable {

    @ApiModelProperty("产线名称")
    private String productLineName;

    @ApiModelProperty("班次日期名称")
    private String classDateStr;

    @ApiModelProperty("班次名称")
    private String className;

    @ApiModelProperty("班组名称")
    private String classGroupName;

    @ApiModelProperty("花纹深度")
    private String figureDepth;

    @ApiModelProperty("厚度")
    private String thickness;

    @ApiModelProperty("平均值，折成3.2mm，折成3.0mm")
    private List<FurtherValueStrVO> module;
}
