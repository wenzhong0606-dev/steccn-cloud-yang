package com.steccn.mes.controller.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.mes.dto.EqpReportDTO;
import com.steccn.mes.service.JsSysEmployeeService;
import com.steccn.mes.service.MesEqpReportService;
import com.steccn.mes.service.MesEquipmentService;
import com.steccn.mes.vo.MesEqpVO;
import com.steccn.mes.vo.MesUserInfoVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/09/05
 */
@Api("设备code")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/mes")
public class MesEqpFeignController {
    private final MesEquipmentService mesEquipmentService;
    private final JsSysEmployeeService jsSysEmployeeService;
    private final MesEqpReportService mesEqpReportService;

    @ApiOperation("根据编码获取备件")
    @GetMapping("/eqp/getByCode")
    public R<MesEqpVO> getByCode(@RequestParam("code") String code) {
        return R.success(mesEquipmentService.getEqpByCode(code));
    }

    @ApiOperation("修改设备运行状态")
    @PostMapping("/eqp/state")
    public R<Boolean> updateEqpState(@RequestBody TupleDTO<String,String> dto) {
        return R.success(mesEquipmentService.changeEqpState(dto));
    }

    @ApiOperation("修改报表数据")
    @PostMapping("/eqp/changeReportData")
    public R<Boolean> changeReportData(@RequestBody EqpReportDTO dto) {
        return R.success(mesEqpReportService.changeReportData(dto));
    }

    @ApiOperation("获取岗位下的人员")
    @PostMapping("/emp/post")
    public R<Map<String, List<String>>> getPostUser(@RequestBody Set<String> dto) {
        return R.success(jsSysEmployeeService.getPostUser(dto));
    }

    @PostMapping("/user/getEmpDepPost")
    @ApiOperation(value = "获取人员部门岗位")
    public R<List<MesUserInfoVO>> getEmpDepPost(@RequestBody TupleDTO<Set<String>,Integer> dto) {
        return R.success(jsSysEmployeeService.getEmpDepPost(dto.getField1()));
    }
}
