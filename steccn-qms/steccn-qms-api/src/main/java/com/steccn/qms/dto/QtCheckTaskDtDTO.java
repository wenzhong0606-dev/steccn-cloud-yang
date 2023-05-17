package com.steccn.qms.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * 数字孪生DTO
 *
 * @author AndrewLuo
 * @date 2023/05/16
 */
@Data
public class QtCheckTaskDtDTO {
    @NotEmpty(message = "产线id不能为空")
    private Set<Integer> productLineId;
}
