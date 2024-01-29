package com.steccn.pms.vo;

import com.steccn.common.dto.SteccnRow;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @desc 深加工月度统计报表
 */
@Data
public class SjgMonthCountReportVO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 左边表格
     */
    private List<SjgMonthCountReportTransverseVO> leftList;

    /**
     * 右边表格表头
     */
    private List<String> rightHeadList;

    /**
     * 右边表格
     */
    private List<SteccnRow<String, List<BigDecimal>>> rightList;

}
