package com.steccn.pms.controller.feign;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenhuan
 * @date 2022/11/9 16:25
 */
@RestController
@Api("合格率feign")
@RequestMapping("/feign/pms/produce/pass/rate")
@RequiredArgsConstructor
public class ProducePassRateFeignController {
}
