package com.steccn.qms.vo;

import com.steccn.pms.vo.BaseReportHeaderVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 透光率视图对象
 * @date 2023/7/14
 */
@Data
@ApiModel("透光率视图对象")
public class QtCheckItemTransmitVO implements Serializable {

    @ApiModelProperty("表头-单行")
    private List<QtSpcHeadTitleVO> headerLeft;

    @ApiModelProperty("表头-多行")
    private List<BaseReportHeaderVO> headerRight;

    @ApiModelProperty("数据集合")
    private List<QtCheckItemTransmitDataVO> dataList;
}
