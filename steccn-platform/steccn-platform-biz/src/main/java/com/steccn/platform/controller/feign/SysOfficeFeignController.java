package com.steccn.platform.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.dto.SysOfficeParentCodeDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.service.SysOfficeService;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.vo.SysOfficeParentVO;
import com.steccn.platform.vo.SysOfficeVO;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 机构feign控制器
 *
 * @author AndrewLuo
 * @date 2022/09/08
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/sys/office")
public class SysOfficeFeignController {
    private final SysOfficeService sysOfficeService;

    @GetMapping()
    public R<List<SysOfficeVO>> listFlatDataByType(@RequestParam String type) {
        return R.success(sysOfficeService.flatData(type));
    }

    @GetMapping("/name")
    public R<String> listFlatDataByType(@RequestParam Integer id) {
        return new R<>(sysOfficeService.findNameById(id));
    }

    @PostMapping("/ids")
    @ApiOperation("根据主键id查询机构的信息")
    R<Map<Integer, SysOfficeVO>> listById(@RequestBody Set<Integer> idList) {
        if (CollUtil.isEmpty(idList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysOfficeService.listById(idList));
    }

    @PostMapping("/ch")
    @ApiOperation("根据主键id查询机构的中文")
    R<Map<Integer, String>> listByCh(@RequestBody Set<Integer> idList) {
        if (CollUtil.isEmpty(idList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysOfficeService.listByCh(idList));
    }

    @PostMapping("/code")
    @ApiOperation("根据code查询中文")
    R<Map<String, SysOfficeVO>> listByCodeSet(@RequestBody Set<String> codeSet) {
        if (CollUtil.isEmpty(codeSet)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysOfficeService.listByCodeSet(codeSet));
    }

    @ApiOperation("根据父id获取下级节点数据")
    @PostMapping("/listByParentId")
    public R<List<SysOfficeVO>> listByParentId(@RequestBody SysOfficeParentDTO dto){
        return R.success(sysOfficeService.listByParentId(dto));
    }

    @ApiOperation("货架的仓库")
    @GetMapping("/shelf/warehouse/{id}")
    public R<SysOfficeVO> shelfWarehouse(@PathVariable("id") Integer id) {
        return R.success(sysOfficeService.shelfWarehouse(id));
    }

    @ApiOperation("根据孩子id获取父亲节点数据")
    @GetMapping("/parent/{id}")
    public R<SysOfficeVO> parent(@PathVariable("id") Integer id) {
        return R.success(sysOfficeService.parentOffice(id));
    }

    /**
     * 查询工位列表
     * @param typeList
     * @return
     */
    @PostMapping("/warehouse")
    @ApiOperation("下拉选项")
    public R<List<SysOfficeVO>> warehouseAndShelf(@RequestBody List<String> typeList) {
        return R.success(sysOfficeService.warehouseAndShelf(typeList));
    }

    @PostMapping("/list/parencodes")
    @ApiOperation("根据车间编码列表选择产线")
    public R<List<SysOfficeParentVO>> getListSysOfficeList(@RequestBody SysOfficeParentCodeDTO dto){
        return R.success(sysOfficeService.getListSysOfficeList(dto));
    }

    @PostMapping("/list/parentOffice")
    @ApiOperation("根据子id获取父级节点数据")
    public R<Map<Integer, SysOfficeVO>> getParentOfficeMap(@RequestBody List<Integer> childIdList){
        return R.success(sysOfficeService.parentOfficeMap(childIdList));
    }

    @PostMapping("/list/listByTypeAndLine")
    @ApiOperation("根据名字和类型选择")
    public R<List<SysOfficeVO>> getListSysOfficeList(@RequestBody SysOfficeDTO dto){
        return R.success(sysOfficeService.findByTypeAndName(dto));
    }
}
