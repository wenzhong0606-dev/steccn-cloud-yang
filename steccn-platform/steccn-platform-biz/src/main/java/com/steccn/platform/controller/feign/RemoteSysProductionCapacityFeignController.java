package com.steccn.platform.controller.feign;

import com.steccn.common.dto.TripleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.service.SysProductionCapacityService;
import com.steccn.platform.vo.SysProductionCapacityDetailVO;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @desc 标准产能
 * @date 2023/12/28
 */


@Api("标准产能")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/sys/production/capacity")
public class RemoteSysProductionCapacityFeignController {

    private final SysProductionCapacityService sysProductionCapacityService;

    /**
     * 查询工位指定日期区间的标准产能
     *
     * @param tripleDTO 类型
     * @return
     */
    @PostMapping("/station")
    public R<List<SysProductionCapacityDetailVO>> capacityStation(@RequestBody TripleDTO<Set<Integer>, LocalDate, LocalDate> tripleDTO) {
        return R.success(sysProductionCapacityService.capacityStation(tripleDTO.getField1(), tripleDTO.getField2(), tripleDTO.getField3()));
    }

}
