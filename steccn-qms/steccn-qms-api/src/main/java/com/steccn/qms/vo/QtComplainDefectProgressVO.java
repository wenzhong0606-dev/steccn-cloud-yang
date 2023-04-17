package com.steccn.qms.vo;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QtComplainDefectProgressVO {
    private Integer id;

    /**
     * 操作步骤
     */
    private String opStep;

    /**
     * 操作备注
     */
    private String opNote;

    /**
     * 操作人
     */
    private String opCreateName;

    /**
     * 操作时间
     */
    private LocalDateTime opTime;

    /**
     * 操作顺序
     */
    private Integer sort;

    /**
     * 业务类型 0客诉 1不合格品
     */
    private Integer busiType;

    /**
     * 业务主键
     */
    private Long busiId;
}
