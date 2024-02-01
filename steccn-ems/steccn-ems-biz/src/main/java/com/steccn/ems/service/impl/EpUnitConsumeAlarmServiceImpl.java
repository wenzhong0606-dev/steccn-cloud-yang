package com.steccn.ems.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.Assert;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.common.util.GetValueUtil;
import com.steccn.common.util.LocalDateUtil;
import com.steccn.common.util.RegularUtil;
import com.steccn.common.vo.EpChartGraphVO;
import com.steccn.common.vo.EpChartGraphXAxisVO;
import com.steccn.common.vo.EpChartUnitConsumeAlarmVO;
import com.steccn.ems.constant.EpManagerConstant;
import com.steccn.ems.domain.*;
import com.steccn.ems.dto.EpTableDTO;
import com.steccn.ems.dto.EpUnitConsumeAlarmDTO;
import com.steccn.ems.mapper.EpUnitConsumeAlarmMapper;
import com.steccn.ems.service.*;
import com.steccn.ems.vo.EpUnitConsumeAlarmVO;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.vo.SysDictVO;
import com.steccn.websocket.constant.WebsocketConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author yequan
* @description 针对表【ep_unit_consume_alarm(单耗告警表)】的数据库操作Service实现
* @createDate 2022-11-29 19:27:28
*/
@RequiredArgsConstructor
@Service
@Slf4j
public class EpUnitConsumeAlarmServiceImpl extends ServiceImpl<EpUnitConsumeAlarmMapper, EpUnitConsumeAlarm>
    implements EpUnitConsumeAlarmService{

    private final EpEnergyTypeService epEnergyTypeService;
    private final EpPartitionService epPartitionService;
    private final EpConsumePartitionChartService epConsumePartitionChartService;
    private final RemoteDictFeignApi remoteDictFeignApi;
    private final EpProductUnitConsumeStandardUserService epProductUnitConsumeStandardUserService;
    private final EpProductUnitConsumeStandardService epProductUnitConsumeStandardService;
    private final RemoteMsgFeignApi remoteMsgFeignApi;

    @Override
    public Page<EpUnitConsumeAlarmVO> page(EpUnitConsumeAlarmDTO dto) {
        LambdaQueryWrapper<EpUnitConsumeAlarm> condition = new LambdaQueryWrapper<EpUnitConsumeAlarm>();

        if (Objects.nonNull(dto.getEnergyType())) {
            condition.eq(EpUnitConsumeAlarm::getEnergyType,dto.getEnergyType());
        }
        if (Objects.nonNull(dto.getPartitionId())) {
            if (Objects.nonNull(dto.getEnergyType())) {
                condition.eq(EpUnitConsumeAlarm::getOfficeId,dto.getPartitionId());
            } else {
                List<EpPartition> partitionList = epPartitionService.list(new LambdaQueryWrapper<EpPartition>().eq(EpPartition::getOfficeId, dto.getPartitionId()));
                Set<Long> partitionIdSet = partitionList.stream().map(EpPartition::getId).collect(Collectors.toSet());
                condition.in(EpUnitConsumeAlarm::getOfficeId,partitionIdSet);
            }
        }
        if (Objects.nonNull(dto.getStartDate())) {
            condition.gt(EpUnitConsumeAlarm::getAlarmDate,dto.getStartDate());
        }
        if (Objects.nonNull(dto.getEndDate())) {
            condition.lt(EpUnitConsumeAlarm::getAlarmDate,dto.getEndDate());
        }
        //定义排序条件
        if (EpManagerConstant.EP_UNIT_CONSUME_ALARM_SORT_ALARM_DATE.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpUnitConsumeAlarm::getAlarmDate);
        }else if (EpManagerConstant.EP_UNIT_CONSUME_ALARM_SORT_ENERGY_TYPE.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpUnitConsumeAlarm::getEnergyType);
        }else if (EpManagerConstant.EP_UNIT_CONSUME_ALARM_SORT_OFFICE_ID.equals(dto.getSortField())){
            condition.orderBy(true,!dto.getAscFlag(),EpUnitConsumeAlarm::getOfficeId);
        } else if (EpManagerConstant.EP_UNIT_CONSUME_ALARM_SORT_ALARM_TYPE.equals(dto.getSortField())) {
            condition.orderBy(true,!dto.getAscFlag(),EpUnitConsumeAlarm::getAlarmType);
        }else{
            condition.orderBy(true, dto.getAscFlag(), EpUnitConsumeAlarm::getAlarmDate);
        }

        Page<EpUnitConsumeAlarmVO> voPage = new Page<>();
        Page<EpUnitConsumeAlarm> page = page(new Page<>(dto.getCurrent(), dto.getSize()), condition);
        List<EpUnitConsumeAlarmVO> epUnitConsumeAlarmVOList = new ArrayList<>();
        List<EpPartition> list = epPartitionService.list();

        if (CollectionUtils.isNotEmpty(page.getRecords())) {
            Set<Integer> energyTypeSet = page.getRecords().stream().map(EpUnitConsumeAlarm::getEnergyType).collect(Collectors.toSet());
            Map<Integer, String> energyTypeMap = epEnergyTypeService.getBySetIds(energyTypeSet);
            for (EpUnitConsumeAlarm epUnitConsumeAlarm : page.getRecords()) {
                EpUnitConsumeAlarmVO epUnitConsumeAlarmVO = new EpUnitConsumeAlarmVO();
                BeanUtils.copyProperties(epUnitConsumeAlarm,epUnitConsumeAlarmVO);
                epUnitConsumeAlarmVO.setEnergyTypeName(energyTypeMap.get(epUnitConsumeAlarm.getEnergyType()));

                StringBuilder sb = new StringBuilder();
                Optional<EpPartition> partitionOptional = list.stream().filter(epPartition -> epPartition.getId().equals(epUnitConsumeAlarm.getOfficeId().longValue())).findAny();
                if (partitionOptional.isPresent()) {
                    sb.insert(0, epUnitConsumeAlarm.getOfficeName());
                    recursion(list, partitionOptional.get(), sb);
                }
                epUnitConsumeAlarmVO.setPartitionName(epUnitConsumeAlarm.getOfficeName());
                epUnitConsumeAlarmVO.setPartitionNameAll(sb.toString());
                epUnitConsumeAlarmVO.setAlarmDetail(epUnitConsumeAlarm.getContent());
                if (epUnitConsumeAlarm.getAlarmType().equals(0)){
                    epUnitConsumeAlarmVO.setAlarmType("日单耗超标");
                }
                epUnitConsumeAlarmVOList.add(epUnitConsumeAlarmVO);
            }

        }

        BeanUtils.copyProperties(page,voPage);
        voPage.setRecords(epUnitConsumeAlarmVOList);
        return voPage;
    }

    @Override
    public Boolean trigger(LocalDate classDate) {
        List<EpChartUnitConsumeAlarmVO> consumeAlarmVOList = new ArrayList<>();
        List<SysDictVO> thickList = FeignOptional.of(() -> remoteDictFeignApi.listByType("product_thickness_range"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Assert.notNull(thickList, "厚度列表为空");
        Map<Long, BigDecimal> thickMap = Maps.newHashMap();
        for (SysDictVO sysDictVO : thickList) {
            thickMap.put(sysDictVO.getId(), RegularUtil.extractValue(sysDictVO.getCode(), null, (x) -> new BigDecimal(x)));
        }
        //获取单签有效的部门标准，提取涉及部门，能源类型和厚度
        List<EpProductUnitConsumeStandard> standardList = epProductUnitConsumeStandardService.getValidDepartmentStandardList(classDate);
        for (EpProductUnitConsumeStandard standard : standardList) {
            //厚度必须存在
            BigDecimal thisThick = thickMap.get(standard.getProductThick());
            if(Objects.isNull(thisThick)){
                thisThick = BigDecimalUtil.MINUS_ONE;
            }
            Set<Integer> applyOffice = standard.getApplyOffice();
            if (CollUtil.isNotEmpty(applyOffice)) {
                //根据能源获取节点
                Map<Integer, EpPartition> epPartitionMap = epPartitionService.getByOfficeId(standard.getEnergyType(), applyOffice);
                if (CollUtil.isNotEmpty(epPartitionMap)) {
                    List<Long> partitionNodeList = epPartitionMap.values().stream().map(ele -> ele.getId()).collect(Collectors.toList());
                    EpTableDTO epTableDTO = new EpTableDTO();
                    epTableDTO.setStart(classDate);
                    epTableDTO.setEnd(classDate);
                    epTableDTO.setPartitionIdList(partitionNodeList);
                    epTableDTO.setEnergyType(standard.getEnergyType());
                    epTableDTO.setThick(Lists.newArrayList(thisThick));
                    epTableDTO.setGranularity(1);
                    EpChartGraphVO<EpChartGraphXAxisVO, BigDecimal> table = epConsumePartitionChartService.unitConsumePartitionContrastChart(epTableDTO);
                    consumeAlarmVOList.addAll(table.getAlarmList());
                }
            }
        }

        List<EpUnitConsumeAlarm> epUnitConsumeAlarmList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(consumeAlarmVOList)) {
            for (EpChartUnitConsumeAlarmVO consumeAlarmVO : consumeAlarmVOList) {
                EpUnitConsumeAlarm epUnitConsumeAlarm = new EpUnitConsumeAlarm();
                BeanUtils.copyProperties(consumeAlarmVO, epUnitConsumeAlarm);
                epUnitConsumeAlarm.setAlarmDate(LocalDateTime.now());
                epUnitConsumeAlarm.setAlarmType(0);
                epUnitConsumeAlarm.setEnergyType(consumeAlarmVO.getEnergyType());
                String dateString = LocalDateUtil.localDateFormat(consumeAlarmVO.getAlarmDateTime().toLocalDate(), DatePattern.NORM_DATE_PATTERN);
                dateString += "【" + epUnitConsumeAlarm.getOfficeName() + "】";
                Integer standardId = consumeAlarmVO.getStandardId();
                EpProductUnitConsumeStandard standard = epProductUnitConsumeStandardService.getById(standardId);
                String standardShowName = GetValueUtil.get(standard, x -> x.getName(), "内部标准");
                String unitSysDictStr = Strings.EMPTY;
                if(Objects.nonNull(standard)){
                    SysDictVO unitSysDict = FeignOptional.of(() -> remoteDictFeignApi.get(standard.getMeasureUnit()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    unitSysDictStr = GetValueUtil.get(unitSysDict, x -> x.getName(), Strings.EMPTY);
                }
                String content = dateString + "日单耗" + consumeAlarmVO.getCurrentValue() + unitSysDictStr + "，" + "超过" + standardShowName + "(" + consumeAlarmVO.getStandardValue() + unitSysDictStr + ")";
                epUnitConsumeAlarm.setContent(content);
                epUnitConsumeAlarmList.add(epUnitConsumeAlarm);
                //拿到消息发送人
                if (consumeAlarmVO.getStandardId() != null) {
                    List<EpProductUnitConsumeStandardUser> userList = epProductUnitConsumeStandardUserService.list(new LambdaQueryWrapper<EpProductUnitConsumeStandardUser>()
                            .select(EpProductUnitConsumeStandardUser::getUserId)
                            .eq(EpProductUnitConsumeStandardUser::getStandardId, consumeAlarmVO.getStandardId()));
                    if (CollUtil.isNotEmpty(userList)) {
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
                        Collections.addAll(dictSet, epProductUnitConsumeStandard.getMeasureUnit(), epProductUnitConsumeStandard.getStandardType());
                        String unitStr = "";
                        String standardStr = "";
                        if (CollUtil.isNotEmpty(dictSet)) {
                            Map<Long, String> dictMap = FeignOptional.of(() -> remoteDictFeignApi.listByIdCh(dictSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                            if (CollUtil.isNotEmpty(dictMap)) {
                                unitStr = dictMap.get(epProductUnitConsumeStandard.getMeasureUnit());
                                standardStr = standardShowName;
                            }
                        }
                        epProductUnitConsumeStandard.getMeasureUnit();
                        sysMsgDTO.setContent(String.format(msgContent, dateString, consumeAlarmVO.getCurrentValue() + unitStr, standardStr + "(" + consumeAlarmVO.getStandardValue() + unitStr + ")"));
                        sysMsgDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
                        FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    }
                } else {
                    log.info("发送失败,消息发送人为空");
                }
            }
        }

        if (CollectionUtils.isNotEmpty(epUnitConsumeAlarmList)) {
            saveBatch(epUnitConsumeAlarmList);
        }
        return Boolean.TRUE;
    }

    private EpPartition recursion(List<EpPartition> list, EpPartition partition, StringBuilder sb) {
        Optional<EpPartition> partitionOptional = list.stream().filter(epPartition -> epPartition.getId().equals(partition.getParentId())).findAny();
        if (partitionOptional.isPresent()) {
            sb.insert(0, partitionOptional.get().getOfficeName() + "/");
            return recursion(list, partitionOptional.get(), sb);
        }
        return null;
    }
}




