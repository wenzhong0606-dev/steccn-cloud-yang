package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/06/29
 */
@Data
public class OaDepartmentTreeVO implements Serializable {
    private Integer id;
    private String departmentname;
    private Integer supdepid;
    private BigDecimal showorder;
    private Integer showorderoftree;
    private List<OaDepartmentTreeVO> children;

    public void addChild(OaDepartmentTreeVO child){
        if(children==null){
            children = new ArrayList<>();
        }
        children.add(child);
    }
}
