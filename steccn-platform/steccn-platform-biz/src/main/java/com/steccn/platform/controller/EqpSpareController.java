package com.steccn.platform.controller;

import com.steccn.common.vo.R;
import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.vo.EqpSpareVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/searchSpares")
    public List<EqpSpareVO> searchSpares(@RequestParam("name")String name) {
        return eqpSpareService.listSpares(name);
    }

    @ApiOperation("根据编码获取备件")
    @GetMapping("/getByCode")
    public R<EqpSpareVO> getByCode(@RequestParam("code")String code){
        return R.success(eqpSpareService.getByCode(code));
    }
}
