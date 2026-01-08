package com.steccn.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.*;
import com.steccn.platform.service.*;
import com.steccn.platform.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

/**
 * iot平台控制器
 *
 * @author AndrewLuo
 * @date 2025/11/11
 */
@Api("iot平台控制器")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/iot")
public class IotDataController {
    private final IotQueryService iotQueryService;
    private final IotTrendGroupService iotTrendGroupService;
    private final IotTrendGroupDetailService iotTrendGroupDetailService;
    private final IotAlertService iotAlertService;
    private final IotAlertRecordService iotAlertRecordService;

    @GetMapping("/eqp/group")
    @ApiOperation(value = "获取设备分组信息")
    public R<List<IotIdNameVO>> eqpGroup() {
        return R.success(iotQueryService.queryEqpGroup());
    }

    @PostMapping("/eqp/device")
    @ApiOperation(value = "获取设备信息")
    public R<Page<IotIdNameVO>> deviceInfo(@RequestBody IotDeviceDTO dto) {
        return R.success(iotQueryService.queryDeviceInfo(dto));
    }

    @PostMapping("/eqp/point")
    @ApiOperation(value = "获取设备信息")
    public R<Page<IotPointVO>> pointInfo(@RequestBody IotDeviceDTO dto) {
        return R.success(iotQueryService.queryPointInfo(dto));
    }

    @GetMapping("/trend/add")
    @ApiOperation(value = "新增趋势组")
    @PreAuthorize("hasAuthority('platform:iot:trend')")
    public R<Boolean> addTrend(@RequestParam("name")String name) {
        return R.success(iotTrendGroupService.add(name));
    }

    @GetMapping("/trend/all")
    @ApiOperation(value = "所有趋势组")
    public R<List<IotTrendGroupVO>> allTrend() {
        return R.success(iotTrendGroupService.IotTrendGroup());
    }

    @DeleteMapping("/trend/{id}")
    @PreAuthorize("hasAuthority('platform:iot:trend')")
    @ApiOperation(value = "删除趋势组")
    public R<Boolean> deleteTrend(@PathVariable("id")Integer id) {
        return R.success(iotTrendGroupService.deleteGroup(id));
    }

    @PostMapping("/trend/detail/save")
    @PreAuthorize("hasAuthority('platform:iot:trend')")
    @ApiOperation(value = "新增趋势组详情")
    public R<Boolean> addTrendDetail(@RequestBody @Valid List<IotTrendDetailDTO> dto) {
        return R.success(iotTrendGroupDetailService.saveInfo(dto));
    }

    @GetMapping("/trend/detail/{id}")
    @ApiOperation(value = "查询趋势组详情")
    public R<List<IotTrendDetailVO>> listTrendInfo(@PathVariable("id")Integer id) {
        return R.success(iotTrendGroupDetailService.infos(id));
    }

    @PostMapping("/trend/data")
    @ApiOperation(value = "数据查询")
    public R<List<IotDataQueryVO>> dataQuery(@RequestBody IotDataQueryDTO dto) {
        return R.success(iotQueryService.queryPointData(dto));
    }

    @ApiModelProperty("Excel导出")
    @PostMapping("/trend/data/export")
    public void exportData(HttpServletResponse response,@RequestBody List<IotDataQueryDTO> dto) throws IOException {
        iotQueryService.excelExportData(response,dto);
    }

    @ApiModelProperty("保存报警规则")
    @PostMapping("/alert")
    public R<Boolean> saveAlert(@RequestBody @Valid IotAlertDTO dto) {
        return R.success(iotAlertService.addAlert(dto));
    }

    @ApiModelProperty("修改报警规则")
    @PutMapping("/alert")
    public R<Boolean> updateAlert(@RequestBody @Valid IotAlertDTO dto) {
        return R.success(iotAlertService.updateAlert(dto));
    }

    @ApiModelProperty("修改报警规则")
    @GetMapping("/alert/{id}")
    public R<IotAlertVO> updateAlert(@PathVariable("id") Integer id) {
        return R.success(iotAlertService.getOneInfo(id));
    }

    @ApiModelProperty("修改报警规则")
    @PostMapping("/alert/page")
    public R<Page<IotAlertVO>> listAlert(@RequestBody BasePageDTO dto) {
        return R.success(iotAlertService.listInfo(dto));
    }

    @ApiModelProperty("修改报警规则")
    @DeleteMapping("/alert/{id}")
    public R<Boolean> deleteAlert(@PathVariable("id") Integer id) {
        return R.success(iotAlertService.deleteInfo(id));
    }

    @ApiModelProperty("修改报警规则")
    @PostMapping("/alert/record/page")
    public R<Page<IotAlertRecordVO>> listAlertRecord(@RequestBody IotAlertRecordDTO dto) {
        return R.success(iotAlertRecordService.listInfo(dto));
    }

    @ApiModelProperty("修改报警规则")
    @GetMapping("/alert/test")
    public R<Boolean> test() {
        return R.success(iotAlertService.alertInfo());
    }

}
