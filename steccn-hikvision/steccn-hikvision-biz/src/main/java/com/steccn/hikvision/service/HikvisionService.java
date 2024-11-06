package com.steccn.hikvision.service;

import com.steccn.common.dto.TupleDTO;
import com.steccn.hikvision.dto.SysHikEventDTO;
import com.steccn.hikvision.vo.CameraVO;
import com.steccn.hikvision.vo.HikCompanyVO;

import java.util.List;

public interface HikvisionService {

    /**
     * 获取监控点树-平铺结构
     *
     * @return
     */
    List<CameraVO> flatCameraList();

    /**
     * 获取监控点树
     *
     * @return
     */
    List<CameraVO> cameraSearchTree();


    /**
     * 获取监控点预览流
     *
     * @param cameraIndexCode
     * @param protocol
     * @return
     */
    String previewUrl(String cameraIndexCode, String protocol);

    /**
     * 获取海康威视单点登录token
     * @return
     */
    String ssoToken();

    /**
     * 获取dto f1 companyId f2 companyName
     * @param dto
     * @return
     */
    List<HikCompanyVO> getCompanyInfo(TupleDTO<String,String> dto);

    /**
     * 订阅事件
     * @param dto 事件订阅
     * @return
     */
    Boolean bookEvent(SysHikEventDTO dto);

    String weighRegUrl(String weighNo);
}
