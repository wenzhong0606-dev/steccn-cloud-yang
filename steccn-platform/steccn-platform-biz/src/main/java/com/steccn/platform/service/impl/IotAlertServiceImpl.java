package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.BasePageDTO;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.domain.IotAlert;
import com.steccn.platform.domain.IotAlertRecord;
import com.steccn.platform.domain.PlatformUser;
import com.steccn.platform.domain.PlatformUserMac;
import com.steccn.platform.dto.IotAlertDTO;
import com.steccn.platform.dto.IotDataQueryDTO;
import com.steccn.platform.dto.IotDataQueryDetailDTO;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.service.*;
import com.steccn.platform.mapper.IotAlertMapper;
import com.steccn.platform.vo.IotAlertVO;
import com.steccn.platform.vo.IotDataQueryVO;
import com.steccn.platform.vo.PlatformUserVO;
import com.steccn.websocket.constant.WebsocketConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author Admin
* @description 针对表【iot_alert(告警表)】的数据库操作Service实现
* @createDate 2025-12-10 14:27:19
*/
@Service
@RequiredArgsConstructor
public class IotAlertServiceImpl extends ServiceImpl<IotAlertMapper, IotAlert>
    implements IotAlertService{
    private final PlatformUserService platformUserService;
    private final IotQueryService iotQueryService;
    private final SysMsgService sysMsgService;
    private final IotAlertRecordService iotAlertRecordService;

    @Override
    public Boolean addAlert(IotAlertDTO dto) {
        IotAlert model = new IotAlert();
        BeanUtil.copyProperties(dto,model);
        model.setAlertTime(LocalDateTime.now());
        save(model);
        return true;
    }

    @Override
    public Boolean updateAlert(IotAlertDTO dto) {
        if(Objects.isNull(dto.getId())){
            throw new BusinessException("数据id不能为空");
        }
        IotAlert model = new IotAlert();
        BeanUtil.copyProperties(dto,model);
        model.setAlertTime(LocalDateTime.now());
        updateById(model);
        return true;
    }

    @Override
    public IotAlertVO getOneInfo(Integer id) {
        IotAlert byId = getById(id);
        IotAlertVO vo = new IotAlertVO();
        BeanUtil.copyProperties(byId,vo);
        List<PlatformUser> platformUsers = platformUserService.listByIds(byId.getReceiver());
        List<String> collect = platformUsers.stream().map(PlatformUser::getName).collect(Collectors.toList());
        vo.setReceiverName(collect);
        return vo;
    }

    @Override
    public Page<IotAlertVO> listInfo(BasePageDTO dto) {
        Page<IotAlert> page = page(new Page<>(dto.getCurrent(), dto.getSize()));
        Page resPage = new Page<IotAlertVO>(dto.getCurrent(), dto.getSize());
        List<IotAlertVO> results = Lists.newArrayList();
        if(CollUtil.isNotEmpty(page.getRecords())){
            Set<Long> collect = page.getRecords().stream().flatMap(v->v.getReceiver().stream()).collect(Collectors.toSet());
            List<PlatformUser> platformUsers = platformUserService.listByIds(collect);
            Map<Long, PlatformUser> userMap = platformUsers.stream()
                    .collect(Collectors.toMap(PlatformUser::getId, u -> u));
            page.getRecords().forEach(tmp->{
                IotAlertVO vo = new IotAlertVO();
                BeanUtil.copyProperties(tmp,vo);
                List<String> userNames = Lists.newArrayList();
                vo.getReceiver().forEach(v->{
                    userNames.add(userMap.get(v).getName());
                });
                vo.setReceiverName(userNames);
                results.add(vo);
            });

        }
        BeanUtils.copyProperties(page, resPage);
        resPage.setRecords(results);
        return resPage;
    }

    @Override
    public Boolean deleteInfo(Integer id) {
        removeById(id);
        return true;
    }

    @Override
//    @Scheduled(fixedDelay = 5000)
    public Boolean alertInfo() {
        LocalDateTime now = LocalDateTime.now();
        List<IotAlert> list = list();
        if(CollUtil.isNotEmpty(list)){
            for(IotAlert tmp:list){
                //如果秒数小于时间间隔,则无需报警
                long seconds = Duration.between(tmp.getAlertTime(),now).getSeconds();
                if(seconds >= tmp.getIntervalSec()){
                    //是否有上下限
                    IotDataQueryDTO dataDto = new IotDataQueryDTO();
                    dataDto.setFill("previous");
                    dataDto.setAggregations(Lists.newArrayList(tmp.getAggregations()));
                    dataDto.setStart(now.minusSeconds(seconds).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    dataDto.setEnd(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    dataDto.setInterval(tmp.getIntervalSec()+"s");
                    List<IotDataQueryDetailDTO> points = Lists.newArrayList();
                    IotDataQueryDetailDTO detailDTO = new IotDataQueryDetailDTO();
                    detailDTO.setAggregations(Lists.newArrayList(tmp.getAggregations()));
                    detailDTO.setDeviceID(tmp.getDeviceId());
                    detailDTO.setPointID(tmp.getPointId());
                    points.add(detailDTO);
                    dataDto.setPoints(points);
                    List<IotDataQueryVO> iotDataQueryVOS = iotQueryService.queryPointData(dataDto);
                    if(CollUtil.isNotEmpty(iotDataQueryVOS.get(0).getValues())){
                        Map<String, Object> valueMap = iotDataQueryVOS.get(0).getValues().get(iotDataQueryVOS.get(0).getValues().size() - 1);
                        Object o = valueMap.get(tmp.getAggregations());
                        //发送消息
                        Set<Long> userIds = new HashSet<>();
                        for(Object v:tmp.getReceiver()){
                            userIds.add(Long.parseLong(v.toString()));
                        }
                        //判断是否为数字
                        if(Objects.nonNull(o) && o.toString().matches("-?\\d+(\\.\\d+)?")){
                            //判断上限，上限比o小了
                            if(Objects.nonNull(tmp.getUpValue())
                                    && tmp.getUpValue().compareTo(new BigDecimal(o.toString())) < 0){
                                SysMsgSendDTO sysMsgDTO = new SysMsgSendDTO();
                                sysMsgDTO.setType(SysMsgConstant.MsgTypeEnum.IOT_WARN.getCode());
                                sysMsgDTO.setTitle(SysMsgConstant.MsgTypeEnum.IOT_WARN.getTitle());
                                sysMsgDTO.setWarnFlag(SysMsgConstant.MsgTypeEnum.IOT_WARN.getWarnFlag());
                                sysMsgDTO.setUserList(userIds);
                                String content = SysMsgConstant.MsgTypeEnum.IOT_WARN.getTemplate();
                                sysMsgDTO.setContent(String.format(content, tmp.getName(),o.toString(),tmp.getUpValue(),tmp.getDownValue()));
                                sysMsgDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
                                sysMsgService.sendMsg(sysMsgDTO);
                                //保存记录
                                IotAlertRecord recordModel = new IotAlertRecord();
                                recordModel.setAlertId(tmp.getId());
                                recordModel.setAlertName(tmp.getName());
                                recordModel.setContent(sysMsgDTO.getContent());
                                recordModel.setAlertTime(now);
                                recordModel.setReceiver(tmp.getReceiver());
                                iotAlertRecordService.save(recordModel);
                            }else if(Objects.nonNull(tmp.getDownValue())
                                    && tmp.getUpValue().compareTo(new BigDecimal(o.toString())) > 0){
                                //发送消息
                                SysMsgSendDTO sysMsgDTO = new SysMsgSendDTO();
                                sysMsgDTO.setType(SysMsgConstant.MsgTypeEnum.IOT_WARN.getCode());
                                sysMsgDTO.setTitle(SysMsgConstant.MsgTypeEnum.IOT_WARN.getTitle());
                                sysMsgDTO.setWarnFlag(SysMsgConstant.MsgTypeEnum.IOT_WARN.getWarnFlag());
                                sysMsgDTO.setUserList(userIds);
                                String content = SysMsgConstant.MsgTypeEnum.IOT_WARN.getTemplate();
                                sysMsgDTO.setContent(String.format(content, tmp.getName(),o.toString(),tmp.getUpValue(),tmp.getDownValue()));
                                sysMsgDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
                                sysMsgService.sendMsg(sysMsgDTO);
                                //保存报警记录
                                IotAlertRecord recordModel = new IotAlertRecord();
                                recordModel.setAlertId(tmp.getId());
                                recordModel.setAlertName(tmp.getName());
                                recordModel.setContent(sysMsgDTO.getContent());
                                recordModel.setAlertTime(now);
                                recordModel.setReceiver(tmp.getReceiver());
                                iotAlertRecordService.save(recordModel);
                            }
                        }
                    }
                    //修改最新报警时间
                    IotAlert model = new IotAlert();
                    model.setId(tmp.getId());
                    model.setAlertTime(now);
                    updateById(model);
                }
            }
        }
        return true;
    }
}




