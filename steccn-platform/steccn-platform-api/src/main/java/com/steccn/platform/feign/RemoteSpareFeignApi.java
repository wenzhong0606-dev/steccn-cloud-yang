package com.steccn.platform.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.EqpSpareDto;
import com.steccn.platform.dto.SparePageDTO;
import com.steccn.platform.dto.SysSpareUnitConversionDTO;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(contextId = "RemoteSpareFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteSpareFeignApi {

    /**
     * 根据名称编码查询备件信息
     *
     * @param name
     * @param code
     * @return R<Map < Long, EqpSpareVO>>
     */
    @RequestMapping("/feign/platform/eqpSpare/searchSpares")
    @ApiOperation("根据名称编码查询备件信息")
    R<Map<Long, EqpSpareVO>> searchSpares(@RequestParam("name") String name, @RequestParam("code") String code);

    @GetMapping("/feign/platform/eqpSpare/getByCode")
    @ApiOperation("根据名称编码查询备件信息")
    R<EqpSpareVO> getByCode(@RequestParam("code") String code);

    @GetMapping("/feign/platform/eqpSpare/getByName")
    @ApiOperation("根据名称查询备件信息")
    R<EqpSpareVO> getByName(@RequestParam("name") String name);

    @RequestMapping("/feign/platform/eqpSpare/listByCode")
    @ApiOperation("根据code查询备件的信息")
    R<Map<String, EqpSpareVO>> listByCode(@RequestBody List<String> codeList);


    /**
     * 根据名称查询备件的信息
     *
     * @param materialNames 物料名称
     * @return
     */
    @PostMapping("/feign/platform/eqpSpare/listByName")
    @ApiOperation("根据id查询备件的信息")
    R<Map<String, EqpSpareVO>> listByName(@RequestBody List<String> materialNames);

    /**
     * 根据id查询备件的信息
     *
     * @param materialIds 物料id
     * @return
     */
    @PostMapping("/feign/platform/eqpSpare/listById")
    @ApiOperation("根据id查询备件的信息")
    R<Map<Long, EqpSpareVO>> listById(@RequestBody List<Long> materialIds);

    /**
     * 根据id查询备件的信息
     *
     * @param materialIds 物料id
     * @return
     */
    @PostMapping("/feign/platform/eqpSpare/listByIdCh")
    @ApiOperation("根据id查询备件的信息")
    R<Map<Long, String>> listByIdCh(@RequestBody Set<Long> materialIds);

    /**
     * 根据codeList查询备件的信息
     *
     * @param codeList 物料codeList
     * @return
     */
    @PostMapping("/feign/platform/eqpSpare/mapByCode")
    @ApiOperation("根据code查询备件的数据,key为id")
    R<Map<Long, EqpSpareVO>> mapByCode(@RequestBody List<String> codeList);

    /**
     * 备件分页列表
     *
     * @param sparePageDTO
     * @return
     */
    @PostMapping("/feign/platform/eqpSpare/listPage")
    @ApiOperation("获取备件分页列表")
    R<Page<EqpSpareVO>> listPage(@RequestBody SparePageDTO sparePageDTO);


    /**
     * 根据分类获取物料信息
     *
     * @param types
     * @return
     */
    @PostMapping("/platform/eqpSpare/getByTypeIdSpareList")
    @ApiOperation("根据分类获取物料信息")
    R<List<EqpSpareVO>> getByTypeIdSpareList(@RequestBody Set<String> types);


    /**
     * 根据id查询备件
     *
     * @param id
     * @return
     */

    @ApiOperation("根据备件查询id")
    @GetMapping("/feign/platform/eqpSpare/getById")
    R<EqpSpareVO> getById(@RequestParam("id") Long id);


    /**
     * 保存备件信息
     *
     * @param eqpSpareDto
     * @return
     */
    @PostMapping("/feign/platform/eqpSpare/save")
    @ApiOperation("保存备件信息")
    R<Boolean> save(@RequestBody EqpSpareDto eqpSpareDto);


    /**
     * 修改备件信息
     *
     * @param eqpSpareDto
     * @return
     */
    @PostMapping("/feign/platform/eqpSpare/update")
    @ApiOperation("修改备件信息")
    R<Boolean> update(@RequestBody EqpSpareDto eqpSpareDto);


    /**
     * 删除备件信息
     *
     * @param id
     * @return
     */
    @DeleteMapping("/feign/platform/eqpSpare/delete")
    @ApiOperation("删除备件信息")
    R<Boolean> delete(@RequestParam("id") Long id);


    /**
     * 获取单位转换数据
     *
     * @param id
     * @return
     */
    @GetMapping("/feign/platform/eqpSpare/getConversion")
    @ApiOperation(("根据id查询备件"))
    R<SysSpareUnitConversionVO> getConversion(@RequestParam("id") Long id);

    /**
     * 删除单位转换数据
     *
     * @param id
     * @return
     */
    @DeleteMapping("/feign/platform/eqpSpare/deleteConversion")
    @ApiOperation("删除单位转换数据")
    R<Boolean> deleteConversion(@RequestParam("id") Long id);


    @PostMapping("/feign/platform/eqpSpare/saveConversion")
    @ApiOperation("保存单位转换数据")
    R<Boolean> saveConversion(@RequestBody SysSpareUnitConversionDTO unitConversionDTO);

    @PostMapping("/feign/platform/eqpSpare/updateConversion")
    @ApiOperation("修改单位转换数据")
    R<Boolean> updateConversion(@RequestBody SysSpareUnitConversionDTO unitConversionDTO);


    @PostMapping("/feign/platform/eqpSpare/listByProperty")
    @ApiOperation("根据产品属性获取物料列表")
    R<List<EqpSpareVO>> listByProperty(@RequestBody TupleDTO<String,Integer> dto);


    @PostMapping("/feign/platform/eqpSpare/listByTypeId")
    @ApiOperation("根据物料类型以及下级获取物料")
    R<List<EqpSpareVO>> listByTypeId(Set<String> strings);


    @GetMapping("/feign/platform/eqpSpare/listBySpecification")
    R<List<Long>> listBySpecification(@RequestParam("specification") String specification);
}
