package com.steccn.platform.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SparePageDTO;
import com.steccn.platform.vo.EqpSpareVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping("/feign/platform/eqpSpare/listByCode")
    @ApiOperation("根据code查询备件的信息")
    R<Map<String, EqpSpareVO>> listByCode(@RequestBody List<String> codeList);


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
     * @param sparePageDTO
     * @return
     */
    @PostMapping("/feign/platform/eqpSpare/listPage")
    @ApiOperation("获取备件分页列表")
    R<Page<EqpSpareVO>> listPage(@RequestBody SparePageDTO sparePageDTO);


    @PostMapping("/platform/eqpSpare/getByTypeIdSpareList")
    @ApiOperation("根据分类获取物料信息")
    R<List<EqpSpareVO>> getByTypeIdSpareList(@RequestBody Set<String> types);


}
