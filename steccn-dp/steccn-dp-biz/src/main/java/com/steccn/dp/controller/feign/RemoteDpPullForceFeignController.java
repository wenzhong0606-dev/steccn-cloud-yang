package com.steccn.dp.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.dp.dto.DpPullForceDTO;
import com.steccn.dp.service.AdsPmsPullForceService;
import com.steccn.qms.constant.QtManagerConstant;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @author yequan
 * @desc 数据中台拉引量汇总
 * @date 2023/12/1
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/dp/pull/force")
public class RemoteDpPullForceFeignController {

    private final AdsPmsPullForceService adsPmsPullForceService;

    @PostMapping("/sum")
    @ApiOperation(value = "数据中台拉引量汇总")
    public R<Map<String, BigDecimal>> pullForceSum(@Valid @RequestBody DpPullForceDTO dto) {
        return R.success(adsPmsPullForceService.pullForceSum(dto));
    }
}
