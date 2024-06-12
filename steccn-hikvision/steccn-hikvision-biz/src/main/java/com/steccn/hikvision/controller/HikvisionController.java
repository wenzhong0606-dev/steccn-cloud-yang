package com.steccn.hikvision.controller;


import com.steccn.common.dto.TupleDTO;
import com.steccn.common.vo.R;

import com.steccn.hikvision.dto.SysHikEventDTO;
import com.steccn.hikvision.service.HikvisionService;
import com.steccn.hikvision.vo.CameraVO;
import com.steccn.hikvision.vo.HikCompanyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.List;

import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/hikvision")
@RequiredArgsConstructor
public class HikvisionController {


    private final HikvisionService hikvisionService;

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
}
