package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * 劳保记录查询
 *
 * @author AndrewLuo
 * @date 2024/04/15
 */
@Data
public class PmsLaborRecordPageDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String postCode;
    private String depCode;
    private String userName;

}
