package com.steccn.platform.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.service.SysDictService;
import com.steccn.platform.vo.SysDictVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api("数据字典控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/sys/dict")
public class SysDictFeignController {

    private final SysDictService sysDictService;

    @RequestMapping("/ids")
    @ApiOperation("根据主键id查询数据字典的值")
    public R<Map<Long, SysDictVO>> listById(@RequestBody List<Long> idList) {
        if (CollUtil.isEmpty(idList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysDictService.listByIdList(idList));
    }

    @GetMapping("/getByCode")
    @ApiOperation("根据编码类型查询数据字典的值")
    public R<SysDictVO> getByCode(@ApiParam("字典编码")@RequestParam("code") String code,@ApiParam("类型")@RequestParam("type") String type){
        return R.success(sysDictService.getByCode(code,type));
    }


    @GetMapping("/exist")
    @ApiOperation("数据字典是否存在于这个类型下的id")
    public R<Boolean> existTypeId(@ApiParam("类型编码") @RequestParam("type") String type, @ApiParam("主键id") @RequestParam("id") Long id) {
        return R.success(sysDictService.exist(type, id));
    }

    @RequestMapping("/listByCode")
    @ApiOperation("根据code查询数据字典的值")
    public R<Map<String, SysDictVO>> listByCode(@RequestBody SysDictDTO sysDictDTO){
        return R.success(sysDictService.listByCode(sysDictDTO));
    }

}
