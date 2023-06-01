package com.steccn.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * mes部门DTO
 *
 * @author AndrewLuo
 * @date 2023/05/30
 */
@Data
public class MesOfficeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String officeCode;

    private String officeName;
}
