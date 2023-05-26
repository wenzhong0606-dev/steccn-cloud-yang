package com.steccn.hikvision.controller;


import com.steccn.common.vo.R;

import com.steccn.hikvision.service.HikvisionService;
import com.steccn.hikvision.vo.CameraVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


import java.util.List;

import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/hikvision")
@RequiredArgsConstructor
public class HikvisionController {


    private final HikvisionService hikvisionService;


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

}
