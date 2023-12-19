package com.steccn.pms.controller.feign;

import cn.hutool.core.lang.Assert;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.pms.service.PmRawMaterialCountService;
import com.steccn.pms.vo.PmRawMaterialVO;
import com.steccn.pms.vo.PmsReportWorkVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/12/19
 */
@RestController
@Api("原料配料feign")
@RequestMapping("/feign/pms/raw/material")
@RequiredArgsConstructor
public class PmsRawMaterialFeignController {
    private final PmRawMaterialCountService pmRawMaterialCountService;

    @PostMapping("/listByDate")
    @ApiOperation("根据日期检索合计值")
    public R<List<PmRawMaterialVO>> listByDate(@RequestBody TupleDTO<LocalDate, LocalDate> tupleDTO) {
        Assert.notNull(tupleDTO.getField1(), "开始时间不能为空");
        Assert.notNull(tupleDTO.getField2(), "结束时间不能为空");
        return R.success(pmRawMaterialCountService.getListByDate(tupleDTO));
    }
}
