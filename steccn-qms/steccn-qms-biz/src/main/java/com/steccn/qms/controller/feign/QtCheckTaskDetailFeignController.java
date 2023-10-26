package com.steccn.qms.controller.feign;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.google.common.collect.Lists;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.dto.ValidList;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.OutputQualityDetailDTO;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtEmsPullAmountDTO;
import com.steccn.qms.dto.QtPullAmountDTO;
import com.steccn.qms.service.QtChartGenerateService;
import com.steccn.qms.service.QtCheckTaskDetailService;
import com.steccn.qms.service.QtDefectiveReportService;
import com.steccn.qms.vo.OutputQualityDetailVO;
import com.steccn.qms.vo.QtCheckTaskDetailItemMaterialVO;
import com.steccn.qms.vo.QtCheckTaskDetailReportVO;
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

    private final QtDefectiveReportService qtDefectiveReportService;

    @ApiOperation("获取在线检缺陷")
    @PostMapping("/defect/flat")
    public R<List<QtCheckTaskDetailReportVO>> defectFlat(@RequestBody QtChartParamDTO dto) {
        return R.success(qtDefectiveReportService.defectiveDataFlat(dto));
    }

    @ApiOperation("根据班次日期查询单片掰边重量、单片重量、拉引速度")
    @PostMapping("/getByClassTime")
    public R<List<QtCheckTaskDetailItemMaterialVO>> getByClassTime(@RequestBody TupleDTO<LocalDateTime, LocalDateTime> tupleDTO) {
        Assert.notNull(tupleDTO.getField1(), "开始时间不能为空");
        Assert.notNull(tupleDTO.getField2(), "结束时间不能为空");
        return R.success(qtCheckTaskDetailService.getByClassTime(tupleDTO.getField1(), tupleDTO.getField2()));
    }

    @ApiOperation("ems获取拉引量")
    @PostMapping("/ems/getPullAmount")
    public R<List<QtEmsPullAmountDTO>> getEmsPullAmount(@RequestBody ValidList<QtEmsPullAmountDTO> dto){
        if(CollUtil.isEmpty(dto.getList())){
            return R.success(Lists.newArrayList());
        }
        return R.success(qtChartGenerateService.calculateEmsPullAmount(dto));
    }

    @ApiOperation("根据条件获取在线检缺陷信息")
    @PostMapping("/list/defective")
    public R<List<OutputQualityDetailVO>> listDefectiveData(@RequestBody OutputQualityDetailDTO outputQualityDetailDTO) {
        return R.success(qtCheckTaskDetailService.listDefectiveData(outputQualityDetailDTO));
    }

    @ApiOperation("根据条件获取巡检平均厚度数据")
    @PostMapping("/list/polling")
    public R<List<OutputQualityDetailVO>> listPollingDate(@RequestBody OutputQualityDetailDTO outputQualityDetailDTO) {
        return R.success(qtCheckTaskDetailService.listPollingDate(outputQualityDetailDTO));
    }


}
