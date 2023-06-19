package com.steccn.ems.scheduled;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.SteccnChartException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.RegularUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.EpChartUnitConsumeAlarmVO;
import com.steccn.ems.domain.EpPartition;
import com.steccn.ems.domain.EpProductUnitConsumeStandard;
import com.steccn.ems.domain.EpProductUnitConsumeStandardUser;
import com.steccn.ems.domain.EpUnitConsumeAlarm;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.service.*;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.websocket.constant.WebsocketConstant;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenhuan
 * @date 2022/12/26 17:33
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class UnitConsumeAlarmScheduledTask {

    private final EpConsumePartitionChartService epConsumePartitionChartService;

    private final EpPartitionService partitionService;

    private final EpUnitConsumeAlarmService epUnitConsumeAlarmService;

    private final RemoteDictFeignApi remoteDictFeignApi;

    private final EpProductUnitConsumeStandardUserService epProductUnitConsumeStandardUserService;

    private final EpProductUnitConsumeStandardService epProductUnitConsumeStandardService;

    private final RemoteMsgFeignApi remoteMsgFeignApi;

    @XxlJob("unitConsumeAlarm")
    public void execute() {
        List<EpChartUnitConsumeAlarmVO> consumeAlarmVOList = new ArrayList<>();
        List<SysDictVO> thickVOList = remoteDictFeignApi.listByType( "product_thickness_range").getData();
        List<BigDecimal> thickList = new ArrayList<>();
        for (SysDictVO sysDictVO : thickVOList) {
            thickList.add(RegularUtil.extractValue(sysDictVO.getCode(), null, (x) -> new BigDecimal(x)));
        }
        thickList.add(BigDecimalUtil.MINUS_ONE);
        for (int i = 1; i < 6; i++) {
            try {
                if (i == 1) {
                    for (BigDecimal thick : thickList) {
                        EpTableDTO epTableSubDTO = getEpTableDTO(i,thick,"基片制造部");
                        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> subVO = epConsumePartitionChartService.unitConsumePartitionContrastChart(epTableSubDTO);
                        consumeAlarmVOList.addAll(subVO.getAlarmList());
                        EpTableDTO epTableDeepDTO = getEpTableDTO(i,thick,"深加工制造部");
                        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> deepVO = epConsumePartitionChartService.unitConsumePartitionContrastChart(epTableDeepDTO);
                        consumeAlarmVOList.addAll(deepVO.getAlarmList());
                    }
                }
                if (i == 2) {
                    EpTableDTO epTableSubDTO = getEpTableDTO(i,null,"基片制造部");
                    EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> subVO = epConsumePartitionChartService.unitConsumePartitionContrastChart(epTableSubDTO);
                    consumeAlarmVOList.addAll(subVO.getAlarmList());
                    EpTableDTO epTableDeepDTO = getEpTableDTO(i,null,"深加工制造部");
                    EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> deepVO = epConsumePartitionChartService.unitConsumePartitionContrastChart(epTableDeepDTO);
                    consumeAlarmVOList.addAll(deepVO.getAlarmList());
                }
                if (i == 3){
                    EpTableDTO epTableDeepDTO = getEpTableDTO(i,null,"基片制造部");
                    EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> deepVO = epConsumePartitionChartService.unitConsumePartitionContrastChart(epTableDeepDTO);
                    consumeAlarmVOList.addAll(deepVO.getAlarmList());
                }
                if (i == 4 || i == 5) {
                    for (BigDecimal thick : thickList) {
                        EpTableDTO epTableSubDTO = getEpTableDTO(i,thick,"基片制造部");
                        EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> subVO = epConsumePartitionChartService.unitConsumePartitionContrastChart(epTableSubDTO);
                        consumeAlarmVOList.addAll(subVO.getAlarmList());
                    }
                }
            } catch (SteccnChartException e) {
                continue;
            }
        }


        List<EpUnitConsumeAlarm> epUnitConsumeAlarmList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(consumeAlarmVOList)) {
            for (EpChartUnitConsumeAlarmVO consumeAlarmVO : consumeAlarmVOList) {
                EpUnitConsumeAlarm epUnitConsumeAlarm = new EpUnitConsumeAlarm();
                BeanUtils.copyProperties(consumeAlarmVO, epUnitConsumeAlarm);
                epUnitConsumeAlarm.setAlarmDate(consumeAlarmVO.getAlarmDateTime());
                epUnitConsumeAlarm.setAlarmType(0);
                epUnitConsumeAlarm.setEnergyType(consumeAlarmVO.getEnergyType());
                String dateString = LocalDateUtil.localDateFormat(consumeAlarmVO.getAlarmDateTime().toLocalDate(), DatePattern.NORM_DATE_PATTERN);
                String content = dateString + "日单耗" + consumeAlarmVO.getCurrentValue() + "," + "超过内部标准" + consumeAlarmVO.getStandardValue();
                epUnitConsumeAlarm.setContent(content);
                epUnitConsumeAlarmList.add(epUnitConsumeAlarm);
                //拿到消息发送人
                if(consumeAlarmVO.getStandardId()!=null){
                    List<EpProductUnitConsumeStandardUser> userList = epProductUnitConsumeStandardUserService.list(new LambdaQueryWrapper<EpProductUnitConsumeStandardUser>()
                            .select(EpProductUnitConsumeStandardUser::getUserId)
                            .eq(EpProductUnitConsumeStandardUser::getStandardId, consumeAlarmVO.getStandardId()));
                    if(CollUtil.isNotEmpty(userList)){
                        Set<Long> userIds = Sets.newHashSet();
                        userList.forEach(tmp -> {
                            userIds.add(tmp.getUserId().longValue());
                        });
                        //发送消息
                        SysMsgSendDTO sysMsgDTO = new SysMsgSendDTO();
                        sysMsgDTO.setType(SysMsgConstant.MsgTypeEnum.UNIT_CONSUME_ALARM.getCode());
                        sysMsgDTO.setTitle(SysMsgConstant.MsgTypeEnum.UNIT_CONSUME_ALARM.getTitle());
                        sysMsgDTO.setWarnFlag(SysMsgConstant.MsgTypeEnum.UNIT_CONSUME_ALARM.getWarnFlag());
                        sysMsgDTO.setUserList(userIds);
                        String msgContent = SysMsgConstant.MsgTypeEnum.UNIT_CONSUME_ALARM.getTemplate();
                        //拿到单位和标准类型
                        EpProductUnitConsumeStandard epProductUnitConsumeStandard = epProductUnitConsumeStandardService.getById(consumeAlarmVO.getStandardId());
                        Set<Long> dictSet = Sets.newHashSet();
                        Collections.addAll(dictSet,epProductUnitConsumeStandard.getMeasureUnit(),epProductUnitConsumeStandard.getStandardType());
                        String unitStr = "";
                        String standardStr = "";
                        if (CollUtil.isNotEmpty(dictSet)){
                            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                            if(CollUtil.isNotEmpty(dictMap)){
                                unitStr = dictMap.get(epProductUnitConsumeStandard.getMeasureUnit());
                                standardStr = dictMap.get(epProductUnitConsumeStandard.getStandardType());
                            }
                        }
                        epProductUnitConsumeStandard.getMeasureUnit();
                        sysMsgDTO.setContent(String.format(msgContent, dateString, consumeAlarmVO.getCurrentValue()+unitStr,standardStr+"("+consumeAlarmVO.getStandardValue()+unitStr+")"));
                        sysMsgDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
                        FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    }
                }else {
                    log.info("发送失败,消息发送人为空");
                }
            }
        }

        if (CollectionUtils.isNotEmpty(epUnitConsumeAlarmList)) {
            epUnitConsumeAlarmService.saveBatch(epUnitConsumeAlarmList);
        }
    }

    private void recursion(List<EpPartition> partitionFilterList, List<EpPartition> epPartitionList, List<Long> partitionIdList) {
        if (CollectionUtils.isEmpty(partitionFilterList)) {
            return;
        }
        List<Long> idList = partitionFilterList.stream().map(EpPartition::getId).collect(Collectors.toList());
        partitionIdList.addAll(idList);

        List<EpPartition> partitionList = epPartitionList.stream().filter(epPartition -> idList.contains(epPartition.getParentId())).collect(Collectors.toList());
        recursion(partitionList, epPartitionList, partitionIdList);
    }

    private EpTableDTO getEpTableDTO(Integer energyType, BigDecimal thick, String officeName) {
        List<Long> partitionIdList = new ArrayList<>();
        LocalDate localDate = LocalDate.now();
        List<BigDecimal> thickList = new ArrayList<>();
        if (Objects.nonNull(thick)) {
            thickList.add(thick);
        }

        LambdaQueryWrapper<EpPartition> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EpPartition::getType, energyType);
        List<EpPartition> epPartitionList = partitionService.list(queryWrapper);
        List<EpPartition> partitionFilterList = epPartitionList.stream().filter(epPartition -> epPartition.getOfficeName().equals(officeName)).collect(Collectors.toList());
        recursion(partitionFilterList, epPartitionList, partitionIdList);

        EpTableDTO epTableDTO = new EpTableDTO();
        epTableDTO.setStart(localDate.minusDays(1));
        epTableDTO.setEnd(localDate.minusDays(1));
        epTableDTO.setPartitionIdList(partitionIdList);
        epTableDTO.setEnergyType(energyType);
        epTableDTO.setThick(thickList);
        epTableDTO.setGranularity(1);
        return epTableDTO;
    }
}
