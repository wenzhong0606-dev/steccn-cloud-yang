package com.steccn.platform.controller;


import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SysOfficeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 机构数据
 *
 * @author Mongknife
 * @date 2022/9/6
 */
@Api("机构数据")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/office")
public class SysOfficeController {

    private final SysOfficeService sysOfficeService;

    @GetMapping
    @ApiOperation("机构数据列表")
    public R<List<SysOfficeVO>> list(@ApiParam("类型编码") @RequestParam("officeType") String officeType) {
        return R.success(sysOfficeService.sysList(officeType));
    }

    @GetMapping("/data")
    @ApiOperation("平级机构数据")
    public R<List<SysOfficeVO>> data(){
        return R.success(sysOfficeService.data());
    }

    @GetMapping("/flat")
    public R<List<SysOfficeVO>> listFlatDataByType(@RequestParam String type) {
        return R.success(sysOfficeService.flatData(type));
    }

    @PostMapping("/getByPcodeAndType")
    public R<List<SysOfficeVO>> listFlatDataByPcode(@RequestBody SysOfficeParentDTO dto) {
        return R.success(sysOfficeService.findDataByPCodeAndType(dto));
    }
}
