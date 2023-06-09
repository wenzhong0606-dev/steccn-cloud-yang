package com.steccn.pms.vo;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("产量质量报表-深加工")
public class OutputQualitySjgVO {


    /**
     * 从表数据集合
     */
    private List<OutputQualityFromDataVO> fromTableList;


    /**
     * 数据集合
     */
    private List<OutputQualitySjgDataVO>  tableList;



    /**
     * 表头
     */
    private List<OutputQualityHeaderVO> columns;


    /**
     * 类型
     */
    private Integer type;
}
