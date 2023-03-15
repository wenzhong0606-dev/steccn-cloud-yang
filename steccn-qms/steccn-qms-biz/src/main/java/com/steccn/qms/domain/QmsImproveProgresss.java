package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 整改处理进度
 * @TableName qms_improve_progresss
 */
@TableName(value ="qms_improve_progresss")
@Data
public class QmsImproveProgresss implements Serializable {
    /**
     * 主键id
     */
    @TableId
    private Integer id;

    /**
     * 记录id
     */
    private Integer recordId;

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
    private Integer opId;

    /**
     * 操作人名
     */
    private String opCreateName;

    /**
     * oa流程id
     */
    private Integer instanceId;

    /**
     * 状态 待批准启动 0、未批准启动 1、D1处理中 2、D2处理中 3、D3处理中 4、D4处理中 5、D5处理中 6、D6处理中 7、D7处理中 8、D8处理中 9、已关闭 10、已中断 11、退回D1 12、退回D2 13、退回D3 14、退回D4 15、退回D5 16、退回D6 17、退回D7 18
     */
    private Integer status;

    /**
     * 创建人id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /**
     * 创建人名称
     */
    @TableField(fill = FieldFill.INSERT)
    private String createName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人id
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /**
     * 更新人名称
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateName;

    /**
     * 更新人时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除标识：0否 1是
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}