package com.steccn.oa.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2024/11/26
 */
@Data
public class OaDateDTO {
    private LocalDate start;
    private LocalDate end;
}
