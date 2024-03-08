package com.steccn.ems.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.ems.dto.EpDiscountFactorPageDTO;
import com.steccn.ems.service.EpDiscountFactorService;
import com.steccn.ems.vo.EpDiscountFactorVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yequan
 * @desc 折标系数
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/ems/discount/factor")
@Slf4j
public class EpDiscountFactorController {

    private final EpDiscountFactorService epDiscountFactorService;

    @PostMapping("/page")
    @ApiOperation(value = "查询折标系数分页列表")
    public R<Page<EpDiscountFactorVO>> page(@RequestBody EpDiscountFactorPageDTO dto) {
        return R.success(epDiscountFactorService.getPage(dto));
    }
}
