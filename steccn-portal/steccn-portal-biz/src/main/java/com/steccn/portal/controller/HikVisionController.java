package com.steccn.portal.controller;

import com.steccn.common.vo.R;
import com.steccn.portal.service.HikVisionService;
import com.steccn.portal.vo.SteccnTokenVO;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 海康威视相关controller
 *
 * @author AndrewLuo
 * @date 2022/11/23
 */
@Api(tags = "海康对应controller")
@RestController
@RequestMapping("/portal/hik")
@Slf4j
@RequiredArgsConstructor
public class HikVisionController {
    private final HikVisionService hikVisionService;

    /**
     * 获取token
     * @param userCode
     * @return
     */
    @GetMapping("/getToken")
    public R<String> getToken(@RequestParam("userCode") String userCode) {
        return R.success(hikVisionService.getToken(userCode));
    }
}
