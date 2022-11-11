package com.steccn.pms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/8 17:25
 */
@Data
public class OutputCountVO {

    private List<String> dx;

    private List<OutputCountListVO> dataList;
}
