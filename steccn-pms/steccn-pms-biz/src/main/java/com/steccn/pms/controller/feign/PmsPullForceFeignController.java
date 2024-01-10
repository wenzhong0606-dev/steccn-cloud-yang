package com.steccn.pms.controller.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.dto.ValidList;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmsPullForce;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import com.steccn.pms.dto.PmsPullForceQueryDTO;
import com.steccn.pms.dto.PullForceSumDTO;
import com.steccn.pms.service.PmsPullForceService;
import com.steccn.pms.vo.PmsPullForceDataVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@Api("拉引量feign")
@RequestMapping("/feign/pms/pull/force")
@RequiredArgsConstructor
public class PmsPullForceFeignController {

    private final PmsPullForceService pmsPullForceService;


    @ApiOperation("拉引量值预填")
    @PostMapping
    public R<Boolean> edit(@Validated(UpdateGroup.class)@Valid @RequestBody ValidList<PmsPullForceEditDTO> dtoList) {
        return R.success(pmsPullForceService.edit(dtoList.getList()));
    }

    @ApiOperation("获取时间段内的拉引量详细值")
    @PostMapping("/data")
    public R<List<PmsPullForceDataVO>> getDataFromPeriods(@RequestBody PmsPullForceQueryDTO dto) {
        return R.success(pmsPullForceService.getDataFromPeriods(dto));
    }

    @PostMapping("/sum")
    @ApiOperation(value = "拉引量汇总")
    R<Map<String, BigDecimal>> pullForceSum(@Valid @RequestBody PullForceSumDTO dto){
       return R.success(pmsPullForceService.pullForceSum(dto));
    }

}
