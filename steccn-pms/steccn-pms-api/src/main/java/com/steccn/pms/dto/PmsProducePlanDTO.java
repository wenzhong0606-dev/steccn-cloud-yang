package com.steccn.pms.dto;

import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/3 14:15
 */
@Data
public class PmsProducePlanDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<PmsProducePlanDetailsDTO> producePlanDetailsDTOList;
    private String materialCode;
    @NotNull(groups = {UpdateGroup.class, InsertGroup.class}, message = "物料id不能为空")
    private Long materialId;
    private String materialName;
    private BigDecimal total;
    private Integer customerId;
    private String customerCode;
    private String customerName;
    private String position;
    /**
     * 单据编码 三方对接
     */
    private String orderCode;
    /**
     * 序号前端编辑用
     */
    private Integer id;

    /**
     * 创建时间
     */
    private LocalDateTime updateTime;
}
