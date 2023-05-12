package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author wangzhixiong
 *
 */
@Data
public class QtCheckaProductTaskListDTO extends BasePageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 生产单号
     */
    private String productOrder;

    /**
     * 工位id
     */
    private List<Long> stationIds;

}
