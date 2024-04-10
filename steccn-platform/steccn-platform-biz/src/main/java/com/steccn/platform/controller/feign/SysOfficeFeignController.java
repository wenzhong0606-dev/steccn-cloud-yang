package com.steccn.platform.controller.feign;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.google.common.collect.Maps;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.domain.SysOffice;
import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.dto.SysOfficeParentIdsDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.service.SysOfficeErpJoinService;
import com.steccn.platform.service.SysOfficeService;
import com.steccn.platform.vo.SysErpOfficeVO;
import com.steccn.platform.vo.SysOfficeParentVO;
import com.steccn.platform.vo.SysOfficeVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
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
    private final SysOfficeErpJoinService sysOfficeErpJoinService;

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


    @PostMapping("/names")
    @ApiOperation("根据主键id查询机构的信息")
    R<Map<String, SysOfficeVO>> listByName(@RequestBody Set<String> names) {
        if (CollUtil.isEmpty(names)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysOfficeService.listByName(names));
    }

    @PostMapping("/ch")
    @ApiOperation("根据主键id查询机构的中文")
    R<Map<Integer, String>> listByCh(@RequestBody Set<Integer> idList) {
        if (CollUtil.isEmpty(idList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysOfficeService.listByCh(idList));
    }

    /**
     * 查询工位列表
     *
     * @param typeList
     * @return
     */
    @ApiOperation("下拉选项")
    @GetMapping("/shelf/warehouse")
    R<List<SysOfficeVO>> warehouseAndShelfNew(@RequestBody List<String> typeList) {
        return R.success(sysOfficeService.warehouseAndShelf(typeList));
    }

    @PostMapping("/code")
    @ApiOperation("根据code查询中文")
    R<Map<String, SysOfficeVO>> listByCodeSet(@RequestBody Set<String> codeSet) {
        if (CollUtil.isEmpty(codeSet)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysOfficeService.listByCodeSet(codeSet));
    }

    @GetMapping("erp/code")
    @ApiOperation("根据id获取erp中的code")
    R<SysErpOfficeVO> getErpCodeByOfficeId(@RequestParam Integer officeId) {
        return R.success(sysOfficeErpJoinService.getErpCodeById(officeId));
    }

    @ApiOperation("根据父id获取下级节点数据")
    @PostMapping("/listByParentId")
    public R<List<SysOfficeVO>> listByParentId(@RequestBody SysOfficeParentDTO dto) {
        return R.success(sysOfficeService.listByParentId(dto));
    }

    @ApiOperation("根据父节点以及子节点类型获取下级子节点数据")
    @PostMapping("/listByParentIdAndType")
    public R<List<SysOfficeVO>> listByParentIdAndType(@RequestBody SysOfficeParentDTO dto){
        return R.success(sysOfficeService.listByParentIdAndType(dto));
    }

    @ApiOperation("货架的仓库")
    @GetMapping("/shelf/warehouse/{id}")
    public R<SysOfficeVO> shelfWarehouse(@PathVariable("id") Integer id) {
        return R.success(sysOfficeService.shelfWarehouse(id));
    }

    /**
     * 获取货架的id仓库信息 2.0
     *
     * @param id
     * @return
     */
    @ApiOperation("货架的仓库")
    @GetMapping("/shelf/shelfWarehouseInfo/{id}")
    public R<SysOfficeVO> shelfWarehouseInfo(@PathVariable("id") Integer id) {
        return R.success(sysOfficeService.shelfWarehouseInfo(id));
    }

    @ApiOperation("根据孩子id获取父亲节点数据")
    @GetMapping("/parent/{id}")
    public R<SysOfficeVO> parent(@PathVariable("id") Integer id) {
        return R.success(sysOfficeService.parentOffice(id));
    }

    /**
     * 查询工位列表
     *
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
    public R<List<SysOfficeParentVO>> getListSysOfficeList(@RequestBody SysOfficeParentIdsDTO dto) {
        return R.success(sysOfficeService.getListSysOfficeList(dto));
    }

    @PostMapping("/list/parentOffice")
    @ApiOperation("根据子id获取父级节点数据")
    public R<Map<Integer, SysOfficeVO>> getParentOfficeMap(@RequestBody List<Integer> childIdList) {
        return R.success(sysOfficeService.parentOfficeMap(childIdList));
    }

    @PostMapping("/list/parentInfo")
    @ApiOperation("根据子节点id 获取父级信息")
    public R<Map<Integer, SysOfficeVO>> parentInfo(@RequestBody Set<Integer> childIdList) {
        if (CollUtil.isEmpty(childIdList)) {
            return R.success(Maps.newHashMap());
        }
        return R.success(sysOfficeService.parentInfo(childIdList));
    }

    @PostMapping("/list/listByTypeAndLine")
    @ApiOperation("根据名字和类型选择")
    public R<List<SysOfficeVO>> getListSysOfficeList(@RequestBody SysOfficeDTO dto) {
        return R.success(sysOfficeService.findByTypeAndName(dto));
    }

    @PostMapping("/list/listByTypeAndKey")
    @ApiOperation("根据机构类型以及关键字名称返回SysOffice列表信息")
    public R<List<SysOfficeVO>> getListByTypeAndKey(@RequestBody SysOfficeTypeKeyDTO dto) {
        return R.success(sysOfficeService.getListByTypeAndKey(dto));
    }

    @PostMapping("/list/listByTypeAndNameList")
    @ApiOperation("根据机构类型以及关键字名称返回SysOffice列表信息")
    public R<List<SysOfficeVO>> getListByTypeAndNameList(@RequestBody SysOfficeTypeKeyDTO dto) {
        return R.success(sysOfficeService.getListByTypeAndNameList(dto));
    }

    @GetMapping("/getByIdSysOfficeInfo")
    @ApiOperation("根据主键获取结构code")
    public R<String> getByIdSysOfficeInfo(@RequestParam Long id) {
        return R.success(sysOfficeService.getByIdSysOfficeInfo(id));
    }

    @ApiOperation("查询指定机构类型，及机构类型的孩子节点，返回平铺数据")
    @GetMapping("/type/children/flat")
    R<Set<Integer>> typeAndChildrenFlat(@RequestParam("officeType") String officeType) {
        return R.success(sysOfficeService.typeAndChildrenFlat(officeType));
    }


    @PostMapping("/save")
    @ApiOperation("保存机构信息")
    public R<Boolean> save(@RequestBody SysOfficeDTO sysOfficeDTO) {
        return R.success(sysOfficeService.saveOffice(sysOfficeDTO));
    }

    @PostMapping("/update")
    @ApiOperation("修改机构信息")
    public R<Boolean> update(@RequestBody SysOfficeDTO sysOfficeDTO) {
        return R.success(sysOfficeService.updateOffice(sysOfficeDTO));
    }

    @DeleteMapping("/delete")
    @ApiOperation("删除机构信息")
    public R<Boolean> delete(@RequestBody SysOfficeDTO sysOfficeDTO) {
        return R.success(sysOfficeService.delete(sysOfficeDTO));
    }


    @PostMapping("/list/filter")
    @ApiOperation("根据机构类型以及多关键字名称返回SysOffice列表信息")
    public R<List<SysOfficeVO>> listMultiFilter(@RequestBody SysOfficeTypeKeyDTO dto){
        return R.success(sysOfficeService.listMultiFilter(dto));
    }

    @PostMapping("/parent/line")
    @ApiOperation("查询一个节点指定类型的父节点-产线")
    public R<Map<Integer, Integer>> parentLine(@RequestBody Set<Integer> officeIdSet) {
        return R.success(sysOfficeService.parentType(officeIdSet, SysOfficeConstant.PRODUCT_LINE));
    }

    @PostMapping("/parent/type")
    @ApiOperation("查询一个节点指定类型的父节点-指定类型")
    public R<Map<Integer, Integer>> parentType(@RequestBody TupleDTO<String, Set<Integer>> tupleDTO) {
        Assert.notEmpty(tupleDTO.getField1(), "指定类型不能为空");
        return R.success(sysOfficeService.parentType(tupleDTO.getField2(), tupleDTO.getField1()));
    }

    @PostMapping("/child/type")
    @ApiOperation("查询一个节点指定类型的孩子节点-指定类型")
    public R<Map<Integer, List<Integer>>> childType(@RequestBody TupleDTO<String, Set<Integer>> tupleDTO) {
        Assert.notEmpty(tupleDTO.getField1(), "指定类型不能为空");
        return R.success(sysOfficeService.childType(tupleDTO.getField2(), tupleDTO.getField1()));
    }

    @PostMapping("/child/workshop")
    @ApiOperation("查询节点的子节点-车间")
    public R<Map<Integer,List<Integer>>> childWorkshop(@RequestBody List<Integer> officeIdSet){
        return R.success(sysOfficeService.childWorkshop(officeIdSet));
    }

    @ApiOperation("获取组织结构信息")
    @GetMapping("/info/{id}")
    R<SysOfficeVO> info(@PathVariable("id") Integer id){
        SysOfficeVO sysOfficeVO = new SysOfficeVO();
        SysOffice sysOffice =  sysOfficeService.getById(id);
        BeanUtils.copyProperties(sysOffice,sysOfficeVO);
        return R.success(sysOfficeVO);
    }

    /**
     * 0基片 1车间
     * @param type
     * @return
     */
    @GetMapping("/workshop/children/list")
    @ApiOperation("根据类型获取基片或者深加工的车间和产线以及工位（平铺数据）")
    public R<List<SysOfficeVO>> listWorkshop(@RequestParam("type") Integer type) {
        return R.success(sysOfficeService.listWorkshop(type));
    }

    @PostMapping("/postDep")
    @ApiOperation("根据岗位code查询机构")
    public R<Map<String,SysOfficeVO>> postDepMap(@RequestBody Set<String> codes){
        return R.success(sysOfficeService.postDepMap(codes));
    }

    @PostMapping("/depPost")
    @ApiOperation("查询部门下的岗位")
    public R<Map<String,List<String>>> depPostMap(@RequestBody Set<String> codes){
        return R.success(sysOfficeService.depPostMap(codes));
    }

}
