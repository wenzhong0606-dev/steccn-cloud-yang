package com.steccn.hikvision.controller;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;

import com.steccn.hikvision.dto.SysHikEventDTO;
import com.steccn.hikvision.service.HikvisionService;
import com.steccn.hikvision.vo.CameraVO;
import com.steccn.hikvision.vo.HikCompanyVO;
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteSysOauthClientDetailFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/hikvision")
@RequiredArgsConstructor
public class HikvisionController {


    private final HikvisionService hikvisionService;

    private final RemoteSysOauthClientDetailFeignApi remoteSysOauthClientDetailFeignApi;

    @GetMapping("/camera/search/flat")
    public R<List<CameraVO>> flatCameraList() {
        return R.success(hikvisionService.flatCameraList());
    }
    /**
     * 监控点树
     *
     * @return
     */
    @GetMapping("/camera/search/tree")
    public R<List<CameraVO>> cameraSearchTree() {
        return R.success(hikvisionService.cameraSearchTree());
    }

    /**
     * 获取监控点预览取流url
     *
     * @param cameraIndexCode 监控点唯一标识
     * @param protocol        取流协议
     * @return
     */
    @GetMapping("/cameras/preview/url")
    public String previewUrl(@RequestParam("cameraIndexCode") String cameraIndexCode, @RequestParam("protocol") String protocol) {
        return hikvisionService.previewUrl(cameraIndexCode, protocol);
    }

    /**
     * 获取单点登录token
     * @return
     */
    @GetMapping("/sso/login")
    public R<String> ssoLogin() {
        return R.success(hikvisionService.ssoToken());
    }

    /**
     * 获取单点登录token
     * @return
     */
    @PostMapping("/bookEvent")
    public R<Boolean> bookEvent(@RequestBody SysHikEventDTO dto) {
        return R.success(hikvisionService.bookEvent(dto));
    }

    @PostMapping("/company")
    public R<List<HikCompanyVO>> hikCompanyInfo(@RequestBody TupleDTO<String,String> dto) {
        return R.success(hikvisionService.getCompanyInfo(dto));
    }

    @GetMapping("/carNum")
    public R<Integer> carNum(HttpServletRequest request) {
        validRequestHeader(request);
        return R.success(hikvisionService.carNum());
    }

    /**
     * 校验请求头信息
     *
     * @param request 请求
     */
    private void validRequestHeader(HttpServletRequest request) {
        List<String> pwdSet = FeignOptional.of(() -> remoteSysOauthClientDetailFeignApi.clientEncodePassword(), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        String clientId = request.getHeader(OaConstant.CLIENT_ID);
        Assert.isTrue(StrUtil.isNotBlank(clientId), "请求头中必须携带client-id");
        Assert.isTrue(pwdSet.contains(clientId), "非法访问，请携带正确的client-id");
    }
}
