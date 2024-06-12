package com.steccn.portal.controller;

import com.steccn.common.vo.R;
import com.steccn.portal.dto.CheckCodeGenerateDTO;
import com.steccn.portal.service.checkcode.CheckCodeService;
import com.steccn.portal.vo.CheckCodeGenerateVO;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author yequan
 * @desc 验证码服务
 */
@RestController
@RequestMapping("/portal/check/code")
@Slf4j
@RequiredArgsConstructor
public class CheckCodeController {

    private final CheckCodeService picCheckCodeService;

    @ApiOperation(value="生成验证信息", notes="生成验证信息")
    @PostMapping(value = "/generate")
    public R<CheckCodeGenerateVO> generatePicCheckCode(CheckCodeGenerateDTO dto){
        return R.success(picCheckCodeService.generate(dto));
    }

    @ApiOperation(value="校验", notes="校验")
    @GetMapping(value = "/verify")
    public Boolean verify(@RequestParam("key") String key, @RequestParam("code") String code) {
        Boolean isSuccess = picCheckCodeService.verify(key, code);
        return isSuccess;
    }
}
