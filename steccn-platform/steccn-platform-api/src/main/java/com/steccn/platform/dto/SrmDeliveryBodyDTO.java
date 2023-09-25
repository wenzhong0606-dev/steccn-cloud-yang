package com.steccn.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/09/22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SrmDeliveryBodyDTO implements Serializable {
    private SrmDeliveryDTO body;
}
