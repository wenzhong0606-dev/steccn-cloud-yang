package com.steccn.pms.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ApiModel("报工记录分页vo")
public class ProcessReportWorkRecordVO implements Serializable {

    private LocalDateTime createTime;

    private String createName;

}
