package com.steccn.dp.vo;

import com.steccn.common.vo.CommonBarLineVO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 客户订单VO
 *
 * @author AndrewLuo
 * @date 2023/12/08
 */
@Data
public class DpSalesContractVO {
    private List<CommonBarLineVO> commonBarLineVO;
    private BigDecimal countData;
}
