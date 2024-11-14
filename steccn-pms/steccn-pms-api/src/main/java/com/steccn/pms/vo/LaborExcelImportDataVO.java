package com.steccn.pms.vo;


import com.steccn.common.dto.SteccnTableDataDTO;
import lombok.Data;

import java.util.Map;

@Data
public class LaborExcelImportDataVO {
    private Boolean successFlag  = Boolean.TRUE;
    private Integer size;
    private SteccnTableDataDTO<Map<String, String>> tableDataDTO;
}
