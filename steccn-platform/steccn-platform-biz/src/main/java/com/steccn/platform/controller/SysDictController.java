package com.steccn.platform.controller;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SysDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @desc 数据字典
 */
@Api("数据字典控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/dict")
public class SysDictController {

    private final SysDictService sysDictService;

    @GetMapping
    @ApiOperation("数据字典")
    public R<List<SysDictVO>> list(@ApiParam("类型编码") @RequestParam("type") String type) {
        return R.success(sysDictService.sysList(type));
    }

    @GetMapping("/unAuth")
    @ApiOperation("数据字典")
    public R<List<SysDictVO>> unAuthList(@ApiParam("类型编码") @RequestParam("type") String type) {
        return R.success(sysDictService.sysList(type));
    }

    @GetMapping("/filter")
    @ApiOperation("数据字典-过滤版")
    public R<List<SysDictVO>> listFilter(@ApiParam("类型编码") @RequestParam("type") String type, @ApiParam("名称过滤") @RequestParam(value = "name", required = false) String name) {
        return R.success(sysDictService.sysListFilter(type, name));
    }

    @PostMapping("/ch")
    @ApiOperation("根据主键id查询数据字典的中文")
    public R<Map<Long, String>> listByIdCh(@RequestBody Set<Long> idList) {
        if (CollUtil.isEmpty(idList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysDictService.listByIdCh(idList));
    }

    @GetMapping("/getByCode")
    @ApiOperation("根据编码查询数据字典的值")
    public R<SysDictVO> getByCode(@ApiParam("编码") @RequestParam("code") String code,@ApiParam("类型") @RequestParam("type") String type) {
        return R.success(sysDictService.getByCode(code, type));
    }

    @RequestMapping("/listByCode")
    @ApiOperation("根据code查询数据字典的值")
    public R<Map<String, SysDictVO>> listByCode(@RequestBody SysDictDTO sysDictDTO){
        return R.success(sysDictService.listByCode(sysDictDTO));
    }

    @PostMapping("/listByCodes")
    @ApiOperation("根据code查询数据字典的值")
    public R<Map<String, SysDictVO>> listByCodes(@RequestBody Set<String> codes) {
        return R.success(sysDictService.listByCodes(codes));
    }

    @PostMapping("/ids")
    @ApiOperation("根据主键id查询数据字典的值")
    public R<Map<Long,String>> getSysDictListByIds(@ApiParam("数据字典主键id的集合") @RequestBody Set<Long> ids){
        return R.success(sysDictService.listByIdCh(ids));
    }

    @GetMapping("/nameAndType")
    @ApiOperation("根据字典名称模糊查询以及数据字典的类型")
    public R<List<SysDictVO>> getByNameAndType(@ApiParam("名称") @RequestParam("name") String name,@ApiParam("类型") @RequestParam("type") String type) {
        return R.success(sysDictService.getByNameAndType(name, type));
    }
}
