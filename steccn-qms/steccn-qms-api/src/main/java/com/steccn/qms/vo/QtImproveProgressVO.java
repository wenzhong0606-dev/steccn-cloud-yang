package com.steccn.qms.vo;



import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@ApiModel("整改记录进度")
@Data
public class QtImproveProgressVO {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 记录id
     */
    private Long recordId;

    /**
     * 操作标题
     */
    private String opTitle;

    /**
     * 备注
     */
    private String opNote;

    /**
     * 操作时间
     */
    private LocalDateTime opTime;

    /**
     * 操作人id
     */
    private Long opId;

    /**
     * 操作人名
     */
    private String opCreateName;

}
