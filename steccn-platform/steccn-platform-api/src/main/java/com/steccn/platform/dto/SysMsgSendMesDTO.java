package com.steccn.platform.dto;

import lombok.Data;

import java.util.Set;

/**
 * mes发送消息类
 *
 * @author AndrewLuo
 * @date 2023/03/13
 */
@Data
public class SysMsgSendMesDTO extends SysMsgSendDTO{
    /**
     * 用户名
     */
    private Set<String> userCodeList;
}
