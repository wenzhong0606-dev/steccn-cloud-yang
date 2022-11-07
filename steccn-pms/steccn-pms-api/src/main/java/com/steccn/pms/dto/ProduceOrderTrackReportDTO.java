package com.steccn.pms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/7 15:54
 */
@Data
public class ProduceOrderTrackReportDTO extends BasePageDTO {

    @ApiModelProperty("产线id集合")
    private List<Integer> productLineList;
}
