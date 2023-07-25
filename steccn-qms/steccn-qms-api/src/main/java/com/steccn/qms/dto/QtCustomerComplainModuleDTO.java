package com.steccn.qms.dto;


import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("客诉记录组件明细DTO")
public class QtCustomerComplainModuleDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 客诉记录id
     */
    private Long customerComplainId;

    /**
     * 组件序列号
     */
    private String moduleNumber;

}
