package com.steccn.qms.controller;

import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtAddReportDTO;
import com.steccn.qms.service.QtReportService;
import com.steccn.qms.vo.QtCheckItemListVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public R<Integer> add(@Validated(InsertGroup.class) @RequestBody QtAddReportDTO dto) {
        return R.success(qtReportService.add(dto));
    }

     @GetMapping("/choosegroup")
     @ApiOperation(value = "根据物料id以及检验类型查询可选择的检验分组")
     public R<List<Integer>>getGroupIdList(@RequestParam("materialId") Long materialId ,
                                     @RequestParam("checkType") Integer checkType){
        return R.success(qtReportService.getGroupIdList(materialId,checkType));
     }

     @GetMapping("/{groupid}")
     @ApiOperation("根据检验分组id选择检验项")
     public R<List<QtCheckItemListVO>> getQtCheckItemList(@PathVariable("groupid") Integer groupid){
        return R.success((qtReportService.getQtCheckItemList(groupid)));
     }

     @DeleteMapping("/delete/{id}")
     @ApiOperation(value = "删除检验方案")
     @SysLog("删除检验方案")
     public R<Boolean> reportDelete(@PathVariable("id") Integer id){
        return R.success(qtReportService.reportDelete(id));
     }

}
