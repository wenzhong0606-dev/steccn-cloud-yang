package com.steccn.pms.controller.feign;

import com.steccn.common.vo.R;
import com.steccn.pms.dto.ProducePassRateDTO;
import com.steccn.pms.service.ProducePassRateService;
import com.steccn.pms.vo.ProducePassRateVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/9 16:25
 */
@RestController
@Api("合格率feign")
@RequestMapping("/feign/pms/produce/pass/rate")
@RequiredArgsConstructor
public class ProducePassRateFeignController {

    private final ProducePassRateService producePassRateService;

    @PostMapping("/producePassRateGroup")
    @ApiOperation("根据分组类型查询合格率")
    public R<List<ProducePassRateVO>> producePassRateGroup(@RequestBody ProducePassRateDTO dto){
        return R.success(producePassRateService.getProducePassRateGroup(dto));
    }
}
