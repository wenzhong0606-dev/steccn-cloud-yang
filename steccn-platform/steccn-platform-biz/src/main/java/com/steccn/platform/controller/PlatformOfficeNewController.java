package com.steccn.platform.controller;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.PlatformOfficeDTO;
import com.steccn.platform.service.PlatformOfficeService;
import com.steccn.platform.vo.PlatformOfficeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2025/07/31
 */
@Api("OA接口数据接收")
@RestController
@RequiredArgsConstructor
@DS("slave")
@RequestMapping("/platform/office/new")
public class PlatformOfficeNewController {
    private final PlatformOfficeService platformOfficeService;

    @PostMapping()
    @ApiOperation(value = "新增机构")
    public R<Boolean> add(@Valid @RequestBody PlatformOfficeDTO dto) {
        return R.success(platformOfficeService.add(dto));
    }

    @PutMapping()
    @ApiOperation(value = "修改机构")
    public R<Boolean> update(@Valid @RequestBody PlatformOfficeDTO dto) {
        return R.success(platformOfficeService.update(dto));
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除机构")
    public R<Boolean> delete(@RequestBody Set<Integer> ids) {
        return R.success(platformOfficeService.deleteById(ids));
    }

    @GetMapping()
    @ApiOperation(value = "机构树")
    public R<List<PlatformOfficeVO>> listData() {
        return R.success(platformOfficeService.getTreeList());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "获取该节点下的树")
    public R<PlatformOfficeVO> parent(@PathVariable("id") Integer id) {
        return R.success(platformOfficeService.getTreeById(id));
    }
}
