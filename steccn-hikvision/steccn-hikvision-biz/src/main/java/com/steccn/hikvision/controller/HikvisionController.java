package com.steccn.hikvision.controller;


import com.steccn.hikvision.api.VideoApi;
import com.steccn.hikvision.entity.Cameras;
import com.steccn.hikvision.entity.PlaybackURLs;
import com.steccn.hikvision.entity.PreviewURLs;
import com.steccn.hikvision.service.HikvisionService;
import com.steccn.hikvision.vo.CameraVO;
import lombok.ToString;
//import org.springframework.web.bind.annotation.RestController;


import java.io.IOException;
import java.util.List;
//@RestController
//@RequestMapping("/hikvision")
public class HikvisionController {


    private HikvisionService hikvisionService;

//
//    /**
//     * 监控点树
//     * @return
//     */
//    @GetMapping("/camera/search/tree")
//    public R<List<CameraVO>> cameraSearchTree(){
//        return R.success(hikvisionService.cameraSearchTree());
//    }
//
//
//    @GetMapping("/test1")
//    public String test1(HttpServletResponse response){
//
//        VideoApi videoApi = new VideoApi();
//        Cameras cameras = new Cameras();
//        cameras.setPageNo(1);
//        cameras.setPageSize(1000);
//        String search = videoApi.search1(cameras);
//        JSONObject dizhi = JSONObject.parseObject(search);
//        System.out.println(dizhi);
////            new RTSPToHTTP().haihai();
//        return dizhi.toJSONString();
//    }
//
//
//
//    @GetMapping("/test")
//    public String test(HttpServletResponse response){
//
//        VideoApi videoApi = new VideoApi();
//        Cameras cameras = new Cameras();
//        cameras.setPageNo(1);
//        cameras.setPageSize(1000);
//        String search = videoApi.search(cameras);
//        JSONObject dizhi = JSONObject.parseObject(search);
//        System.out.println(dizhi);
////            new RTSPToHTTP().haihai();
//        return dizhi.toJSONString();
//    }
//
//
//    @GetMapping("/index/code")
//    public String indexCode(HttpServletResponse response,String cameraIndexCode){
//
//        VideoApi videoApi = new VideoApi();
//        PreviewURLs previewURLs = new PreviewURLs();
//        previewURLs.setCameraIndexCode(cameraIndexCode);
//        String indexCode = videoApi.indexCode(previewURLs);
//        JSONObject test = JSONObject.parseObject(indexCode);
//        System.out.println(test);
////            new RTSPToHTTP().haihai();
//        return test.toJSONString();
//    }
//
//
//
//
//    @GetMapping("/shangsheng")
//    public String shangsheng(HttpServletResponse response, String cameraIndexCode,String protocol) throws IOException, InterruptedException {
//        response.setHeader("Access-Control-Allow-Origin", "*");
//
////        //vlc播放器可以正常播放
////        VideoApi videoApi = new VideoApi();
////        PreviewURLs previewURLs = new PreviewURLs();
//
////        previewURLs.setStreamType(0);
////        previewURLs.setProtocol("rtsp");
////        previewURLs.setTransmode(1);
////        previewURLs.setExpand("streamform=ps");
////        previewURLs.setStreamform("rtp");
////        String url = videoApi.P(previewURLs);
////        JSONObject dizhi = JSONObject.parseObject(url);
////        System.out.println(dizhi);
//            VideoApi videoApi = new VideoApi();
//            PreviewURLs previewURLs = new PreviewURLs();
////            previewURLs.setCameraIndexCode("5058d89c344d43b98f187419dee2f47d");
//        previewURLs.setCameraIndexCode(cameraIndexCode);
//            previewURLs.setStreamType(0);
////            previewURLs.setProtocol("hls");
//        previewURLs.setProtocol(protocol);
//            previewURLs.setTransmode(1);
//            previewURLs.setExpand("transcode=0");
//            previewURLs.setStreamform("ps");
//            String url = videoApi.P(previewURLs);
//            JSONObject dizhi = JSONObject.parseObject(url);
////            VideoApi videoApi = new VideoApi();
////        PreviewURLs previewURLs = new PreviewURLs();
////        previewURLs.setCameraIndexCode(cameraIndexCode);
////        previewURLs.setStreamType(0);
////        previewURLs.setProtocol("ws");
////        previewURLs.setTransmode(1);
////        previewURLs.setExpand("transcode=0");
////        previewURLs.setStreamform("ps");
////        String url = videoApi.P(previewURLs);
////        JSONObject dizhi = JSONObject.parseObject(url);
//        System.out.println(dizhi);
////            new RTSPToHTTP().haihai();
//        return dizhi.toJSONString();
//
//    }
//
//
//    @GetMapping("/hf")
//    public String hf(HttpServletResponse response,String cameraIndexCode, String startTime,String endTime) throws IOException, InterruptedException {
//        response.setHeader("Access-Control-Allow-Origin", "*");
//
//        //vlc播放器可以正常播放
//        VideoApi videoApi = new VideoApi();
//        PlaybackURLs playbackURLs = new PlaybackURLs();
//        playbackURLs.setCameraIndexCode(cameraIndexCode);
//        playbackURLs.setRecordLocation("0");
//        playbackURLs.setProtocol("rtsp");
//        playbackURLs.setTransmode(0);
//        playbackURLs.setBeginTime(startTime+"T00:00:00.000+08:00");
//        playbackURLs.setEndTime(endTime+"T00:00:00.000+08:00");
//        playbackURLs.setExpand("streamform=rtp");
//        playbackURLs.setStreamform("rtp");
//        playbackURLs.setLockType(0);
//        String url = videoApi.back(playbackURLs);
//        JSONObject dizhi = JSONObject.parseObject(url);
//        System.out.println(dizhi);
////        VideoApi videoApi = new VideoApi();
////        PreviewURLs previewURLs = new PreviewURLs();
////        previewURLs.setCameraIndexCode("fde2cd1293da44018610e93d7970c065");
////        previewURLs.setStreamType(0);
////        previewURLs.setProtocol("hls");
////        previewURLs.setTransmode(1);
////        previewURLs.setExpand("transcode=0");
////        previewURLs.setStreamform("ps");
////        String url = videoApi.P(previewURLs);
////        JSONObject dizhi = JSONObject.parseObject(url);
//        return dizhi.toJSONString();
//    }
//
//
//
//    @GetMapping("/wy")
//    public void wy(HttpServletResponse response) {
//        response.setHeader("Access-Control-Allow-Origin", "*");
//
//        //vlc播放器可以正常播放
//        VideoApi videoApi = new VideoApi();
//        Cameras cameras = new Cameras();
//        cameras.setPageSize(1000);
//        cameras.setPageNo(1);
//        String url = videoApi.C(cameras);
//        JSONObject dizhi = JSONObject.parseObject(url);
//        System.out.println(dizhi);
//
//    }

}
