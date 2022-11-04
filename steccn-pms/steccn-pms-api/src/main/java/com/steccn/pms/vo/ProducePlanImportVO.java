package com.steccn.pms.vo;

import lombok.Data;

import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/4 10:44
 */
@Data
public class ProducePlanImportVO {
    private Boolean importFlag;
    private List<ProducePlanMsgVO> producePlanMsgVOList;
}
