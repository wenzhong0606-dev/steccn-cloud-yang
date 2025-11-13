package com.steccn.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.IotDeviceDTO;
import com.steccn.platform.dto.IotTrendDetailDTO;
import com.steccn.platform.dto.OaProcessPushDTO;
import com.steccn.platform.service.IotQueryService;
import com.steccn.platform.service.IotTrendGroupDetailService;
import com.steccn.platform.service.IotTrendGroupService;
import com.steccn.platform.vo.IotIdNameVO;
import com.steccn.platform.vo.IotPointVO;
import com.steccn.platform.vo.IotTrendDetailVO;
import com.steccn.platform.vo.IotTrendGroupVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
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
    public R<Boolean> addTrend(@RequestParam("name")String name) {
        return R.success(iotTrendGroupService.add(name));
    }

    @GetMapping("/trend/all")
    @ApiOperation(value = "所有趋势组")
    public R<List<IotTrendGroupVO>> allTrend() {
        return R.success(iotTrendGroupService.IotTrendGroup());
    }

    @DeleteMapping("/trend/{id}")
    @ApiOperation(value = "删除趋势组")
    public R<Boolean> deleteTrend(@PathVariable("id")Integer id) {
        return R.success(iotTrendGroupService.deleteGroup(id));
    }

    @PostMapping("/trend/detail/save")
    @ApiOperation(value = "新增趋势组详情")
    public R<Boolean> addTrendDetail(@RequestBody @Valid List<IotTrendDetailDTO> dto) {
        return R.success(iotTrendGroupDetailService.saveInfo(dto));
    }

    @GetMapping("/trend/detail/{id}")
    @ApiOperation(value = "查询趋势组详情")
    public R<List<IotTrendDetailVO>> listTrendInfo(@PathVariable("id")Integer id) {
        return R.success(iotTrendGroupDetailService.infos(id));
    }
}
