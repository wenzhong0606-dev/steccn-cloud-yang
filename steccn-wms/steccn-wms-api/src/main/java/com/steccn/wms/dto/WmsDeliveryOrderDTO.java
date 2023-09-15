package com.steccn.wms.dto;

import com.steccn.common.dto.BasePageDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * 收料单号
 *
 * @author AndrewLuo
 * @date 2023/09/14
 */
@Data
public class WmsDeliveryOrderDTO extends BasePageDTO implements Serializable {
    private String deliveryOrderCode;
}
