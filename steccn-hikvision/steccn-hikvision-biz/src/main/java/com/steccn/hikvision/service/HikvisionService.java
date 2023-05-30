package com.steccn.hikvision.service;

import com.steccn.hikvision.vo.CameraVO;

import java.util.List;

public interface HikvisionService {


    /**
     * 获取监控点树
     * @return
     */
    List<CameraVO> cameraSearchTree();


    /**
     * 获取监控点预览流
     * @param cameraIndexCode
     * @param protocol
     * @return
     */
    String previewUrl(String cameraIndexCode, String protocol);
}
