package com.steccn.platform.controller;


import com.steccn.common.vo.BaseSelectVO;
import com.steccn.platform.dto.SysOfficeParentCodeDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.vo.StationLineVO;
import com.steccn.platform.vo.SysOfficeParentVO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SysOfficeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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
    @ApiOperation("下拉选项")
    public R<List<SysOfficeVO>> warehouseAndShelf(@RequestBody List<String> typeList) {
        return R.success(sysOfficeService.warehouseAndShelf(typeList));
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

    @PostMapping("/list/parentcodes")
    @ApiOperation("根据车间编码列表选择产线")
    public R<List<SysOfficeParentVO>> getListSysOfficeList(@RequestBody SysOfficeParentCodeDTO dto){
        return R.success(sysOfficeService.getListSysOfficeList(dto));
    }
}
