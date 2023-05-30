package com.steccn.platform.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * mes公司dto
 *
 * @author AndrewLuo
 * @date 2023/05/30
 */
@Data
public class MesCompanyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String companyCode;

    private String companyName;
}
