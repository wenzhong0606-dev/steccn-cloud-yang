package com.steccn.platform.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author yequan
 * @desc 标准产能请求对象
 * @date 2023/12/20
 */
@Data
public class SysProductionCapacityDTO extends BasePageDTO implements Serializable {

    /**
     * 月份字符串
     */
    @Pattern(regexp = "yyyy-MM", message = "月份必须要符合yyyy-MM格式")
    private String monthStr;
}
