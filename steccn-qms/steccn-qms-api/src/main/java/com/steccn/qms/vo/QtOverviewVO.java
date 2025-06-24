package com.steccn.qms.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 成品率VO
 *
 * @author AndrewLuo
 * @date 2025/06/24
 */
@Data
public class QtOverviewVO {
    @ExcelProperty(value = "日期", index = 0)
    private String date;
    @ExcelProperty(value = "成品率", index = 1)
    private String value;
}
