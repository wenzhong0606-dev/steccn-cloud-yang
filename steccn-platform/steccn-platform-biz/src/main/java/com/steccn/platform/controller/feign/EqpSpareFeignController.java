package com.steccn.platform.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.domain.SysSpareUnitConversion;
import com.steccn.platform.dto.EqpSpareDto;
import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.dto.SparePageDTO;
import com.steccn.platform.dto.SysSpareUnitConversionDTO;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.service.SysSpareUnitConversionService;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/2
 */
@Api("备件信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/eqpSpare")
public class EqpSpareFeignController {

    private final SysEqpSpareService eqpSpareService;

    private final SysSpareUnitConversionService conversionService;

    @ApiOperation("获取备件列表")
    @GetMapping("/searchSpares")
    public R<Map<Long, EqpSpareVO>> searchSpares(@RequestParam("name") String name, @RequestParam("code") String code) {
        return R.success(eqpSpareService.searchSpares(name, code));
    }

    @ApiOperation("根据编码获取备件")
    @GetMapping("/getByCode")
    public R<EqpSpareVO> getByCode(@RequestParam("code") String code) {
        return R.success(eqpSpareService.getByCode(code));
    }

    @ApiOperation("根据物料名字查询")
    @GetMapping("/getByName")
    public R<EqpSpareVO> getByName(@RequestParam("name") String name) {
        return R.success(eqpSpareService.getByName(name));
    }

    /**
     * 根据名称查询备件的信息
     *
     * @param materialNames 物料名称
     * @return
     */
    @PostMapping("/listByName")
    @ApiOperation("根据id查询备件的信息")
    public R<Map<String, EqpSpareVO>> listByName(@RequestBody List<String> materialNames){
        return R.success(eqpSpareService.listByName(materialNames));
    }

    @RequestMapping("/listByCode")
    @ApiOperation("根据code查询备件的数据")
    public R<Map<String, EqpSpareVO>> listByCode(@RequestBody List<String> codeList) {
        if (CollUtil.isEmpty(codeList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(eqpSpareService.listByCode(codeList));
    }

    @PostMapping("/listById")
    @ApiOperation("根据id查询备件的数据")
    public R<Map<Long, EqpSpareVO>> listById(@RequestBody List<Long> materialIds) {
        if (CollUtil.isEmpty(materialIds)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(eqpSpareService.listById(materialIds));
    }

    @PostMapping("/listByIdCh")
    @ApiOperation("根据id查询备件的数据")
    public R<Map<Long, String>> listByIdCh(@RequestBody Set<Long> materialIds) {
        if (CollUtil.isEmpty(materialIds)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(eqpSpareService.listByIdCh(materialIds));
    }

    @RequestMapping("/mapByCode")
    @ApiOperation("根据code查询备件的数据,key为id")
    public R<Map<Long, EqpSpareVO>> mapByCode(@RequestBody List<String> codeList) {
        if (CollUtil.isEmpty(codeList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(eqpSpareService.mapByCode(codeList));
    }

    @PostMapping("/listPage")
    @ApiOperation("获取备件分页列表")
    public R<Page<EqpSpareVO>> listPage(@RequestBody SparePageDTO sparePageDTO) {
        return R.success(eqpSpareService.listPage(sparePageDTO));
    }


    @GetMapping("/getById")
    @ApiOperation("根据备件id获取")
    public R<EqpSpareVO> getById(@RequestParam("id") Long id) {
        return R.success(eqpSpareService.get(id));
    }

    @PostMapping("/save")
    @ApiOperation("保存备件信息")
    public R<Boolean> save(@RequestBody EqpSpareDto eqpSpareDto) {
        return R.success(eqpSpareService.saveSpare(eqpSpareDto));
    }

    @PostMapping("/update")
    @ApiOperation("修改备件信息")
    public R<Boolean> update(@RequestBody EqpSpareDto eqpSpareDto) {
        return R.success(eqpSpareService.updateSpare(eqpSpareDto));
    }

    @DeleteMapping("/delete")
    @ApiOperation("修改备件信息")
    public R<Boolean> delete(@RequestParam("id") Long id) {
        return R.success(eqpSpareService.removeById(id));
    }


    @GetMapping("/getConversion")
    @ApiOperation("获取备件单位转换数据")
    public R<SysSpareUnitConversionVO> getConversion(@RequestParam("id") Long id) {
        return R.success(conversionService.getConversion(id));
    }


    @PostMapping("/saveConversion")
    @ApiOperation("保存单位转换数据")
    public R<Boolean> saveConversion(@RequestBody SysSpareUnitConversionDTO unitConversionDTO) {
        return R.success(conversionService.saveConversion(unitConversionDTO));
    }

    @PostMapping("/updateConversion")
    @ApiOperation("修改单位转换数据")
    public R<Boolean> updateConversion(@RequestBody SysSpareUnitConversionDTO unitConversionDTO) {
        return R.success(conversionService.updateConversion(unitConversionDTO));
    }

    @DeleteMapping("/deleteConversion")
    @ApiOperation("删除单位转换数据")
    public R<Boolean> deleteConversion(@RequestParam("id") Long id) {
        return R.success(conversionService.removeById(id));
    }


    @PostMapping("/listByProperty")
    @ApiOperation("根据产品属性获取物料列表")
    public R<List<EqpSpareVO>> listByProperty(@RequestBody TupleDTO<String,Integer> dto){
        return R.success(eqpSpareService.listByProperty(dto));
    }


    @PostMapping("/listByTypeId")
    @ApiOperation("根据分类获取物料信息")
    public R<List<EqpSpareVO>> listByTypeId(@RequestBody  Set<String> types) {
        return R.success(eqpSpareService.listByTypeId(types));
    }
}
