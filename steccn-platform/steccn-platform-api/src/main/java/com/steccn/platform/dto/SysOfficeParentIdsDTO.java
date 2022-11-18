package com.steccn.platform.dto;

import lombok.Data;

import java.util.List;

/**
 * 车间父id
 */
@Data
public class SysOfficeParentIdsDTO {
    /**
     * 父级id
     */
    List<Integer> officeParentIds;
}
