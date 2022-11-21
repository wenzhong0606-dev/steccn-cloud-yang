package com.steccn.platform.controller.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.annotation.SysLog;
import com.steccn.common.valid.InsertGroup;
import com.steccn.common.valid.UpdateGroup;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysStationWarehouseAddDTO;
import com.steccn.platform.dto.SysStationWarehouseDTO;
import com.steccn.platform.service.SysStationWarehouseService;
import com.steccn.platform.vo.SysSpareUnitConversionVO;
import com.steccn.platform.vo.SysStationWarehouseVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author chenhuan
 * @date 2022/11/2 16:45
 */
@Api("机构数据")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/station/warehouse")
public class SysStationWarehouseFeignController {

    private final SysStationWarehouseService sysStationWarehouseService;

    @PostMapping("/list/stations")
    @ApiOperation("批量获取工位绑定的线边仓数据")
    public R<Map<Integer, List<SysStationWarehouseVO>>> listByStationIds(@RequestBody Set<Integer> stations) {
        return R.success(sysStationWarehouseService.listByStationIds(stations));
    }
}
