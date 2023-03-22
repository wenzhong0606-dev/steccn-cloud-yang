package com.steccn.qms.domain;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 整改记录
 * @TableName qms_improve_record
 */
@TableName(value ="qt_improve_record")
@Data
public class QtImproveRecord implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 整改单号
     */
    private String improveOrder;

    /**
     * 描述
     */
    private String description;

    /**
     * 客诉id
     */
    private Long customerComplainId;

    /**
     * 客诉标题
     */
    private String customerComplainTitle;

    /**
     * 不合格品id
     */
    private Long defectProductId;

    /**
     * 不合格品标题
     */
    private String defectProductTitle;

    /**
     * 状态 待批准启动 0、未批准启动 1、D1处理中 2、D2处理中 3、D3处理中 4、D4处理中 5、D5处理中 6、D6处理中 7、D7处理中 8、D8处理中 9、已关闭 10、已中断 11
     */
    private Integer status;

    /**
     * 整改启动审批人id
     */
    private Integer startApproverId;

    /**
     * 整改启动审批人名
     */
    private String startApproverName;

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