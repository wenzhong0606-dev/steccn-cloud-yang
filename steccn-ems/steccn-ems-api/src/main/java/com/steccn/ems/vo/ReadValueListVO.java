package com.steccn.ems.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author chenhuan
 * @date 2022/12/1 9:01
 */
@Data
public class ReadValueListVO {
    /**
     * 仪表id
     */
    private Integer meterId;
    private String readDate;
    private BigDecimal readValue;
    /**
     * 0实体远传仪表、1实体非远传仪表、2虚拟仪表
     */
    private Integer entityTable;
}
