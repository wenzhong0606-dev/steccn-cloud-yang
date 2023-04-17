package com.steccn.qms.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;


@Data
@ApiModel("整改记录处理dto")
public class QtImproveRecordDisposeDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 整改单号
     */
    private String improveOrder;

    /**
     * 整改标题
     */
    private String title;

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
     * d1整改启动审批人id
     */
    private Long startApproverId;

    /**
     * d2整改启动审批人名
     */
    private String startApproverName;


    /**
     * D2处理人
     */
    private Long d2Processor;

    /**
     * D2处理人人名
     */
    private String d2ProcessorName;


    /**
     * D3处理人
     */
    private Long d3Processor;

    /**
     * D3处理人人名
     */
    private String d3ProcessorName;

    /**
     * D4处理人
     */
    private Long d4Processor;

    /**
     * D4处理人人名
     */
    private String d4ProcessorName;


    /**
     * D5处理人
     */
    private Long d5Processor;

    /**
     * D5处理人人名
     */
    private String d5ProcessorName;


    /**
     * D6处理人
     */
    private Long d6Processor;

    /**
     * D6处理人人名
     */
    private String d6ProcessorName;


    /**
     * D7处理人
     */
    private Long d7Processor;

    /**
     * D7处理人人名
     */
    private String d7ProcessorName;


    /**
     * D8处理人
     */
    private Long d8Processor;

    /**
     * D8处理人人名
     */
    private String d8ProcessorName;


    /**
     * 处理结论
     */
    @Size(min = 0, max = 500, message = "结论长度不能超过 500个字符")
    private String approverConclusion;


    /**
     * d8抄送人
     */
    private List<QtCarbonCopyManDTO>  carbonCopyManList;




}
