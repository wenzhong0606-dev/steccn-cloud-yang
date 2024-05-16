package com.steccn.platform.dto;

import lombok.Data;

/**
 * 海康地磅DTO
 *
 * @author AndrewLuo
 * @date 2024/05/15
 */
@Data
public class HikDbWeighRegDTO {
    private String model_data_id;
    private String  create_time;
    private String update_time;
    private String weigh_no;
    private Integer weigh_count;
    private Integer weigh_status;
    private String plate_no;
    private String plate_no_in_progress;
    private String driver_name;
    private String driver_phone;
    private String note;
    private String carrier_type;
    private String scene_id;
    private Integer scene_version;
    private String reserve_no;
    private String operateTime;
    private String endTime;
    private Integer currentWeighCount;
    private Boolean just_returned;
    private String new_weigh_ev_id;
    private String trans_carrierTypeName;
}
