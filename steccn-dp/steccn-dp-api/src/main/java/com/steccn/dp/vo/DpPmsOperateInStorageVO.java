package com.steccn.dp.vo;

import com.steccn.common.vo.CommonBarLineVO;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author yequan
 * @desc 总入库数，入库合格率
 * @date 2023/11/29
 */
@Data
public class DpPmsOperateInStorageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总入库数
     */
    private BigDecimal totalInStorageQuantity;

    /**
     * 入库合格率
     */
    private BigDecimal totalInStorageRate;

    /**
     * 折线图 + 条形图
     */
    private CommonBarLineVO commonBarLineVO;

}
