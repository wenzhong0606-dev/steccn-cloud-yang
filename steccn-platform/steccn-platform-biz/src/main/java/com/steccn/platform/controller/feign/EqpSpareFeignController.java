package com.steccn.platform.controller.feign;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Maps;
import com.steccn.common.vo.R;
import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.SysDictVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


/**
 * 类说明
 *
 * @author Mongknife
 * @date 2022/9/2
 */
@Api("备件信息")
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/platform/eqpSpare")
public class EqpSpareFeignController {

    private final SysEqpSpareService eqpSpareService;

    @ApiOperation("获取备件列表")
    @GetMapping("/searchSpares")
    public R<Map<Long, EqpSpareVO>> searchSpares(@RequestParam("name")String name, @RequestParam("code") String code) {
        return R.success(eqpSpareService.searchSpares(name,code));
    }

    @ApiOperation("根据编码获取备件")
    @GetMapping("/getByCode")
    public R<EqpSpareVO> getByCode(@RequestParam("code")String code){
        return R.success(eqpSpareService.getByCode(code));
    }


}
