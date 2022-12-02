package com.steccn.ems.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;

@Data
public class EpInstrumentAccountPageDTO extends BasePageDTO implements Serializable {
    /**
     * 仪表名称
     */
    private String meterName;

}
