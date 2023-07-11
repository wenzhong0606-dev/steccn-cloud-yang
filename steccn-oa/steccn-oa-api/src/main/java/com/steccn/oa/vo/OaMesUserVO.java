package com.steccn.oa.vo;

import lombok.Data;

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
    private String officeCode;
    private String username;
    private Integer oaId;
}
