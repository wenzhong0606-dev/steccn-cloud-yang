package com.steccn.oa.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * oa考勤组
 *
 * @author AndrewLuo
 * @date 2023/04/04
 */
@Data
@AllArgsConstructor
public class OaGroupIdAndNameVO implements Serializable {
    private Integer groupId;
    private String groupName;
}
