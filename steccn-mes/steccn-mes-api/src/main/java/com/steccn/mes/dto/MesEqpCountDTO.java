package com.steccn.mes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备状态计数器
 *
 * @author AndrewLuo
 * @date 2024/01/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesEqpCountDTO {
    private Integer num;
    private Integer state;
}
