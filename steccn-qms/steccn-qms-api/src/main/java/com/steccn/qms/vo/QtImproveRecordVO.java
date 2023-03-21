package com.steccn.qms.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@ApiModel("整改记录")
public class QtImproveRecordVO {

    /**
     * 主键
     */
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
    private Long startApproverId;

    /**
     * 整改启动审批人名
     */
    private String startApproverName;

    /**
     * 创建人id
     */
    private Long createBy;

    /**
     * 创建人名称
     */
    private String createName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
