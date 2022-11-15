package com.steccn.platform.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
@Data
public class SysOfficeTypeKeyDTO implements Serializable {
    // 省级公司    1
    // 市级公司    2
    // 后勤部门    3
    // 生产部门    4
    // 货架    5
    // 岗位    6
    // 客户    7
    // 车间    8
    // 产线    9
    // 工段    10
    // 供应商    11
    // 仓库    12
    // 工位    13

    /**
     * type 根据某个机构的类型获取该类型下的所有机构信息 例如(8)获取所有车间
     */
   private List<String> type;

    /**
     * key  输入模糊查询的关键字
     */
   private String key;
}
