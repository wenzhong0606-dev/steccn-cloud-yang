package com.steccn.ems.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @desc 能耗分区对比平均值，最值表格
 */
@Data
public class ConsumePartitionContrastTableVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("日期格式")
    private List<String> dateStrList;

    @ApiModelProperty("行数据集合")
    private List<ConsumePartitionContrastTableRowVO> rowList;

}
