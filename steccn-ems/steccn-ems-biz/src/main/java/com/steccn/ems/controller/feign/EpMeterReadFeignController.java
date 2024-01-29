package com.steccn.ems.controller.feign;

import cn.hutool.core.lang.Assert;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.dto.CommonReportDTO;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.Tuple5DTO;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpMeterReadAddDTO;
import com.steccn.ems.dto.EpMeterReadUpdateDTO;
import com.steccn.ems.service.EpDayReportService;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.vo.EpMeterReadDetailsVO;
import com.steccn.ems.vo.EpMeterReadVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @date 2024/01/03 15:30
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/ems/meter/read")
public class EpMeterReadFeignController {

    private final EpMeterReadService epMeterReadService;
    private final EpDayReportService epDayReportService;

    /**
     * 获取指定能源的耗用数
     *
     * @param dto 能源类型，车间，日期start，日期end，粒度  5元组
     * @return
     */
    @PostMapping("/energy/consume")
    public R<CommonReportDTO> energyConsume(@RequestBody Tuple5DTO<Integer, Integer, LocalDate, LocalDate, Integer> dto) {
        Assert.isTrue(LocalDateUtil.le(dto.getField3(), dto.getField4()), "结束时间必须小于等于开始时间");
        return R.success(epMeterReadService.energyConsume(dto.getField1(), dto.getField2(), dto.getField3(), dto.getField4(), dto.getField5()));
    }

    @PostMapping("/energy/consume/office")
    R<Map<String, BigDecimal>> energyConsumeOffice(@RequestBody Tuple5DTO<Integer, List<Integer>, LocalDate, LocalDate, Integer> dto){
        Assert.isTrue(LocalDateUtil.le(dto.getField3(), dto.getField4()), "结束时间必须小于等于开始时间");
        return R.success(epMeterReadService.energyConsumeOffice(dto.getField1(), dto.getField2(), dto.getField3(), dto.getField4(), dto.getField5()));
    }

    /**
     * 余热发电
     *
     * @param dto 日期start，日期end，粒度  3元组
     * @return
     */
    @PostMapping("/heat/power")
    R<List<CommonReportDTO>> heatPower(@RequestBody TripleDTO<LocalDate, LocalDate, Integer> dto) {
        Assert.isTrue(LocalDateUtil.le(dto.getField1(), dto.getField2()), "结束时间必须小于等于开始时间");
        return R.success(epDayReportService.heatPowerElecWater(dto.getField1(), dto.getField2(), dto.getField3()));
    }

    /**
     * 光伏发电
     *
     * @param dto 日期start，日期end，粒度  3元组
     * @return
     */
    @PostMapping("/gf/power")
    R<List<CommonReportDTO>> gfPowerList(@RequestBody TripleDTO<LocalDate, LocalDate, Integer> dto){
        Assert.isTrue(LocalDateUtil.le(dto.getField1(), dto.getField2()), "结束时间必须小于等于开始时间");
        return R.success(epDayReportService.gfPowerList(dto.getField1(), dto.getField2(), dto.getField3()));
    }

    /**
     * 脱硫脱硝
     *
     * @param dto 日期start，日期end，粒度  3元组
     * @return
     */
    @PostMapping("/sc")
    R<List<CommonReportDTO>> scList(@RequestBody TripleDTO<LocalDate, LocalDate, Integer> dto) {
        Assert.isTrue(LocalDateUtil.le(dto.getField1(), dto.getField2()), "结束时间必须小于等于开始时间");
        return R.success(epDayReportService.scList(dto.getField1(), dto.getField2(), dto.getField3()));
    }

}
