package com.steccn.platform.controller;


import com.google.common.collect.Lists;
import com.steccn.common.vo.R;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.domain.SysOffice;
import com.steccn.platform.dto.*;
import com.steccn.platform.service.SysOfficeService;
import com.steccn.platform.vo.StationLineVO;
import com.steccn.platform.vo.SysOfficeParentVO;
import com.steccn.platform.vo.SysOfficeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 机构数据
 *
 * @author Mongknife
 * @date 2022/9/6
 */
@Api("机构数据")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/sys/office")
public class SysOfficeController {

    private final SysOfficeService sysOfficeService;

    @GetMapping
    @ApiOperation("机构数据列表")
    public R<List<SysOfficeVO>> list(@ApiParam("类型编码") @RequestParam("officeType") String officeType) {
        return R.success(sysOfficeService.sysList(officeType));
    }

    @GetMapping("/data")
    @ApiOperation("平级机构数据")
    public R<List<SysOfficeVO>> data(){
        return R.success(sysOfficeService.data());
    }

    @ApiOperation("返回数据类型下所有数据")
    @GetMapping("/flat")
    public R<List<SysOfficeVO>> listFlatDataByType(@RequestParam String type) {
        return R.success(sysOfficeService.flatData(type));
    }

    @ApiOperation("返回客户信息")
    @GetMapping("/customer")
    public R<List<SysOfficeVO>> listCustomer(@RequestParam String name) {
        return R.success(sysOfficeService.getCustomer(name));
    }

    @ApiOperation("返回类型和父级节点下数据")
    @PostMapping("/getByPcodeAndType")
    public R<List<SysOfficeVO>> listFlatDataByPcode(@RequestBody SysOfficeParentDTO dto) {
        return R.success(sysOfficeService.findDataByPCodeAndType(dto));
    }

    /**
     * 仓库和货架
     * @return
     */
    @PostMapping("/warehouse")
    @ApiOperation("根据机构类型查询下拉选项树结构")
    public R<List<SysOfficeVO>> warehouseAndShelf(@RequestBody List<String> typeList) {
        return R.success(typeDataList(typeList));
    }

    /**
     * 根据组织机构类型获取数据
     * @param typeList 类型集合
     * @return
     */
    private List<SysOfficeVO> typeDataList(List<String> typeList) {
        if (typeList.size() == 3 && typeList.contains(SysOfficeConstant.WORKSHOP) && typeList.contains(SysOfficeConstant.PRODUCT_LINE) && typeList.contains(SysOfficeConstant.STATION)) {
            return sysOfficeService.workShopLineStation(Lists.newArrayList(SysOfficeConstant.WORKSHOP, SysOfficeConstant.PRODUCT_LINE, SysOfficeConstant.STATION));
        } else if (typeList.size() == 2 && typeList.contains(SysOfficeConstant.WORKSHOP) && typeList.contains(SysOfficeConstant.PRODUCT_LINE)) {
            return sysOfficeService.workShopLineStation(Lists.newArrayList(SysOfficeConstant.WORKSHOP, SysOfficeConstant.PRODUCT_LINE));
        } else if (typeList.size() == 2 && typeList.contains(SysOfficeConstant.PRODUCT_LINE) && typeList.contains(SysOfficeConstant.STATION)) {
            return sysOfficeService.workShopLineStation(Lists.newArrayList(SysOfficeConstant.PRODUCT_LINE, SysOfficeConstant.STATION));
        } else {
            return sysOfficeService.warehouseAndShelf(typeList);
        }
    }

    @PostMapping("/multi/tree")
    @ApiOperation("根据机构类型+根节点查询下拉选项树结构")
    public R<List<SysOfficeVO>> multiTree(@RequestBody SysOfficeMultiTreeDTO dto) {
        List<SysOfficeVO> sysOfficeVOList = typeDataList(dto.getOfficeTypeList());
        List<SysOfficeVO> collect = sysOfficeVOList.stream().filter(ele -> dto.getRootCodeList().contains(ele.getOfficeCode())).collect(Collectors.toList());
        return R.success(collect);
    }

