package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc x轴
 * @date 2023/9/7
 */
@Data
@ApiModel("班组产量")
@AllArgsConstructor
@NoArgsConstructor
public class LineProduceClassGroupVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> data;

    private List<String> data2;
}
