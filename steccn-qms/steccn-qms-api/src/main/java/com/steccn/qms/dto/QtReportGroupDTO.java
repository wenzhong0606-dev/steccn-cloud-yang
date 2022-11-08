package com.steccn.qms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class QtReportGroupDTO implements Serializable {
    @NotBlank(message = "SPC卡片分组名称不能为空",groups = {InsertGroup.class, UpdateGroup.class})
    private String reportGroupName;
}
