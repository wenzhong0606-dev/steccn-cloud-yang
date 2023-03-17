package com.steccn.qms.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.qms.dto.QtDefectProductDTO;
import com.steccn.qms.dto.QtDefectProductPageDTO;
import com.steccn.qms.service.QtDefectProductService;
import com.steccn.qms.vo.QtDefectProductVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/defect/product")
@ApiModel("不合格品处理控制器")
@Slf4j
public class QtDefectProductController {


    private final QtDefectProductService qtDefectProductService;

    @PostMapping("/add")
    @ApiOperation(value = "新增不合格品处理记录")
    @SysLog("新增不合格品处理记录")
    public R<Boolean> add(@RequestBody QtDefectProductDTO dto) {
        return R.success(qtDefectProductService.add(dto));
    }


    @ApiOperation(value = "不合格品处理记录列表")
    @PostMapping("/list")
    public R<Page<QtDefectProductVO>> list(@RequestBody QtDefectProductPageDTO dto){
        return R.success(qtDefectProductService.listPage(dto));
    }

    @PutMapping()
    @ApiOperation(value = "修改不合格品处理记录")
    @SysLog("修改不合格品处理记录")
    public R<Boolean> edit(@RequestBody QtDefectProductDTO dto) {
        return R.success(qtDefectProductService.edit(dto));
    }


    @DeleteMapping("/{id}")
    @ApiOperation(value= "删除不合格品处理记录")
    @SysLog("删除不合格品处理记录")
    public R<Boolean> delete(@PathVariable("id") Long id) {
        return R.success(qtDefectProductService.delete(id));
    }
}
