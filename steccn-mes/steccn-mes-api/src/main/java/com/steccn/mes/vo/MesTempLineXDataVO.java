package com.steccn.mes.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 温度图表x轴VO
 *
 * @author AndrewLuo
 * @date 2023/11/15
 */
@Data
@ApiModel("温度图表x轴VO")
@AllArgsConstructor
@NoArgsConstructor
public class MesTempLineXDataVO {
    private static final long serialVersionUID = 1L;

    private List<String> data;

    private List<String> data2;

    private List<String> data3;
}
