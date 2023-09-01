package com.steccn.mes.controller;

import com.steccn.common.vo.R;
import com.steccn.mes.dto.ErpCustomerDTO;
import com.steccn.mes.service.MesEqpSpareService;
import com.steccn.mes.service.MesEquipmentService;
import com.steccn.mes.vo.MesEqpVo;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 获取设备信息
 *
 * @author AndrewLuo
 * @date 2023/09/01
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mes/eqp")
@Slf4j
public class MesEqpController {
    private final MesEquipmentService mesEquipmentService;

    @GetMapping("/thirdParty")
    @ApiOperation(value = "获取设备信息")
    public R<List<MesEqpVo>> getEqpInfo() {
        return R.success(mesEquipmentService.getEqpInfo());
    }
}
