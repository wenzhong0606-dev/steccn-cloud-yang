package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.dto.SysOfficeParentIdsDTO;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.vo.SysErpOfficeVO;
import com.steccn.platform.vo.SysOfficeParentVO;
import com.steccn.platform.vo.SysOfficeVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2022/09/07
 */
@FeignClient(contextId = "remoteOfficeFeign", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteOfficeFeign {
    /**
     * 根据officeType查询机构
     *
     * @param officeType 机构类型
     * @return
     */
    @GetMapping("/platform/sys/office")
    @ApiOperation("根据officeType查询机构")
    R<List<SysOfficeVO>> list(@RequestParam("officeType") String officeType);

    /**
     * 根据officeType查询机构
     *
     * @param
     * @return
     */
    @GetMapping("/platform/sys/office/data")
    @ApiOperation("平级机构数据")
    R<List<SysOfficeVO>> data();

    /**
     * 根据officeType查询机构
     *
     * @param type 机构类型
     * @return
     */
    @GetMapping("feign/platform/sys/office")
    @ApiOperation("平级机构数据")
    R<List<SysOfficeVO>> flatData(@RequestParam("type") String type);

    /**
     * 根据id查询名字
     *
     * @param id 机构id
     * @return
     */
    @GetMapping("/feign/platform/sys/office/name")
    @ApiOperation("通过id获取名字")
    R<String> getNameById(@RequestParam("id") Integer id);


    /**
     * 根据主键id查询机构的信息
     *
     * @param idList 机构id集合
     * @return
     */
    @PostMapping("/feign/platform/sys/office/ids")
    @ApiOperation("根据主键id查询机构的信息")
    R<Map<Integer, SysOfficeVO>> listById(@RequestBody Set<Integer> idList);


    /**
     * 根据名称查询机构的信息
     *
     * @param names 机构id集合
     * @return
     */
    @PostMapping("/feign/platform/sys/office/names")
    @ApiOperation("根据名称查询机构的信息")
    R<Map<String, SysOfficeVO>> listByName(@RequestBody Set<String> names);


    /**
     * 根据主键id查询机构的中文
     *
     * @param idList 机构id集合
     * @return
     */
    @PostMapping("/feign/platform/sys/office/ch")
    @ApiOperation("根据主键id查询机构的信息")
    R<Map<Integer, String>> listByIdCh(@RequestBody Set<Integer> idList);


    /**
     * 根据code查询中文
     *
     * @param codeSet 编码set
     * @return
     */
    @PostMapping("/feign/platform/sys/office/code")
    @ApiOperation("根据code查询中文")
    R<Map<String, SysOfficeVO>> listByCodeSet(@RequestBody Set<String> codeSet);

    /**
     * 根据父id获取下级节点数据
     *
     * @param dto
     * @return
     */
    @ApiOperation("根据父id获取下级节点数据")
    @PostMapping("/feign/platform/sys/office/listByParentId")
    R<List<SysOfficeVO>> listByParentId(@RequestBody SysOfficeParentDTO dto);

    @ApiOperation("根据父id获取下级节点数据")
    @PostMapping("/feign/platform/sys/office/listByParentIdAndType")
    R<List<SysOfficeVO>> listByParentIdAndType(@RequestBody SysOfficeParentDTO dto);


    /**
     * 获取货架的id仓库信息
     *
     * @param id
     * @return
     */
    @ApiOperation("货架的仓库")
    @GetMapping("/feign/platform/sys/office/shelf/warehouse/{id}")
    R<SysOfficeVO> shelfWarehouse(@PathVariable("id") Integer id);

    /**
     * 获取货架的id仓库信息 2.0
     *
     * @param id
     * @return
     */
    @ApiOperation("货架的仓库")
    @GetMapping("/feign/platform/sys/office/shelf/shelfWarehouseInfo/{id}")
    R<SysOfficeVO> shelfWarehouseInfo(@PathVariable("id") Integer id);

    /**
     * 根据孩子id获取父亲节点数据
     *
     * @param id 孩子id
     * @return
     */
    @ApiOperation("根据孩子id获取父亲节点数据")
    @GetMapping("/feign/platform/sys/office/parent/{id}")
    R<SysOfficeVO> parent(@PathVariable("id") Integer id);

    /**
     * 查询工位列表
     *
     * @param typeList
     * @return
     */
    @ApiOperation("下拉选项")
    @GetMapping("/feign/platform/sys/office/shelf/warehouse")
    R<List<SysOfficeVO>> warehouseAndShelf(@RequestBody List<String> typeList);

    /**
     * 根据子id获取父级节点数据
     *
     * @param childIdList
     * @return
     */
    @PostMapping("/feign/platform/sys/office/list/parentOffice")
    @ApiOperation("根据子id获取父级节点数据")
    R<Map<Integer, SysOfficeVO>> getParentOfficeMap(@RequestBody List<Integer> childIdList);

    /**
     * 根据子节点id 获取父级详情信息
     *
     * @param childIdList
     * @return
     */
    @PostMapping("/feign/platform/sys/office/list/parentInfo")
    @ApiOperation("根据子id获取父级节点数据")
    R<Map<Integer, SysOfficeVO>> parentInfo(@RequestBody List<Integer> childIdList);


    /**
     * 根据机构类型和名字查找数据
     *
     * @param dto
     * @return
     */
    @PostMapping("/feign/platform/sys/office/list/listByTypeAndLine")
    @ApiOperation("根据机构类型和名字查找数据")
    R<List<SysOfficeVO>> getByTypeAndLine(@RequestBody SysOfficeDTO dto);

    @PostMapping("/feign/platform/sys/office/list/listByTypeAndKey")
    @ApiOperation("根据机构类型以及关键字名称返回SysOffice列表信息")
    R<List<SysOfficeVO>> getListByTypeAndKey(@RequestBody SysOfficeTypeKeyDTO dto);

    @PostMapping("/feign/platform/sys/office/list/listByTypeAndNameList")
    @ApiOperation("根据机构类型以及名称返回SysOffice列表信息")
    R<List<SysOfficeVO>> getListByTypeAndNameList(@RequestBody SysOfficeTypeKeyDTO dto);

    @PostMapping("/list/parentIds")
    @ApiOperation("根据车间编码列表选择产线")
    R<List<SysOfficeParentVO>> getListSysOfficeList(@RequestBody SysOfficeParentIdsDTO dto);

    @GetMapping("/feign/platform/sys/office/getByIdSysOfficeInfo")
    @ApiOperation("根据主键获取结构信息code")
    R<String> getByIdSysOfficeInfo(@RequestParam("id") Long id);

    @ApiOperation("获取组织结构信息")
    @GetMapping("/feign/platform/sys/office/info/{id}")
    R<SysOfficeVO> info(@PathVariable("id") Integer id);

    @ApiOperation("获取组织结构信息")
    @GetMapping("/feign/platform/sys/office/info/code")
    R<SysOfficeVO> infoCode(@RequestParam("type") String type, @RequestParam("code") String code);

    @ApiOperation("获取组织结构信息")
    @GetMapping("/platform/sys/office/sysOfficeInfo/{id}")
    R<SysOfficeVO> sysOfficeInfo(@PathVariable("id") Integer id);

    /**
     * 查询指定机构类型，及机构类型的孩子节点，返回平铺数据
     *
     * @param officeType 组织机构类型
     * @return
     */
    @ApiOperation("查询指定机构类型，及机构类型的孩子节点，返回平铺数据")
    @GetMapping("/feign/platform/sys/office/type/children/flat")
    R<Set<Integer>> typeAndChildrenFlat(@RequestParam("officeType") String officeType);


    /**
     * 保存机构信息
     *
     * @param sysOfficeDTO
     * @return
     */
    @PostMapping("/feign/platform/sys/office/save")
    @ApiOperation("保存机构信息")
    R<Boolean> save(@RequestBody SysOfficeDTO sysOfficeDTO);

    /**
     * 根据机构编码修改机构信息
     *
     * @param sysOfficeDTO
     * @return
     */
    @PostMapping("/feign/platform/sys/office/update")
    @ApiOperation("修改机构信息")
    R<Boolean> update(@RequestBody SysOfficeDTO sysOfficeDTO);


    /**
     * 根据机构编码删除机构信息
     *
     * @param sysOfficeDTO
     * @return
     */
    @DeleteMapping("/feign/platform/sys/office/delete")
    @ApiOperation("删除机构信息")
    R<Boolean> delete(@RequestBody SysOfficeDTO sysOfficeDTO);


    /**
     * 根据机构类型以及多关键字名称返回SysOffice列表信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/feign/platform/sys/office/list/filter")
    @ApiOperation("根据机构类型以及多关键字名称返回SysOffice列表信息")
    R<List<SysOfficeVO>> listMultiFilter(@RequestBody SysOfficeTypeKeyDTO dto);

    /**
     * 查询一个节点指定类型的父节点-产线
     *
     * @param officeIdSet 组织机构id集合
     * @return
     */
    @PostMapping("/feign/platform/sys/office/parent/line")
    @ApiOperation("查询一个节点指定类型的父节点-产线")
    R<Map<Integer, Integer>> parentLine(@RequestBody Set<Integer> officeIdSet);

    /**
     * 查询一个节点指定类型的父节点
     *
     * @param tupleDTO 指定类型 孩子节点集合
     * @return list
     */
    @PostMapping("/feign/platform/sys/office/parent/type")
    @ApiOperation("查询一个节点指定类型的父节点")
    R<Map<Integer, Integer>> parentType(@RequestBody TupleDTO<String, Set<Integer>> tupleDTO);

    /**
     * 查询一个节点指定类型的孩子节点
     *
     * @param tupleDTO 指定类型 父节点节点集合
     * @return list
     */
    @PostMapping("/feign/platform/sys/office/child/type")
    @ApiOperation("查询一个节点指定类型的父节点")
    R<Map<Integer, List<Integer>>> childType(@RequestBody TupleDTO<String, Set<Integer>> tupleDTO);

    @PostMapping("/feign/platform/sys/office/child/workshop")
    @ApiOperation("查询节点的子节点-车间")
    R<Map<Integer, List<Integer>>> childWorkshop(@RequestBody List<Integer> officeIdSet);

    @GetMapping("/feign/platform/sys/office/erp/code")
    @ApiOperation("根据id获取erp中的code")
    R<SysErpOfficeVO> getErpCodeByOfficeId(@RequestParam("officeId") Integer officeId);


    /**
     * 0基片 1 深加工
     * @param type
     * @return
     */
    @GetMapping("/feign/platform/sys/office/workshop/children/list")
    @ApiOperation("根据类型获取基片或者深加工的车间和产线以及工位（平铺数据）")
    R<List<SysOfficeVO>> listWorkshop(@RequestParam("type") Integer type);

    @PostMapping("/feign/platform/sys/office/postDep")
    @ApiOperation("根据岗位code查询机构")
    R<Map<String,SysOfficeVO>> postDepMap(@RequestBody Set<String> codes);

    @PostMapping("/feign/platform/sys/office/depPost")
    @ApiOperation("查询部门下的岗位")
    R<Map<String,List<String>>> depPostMap(@RequestBody Set<String> codes);
    @GetMapping("/feign/platform/sys/office/code")
    @ApiOperation("根据机构code获取机构信息")
    R<SysOfficeVO> getByCode(@RequestParam("code") String warehouseCode);
}
