package com.steccn.pms.vo;

import com.steccn.common.dto.TableHeadColumnDTO;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author yequan
 * @desc 深加工统计报表
 * @date 2023/12/21
 */
@Data
public class SjgCountReportVO implements Serializable {

    private final static long serialVersionUID = 1L;

    /**
     * 表头
     */
    private List<TableHeadColumnDTO> columns;

    /**
     * 行对象
     */
    private List<SjgCountReportRowVO> rowList;

}
