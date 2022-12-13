package com.steccn.ems.controller;

import com.steccn.ems.service.EpComputeProduceDetailService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chenhuan
 * @date 2022/12/13 11:31
 */
@Api("领导驾驶舱概览卡片")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/over/view")
public class EpOverViewController {

    private final EpComputeProduceDetailService epComputeProduceDetailService;

//    @GetMapping("/list")
//    public R<List<EpProduceDetailVO>> get(@RequestParam("startDate")LocalDate startDate, @RequestParam("endDate")LocalDate endDate) {
//        return R.success(epComputeProduceDetailService.getProduceDetailList(startDate, endDate));
//    }
}
