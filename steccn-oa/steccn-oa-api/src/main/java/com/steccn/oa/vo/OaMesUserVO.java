package com.steccn.oa.vo;

import lombok.Data;

import java.util.List;

/**
 * MesUser接口
 *
 * @author AndrewLuo
 * @date 2023/07/11
 */
@Data
public class OaMesUserVO {
    private String userCode;
    private String loginCode;
    /**
     * 部门code
     */
    private String officeCode;
    /**
     * 岗位code
     */
    private String postCode;
    private String username;
    private Integer oaId;
    private String officeType;
    /**
     * 分组id
     */
    private Integer groupId;
}
