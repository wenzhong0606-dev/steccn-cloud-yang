package com.steccn.dp.vo;

import lombok.Data;

/**
 * 温度VO
 *
 * @author AndrewLuo
 * @date 2024/11/12
 */
@Data
public class DpTempVO {
    private String datetime;
    private String eUnit;
    private String eValue;
    private String eKey;
    private String eName;
    private String eNum;
}
