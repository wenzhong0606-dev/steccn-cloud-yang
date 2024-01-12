package com.steccn.pms.vo;

import com.steccn.common.vo.CommonBarLineVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 总入库数，入库合格率 | 总完工数，完工合格率
 * @date 2024/01/11
 */
@Data
public class PmsOperateDriverCompleteStorageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总入库数 , 完工数
     */
    private BigDecimal totalQuantity;

    /**
     * 入库合格率 ， 完工合格率
     */
    private BigDecimal totalRate;

    /**
     * 折线图 + 条形图
     */
    private CommonBarLineVO commonBarLineVO;

}
