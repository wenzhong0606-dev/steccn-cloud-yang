package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 配料抽检合格率DTO
 */
@Data
public class QtAccessoryAcceptabilityDTO extends BasePageDTO implements Serializable {
    /**
     * 检验完成时间在选中日期8:00 到20:00内的所有配料抽检任务
     */
    @NotNull(message = "时间不能为空")
    private LocalDate dateTime;

    /**
     * 对比
     */
    private Integer compare;

    /**
     * 物料使用车间
     */
    private Integer materialWorkshopId;

    private List<Integer> workshopIdList;

    /**
     * 区分柱状图饼状图的字段
     */
    private Integer type;

}
