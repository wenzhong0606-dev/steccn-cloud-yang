package com.steccn.wms.controller.feign;

import com.steccn.wms.service.WmsTransferSlipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 调拨单明细
 *
 * @author Mongknife
 * @date 2022/12/7
 */
@RestController
@RequestMapping("/feign/wms/transfer/slip")
@RequiredArgsConstructor
@Slf4j
public class WmsTransferSlipFeignController {

    private final WmsTransferSlipService wmsTransferSlipService;

}
