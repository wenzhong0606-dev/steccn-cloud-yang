package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import com.alibaba.excel.util.MapUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.SteccnChartException;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.dto.ParentTupleDTO;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.CollectorsUtil;
import com.steccn.common.util.EpTreeGraphUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.EpTreeGraphDataVO;
import com.steccn.common.vo.EpTreeGraphLinkVO;
import com.steccn.common.vo.EpTreeGraphVO;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.EpEnergyType;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.dto.EpEnergyFlowDTO;
import com.steccn.ems.dto.EpEnergyFlowLeaderDTO;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.*;
import com.steccn.ems.util.DownloadUtil;
import com.steccn.ems.vo.*;
import com.steccn.pms.vo.ProducePassRateVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.vo.QtIQCAcceptabilityDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 能源流向impl
 *
 * @author AndrewLuo
 * @date 2022/12/12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EpEnergyFlowServiceImpl implements EpEnergyFlowService {
    private final EpConsumePartitionChartService epConsumePartitionChartService;

    private final EpPartitionService epPartitionService;

    private final EpEnergyTypeService epEnergyTypeService;

    /**
     * 获取能源流向详情数据
     * @param dto
     * @return
     */
    @Override
    public EpConsumeDetailVO getDetails(EpEnergyFlowDTO dto) {
        EpConsumeDetailVO result = new EpConsumeDetailVO();
        List<EpPartition> epPartitionList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, dto.getEnergyTypeId()));
        if(CollUtil.isEmpty(epPartitionList)){
            log.debug("节点id为空");
            return result;
        }
        List<Long> partitionIdList = Lists.newArrayList();
        epPartitionList.forEach(tmp -> {
            partitionIdList.add(tmp.getId());
        });
        //拿到该能源类型下所有的节点
        EpTableDTO tableDTO = new EpTableDTO();
        tableDTO.setStart(dto.getDateStart());
        tableDTO.setEnd(dto.getDateEnd());
        tableDTO.setEnergyType(dto.getEnergyTypeId());
        tableDTO.setPartitionIdList(partitionIdList);
        //拿到厚度
        List<BaseSelectVO<BigDecimal>> thick = epConsumePartitionChartService.thick(tableDTO);
        if(CollUtil.isEmpty(thick)){
            log.debug("厚度为空");
            return result;
        }
        List<BigDecimal> thickList = Lists.newArrayList();
        thick.forEach(tmp -> {
            thickList.add(tmp.getId());
        });
        tableDTO.setThick(thickList);
        if(dto.getCurrent()!=null){
            tableDTO.setCurrent(dto.getCurrent());
        }
        if(dto.getSize()!=null){
            tableDTO.setSize(dto.getSize());
        }
        EpConsumeDetailVO vo = epConsumePartitionChartService.consumePartitionContrastDetail(tableDTO);
        result = vo;
        return result;
    }

    /**
     * 生成树状图表数据
     * @param dto
     * @return
     */
    @Override
    public EpTreeGraphVO generateCharts(EpEnergyFlowDTO dto) {
        EpTreeGraphVO result = new EpTreeGraphVO();
        dto.setCurrent(1);
        dto.setSize(99999);
        //该情况下必定有数据
        EpConsumeDetailVO data = this.getDetails(dto);
        //根据节点数据拿到拿到对应的数据并进行加总
        List<String> headData = data.getHeadData();
        List<EpConsumeDetailRowVO> records = data.getDataList().getRecords();
        //根据officeId分组算值
        Map<String,BigDecimal> resultMap = new LinkedHashMap<>();
        for(int i = 0; i < headData.size(); i++){
            BigDecimal numRes = BigDecimal.ZERO;
            //对每一行进行遍历,加总所有的值
            for(EpConsumeDetailRowVO tmp:records){
                List<String> tmpData = tmp.getConsumeList();
                if(tmpData.get(i)!=null){
                    BigDecimal tmpNum = new BigDecimal(tmpData.get(i));
                    numRes = BigDecimalUtil.add(numRes,tmpNum);
                }
            }
            resultMap.put(headData.get(i),numRes);
        }
//        EpPartitionNameListVO epEnergyTypeVOList = epPartitionService.getEpEnergyTypeVOList(dto.getEnergyTypeId());
//        List<EpPartitionVO> epPartitionVOList = epEnergyTypeVOList.getEpPartitionVOList();
        //一把拉出所有的树状数据
        List<EpPartition> epPartitionList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getType, dto.getEnergyTypeId()));
        //根据parentId排序一下
        epPartitionList = epPartitionList.stream().sorted(Comparator.comparing(EpPartition::getOfficeName, Comparator.nullsFirst(String::compareTo))).collect(Collectors.toList());
        List<EpPartition> epPartitionList1 = epPartitionList;
        //组装数据
        List<EpTreeGraphDataVO> resultData = Lists.newArrayList();
        List<EpTreeGraphLinkVO> links = Lists.newArrayList();
        epPartitionList.forEach(tmp -> {
            EpTreeGraphDataVO dataTmp = new EpTreeGraphDataVO();
            if(tmp.getParentId() != null){
                dataTmp.setName(tmp.getOfficeName());
                resultData.add(dataTmp);
                EpTreeGraphLinkVO linkTmp = new EpTreeGraphLinkVO();
                linkTmp.setTarget(tmp.getOfficeName());
                //获取parentId
                List<EpPartition> epPartitionTmp = epPartitionList1.stream().filter(t->t.getId().equals(tmp.getParentId())).collect(Collectors.toList());
                if(CollUtil.isNotEmpty(epPartitionTmp)){
                    linkTmp.setSource(epPartitionTmp.get(0).getOfficeName());
                }else {
                    log.debug(tmp.getOfficeName() + "不存在父级节点");
                }
                if(resultMap.containsKey(tmp.getOfficeName())){
                    linkTmp.setValue(resultMap.get(tmp.getOfficeName()));
                }else {
                    linkTmp.setValue(BigDecimal.ZERO);
                }
                links.add(linkTmp);
            }else {
                dataTmp.setName(tmp.getOfficeName());
                resultData.add(dataTmp);
            }
        });
        result.setData(resultData);
        result.setLinks(links);
        //找到根节点
