package com.steccn.platform.feign;


import com.steccn.platform.dto.SysDictDTO;
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
     * 根据编码类型查询数据字典的值
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
    @PostMapping("/listByCodes")
    @ApiOperation("根据code查询数据字典的值")
    R<Map<String, SysDictVO>> listByCodes(@RequestBody Set<String> codes);
}
