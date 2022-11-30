package com.steccn.ems.controller;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenhuan
 * @date 2022/11/30 15:38
 */
@Api("单耗告警")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/unit/consume/alarm")
public class EpUnitConsumeAlarm {
}
