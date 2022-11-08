package com.steccn.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/8 17:25
 */
@Data
public class OutputCountVO {

    List<String> productLineList;

    List<OutputCountListVO> dataList;
}
