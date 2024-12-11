package com.steccn.oa.vo;

import lombok.Data;

import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/12/11
 */
@Data
public class OaFormTableMain441VO {
    private Integer id;

    /**
     *
     */
    private Integer requestid;

    /**
     *
     */
    private String khmc;

    /**
     *
     */
    private String glhth;

    /**
     *
     */
    private Integer sqr;

    /**
     *
     */
    private Integer sqbm;

    /**
     *
     */
    private String sqrq;

    /**
     *
     */
    private String fhyf;

    /**
     *
     */
    private String jhdz;

    /**
     *
     */
    private Integer jhlx;

    /**
     *
     */
    private Integer jsbz;

    List<OaFormTableDT441VO> detailList;
}
