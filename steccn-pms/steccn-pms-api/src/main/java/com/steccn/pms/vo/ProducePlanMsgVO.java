package com.steccn.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/4 10:39
 */
@Data
public class ProducePlanMsgVO {
    private Integer rowIndex;
    private List<String> errorMsgList;
}
