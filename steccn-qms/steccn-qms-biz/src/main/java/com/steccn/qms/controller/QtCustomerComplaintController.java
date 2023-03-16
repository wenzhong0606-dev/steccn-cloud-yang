package com.steccn.qms.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.CustomerComplainPageDTO;
import com.steccn.qms.dto.QtCustomerComplainDTO;
import com.steccn.qms.service.QtCustomerComplainService;
import com.steccn.qms.vo.QtCustomerComplainVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qms/customer/complaint")
@ApiModel("客诉控制器")
@Slf4j
public class QtCustomerComplaintController {

    private final QtCustomerComplainService qtCustomerComplainService;


    @PostMapping("/add")
    @ApiOperation(value = "新增客诉记录")
    @SysLog("新增客诉记录")
    public R<Boolean> add(@RequestBody QtCustomerComplainDTO dto) {
        return R.success(qtCustomerComplainService.add(dto));
    }


    @ApiOperation(value = "客诉记录列表")
    @PostMapping("/list")
    public R<Page<QtCustomerComplainVO>> list(@RequestBody CustomerComplainPageDTO dto){
        return R.success(qtCustomerComplainService.listPage(dto));
    }

    @ApiModelProperty("获取客诉记录列表详情")
    @GetMapping("/{id}")
    public R<QtCustomerComplainVO> getInfo(@PathVariable("id") Long id) {
        return R.success(qtCustomerComplainService.getInfo(id));
    }


    @ApiOperation("编辑客诉记录")
    @PutMapping()
    public R<Boolean> edit(@RequestBody QtCustomerComplainDTO qtCustomerComplainDTO){
        return  R.success(qtCustomerComplainService.edit(qtCustomerComplainDTO));
    }


    @ApiOperation("处理客诉记录")
    @PutMapping("/dispose")
    public R<Boolean> dispose(@RequestBody QtCustomerComplainDTO qtCustomerComplainDTO){
        return  R.success(qtCustomerComplainService.dispose(qtCustomerComplainDTO));
    }

    @ApiOperation("删除客诉记录")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable("id") Long id){
        return R.success(qtCustomerComplainService.delete(id));
    }



}
