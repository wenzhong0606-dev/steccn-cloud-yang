package com.steccn.ems.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/1 9:04
 */
@Data
public class EpMeterReadVO {
    private List<MeterReadListVO> meterReadListVOList;
    private List<LocalDate> labelList;
}
