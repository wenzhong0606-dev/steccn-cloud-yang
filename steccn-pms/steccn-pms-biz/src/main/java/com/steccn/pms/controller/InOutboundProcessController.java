package com.steccn.pms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.PmsProcessInStationDTO;
import com.steccn.pms.dto.PmsProcessOutStationPageDTO;
import com.steccn.pms.service.PmsProcessInStationService;
import com.steccn.pms.service.PmsProcessOutStationService;
import com.steccn.pms.vo.PmsProcessInStationVO;
import com.steccn.pms.vo.PmsProcessOutStationVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 进出站工序控制器
 *
 * @author Mongknife
 * @date 2022/10/12
 */

@RestController
@RequestMapping("/pms/in/outbound")
@RequiredArgsConstructor
@Slf4j
public class InOutboundProcessController {

    private final PmsProcessInStationService inStationService;

    private final PmsProcessOutStationService outStationService;

    @ApiOperation("工艺信息-进站分页列表")
    @PostMapping("/in/page")
    public R<Page<PmsProcessInStationVO>> inPage(@RequestBody PmsProcessInStationDTO dto) {
        return R.success(inStationService.inPage(dto));
    }

    @ApiOperation("工艺信息-出站分页列表")
    @PostMapping("/out/page")
    public R<Page<PmsProcessOutStationVO>> outPage(@RequestBody PmsProcessOutStationPageDTO dto){
        return R.success(outStationService.outPage(dto));
    }

}


