package com.steccn.mes.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 点检项id
 *
 * @author AndrewLuo
 * @date 2023/11/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesItemIdNameVO {
    private Long itemId;
    private String itemName;
}
