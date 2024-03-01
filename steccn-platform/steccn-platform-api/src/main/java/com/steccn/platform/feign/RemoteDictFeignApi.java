package com.steccn.platform.feign;


import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.dto.SysDictTypeDTO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 */
@FeignClient(contextId = "remoteDictFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteDictFeignApi {

    /**
     * 根据主键id查询数据字典的值
     *
     * @param idList 数据字典id集合
     * @return
     */
    @PostMapping("/feign/platform/sys/dict/ids")
    @ApiOperation("根据主键id查询数据字典的值")
    R<Map<Long, SysDictVO>> listById(@RequestBody List<Long> idList);


    /**
     * 根据主键id查询数据字典的值
     *
     * @param idList 数据字典id集合
     * @return
     */
    @PostMapping("/feign/platform/sys/dict/ch")
    @ApiOperation("根据主键id查询数据字典的名称")
    R<Map<Long, String>> listByIdCh(@RequestBody Set<Long> idList);

    /**
     * 根据name查询数据字典的id
     *
     * @param nameList 数据字典name集合
     * @return
     */
    @PostMapping("/feign/platform/sys/dict/names")
    @ApiOperation("根据name查询数据字典的id")
    R<Map<String, Long>> listByNameCh(@RequestBody Set<String> nameList);

    /**
     * 根据编码类型查询数据字典的值
     *
     * @param code
     * @param type
     * @return
     */
    @GetMapping("/feign/platform/sys/dict/getByCode")
    @ApiOperation("根据编码类型查询数据字典的值")
    R<SysDictVO> getByCode(@ApiParam("字典编码") @RequestParam("code") String code, @ApiParam("类型") @RequestParam("type") String type);


    /**
     * 数据字典是否存在于这个类型下的id
     *
     * @param type 类型
     * @param id   主键
     * @return
     */
    @GetMapping("/feign/platform/sys/dict/exist")
    @ApiOperation("数据字典是否存在于这个类型下的id")
    R<Boolean> existTypeId(@ApiParam("类型编码") @RequestParam("type") String type, @ApiParam("主键id") @RequestParam("id") Long id);

    /**
     * 根据code查询数据字典的值
     *
     * @param sysDictDTO
     * @return
     */
    @PostMapping("/feign/platform/sys/dict/listByCode")
    @ApiOperation("根据code查询数据字典的值")
    R<Map<String, SysDictVO>> listByCode(@RequestBody SysDictDTO sysDictDTO);


    /**
     * 根据编码类型查询数据字典的值
     *
     * @param codes
     * @param type
     * @return
     */
    @GetMapping("/feign/platform/sys/dict/listByCode")
    @ApiOperation("根据编码类型查询数据字典的值")
    R<Map<String, String>> listByCode(@ApiParam("字典编码") @RequestParam("codes") Set<String> codes, @ApiParam("类型") @RequestParam("type") String type);


    /**
     * 根据code查询数据字典的值
     *
     * @param codes
     * @return
     */
    @PostMapping("/feign/platform/sys/dict/listByCodes")
    @ApiOperation("根据code查询数据字典的值")
    R<Map<String, SysDictVO>> listByCodes(@RequestBody Set<String> codes);

    /**
     * 根据type查询数据字典的值
     *
     * @param type
     * @return
     */
    @GetMapping("/feign/platform/sys/dict/listByType")
    @ApiOperation("根据type查询数据字典的值")
    R<List<SysDictVO>> listByType(@RequestParam("type") String type);


    /**
     * 保存字典数据
     * @param sysDictDTO
     * @return
     */
    @PostMapping("/feign/platform/sys/dict/save")
    @ApiOperation("保存字典数据")
    R<Boolean> save(@RequestBody SysDictDTO sysDictDTO);


    /**
     * 获取字典数据
     * @param id
     * @return
     */
    @GetMapping("/feign/platform/sys/dict/id")
    @ApiOperation("获取字典数据")
    R<SysDictVO> get(@RequestParam("id") Long id);

    /**
     * 字典修改
     * @param sysDictDTO
     * @return
     */
    @PostMapping("/feign/platform/sys/dict/update")
    @ApiOperation("字典修改")
    R<Boolean> update(SysDictDTO sysDictDTO);

    /**
     * 删除数据
     * @param id
     * @return
     */
    @ApiOperation("删除数据")
    @DeleteMapping("/feign/platform/sys/dict/delete")
    R<Boolean> delete(@RequestParam("id") Long id);


    /**
     * 获取字典类型数据
     * @param id
     * @return
     */
    @GetMapping("/feign/platform/sys/dict/type/id")
    @ApiOperation("获取字典数据")
    R<SysDictTypeDTO> typeById(@RequestParam("id") Long id);


    /**
     * 保存字典类型数据
     * @param sysDictTypeDTO
     * @return
     */
    @PostMapping("/feign/platform/sys/dict/type/save")
    @ApiOperation("保存字典类型数据")
    R<Boolean> saveType(SysDictTypeDTO sysDictTypeDTO);


    /**
     * 修改字典类型数据
     * @param sysDictTypeDTO
     * @return
     */
    @PostMapping("/feign/platform/sys/dict/type/update")
    @ApiOperation("修改字典类型数据")
    R<Boolean> updateType(SysDictTypeDTO sysDictTypeDTO);


    /**
     * 删除数据
     * @param id
     * @return
     */
    @ApiOperation("删除数据")
    @DeleteMapping("/feign/platform/sys/dict/type/delete")
    R<Boolean> deleteType(@RequestParam("id") Long id);

    /**
     * 根据测点code获取名字
     * @param code
     * @return
     */
    @GetMapping("/feign/platform/sys/dict/checkPoint/name")
    @ApiOperation("根据测点code获取名字")
    R<Map<String, String>> getCheckPointName(@RequestParam("code") Set<String> code);
}
