package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("SPC控制图明细分页")
public class QtSpcReportDetailPageDTO extends BasePageDTO implements Serializable {
/**
 * Spc控制图的id
 */
@NotNull(message = "SPC控制图id不能为空")
private Integer id;
}
