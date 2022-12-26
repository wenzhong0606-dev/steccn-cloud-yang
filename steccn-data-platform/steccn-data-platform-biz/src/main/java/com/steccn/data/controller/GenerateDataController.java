package com.steccn.data.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.data.domain.EpMeterCollect;
import com.steccn.data.service.EpMeterCollectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;

@Api("生成数据的controller")
@RestController
@RequiredArgsConstructor
@RequestMapping("/data/platform")
@Slf4j
public class GenerateDataController {

    private final EpMeterCollectService epMeterCollectService;

    @PostMapping("/generate/em/data")
    @ApiOperation(value = "生成数据")
    public R<Boolean> generateEmData(@RequestParam("date") LocalDate date) {
        Map<String, BigDecimal> linkCodeMap = getLinkCodeMap();

        return R.success();
    }

    private Map<String, BigDecimal> getLinkCodeMap() {
        Map<String, BigDecimal> linkCodeMap = Maps.newHashMap();
        linkCodeMap.put("A-001", BigDecimal.ZERO);
        linkCodeMap.put("B-001", BigDecimal.ZERO);
        linkCodeMap.put("C-001", BigDecimal.ZERO);
        //遍历所有的
        linkCodeMap.forEach((k, v) -> {
            EpMeterCollect one = epMeterCollectService.getOne(new QueryWrapper<EpMeterCollect>().select("max(value) as value").lambda().eq(EpMeterCollect::getCode, k));
            if (Objects.nonNull(one)) {
                v = one.getValue();
            }
        });
        log.info("{}", linkCodeMap);
        return linkCodeMap;
    }

}
