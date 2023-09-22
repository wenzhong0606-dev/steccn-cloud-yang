package com.steccn.pms.dto;

import lombok.Data;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * qms拿到基片日报表数据
 *
 * @author AndrewLuo
 * @date 2023/09/19
 */
@Data
public class SubstrateDataDTO {
    /**
     * 产线id
     */
    List<Integer> productLineIdList;
    /**
     * 开始时间
     */
    LocalDateTime start;
    /**
     * 结束时间
     */
    LocalDateTime end;
}
