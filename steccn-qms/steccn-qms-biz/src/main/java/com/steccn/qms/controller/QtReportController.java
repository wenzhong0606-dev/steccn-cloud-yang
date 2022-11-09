package com.steccn.qms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtDefectiveItemPageDTO;
import com.steccn.qms.dto.QtReportDTO;
import com.steccn.qms.dto.QtSpcReportDetailPageDTO;
import com.steccn.qms.service.QtReportService;
import com.steccn.qms.vo.*;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * SPC控制图
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/report")
@Slf4j
public class QtReportController {
    private final QtReportService qtReportService;

    @PostMapping
    @ApiOperation(value = "新建报表任务")
    @SysLog("新建报表任务")
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody QtReportDTO dto) {
        return R.success(qtReportService.add(dto));
    }

    @GetMapping("/choosegroup")
    @ApiOperation(value = "根据物料id以及检验类型查询可选择的检验分组")
    public R<List<QtCheckGroupVO>> getGroupIdList(@RequestParam("materialId") Long materialId,
                                                  @RequestParam("checkType") Integer checkType) {
        return R.success(qtReportService.getGroupIdList(materialId, checkType));
    }

    @GetMapping("/type")
    @ApiOperation(value = "根据卡片类型模糊查询给前端返回控制图卡片列表")
    public R<List<QtReportVO>> getQtReportVOListByType(@RequestParam("reportCardType") String reportCardType) {
        return R.success(qtReportService.getQtReportVOListByType(reportCardType));
    }

    @GetMapping("/{groupid}")
    @ApiOperation(value = "根据检验分组id选择检验项")
    public R<List<QtCheckItemSPCVO>> getQtCheckItemList(@PathVariable("groupid") Integer groupid) {
        return R.success((qtReportService.getQtCheckItemList(groupid)));
    }

    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除SPC控制图")
    @SysLog("删除SPC控制图")
    public R<Boolean> reportDelete(@PathVariable("id") Integer id) {
        return R.success(qtReportService.reportDelete(id));
    }

    @GetMapping("/spcid/{id}")
    @ApiOperation(value = "根据id查询spc控制图表")
    public R<QtReportVO> getQtReport(@PathVariable("id") Integer id) {
        return R.success(qtReportService.getQtReport(id));
    }

    @PutMapping("/edit")
    @ApiOperation(value = "编辑SPC报表卡片")
    @SysLog("编辑SPC报表任务")
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody QtReportDTO qtReportDTO) {
        return R.success(qtReportService.edit(qtReportDTO));
    }

    @PostMapping("/page")
    @ApiOperation(value = "Spc控制图明细")
    public R<Page<QtSpcReportDetailVO>> pageList(@Valid @RequestBody QtSpcReportDetailPageDTO dto) {
        return R.success(qtReportService.pageList(dto));
    }

    @GetMapping("/itemid")
    @ApiOperation(value = "根据分组id以及检验项的id返回前端标准值、上偏差、下偏差,单位")
    public R<QtCheckItemUnitVO> getQtCheckItemUnitVO(@RequestParam("groupId") Integer groupId,@RequestParam("itemId")Integer itemId){
        return R.success(qtReportService.getQtCheckItemUnitVO(groupId,itemId));
    }

    /**
     * 下载Spc报表明细加图表
     */
    @GetMapping("/download")
    @ApiModelProperty(value = "下载模板")
    public void download(HttpServletResponse response) throws IOException {
        qtReportService.download(response);
    }


}
