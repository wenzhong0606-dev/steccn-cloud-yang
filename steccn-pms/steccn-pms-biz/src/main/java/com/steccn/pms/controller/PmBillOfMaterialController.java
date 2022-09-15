package com.steccn.pms.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmBillOfMaterialDTO;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.dto.PmsBillExcelDTO;
import com.steccn.pms.service.PmBillOfMaterialService;
import com.steccn.pms.vo.PmBillOfMaterialVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 物料清单
 *
 * @author Mongknife
 * @date 2022/9/6
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pms/material")
@Slf4j
public class PmBillOfMaterialController {

    private final PmBillOfMaterialService pmBillOfMaterialService;

    @ApiModelProperty("新增物料清单")
    @PostMapping
    public R<Boolean> add(@Validated(InsertGroup.class) @Valid @RequestBody PmBillOfMaterialDTO dto) {
        return pmBillOfMaterialService.add(dto);
    }

    @ApiModelProperty("物料分页列表")
    @PostMapping("/list")
    public R<Page<PmBillOfMaterialVO>> pageList(@RequestBody PmBillOfMaterialDTO dto) {
        return R.success(pmBillOfMaterialService.pageList(dto));
    }

    @ApiModelProperty("编辑物料清单")
    @PutMapping
    public R<Boolean> edit(@Validated(UpdateGroup.class) @Valid @RequestBody PmBillOfMaterialDTO dto) {
        return pmBillOfMaterialService.edit(dto);
    }

    @ApiModelProperty("删除物料清单")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(pmBillOfMaterialService.delete(id));
    }

    @ApiModelProperty("Excel导入")
    @PostMapping("/excelImportData")
    public R<Boolean> excelImportData(@Validated(InsertGroup.class) @RequestBody PmExcelDTO dto) throws IOException {
        return R.success(pmBillOfMaterialService.excelImportData(dto));
    }

    @ApiModelProperty("Excel导出")
    @GetMapping("/excelExportData")
    public void excelExportData(HttpServletResponse response) throws IOException {
        pmBillOfMaterialService.excelExportData(response);
    }

    @ApiModelProperty("获取当前物料下的版本号")
    @GetMapping("/getVersion")
    public R<List<String>>  getVersion(@RequestParam("code") String code){
        return pmBillOfMaterialService.getVersion(code);
    }


    @ApiModelProperty("校验当前物料下的工艺版本")
    @GetMapping("/verifyVersion")
    public R<Boolean> verifyVersion(@RequestParam("id")Long id,@RequestParam("version")String version){
        return R.success(pmBillOfMaterialService.existVersion(id,version));
    }

}
