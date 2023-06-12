package com.steccn.pms.vo;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("产量质量报表")
public class OutputQualityVO {


    /**
     * 从表数据集合
     */
    private List<OutputQualityFromDataVO> fromTableList;


    /**
     * 数据集合
     */
    private List<OutputQualityDataVO>  tableList;



    /**
     * 表头
     */
    private List<OutputQualityHeaderVO> columns;


    /**
     * 类型
     */
    private Integer type;
}
