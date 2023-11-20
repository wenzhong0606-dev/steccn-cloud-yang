package com.steccn.platform.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Oa数据常量
 *
 * @author AndrewLuo
 * @date 2022/12/26
 */
public interface OaDataConstant {

    String STR_CLIENT_ID = "client-id";
    /**
     * 流程退回
     */
    String WORKFLOW_DISAGREE = "01";

    /**
     * 流程同意，批准
     */
    String WORKFLOW_AGREE = "03";

    /**
     * 数字3
     */
    String NUM_3 = "3";

    /**
     * 流程归档
     */
    String WORKFLOW_FINISHED = "主管副总";

    /**
     * 归档
     */
    String WORKFLOW_END = "归档";

    /**
     * 流程节点名称
     */
    String WORKFLOW_APPROVE = "审批";

    /**
     * OA流程节点名称
     */
    String WORKFLOW_CREATE = "创建";

    /**
     * 成功
     */
    String SUCCESS = "success";




    @Getter
    @AllArgsConstructor
    enum Oa_Data_Type {
        PROCESS(1, "新流程推送接口"), MESSAGE(2, "新消息推送接口"), NOTIFY(3, "流程审批返回信息接口");

        private Integer code;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    enum Oa_WorkFlow_Type {
        REQUISITION(131, "生产领用单审批"),

        CUSTOMER_COMPLAIN(151, "客诉流程处理审批"),

        DEFECT_PRODUCT(152, "不合格品处理审批流程"),

        IMPROVE_PROGRESS_START(153, "整改启动审批流程"),
        IMPROVE_PROGRESS_END(154, "整改结案审批流程");


        private Integer workflowId;
        private String name;
    }
}
