package com.steccn.oa.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * oa公司树列表
 *
 * @author AndrewLuo
 * @date 2022/11/03
 */
@Data
public class OaCompanyVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 公司id
     */
    private Integer id;

    /**
     * 公司名字
     */
    private String name;

    /**
     * true表示有下级
     */
    private Boolean isParent;

    /**
     * 子节点
     */
    private List<OaCompanyVO> sub;
}
