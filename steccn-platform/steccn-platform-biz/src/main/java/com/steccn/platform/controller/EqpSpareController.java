package com.steccn.platform.controller;

import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.service.SysEqpSpareService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/2
 */
@Api("备件信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/platform/eqpSpare")
public class EqpSpareController {

    private final SysEqpSpareService eqpSpareService;

    @ApiOperation("获取备件列表")
    @PostMapping("/searchSpares")
    public List<SpareDTO> searchSpares(@RequestParam("name")String name) {
        return eqpSpareService.listSpares(name);
    }
}
