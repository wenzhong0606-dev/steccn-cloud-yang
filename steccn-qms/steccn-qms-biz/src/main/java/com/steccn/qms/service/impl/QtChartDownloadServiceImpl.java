package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.EnumUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.data.ImageData;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.steccn.common.vo.R;
import com.steccn.pms.dto.ProducePassRateDTO;
import com.steccn.pms.feign.RemotePassRateFeignApi;
import com.steccn.pms.vo.ProducePassRateVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.dto.QtChartParamDTO;
import com.steccn.qms.dto.QtChartYieldDownloadDTO;
import com.steccn.qms.service.QtChartDownloadService;
import com.steccn.qms.service.QtChartGenerateService;
import com.steccn.qms.util.DownloadModelUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 下载类
 *
 * @author AndrewLuo
 * @date 2022/11/09
 */
@Service
@RequiredArgsConstructor
public class QtChartDownloadServiceImpl implements QtChartDownloadService {
    private final RemotePassRateFeignApi remotePassRateFeignApi;

    private final QtChartGenerateService qtChartGenerateService;

    @Override
    public void downloadSubstrate(HttpServletResponse response, QtChartYieldDownloadDTO dto) throws IOException {
        Map<String, Object> map = MapUtils.newHashMap();
        //筛选dto数据
        if(StrUtil.isBlank(dto.getWorkOrderName())){
            dto.setWorkOrderName("");
        }
        if(StrUtil.isBlank(dto.getProductWorkShopName())){
            dto.setProductWorkShopName("");
        }
        if(StrUtil.isBlank(dto.getProductLineName())){
            dto.setProductLineName("");
        }
        //根据自己的模版put数据
        map.put("workOrderNameCondition", dto.getWorkOrderName());
        map.put("workShopCondition", dto.getProductWorkShopName());
        map.put("workLineCondition",dto.getProductLineName());
        map.put("dateStart",dto.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        map.put("dateEnd",dto.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())){
            map.put("workOrderCondition",QtManagerConstant.QtChartCompareEnum.WORK.getName());
        }
        if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())){
            map.put("workOrderCondition",QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getName());
        }
        if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode())){
            map.put("workOrderCondition",QtManagerConstant.QtChartCompareEnum.WORKSHOP.getName());
        }
        //对全量数据表进行筛选
        ProducePassRateDTO pmsDTO =new ProducePassRateDTO();
        pmsDTO.setBeginDate(dto.getStartTime());
        pmsDTO.setEndDate(dto.getEndTime().plusDays(1));
        R<List<ProducePassRateVO>> rResult = remotePassRateFeignApi.producePassRateGroup(pmsDTO);
        List<ProducePassRateVO> data = new ArrayList<>();
        if(CollUtil.isNotEmpty(rResult.getData())){
            data = rResult.getData();
            QtChartParamDTO param = new QtChartParamDTO();
            BeanUtils.copyProperties(dto,param);
            //过滤筛选数据
            data = qtChartGenerateService.rListFilter(data,param);
            //过滤图表数据
            if(CollUtil.isNotEmpty(data)&&CollUtil.isNotEmpty(dto.getFilters())){
                if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())){
                    data.stream().filter(s->dto.getFilters().contains(s.getClassesName())).collect(Collectors.toList());
                }
                if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())){
                    data.stream().filter(s->dto.getFilters().contains(s.getProductLineName())).collect(Collectors.toList());
                }
                if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode())){
                    data.stream().filter(s->dto.getFilters().contains(s.getWorkshopName())).collect(Collectors.toList());
                }
            }
        }
        //封装的下载
        this.downloadTemplate(response,QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_JPCP,QtManagerConstant.QT_CHART_JPCP_NAME,map,data,dto.getBase64());
    }

    @Override
    public void downloadDeepProcess(HttpServletResponse response) throws IOException{
        //1、从服务器获取模板数据
        InputStream modelIn = DownloadModelUtil.getModelInputStream(QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_JPCP);
        //2、设置下载头
        this.setDownloadHeader(response,QtManagerConstant.QT_CHART_DEEP_PROCESS_NAME);
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).autoCloseStream(Boolean.FALSE).withTemplate(modelIn).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            Map<String, Object> map = MapUtils.newHashMap();
            map.put("workOrderNameCondition", "班次");
            map.put("workShopCondition", "基片一车间");
            ImageData imageData = new ImageData();
            imageData.setImageType(ImageData.ImageType.PICTURE_TYPE_JPEG);
            map.put("img",imageData);
            excelWriter.fill(map, writeSheet);
        }
    }

    /**
     * 基片缺陷项分布
     * @param response
     * @throws IOException
     */
    @Override
    public void downloadDefectSubstrate(HttpServletResponse response) throws IOException {
        //1、从服务器获取模板数据
        InputStream modelIn = DownloadModelUtil.getModelInputStream(QtManagerConstant.QT_CHART_DEFECT_JPFB);
        //2、设置下载头
        this.setDownloadHeader(response,QtManagerConstant.QT_CHART_DEFECT_JPFB_NAME);
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).autoCloseStream(Boolean.FALSE).withTemplate(modelIn).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            // 这里注意 入参用了forceNewRow 代表在写入list的时候不管list下面有没有空行 都会创建一行，然后下面的数据往后移动。默认 是false，会直接使用下一行，如果没有则创建。
            // forceNewRow 如果设置了true,有个缺点 就是他会把所有的数据都放到内存了，所以慎用
            // 简单的说 如果你的模板有list,且list不是最后一行，下面还有数据需要填充 就必须设置 forceNewRow=true 但是这个就会把所有数据放到内存 会很耗内存
            // 如果数据量大 list不是最后一行 参照下一个
