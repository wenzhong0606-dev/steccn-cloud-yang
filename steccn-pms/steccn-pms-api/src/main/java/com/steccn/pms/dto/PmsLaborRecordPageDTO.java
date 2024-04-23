package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 劳保记录查询
 *
 * @author AndrewLuo
 * @date 2024/04/15
 */
@Data
public class PmsLaborRecordPageDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Set<String> postId;
    private Set<String> officeId;
    private String userName;
    private Set<Long> materialId;

}
