package com.steccn.platform.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OA查询物料接口DTO
 *
 * @author AndrewLuo
 * @date 2022/12/26
 */
@Data
public class OaMaterialDTO extends BasePageDTO implements Serializable {
    /**
     * 查询的关键字
     */
    private String key;

    /**
     *修改时间
     */
    private LocalDateTime updateTime;
}
