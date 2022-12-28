package com.steccn.data.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.data.domain.EpMeterCollect;
import com.steccn.data.service.EpMeterCollectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * @author yequan
 */
@Api("生成数据的controller")
@RestController
@RequiredArgsConstructor
@RequestMapping("/data/platform")
@Slf4j
public class GenerateDataController {

    private final EpMeterCollectService epMeterCollectService;

    @GetMapping("/generate/em/data")
    @ApiOperation(value = "生成数据")
    public R<Boolean> generateEmData(@RequestParam("date") LocalDate date) {
        List<String> codeList = Arrays.asList("A-001", "A-0004", "B-001", "C-001");
        Map<String, BigDecimal> linkCodeMap = getLinkCodeMap(codeList);
        Random random = new Random();
        Map<String, LocalDateTime> dtMap = Maps.newHashMap();
        for (String code : codeList) {
            dtMap.put(code, LocalDateTime.of(date, LocalTime.MIN));
        }
        List<EpMeterCollect> resList = Lists.newArrayList();
        for (Map.Entry<String, BigDecimal> entry : linkCodeMap.entrySet()) {
            while (dtMap.get(entry.getKey()).isBefore(LocalDateTime.of(date.plusDays(1), LocalTime.MIN))) {
                BigDecimal bg = new BigDecimal(String.valueOf(random.nextDouble()));
                bg = bg.multiply(new BigDecimal("100"));
                entry.setValue(entry.getValue().add(bg));
                EpMeterCollect collect = new EpMeterCollect();
                LocalDateTime recordTime = dtMap.get(entry.getKey());
                collect.setRecordTime(recordTime);
                dtMap.put(entry.getKey(), recordTime.plusSeconds(random.nextInt(200)));
                collect.setValue(entry.getValue());
                collect.setCreateTime(LocalDateTime.now());
                collect.setCode(entry.getKey());
                resList.add(collect);

            }
        }
        epMeterCollectService.saveBatch(resList);
        return R.success(Boolean.TRUE);
    }

    private Map<String, BigDecimal> getLinkCodeMap(List<String> codeList) {
        Map<String, BigDecimal> linkCodeMap = Maps.newHashMap();
        for (String code : codeList) {
            linkCodeMap.put(code, BigDecimal.ZERO);
        }
        //遍历所有的
        for (Map.Entry<String, BigDecimal> entry : linkCodeMap.entrySet()) {
            EpMeterCollect one = epMeterCollectService.getOne(new QueryWrapper<EpMeterCollect>().select("max(value) as value").lambda().eq(EpMeterCollect::getCode, entry.getKey()));
            if (Objects.nonNull(one)) {
                entry.setValue(one.getValue());
            }
        }
        return linkCodeMap;
    }

}
