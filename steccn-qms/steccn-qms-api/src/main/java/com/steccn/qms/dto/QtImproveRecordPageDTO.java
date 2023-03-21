package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@ApiModel("整改记录分页查询")
public class QtImproveRecordPageDTO extends BasePageDTO {


    /**
     * 整改单号
     */
    private String improveOrder;

    /**
     * 描述
     */
    private String description;


    /**
     * 客诉标题
     */
    private String customerComplainTitle;

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

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;


    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
