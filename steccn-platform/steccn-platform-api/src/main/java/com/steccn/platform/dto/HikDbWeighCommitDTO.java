package com.steccn.platform.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 海康地磅DTO
 *
 * @author AndrewLuo
 * @date 2024/05/15
 */
@Data
public class HikDbWeighCommitDTO {
    private String model_data_id;
    private String weigh_reg_id;
    private String create_time;
    private String update_time;
    private Integer commit_status;
    private String event;
    private BigDecimal weight;
    private String material;
    private String order_id;
    private Integer valid;
    private String warehouse_loc;
    private String commit_user;
    private String commit_time;
    private String company_id;
    private String biz_type;
    private String trans_eventTime;
    private String trans_eventPeopleInCab;
    private BigDecimal result;
    private Integer net_weight_related_last_commit;
    private BigDecimal deduct_weight;
    private Integer net_weight_seq_no;
    private BigDecimal raw_net_weight;
    private BigDecimal plan_weight;
    private String trans_materialName;
    private String note;
}
