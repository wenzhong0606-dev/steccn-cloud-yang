package com.steccn.pms.vo;

import com.steccn.pms.dto.OutSourcingListDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 外购原片不良日报表
 * @date 2023/11/2
 */
@Data
public class OutsourcingDayReportVO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 表头
     */
    private List<BaseReportHeaderVO> headerList;

    /**
     *  数据集合
     */
    private List<OutSourcingListDTO> dataList;



}
