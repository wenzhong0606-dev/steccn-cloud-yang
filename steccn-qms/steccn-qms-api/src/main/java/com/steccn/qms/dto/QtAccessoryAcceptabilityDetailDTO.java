package com.steccn.qms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/17 11:07
 */
@Data
public class QtAccessoryAcceptabilityDetailDTO extends BasePageDTO {

    @NotNull(message = "时间不能为空")
    private LocalDate dateTime;

    private List<String> workshopNameList;
}