//        EpPartition rootPartition = epPartitionService.getOne(new LambdaQueryWrapper<EpPartition>()
//                .eq(EpPartition::getType,dto.getEnergyTypeId())
//                .isNull(EpPartition::getParentId),false);
//        ParentTupleDTO<String, BigDecimal> root = new ParentTupleDTO<>();
//        root.setField1(rootPartition.getOfficeName());
//        if(resultMap.containsKey(rootPartition.getOfficeName())){
//            root.setField2(resultMap.get(rootPartition.getOfficeName()));
//        }else {
//            root.setField2(BigDecimal.ZERO);
//        }
//        List<ParentTupleDTO<String, BigDecimal>> resultList = new ArrayList<>();
//        List<EpPartition> partitionResult = new ArrayList<>();
//        //双循环获取树状数据
//        for(EpPartition partitionTmp:epPartitionList){
//            ParentTupleDTO<String, BigDecimal> dataPartitionTmp = new ParentTupleDTO<>();
//            if(partitionTmp.getParentId()==null){
//                ParentTupleDTO<String, BigDecimal> rootTmp = new ParentTupleDTO<>();
//                rootTmp.setField1(partitionTmp.getOfficeName());
//                if(resultMap.containsKey(partitionTmp.getOfficeName())){
//                    rootTmp.setField2(resultMap.get(partitionTmp.getOfficeName()));
//                }else {
//                    rootTmp.setField2(BigDecimal.ZERO);
//                }
//                resultList.add(rootTmp);
//                partitionResult.add(partitionTmp);
//            }
//            for(EpPartition child:epPartitionList){
//                ParentTupleDTO<String, BigDecimal> childTmp = new ParentTupleDTO<>();
//                if(child.getParentId().equals(partitionTmp.getId())){
//                    List<EpPartition> children = partitionTmp.getChildren();
//                    List<ParentTupleDTO<String, BigDecimal>> childrenResult = new ArrayList<>();
//                    if(children == null){
//                        children = new ArrayList<>();
//                        childrenResult = new ArrayList<>();
//                        partitionTmp.setChildren(children);
//                        dataPartitionTmp.setChildren(childrenResult);
//                    }
//                    children.add(child);
//                    childTmp.setField1(child.getOfficeName());
//                    if(resultMap.containsKey(child.getOfficeName())){
//                        childTmp.setField2(resultMap.get(child.getOfficeName()));
//                    }else {
//                        childTmp.setField2(BigDecimal.ZERO);
//                    }
//                    childrenResult.add(childTmp);
//                }
//            }
//        }
//        root = resultList.get(0);
        return result;
    }

    /**
     * 生成领导驾驶舱数据
     * @param dto
     * @return
     */
    @Override
    public EpTreeGraphVO generateLeaderCharts(EpEnergyFlowLeaderDTO dto) {
        EpEnergyFlowDTO tmpDTO = new EpEnergyFlowDTO();
        tmpDTO.setEnergyTypeId(EpManagerConstant.EnergyPartitioningTree.POWER_CONSUMPTION_PARTITION.getEnergyType());
        tmpDTO.setDateEnd(dto.getDateStart());
        tmpDTO.setDateStart(dto.getDateStart().minusDays(6));
        EpTreeGraphVO epTreeGraphVO = generateCharts(tmpDTO);
        return epTreeGraphVO;
    }

    /**
     * 下载数据
     * @param dto
     * @param response
     */
    @SneakyThrows
    @Override
    public void downloadFlow(EpEnergyFlowDTO dto, HttpServletResponse response) {
        dto.setCurrent(1);
        dto.setSize(99999);
        EpConsumeDetailVO detail = getDetails(dto);
        EpEnergyType energyType = epEnergyTypeService.getOne(new LambdaQueryWrapper<EpEnergyType>().eq(EpEnergyType::getId, dto.getEnergyTypeId()), false);
        Map<String, Object> map = MapUtils.newHashMap();
        map.put("energyType",energyType.getName());
        map.put("dateStart",LocalDateUtil.localDateFormat(dto.getDateStart(), DatePattern.NORM_DATE_PATTERN));
        map.put("dateEnd",LocalDateUtil.localDateFormat(dto.getDateEnd(),DatePattern.NORM_DATE_PATTERN));
        String excelName = "能耗流向(" + energyType.getName() +  ")_"
                + LocalDateUtil.localDateFormat(dto.getDateStart(), DatePattern.NORM_DATE_PATTERN) + "_"
                + LocalDateUtil.localDateFormat(dto.getDateEnd(),DatePattern.NORM_DATE_PATTERN);
        DownloadUtil.downloadEnergyTemplate(response, EpManagerConstant.EP_DOWNLOAD_MODEL_NAME_NYLX,excelName,map,detail,dto.getBase64());
    }

}
