package com.steccn.hikvision.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.dto.TupleDTO;
import com.steccn.hikvision.api.VideoApi;
import com.steccn.hikvision.dto.SysHikEventDTO;
import com.steccn.hikvision.entity.Cameras;
import com.steccn.hikvision.entity.HikCommonR;
import com.steccn.hikvision.entity.HikvisionR;
import com.steccn.hikvision.entity.PreviewURLs;
import com.steccn.hikvision.service.HikvisionService;
import com.steccn.hikvision.vo.CameraVO;
import com.steccn.hikvision.vo.HikCompanyVO;
import com.steccn.oa.constant.OaConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class HikvisionServiceImpl implements HikvisionService {

    @Override
    public List<CameraVO> flatCameraList(){
        Cameras cameras = new Cameras();
        cameras.setPageSize(1000);
        cameras.setPageNo(1);
        VideoApi videoApi = new VideoApi();
        String search = videoApi.search1(cameras);
        HikvisionR r = JSONObject.parseObject(search, HikvisionR.class);
        List<CameraVO> list = r.getData().getList();
        return list;
    }

    @Override
    public List<CameraVO> cameraSearchTree() {
        List<CameraVO> res = new ArrayList<>();
        Cameras cameras = new Cameras();
        cameras.setPageSize(1000);
        cameras.setPageNo(1);
        VideoApi videoApi = new VideoApi();
        String search = videoApi.search1(cameras);
        HikvisionR r = JSONObject.parseObject(search, HikvisionR.class);
        List<CameraVO> list = r.getData().getList();
        if (CollUtil.isNotEmpty(list)) {
            //处理树级结构数据
            res = convertToTree(list);
        }
        return res;
    }

    /**
     * 获取视频预览流
     * @param cameraIndexCode
     * @param protocol
     * @return
     */
    @Override
    public String previewUrl(String cameraIndexCode, String protocol) {
        VideoApi videoApi = new VideoApi();
        PreviewURLs previewURLs = new PreviewURLs();
        previewURLs.setCameraIndexCode(cameraIndexCode);
        previewURLs.setProtocol(protocol);
        previewURLs.setExpand("transcode=0");
        previewURLs.setStreamform("ps");
        String url = videoApi.P(previewURLs);
        return JSONObject.parseObject(url).toJSONString();
    }

    @Override
    public String ssoToken() {
        VideoApi videoApi = new VideoApi();
        Map<String,String> querys = Maps.newHashMap();
        querys.put("language", "zh_CN");
        querys.put("service", "http://192.168.200.100/portal/ui/index");
        String username = SecurityUtils.getUser().getUsername();
        querys.put("userCode", username);
        String result = videoApi.ssoLogin(querys);
        JSONObject jsonObject = JSONObject.parseObject(result);
        String token = jsonObject.getJSONObject("data").getString("token");
        return token;
    }

    @Override
    public List<HikCompanyVO> getCompanyInfo(TupleDTO<String, String> dto) {
        VideoApi videoApi = new VideoApi();
        Map<String,String> querys = Maps.newHashMap();
        querys.put("pageSize","20");
        querys.put("pageNo","1");
        if(StrUtil.isNotBlank(dto.getField1())){
            querys.put("companyId",dto.getField1());
        }
        if(StrUtil.isNotBlank(dto.getField2())){
            querys.put("companyName",dto.getField2());
        }
        if(StrUtil.isBlank(dto.getField1())&&StrUtil.isBlank(dto.getField2())){
            return Lists.newArrayList();
        }
        String result = videoApi.getCompanyInfo(querys);
        HikCommonR<HikCompanyVO> r = JSONObject.parseObject(result, HikCommonR.class);
        return r.getData().getList();
    }

    @Override
    public Boolean bookEvent(SysHikEventDTO dto) {
        VideoApi videoApi = new VideoApi();
        String s = videoApi.bookEvent(dto);
        HikCommonR r = JSONObject.parseObject(s, HikCommonR.class);
        if(r.getCode().equals(OaConstant.NO)){
            return true;
        }
        return false;
    }

    /**
     * 将 CameraVO 列表转换为树形结构
     * @param cameraList CameraVO 列表
     * @return 树形结构
     */
    public static List<CameraVO> convertToTree(List<CameraVO> cameraList) {
        long id = 1L;

        // 创建根节点
        CameraVO root = new CameraVO();
        root.setRegionPathName("/");
        root.setChildren(new ArrayList<>());

        // 创建 Map 存储所有节点，以便后续查找
        Map<String, CameraVO> map = new HashMap<>();
        map.put("/", root);

        // 遍历 CameraVO 列表，将每个节点添加到对应的父节点下
        for (CameraVO camera : cameraList) {
            camera.setId(id++);
            String pathName = camera.getRegionPathName();
            String[] pathArr = pathName.split("/");
            String parentPath = "/";
            CameraVO parent = root;
            for (String path : pathArr) {
                if (path.isEmpty()) {
                    continue;
                }
                parentPath += path + "/";
                if (map.containsKey(parentPath)) {
                    parent = map.get(parentPath);
                } else {
                    CameraVO parentNode = new CameraVO();
                    parentNode.setId(id++);
                    parentNode.setRegionPathName(parentPath);
                    parentNode.setRegionName(path);
                    parentNode.setName(path);
                    parentNode.setIndexCode(path);
                    parentNode.setChildren(new ArrayList<>());
                    parent.getChildren().add(parentNode);
                    map.put(parentPath, parentNode);
                    parent = parentNode;
                }
            }
            parent.getChildren().add(camera);
        }

        return root.getChildren();
    }


}