    @ApiOperation("货架的仓库")
    @GetMapping("/shelf/warehouse/{id}")
    public R<SysOfficeVO> shelfWarehouse(@PathVariable("id") Integer id) {
        return R.success(sysOfficeService.shelfWarehouse(id));
    }

    @ApiOperation("扫描工位二维码")
    @GetMapping("/station/scan/{id}")
    public R<StationLineVO> stationScan(@PathVariable("id") Integer id){
        return R.success(sysOfficeService.stationScan(id));
    }

    @ApiOperation("根据父id获取下级节点数据")
    @PostMapping("/listByParentId")
    public R<List<SysOfficeVO>> listByParentId(@RequestBody SysOfficeParentDTO dto){
        return R.success(sysOfficeService.listByParentId(dto));
    }

    @PostMapping("/list/parentIds")
    @ApiOperation("根据车间编码列表选择产线")
    public R<List<SysOfficeParentVO>> getListSysOfficeList(@RequestBody SysOfficeParentIdsDTO dto){
        return R.success(sysOfficeService.getListSysOfficeList(dto));
    }

    @PostMapping("/list/listByTypeAndLine")
    @ApiOperation("根据车间编码列表选择产线")
    public R<List<SysOfficeVO>> getListSysOfficeList(@RequestBody SysOfficeDTO dto){
        return R.success(sysOfficeService.findByTypeAndName(dto));
    }

    @PostMapping("/list/listByTypeAndKey")
    @ApiOperation("根据机构类型以及关键字名称返回SysOffice列表信息")
    public R<List<SysOfficeVO>> getListByTypeAndKey(@RequestBody SysOfficeTypeKeyDTO dto){
        return R.success(sysOfficeService.getListByTypeAndKey(dto));
    }

    @PostMapping("/list/filter")
    @ApiOperation("根据机构类型以及多关键字名称返回SysOffice列表信息")
    public R<List<SysOfficeVO>> listMultiFilter(@RequestBody SysOfficeTypeKeyDTO dto){
        return R.success(sysOfficeService.listMultiFilter(dto));
    }

    @ApiOperation("获取组织结构信息")
    @GetMapping("/sysOfficeInfo/{id}")
    public R<SysOfficeVO> sysOfficeInfo(@PathVariable("id") Integer id) {
        SysOfficeVO sysOfficeVO = new SysOfficeVO();
        SysOffice sysOffice =  sysOfficeService.getById(id);
        BeanUtils.copyProperties(sysOffice,sysOfficeVO);
        return R.success(sysOfficeVO);
    }

    @PostMapping("/parent/line")
    @ApiOperation("查询一个节点指定类型的父节点-产线")
    public R<Map<Integer, Integer>> parentLine(@RequestBody Set<Integer> officeIdSet) {
        return R.success(sysOfficeService.parentType(officeIdSet, SysOfficeConstant.PRODUCT_LINE));
    }

    @PostMapping("/parent/warehouse")
    @ApiOperation("查询一个节点指定类型的父节点-仓库")
    public R<Map<Integer, Integer>> parentWarehouse(@RequestBody Set<Integer> officeIdSet) {
        return R.success(sysOfficeService.parentType(officeIdSet, SysOfficeConstant.WAREHOUSE_TYPE));
    }

    /**
     * 0基片 1车间
     * @param type
     * @return
     */
    @GetMapping("/workshop/children")
    @ApiOperation("根据类型获取基片或者深加工的车间和产线")
    public R<List<SysOfficeVO>> listWorkshop(@RequestParam("type") Integer type) {
        return R.success(sysOfficeService.getWorkShopChildren(type));
    }

    @PostMapping("/workshops/children")
    @ApiOperation("查询一个节点指定类型的父节点-仓库")
    public R<List<SysOfficeVO>> listByParentIds(@RequestBody Set<Long> ids) {
        return R.success(sysOfficeService.listByParentIds(ids));
    }
}
