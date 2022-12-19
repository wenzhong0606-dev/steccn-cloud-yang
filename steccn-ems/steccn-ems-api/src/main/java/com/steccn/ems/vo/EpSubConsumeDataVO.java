package com.steccn.ems.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/16 10:40
 */
@Data
public class EpSubConsumeDataVO {
    private List<BigDecimal> dataList;
    private String name;
}
