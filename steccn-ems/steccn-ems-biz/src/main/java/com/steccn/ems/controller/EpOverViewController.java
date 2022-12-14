package com.steccn.ems.controller;

import com.steccn.common.vo.R;
import com.steccn.ems.service.EpComputeProduceDetailService;
import com.steccn.ems.service.EpOverViewService;
import com.steccn.ems.vo.EpOverViewVO;
import com.steccn.ems.vo.EpProduceDetailVO;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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

    private final EpOverViewService epOverViewService;

    @GetMapping("/list")
    public R<List<EpProduceDetailVO>> get(@RequestParam("startDate") LocalDate startDate, @RequestParam("endDate")LocalDate endDate) {
        return R.success(epComputeProduceDetailService.getProduceDetailList(startDate, endDate));
    }

    @GetMapping("/card")
    public R<EpOverViewVO> card(@RequestParam("selectDate") LocalDate selectDate, @RequestParam("type") Integer type) {
        return R.success(epOverViewService.getOverViewByType(type, selectDate));
    }
}
