package com.steccn.hikvision.service;

import com.steccn.hikvision.vo.CameraVO;

import java.util.List;

public interface HikvisionService {
    List<CameraVO> cameraSearchTree();
}
