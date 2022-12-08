package com.steccn.ems.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * 峰谷平分布接收参数类
 *
 * @author AndrewLuo
 * @date 2022/12/06
 */
@Data
public class EpSectionPriceDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 开始日期
     */
    private LocalDate dateStart;

    /**
     * 结束日期
     */
    private LocalDate dateEnd;

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
