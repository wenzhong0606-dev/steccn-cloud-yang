package com.steccn.qms.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.steccn.common.dto.BasePageDTO;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * SPC报表excel导入
 */
@Data
public class QtReportExcelDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 报表id
     */
    @NotNull(message = "报表id不能为空")
    private Integer reportId;

    /**
     * 筛选条件
     */
    private List<String> filterConditions;

    /**
     * 图表base64
     */
    private String base64;
    
}
