package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.dto.PmsProcessInStationDTO;
import com.steccn.pms.dto.PmsProcessOutStationPageDTO;
import com.steccn.pms.service.PmProcessService;
import com.steccn.pms.dto.PmProcessDTO;
import com.steccn.pms.vo.PmProcessVO;
import com.steccn.pms.vo.PmsProcessInStationVO;
import com.steccn.pms.vo.PmsProcessOutStationVO;
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

/**
 * 工序管理相关内容
 *
 * @author Mongknife
 * @date 2022/9/5
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/process")
@Slf4j
public class PmProcessController {

    private final PmProcessService processService;

    @ApiModelProperty("新增工序")
    @PostMapping
    public R<Boolean> add(@Validated(InsertGroup.class) @RequestBody PmProcessDTO dto) {
        return processService.saveProcess(dto);
    }

    @ApiModelProperty("工序分页列表")
    @PostMapping("/list")
    public R<Page<PmProcessVO>> pageList(@RequestBody PmProcessDTO dto) {
        return R.success(processService.pageList(dto));
    }

    @ApiModelProperty("工序列表数据")
    @GetMapping("/listData")
    public R<List<PmProcessVO>> listData() {
        return R.success(processService.listData());
    }

    @ApiModelProperty("删除工序")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(processService.delete(id));
    }

    @ApiModelProperty("修改工序")
    @PutMapping
    public R<Boolean> edit(@Validated(UpdateGroup.class) @RequestBody PmProcessDTO dto) {
        return processService.edit(dto);
    }

    @ApiModelProperty("Excel导入")
    @PostMapping("/excelImportData")
    public R<Boolean> excelImportData(@Validated(InsertGroup.class) PmExcelDTO dto) throws IOException {
        return R.success(processService.excelImportData(dto));
    }

    /**
     * 下载物料清单模板
     */
    @ApiModelProperty("下载模板")
    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        processService.download(response);
    }

    @ApiModelProperty("Excel导出")
    @GetMapping("/excelExportData")
    public void excelExportData(HttpServletResponse response) throws IOException {
        processService.excelExportData(response);
    }



}
