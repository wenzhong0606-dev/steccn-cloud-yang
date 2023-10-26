package com.steccn.qms.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yequan
 * @desc 客诉报表请求类
 * @date 2023/10/12
 */
@Data
public class QtCustomerComplaintReportDTO implements Serializable {

    /**
     * 开始日期
     */
    @NotNull(message = "开始日期不能为空")
    private LocalDate start;

    /**
     * 结束日期
     */
    @NotNull(message = "结束日期不能为空")
    private LocalDate end;

    /**
     * 投诉类型
     */
    private List<Integer> complainTypeList;

    /**
     * 物料名称
     */
    private List<Long> materialIdList;

    /**
     * 客户
     */
    private List<Integer> customerIdList;

}