//            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
//            excelWriter.fill(data(), fillConfig, writeSheet);
//            excelWriter.fill(data(), fillConfig, writeSheet);
            Map<String, Object> map = MapUtils.newHashMap();
            map.put("workOrderCondition", "班次");
            map.put("workShopCondition", "基片一车间");
            ImageData imageData = new ImageData();
//            imageData.setImage();
            imageData.setImageType(ImageData.ImageType.PICTURE_TYPE_JPEG);
            map.put("img",imageData);
            excelWriter.fill(map, writeSheet);
        }

    }

    /**
     * 对下载进行封装
     * @param response 响应
     * @param modelName 服务器上的模版名字
     * @param downloadName 下载文件的名字
     * @param fixMap 固定值的map
     * @param dataList 数据list
     * @param base64 图片base64编码
     * @throws IOException
     */
    public void downloadTemplate(HttpServletResponse response,String modelName,String downloadName, Map<String, Object> fixMap, List dataList,String base64) throws IOException {
        //1、从服务器获取模板数据
        InputStream modelIn = DownloadModelUtil.getModelInputStream(modelName);
        //2、设置下载头
        this.setDownloadHeader(response,downloadName);
        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).autoCloseStream(Boolean.FALSE).withTemplate(modelIn).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            // 这里注意 入参用了forceNewRow 代表在写入list的时候不管list下面有没有空行 都会创建一行，然后下面的数据往后移动。默认 是false，会直接使用下一行，如果没有则创建。
            // forceNewRow 如果设置了true,有个缺点 就是他会把所有的数据都放到内存了，所以慎用
            // 简单的说 如果你的模板有list,且list不是最后一行，下面还有数据需要填充 就必须设置 forceNewRow=true 但是这个就会把所有数据放到内存 会很耗内存
            // 如果数据量大 list不是最后一行 参照下一个
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.TRUE).build();
            excelWriter.fill(dataList, fillConfig, writeSheet);
            excelWriter.fill(dataList, fillConfig, writeSheet);
            ImageData imageData = new ImageData();
            imageData.setImage(Base64.getDecoder().decode(base64));
            imageData.setImageType(ImageData.ImageType.PICTURE_TYPE_JPEG);
            fixMap.put("img",imageData);
            excelWriter.fill(fixMap, writeSheet);
        }
    }

    /**
     * 下载excel Response封装
     * @param response
     * @return
     */
    public void setDownloadHeader(HttpServletResponse response,String name) throws UnsupportedEncodingException {
        response.setContentType(QtManagerConstant.QT_CHART_EXCEL_CONTENT_TYPE);
        response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode(name, QtManagerConstant.QT_CHART_EXCEL_ENCODE).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
    }

    private static InputStream base2InputStream(String base64string) {
        ByteArrayInputStream stream = null;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            byte[] bytes1 = decoder.decodeBuffer(base64string);
            stream = new ByteArrayInputStream(bytes1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stream;
    }
}
