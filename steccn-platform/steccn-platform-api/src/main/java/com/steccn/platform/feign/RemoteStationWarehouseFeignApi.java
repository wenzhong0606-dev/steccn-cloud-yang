package com.steccn.platform.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SysStationWarehouseDTO;
import com.steccn.platform.vo.SysStationWarehouseVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/11/21
 */
@FeignClient(contextId = "remoteStationWarehouseFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteStationWarehouseFeignApi {

    /**
     * 批量获取工位绑定的线边仓数据
     * @param stations
     * @return
     */
    @PostMapping("/feign/platform/station/warehouse/list/stations")
    @ApiOperation("批量获取工位绑定的线边仓数据")
    R<Map<Integer, List<SysStationWarehouseVO>>> listByStationIds(@RequestBody Set<Integer> stations);


    @PostMapping("/feign/platform/station/warehouse/page")
    @ApiOperation("工位配置列表")
    R<Page<SysStationWarehouseVO>> pageList(@RequestBody SysStationWarehouseDTO dto);
}
