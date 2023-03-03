package com.steccn.platform.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.dto.SysDictTypeDTO;
import com.steccn.platform.service.SysDictService;
import com.steccn.platform.service.SysDictTypeService;
import com.steccn.platform.vo.SysDictVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 */
@Api("数据字典控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/sys/dict")
public class SysDictFeignController {

    private final SysDictService sysDictService;

    private final SysDictTypeService sysDictTypeService;

    @PostMapping("/ids")
    @ApiOperation("根据主键id查询数据字典的值")
    public R<Map<Long, SysDictVO>> listById(@RequestBody List<Long> idList) {
        if (CollUtil.isEmpty(idList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysDictService.listByIdList(idList));
    }

    @PostMapping("/names")
    @ApiOperation("根据名称查询字典的值")
    public R<Map<String, Long>> listByNameCh(@RequestBody Set<String> nameList) {
        if (CollUtil.isEmpty(nameList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysDictService.listByNameCh(nameList));
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
    @ApiOperation("根据编码类型查询数据字典的值")
    public R<SysDictVO> getByCode(@ApiParam("字典编码") @RequestParam("code") String code, @ApiParam("类型") @RequestParam("type") String type) {
        return R.success(sysDictService.getByCode(code, type));
    }


    @GetMapping("/exist")
    @ApiOperation("数据字典是否存在于这个类型下的id")
    public R<Boolean> existTypeId(@ApiParam("类型编码") @RequestParam("type") String type, @ApiParam("主键id") @RequestParam("id") Long id) {
        return R.success(sysDictService.exist(type, id));
    }

    @PostMapping("/listByCode")
    @ApiOperation("根据code查询数据字典的值")
    public R<Map<String, SysDictVO>> listByCode(@RequestBody SysDictDTO sysDictDTO) {
        return R.success(sysDictService.listByCode(sysDictDTO));
    }

    @GetMapping("/listByCode")
    @ApiOperation("根据编码类型查询数据字典的名称")
    public R<Map<String, String>> listByCode(@ApiParam("字典编码") @RequestParam("codes") Set<String> codes, @ApiParam("类型") @RequestParam("type") String type) {
        return R.success(sysDictService.listByCode(codes, type));
    }

    @PostMapping("/listByCodes")
    @ApiOperation("根据code查询数据字典的值")
    public R<Map<String, SysDictVO>> listByCodes(@RequestBody Set<String> codes) {
        return R.success(sysDictService.listByCodes(codes));
    }

    @GetMapping("/listByType")
    @ApiOperation("根据type查询数据字典的值")
    public R<List<SysDictVO>> listByType(@RequestParam("type") String type) {
        return R.success(sysDictService.sysList(type));
    }

    @PostMapping("/save")
    @ApiOperation("新增字典")
    public R<Boolean> save(@RequestBody SysDictDTO sysDictDTO) {
        return R.success(sysDictService.saveData(sysDictDTO));
    }

    @GetMapping("/id")
    @ApiOperation("获取字典")
    public R<SysDictVO> getById(@RequestParam("id") Long id) {
        return R.success(sysDictService.get(id));
    }

    @PostMapping("/update")
    @ApiOperation("修改字典")
    public R<Boolean> update(@RequestBody SysDictDTO sysDictDTO) {
        return R.success(sysDictService.updateDict(sysDictDTO));
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除字典数据")
    public R<Boolean> delete(@RequestParam("id") Long id) {
        return R.success(sysDictService.removeById(id));
    }

    @GetMapping("/type/id")
    @ApiOperation("获取字典类型数据")
    public R<SysDictTypeDTO> typeById(@RequestParam("id") Long id) {
        return R.success(sysDictTypeService.get(id));
    }

    @PostMapping("/type/save")
    @ApiOperation("保存字典类型数据")
    public R<Boolean> saveType(@RequestBody SysDictTypeDTO sysDictTypeDTO) {
        return R.success(sysDictTypeService.saveType(sysDictTypeDTO));
    }

    @DeleteMapping("/type/delete")
    @ApiOperation("删除字典类型数据")
    public R<Boolean> delete(@RequestParam("id") Integer id) {
        return R.success(sysDictTypeService.removeById(id));
    }


}
