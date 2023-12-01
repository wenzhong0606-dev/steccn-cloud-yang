package com.steccn.dp.controller.feign;

import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;
import com.steccn.dp.service.AdsOpcUaAll3Service;
import com.steccn.dp.vo.DpValueDateVO;
import com.steccn.dp.vo.DpWeightVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/11/23
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/dp/weight")
public class DpWeightFeignController {
    private final AdsOpcUaAll3Service adsOpcUaAll3Service;

    @PostMapping("/count")
    @ApiOperation(value = "电子秤读数")
    public R<List<DpWeightVO>> count(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto) {
        return R.success(adsOpcUaAll3Service.getWeighCount(dto));
    }

    @PostMapping("/countAsDay")
    @ApiOperation(value = "电子秤读数按天")
    public R<List<DpValueDateVO>> countAsDay(@RequestBody TupleDTO<LocalDateTime,LocalDateTime> dto) {
        return R.success(adsOpcUaAll3Service.weightCountAsDay(dto));
    }
}
