package com.steccn.mes.controller;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.mes.service.JsSysEmployeeService;
import com.steccn.mes.vo.MesUserInfoVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * mes用户控制器
 *
 * @author AndrewLuo
 * @date 2024/04/11
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/mes/user")
@Slf4j
public class MesUserController {
    private final JsSysEmployeeService jsSysEmployeeService;

    @PostMapping("/getEmpDepPost")
    @ApiOperation(value = "获取用户所属部门")
    public R<List<MesUserInfoVO>> getEmpDepPost(@RequestBody TupleDTO<Set<String>,Integer> dto) {
        return R.success(jsSysEmployeeService.getEmpDepPost(dto.getField1()));
    }

    @PostMapping("/getEmpDepPost/tree")
    @ApiOperation(value = "获取用户所属部门")
    public R<List<String>> getEmpDepPostTree(@RequestBody TupleDTO<Set<String>,Integer> dto) {
        return R.success(jsSysEmployeeService.getEmpTree(dto.getField1()));
    }

    @GetMapping("/synOaUser")
    public R<String> synOaUser(){
        return R.success(jsSysEmployeeService.synOaUser());
    }
}
