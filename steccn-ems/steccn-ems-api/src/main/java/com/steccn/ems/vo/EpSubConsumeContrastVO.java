package com.steccn.ems.vo;

import lombok.Data;

import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/16 10:38
 */
@Data
public class EpSubConsumeContrastVO {
    private List<String> xAxis;
    private List<EpSubConsumeDataVO> yData;
}
