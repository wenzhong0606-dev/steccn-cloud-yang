package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmBillOfMaterialDTO;
import com.steccn.pms.dto.PmProcessDTO;
import com.steccn.pms.service.PmBillOfMaterialService;
import com.steccn.pms.vo.PmBillOfMaterialVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public R<Boolean> add(@Valid @RequestBody PmBillOfMaterialDTO dto) {
        return pmBillOfMaterialService.add(dto);
    }

    @ApiModelProperty("物料分页列表")
    @PostMapping("/list")
    public R<Page<List<PmBillOfMaterialVO>>> pageList(@RequestBody PmBillOfMaterialDTO dto) {
        return R.success(pmBillOfMaterialService.pageList(dto));
    }

    @ApiModelProperty("编辑物料清单")
    @PutMapping
    public R<Boolean> edit(@Valid @RequestBody PmBillOfMaterialDTO dto) {
        return pmBillOfMaterialService.edit(dto);
    }

    @ApiModelProperty("删除物料清单")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Integer id) {
        return R.success(pmBillOfMaterialService.delete(id));
    }

}
