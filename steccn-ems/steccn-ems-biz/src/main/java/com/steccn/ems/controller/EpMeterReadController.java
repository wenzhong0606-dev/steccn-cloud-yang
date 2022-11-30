package com.steccn.ems.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenhuan
 * @date 2022/11/30 15:30
 */
@Api("抄表管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/meter/read")
public class EpMeterReadController {
}
