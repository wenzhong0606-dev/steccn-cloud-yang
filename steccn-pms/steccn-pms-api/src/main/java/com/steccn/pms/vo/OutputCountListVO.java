package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/8 17:30
 */
@Data
public class OutputCountListVO {

    private String className;

    private String dataType;

    private List<BigDecimal> quantityList;
}
