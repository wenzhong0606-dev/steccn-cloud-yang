package com.steccn.platform.feign;


import com.steccn.platform.dto.SysDictDTO;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(contextId = "remoteDictFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteDictFeignApi {

    /**
     * 根据主键id查询数据字典的值
     *
     * @param idList 数据字典id集合
     * @return
     */
    @RequestMapping("/feign/platform/sys/dict/ids")
    @ApiOperation("根据主键id查询数据字典的值")
    R<Map<Long, SysDictVO>> listById(@RequestBody List<Long> idList);

    /**
     * 根据编码类型查询数据字典的值
     */
    @GetMapping("/feign/platform/sys/dict/getByCode")
    @ApiOperation("根据编码类型查询数据字典的值")
    R<SysDictVO> getByCode(@ApiParam("字典编码")@RequestParam("code") String code,@ApiParam("类型")@RequestParam("type") String type);


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

    @RequestMapping("/feign/platform/sys/dict/listByCode")
    @ApiOperation("根据code查询数据字典的值")
    R<Map<String, SysDictVO>> listByCode(@RequestBody SysDictDTO sysDictDTO);
}
