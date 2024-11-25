package com.steccn.pms.vo;

import com.steccn.common.dto.TupleDTO;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MaterialRequisitionLaborVO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 劳保类别
     */
    private List<String> laborCategory;

    /**
     * 数据集合
     */
    private List<TupleDTO<String, List<BigDecimal>>> dataList;

}
