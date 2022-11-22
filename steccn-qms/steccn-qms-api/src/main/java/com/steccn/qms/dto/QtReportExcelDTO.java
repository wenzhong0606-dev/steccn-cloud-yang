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
     * 粒度
     */
    @NotBlank(message = "粒度不能为空")
    private String  granularity;

    /**
     * 报表卡片名称
     */
    private String reportName;

    /**
     * 物料id
     */
    @NotNull(message = "物料id不能为空")
    private Long materialId;

    /**
     * 检验项id
     */
    private Integer checkItemId;

    /**
     * 检验项名称
     */
    private String checkItemName;

    /**
     * 检验项分组id
     */
    private Integer checkGroupId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 车间id
     */
    private Integer workshopId;

    /**
     * 车间名称
     */
    private String workshopName;

    /**
     * 任务类型 来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7
     */
    @NotNull(message = "检验类型不能为空")
    private Integer checkType;

    /**
     * 检验类型名称
     */
    private String checkTypeName;

    /**
     * 客户id
     */
    private Integer customerId;

    /**
     * 客户
     */
    private String customerName;

    /**
     * 标准值
     */
    private BigDecimal standardValue;

    /**
     * 上限
     */
    private BigDecimal upLimit;

    /**
     * 下限
     */
    private BigDecimal downLimit;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDate timeScopeStart;

    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDate timeScopeEnd;

    /**
     * 图表base64
     */
    private String base64;
    
}
