package com.steccn.ems.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 历史分布
 *
 * @author AndrewLuo
 * @date 2022/12/09
 */
@Data
public class EpSectionPriceHisDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 第一个开始日期范围
     */
    private LocalDate dateStart1;

    /**
     * 第一个结束日期范围
     */
    private LocalDate dateEnd1;

    /**
     * 第二个开始日期范围
     */
    private LocalDate dateStart2;

    /**
     * 第二个结束日期范围
     */
    private LocalDate dateEnd2;

    /**
     * 节点id
     */
    @NotNull(message = "节点id不能为空")
    private Long partitionId;

    /**
     * 过滤
     */
    private List<String> filters;

    /**
     * 图片base64
     */
    private String base64;
}
