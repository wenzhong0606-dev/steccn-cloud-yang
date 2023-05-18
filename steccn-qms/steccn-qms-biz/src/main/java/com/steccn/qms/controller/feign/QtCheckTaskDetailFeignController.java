package com.steccn.qms.controller.feign;

import cn.hutool.core.lang.Assert;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.dto.ValidList;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtEmsPullAmountDTO;
import com.steccn.qms.dto.QtPullAmountDTO;
import com.steccn.qms.service.QtChartGenerateService;
import com.steccn.qms.service.QtCheckTaskDetailService;
import com.steccn.qms.vo.QtCheckTaskDetailItemMaterialVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenhuan
 * @date 2022/12/9 9:28
 */
@RestController
@Api("任务详情feign")
@RequestMapping("/feign/qms/task/detail")
@RequiredArgsConstructor
public class QtCheckTaskDetailFeignController {

    private final QtCheckTaskDetailService qtCheckTaskDetailService;

    private final QtChartGenerateService qtChartGenerateService;

    @ApiOperation("根据班次日期查询单片掰边重量、单片重量、拉引速度")
    @PostMapping("/getByClassTime")
    public R<List<QtCheckTaskDetailItemMaterialVO>> getByClassTime(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> tupleDTO){
        Assert.notNull(tupleDTO.getField1(),"开始时间不能为空");
        Assert.notNull(tupleDTO.getField2(),"结束时间不能为空");
        return R.success(qtCheckTaskDetailService.getByClassTime(tupleDTO.getField1(), tupleDTO.getField2()));
    }

    @ApiOperation("ems获取拉引量")
    @PostMapping("/ems/getPullAmount")
    public R<List<QtEmsPullAmountDTO>> getByClassTime(@RequestBody @Valid ValidList<QtEmsPullAmountDTO> dto){
        return R.success(qtChartGenerateService.calculateEmsPullAmount(dto));
    }
}
