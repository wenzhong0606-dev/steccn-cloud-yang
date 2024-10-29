package com.steccn.mes.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TupleDTO;
import com.steccn.mes.constant.MesManagerConstant;
import com.steccn.mes.domain.JsSysEmployee;
import com.steccn.mes.domain.JsSysOffice;
import com.steccn.mes.domain.MesEquipment;
import com.steccn.mes.dto.EqpListenerDTO;
import com.steccn.mes.mapper.MesEquipmentMapper;
import com.steccn.mes.service.JsSysEmployeeService;
import com.steccn.mes.service.JsSysOfficeService;
import com.steccn.mes.service.MesEquipmentService;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.SysConfigurationDTO;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.feign.RemoteSysConfigurationFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author AndrewLuo
 * @desc kafka消费者
 * @date 2023/12/28
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {
    private final StringRedisTemplate stringRedisTemplate;
    private final RemoteSysConfigurationFeignApi remoteSysConfigurationFeignApi;
    private final MesEquipmentService mesEquipmentService;
    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(3, 5, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000), Executors.defaultThreadFactory());
    private final JsSysOfficeService jsSysOfficeService;
    private final JsSysEmployeeService jsSysEmployeeService;
    private final OaInfoService oaInfoService;
    private final RemoteMsgFeignApi remoteMsgFeignApi;
    /**
     * 消费监听
     *
     * @param record
     */
    @KafkaListener(topics = "eqp_signal_data_order", concurrency = "1")
    @Transactional(rollbackFor = Exception.class)
    public void tpFirst(ConsumerRecord<Object, String> record, Acknowledgment ack) {
        // 消费的哪个topic、partition的消息,打印出消息内容
//        log.info(Thread.currentThread().getId() + ":" + record.topic() + "-" + record.partition() + "-" + record.value());
        if(!record.key().toString().contains("OA1.BOOL.")){
            return;
        }
        //对消息进行解析
        EqpListenerDTO eqpListenerDTO = JSONObject.parseObject(record.value(), EqpListenerDTO.class);
        //如果是脉冲消息则直接修改redis
        if(eqpListenerDTO.getTagId().equals("OA1.BOOL.STOP_BOOL")){
            if(eqpListenerDTO.getValueAsString().equals("true")){
                stringRedisTemplate.opsForValue().set("KafkaEqpSignal::pulse","false");
            }
            ack.acknowledge();
            return;
        }
        //start没有作用
        if(eqpListenerDTO.getTagId().equals("OA1.BOOL.START_BOOL")){
            if(eqpListenerDTO.getValueAsString().equals("true")){
                stringRedisTemplate.opsForValue().set("KafkaEqpSignal::pulse","true");
            }
            ack.acknowledge();
            return;
        }
        //获取消息对应的类型
        String redisResult = stringRedisTemplate.opsForValue().get("KafkaEqpSignal::" + eqpListenerDTO.getTagId());
        SysConfigurationDTO config = new SysConfigurationDTO();
        if(StrUtil.isNotBlank(redisResult)){
            config = JSONObject.parseObject(redisResult,SysConfigurationDTO.class);
        }else {
            config = FeignOptional.of(() -> remoteSysConfigurationFeignApi.infoByContentAndType(new TupleDTO<>("KAFKA_EQP_SIGNAL", eqpListenerDTO.getTagId())), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            if(StrUtil.isBlank(config.getContent())){
                log.error(eqpListenerDTO.getTagId()+"不存在对应的设备");
                ack.acknowledge();
                return;
            }
            stringRedisTemplate.opsForValue().set("KafkaEqpSignal::"+config.getContent(),JSONObject.toJSONString(config));
        }
        //先处理故障状态
        if(config.getOtherContent().equals(OaConstant.NUM_1.toString())){
            //如果出现故障则修改状态并发送消息
            MesEquipment one = mesEquipmentService.getOne(new LambdaQueryWrapper<MesEquipment>()
                    .eq(MesEquipment::getEquipmentCode, config.getRefContent())
                    .eq(MesEquipment::getStatus, OaConstant.NO)
                    .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
                    .last("limit 1"), false);
            if(eqpListenerDTO.getValueAsString().equals("true")){
                MesEquipment eqpTmp = new MesEquipment();
                eqpTmp.setState(OaConstant.NUM_1.toString());
                mesEquipmentService.update(eqpTmp,new LambdaQueryWrapper<MesEquipment>()
                        .eq(MesEquipment::getEquipmentCode,config.getRefContent())
                        .eq(MesEquipment::getStatus,OaConstant.NO));
                //给设备所属部门的人发送消息
                if(Objects.nonNull(one)&&StrUtil.isNotBlank(one.getDepartment())){
                    poolExecutor.execute(()->{
                        //若部门的上级包含(设备动力部、深加工制造部、基片制造部)则发给下面的所有人
                        JsSysOffice officeOne = jsSysOfficeService.getOne(new LambdaQueryWrapper<JsSysOffice>()
                                .eq(JsSysOffice::getOfficeCode, one.getDepartment())
                                .eq(JsSysOffice::getStatus, OaConstant.NO)
                                .last("LIMIT 1"));
                        String departmentStr = "";
                        if(officeOne.getOfficeCode().equals(MesManagerConstant.PLANT_POWER)||
                        officeOne.getParentCodes().contains(MesManagerConstant.PLANT_POWER+",")){
                            departmentStr = MesManagerConstant.PLANT_POWER+",";
                        }else if (officeOne.getOfficeCode().equals(MesManagerConstant.DEEP_PROCESSING)||
                                officeOne.getParentCodes().contains(MesManagerConstant.DEEP_PROCESSING+",")){
                            departmentStr = MesManagerConstant.DEEP_PROCESSING+",";
                        }else if (officeOne.getOfficeCode().equals(MesManagerConstant.SUB_MANU_FAC)||
                                officeOne.getParentCodes().contains(MesManagerConstant.SUB_MANU_FAC+",")){
                            departmentStr = MesManagerConstant.SUB_MANU_FAC+",";
                        }
                        //获取部门及部门下的岗位
                        List<JsSysOffice> sysOffices = Lists.newArrayList();
                        if(StrUtil.isNotBlank(departmentStr)){
                            if(officeOne.getOfficeCode().equals(MesManagerConstant.PLANT_POWER)
                                    ||officeOne.getOfficeCode().equals(MesManagerConstant.DEEP_PROCESSING)
                                    ||officeOne.getOfficeCode().equals(MesManagerConstant.SUB_MANU_FAC)){
                                sysOffices = jsSysOfficeService.list(new LambdaQueryWrapper<JsSysOffice>()
                                        .select(JsSysOffice::getOfficeCode)
                                        .like(JsSysOffice::getParentCodes, departmentStr)
                                        .eq(JsSysOffice::getStatus,OaConstant.NO));
                            }else {
                                //加逻辑，若设备的使用部门是设备动力部、深加工制造部、基片制造部的下级，则只需要发给设备动力部岗位下的人和自身部门下的人
                                if(officeOne.getOfficeCode().equals(MesManagerConstant.PLANT_POWER+",")){
                                    departmentStr = departmentStr+MesManagerConstant.PLANT_POWER_POST+",";
                                }else if (officeOne.getOfficeCode().equals(MesManagerConstant.DEEP_PROCESSING+",")){
                                    departmentStr = departmentStr+MesManagerConstant.DEEP_PROCESSING_POST+",";
                                }else if (officeOne.getOfficeCode().equals(MesManagerConstant.SUB_MANU_FAC+",")){
                                    departmentStr = departmentStr+MesManagerConstant.SUB_MANU_FAC_POST+",";
                                }
                                sysOffices = jsSysOfficeService.list(new LambdaQueryWrapper<JsSysOffice>()
                                        .select(JsSysOffice::getOfficeCode)
                                        .like(JsSysOffice::getParentCodes, departmentStr)
                                        .eq(JsSysOffice::getStatus,OaConstant.NO));
                                //自身使用部门下的人
                                List<JsSysOffice> office2 = jsSysOfficeService.list(new LambdaQueryWrapper<JsSysOffice>()
                                        .select(JsSysOffice::getOfficeCode)
                                        .like(JsSysOffice::getParentCodes, one.getDepartment() + ",")
                                        .eq(JsSysOffice::getStatus, OaConstant.NO));
                                sysOffices.addAll(office2);
                            }
                        }
                        Set<String> officeCodes = Sets.newHashSet();
                        if(CollUtil.isNotEmpty(sysOffices)){
                            officeCodes = sysOffices.stream().map(JsSysOffice::getOfficeCode).collect(Collectors.toSet());
                        }
                        officeCodes.add(one.getDepartment());
                        //若部门的上级包含(设备动力部、深加工制造部、基片制造部)则发给下面的所有人
                        //获取人
                        List<JsSysEmployee> employees = jsSysEmployeeService.list(new LambdaQueryWrapper<JsSysEmployee>()
                                .select(JsSysEmployee::getEmpCode)
                                .in(JsSysEmployee::getOfficeCode, officeCodes)
                                .eq(JsSysEmployee::getStatus, OaConstant.NO));
                        if(CollUtil.isNotEmpty(employees)){
                            List<String> userNames = Lists.newArrayList();
                            employees.forEach(tmp->{
                                userNames.add(tmp.getEmpCode().split("_")[0]);
                            });
                            List<Integer> userIds = FeignOptional.of(() -> oaInfoService.getIdByWorkCode(userNames), FeignConstant.STECCN_OA_SERVICE_NAME).get();
                            //发送消息
                            if(CollUtil.isNotEmpty(userIds)){
                                SysMsgSendDTO sysMsgDTO = new SysMsgSendDTO();
                                sysMsgDTO.setType(SysMsgConstant.MsgTypeEnum.EQP_NOTICE.getCode());
                                sysMsgDTO.setTitle(SysMsgConstant.MsgTypeEnum.EQP_NOTICE.getTitle());
                                sysMsgDTO.setWarnFlag(SysMsgConstant.MsgTypeEnum.EQP_NOTICE.getWarnFlag());
                                sysMsgDTO.setUserList(userIds.stream()
                                        .map(Integer::longValue)
                                        .collect(Collectors.toSet()));
                                String content = SysMsgConstant.MsgTypeEnum.EQP_NOTICE.getTemplate();
                                sysMsgDTO.setContent(String.format(content, one.getEquipmentName()));
                                sysMsgDTO.setPushType(OaConstant.NUM_1);
                                FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                            }
                        }

                    });
                }

            }else {
                //如果设备为正常状态则不动
                //如果是告警状态则改成运行状态
                if(Objects.nonNull(one)&&one.getState().equals(OaConstant.NUM_1.toString())){
                    MesEquipment eqpTmp = new MesEquipment();
                    eqpTmp.setState(OaConstant.NO.toString());
                    mesEquipmentService.update(eqpTmp,new LambdaQueryWrapper<MesEquipment>()
                            .eq(MesEquipment::getEquipmentCode,config.getRefContent())
                            .eq(MesEquipment::getStatus,OaConstant.NO));
                }
            }
        }else if(config.getOtherContent().equals(OaConstant.NO.toString())){
            //找到设备code信号
            Set<String> signalCode = FeignOptional.of(() -> remoteSysConfigurationFeignApi.contentByType("EQP_SIGNAL"), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //运行状态
            //先获取脉冲状态
            Boolean pulse;
            String pulseStr = stringRedisTemplate.opsForValue().get("KafkaEqpSignal::pulse");
            if(StrUtil.isNotBlank(pulseStr)&&pulseStr.equals("true")){
                pulse = true;
            }else {
                pulse = false;
            }
            //修改的对象
            MesEquipment equipment = new MesEquipment();
            if(eqpListenerDTO.getValueAsString().equals("true")||eqpListenerDTO.getValueAsString().equals("1")) {
                equipment.setState(OaConstant.NO.toString());
            }else {
                //判断启动/停止信号：信号为启动，更新设备状态为待机，否则为停机
                if(pulse){
                    equipment.setState(OaConstant.NUM_2.toString());
                }else {
                    equipment.setState(OaConstant.NUM_3.toString());
                }
            }
            //需要修改的id
            Set<Long> idSet = Sets.newHashSet();
            //获取设备自身信息和上级信息
            MesEquipment eqp = mesEquipmentService.getOne(new LambdaQueryWrapper<MesEquipment>()
                    .eq(MesEquipment::getEquipmentCode, config.getRefContent())
                    .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                    .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
                    .last("limit 1"));
            if(Objects.isNull(eqp)){
                log.error(config.getRefContent()+"设备不存在");
                throw new BusinessException(config.getRefContent()+"设备不存在");
            }
            //写死原料车间设备
            if(eqp.getParentCodes().contains("1501738403312599040")){
                if(eqpListenerDTO.getValueAsString().equals("true")) {
                    equipment.setState(OaConstant.NO.toString());
                }else {
                    equipment.setState(OaConstant.NUM_3.toString());
                }
                //注:eqp是传过来的设备信息,equipment代表设备的改变状态
                ack.acknowledge();
                return;
            }

//            if(eqp.getParentCodes().contains("1501738403312599040")){
//                if(eqpListenerDTO.getValueAsString().equals("true")) {
//                    equipment.setState(OaConstant.NO.toString());
//                }else {
//                    equipment.setState(OaConstant.NUM_3.toString());
//                }
//                //特殊处理一号强制式混合机、二号强制式混合机、缓冲仓闸门（混合机)
//                if(eqp.getId().equals(1543243396975095808L)
//                        ||eqp.getId().equals(1543243870138724352L)
//                        ||eqp.getId().equals(1548571027763810304L)
//                        ||eqp.getId().equals(1747819792324792320L)){
//                    List<MesEquipment> childrenList = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
//                            .like(MesEquipment::getParentCodes, eqp.getId())
//                            .eq(MesEquipment::getStatus, OaConstant.NO.toString())
//                            .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
//                    idSet.addAll(childrenList.stream().map(MesEquipment::getId).collect(Collectors.toSet()));
//                    idSet.add(eqp.getId());
//                    mesEquipmentService.update(equipment,new LambdaQueryWrapper<MesEquipment>()
//                            .in(MesEquipment::getId,idSet)
//                            .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                    //查看混合机是否一致
//                    List<MesEquipment> brotherList = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getParentCode, "1590633171971465216")
//                            .eq(MesEquipment::getStatus, OaConstant.NO.toString())
//                            .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
//                    //如果都一致如果状态都一致那么其上级设备也改变
//                    int offNum = (int)brotherList.stream().filter(v->v.getState().equals(OaConstant.NUM_3.toString())).count();
//                    if(offNum == brotherList.size()){
//                        MesEquipment eqpTmp1 = new MesEquipment();
//                        eqpTmp1.setState(OaConstant.NUM_3.toString());
//                        mesEquipmentService.update(eqpTmp1,new LambdaQueryWrapper<MesEquipment>()
//                                .eq(MesEquipment::getId,"1590633171971465216")
//                                .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                    }else {
//                        MesEquipment eqpTmp1 = new MesEquipment();
//                        eqpTmp1.setState(OaConstant.NO.toString());
//                        mesEquipmentService.update(eqpTmp1,new LambdaQueryWrapper<MesEquipment>()
//                                .eq(MesEquipment::getId,"1590633171971465216")
//                                .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                    }
//                    ack.acknowledge();
//                    return;
//
//                }
//                //1#混合机油泵电机只有一台开
//                if(eqp.getId().equals(1554749971972489216L)){
//                    Long eqpIdTmp = 1554750034203377664L;
//                    mesEquipmentService.update(equipment,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,"1554749971972489216"));
//                    MesEquipment equipmentTmp = new MesEquipment();
//                    if(equipment.getState().equals(OaConstant.NO.toString())
//                            ||equipment.getState().equals(OaConstant.NUM_2.toString())){
//                        equipmentTmp.setState(OaConstant.NUM_3.toString());
//                    }else {
//                        equipmentTmp.setState(OaConstant.NO.toString());
//                    }
//                    mesEquipmentService.update(equipmentTmp,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,eqpIdTmp));
//                    ack.acknowledge();
//                    return;
//                } else if(eqp.getId().equals(1554750034203377664L)){
//                    Long eqpIdTmp = 1554749971972489216L;
//                    MesEquipment equipmentTmp = new MesEquipment();
//                    mesEquipmentService.update(equipment,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,"1554750034203377664"));
//                    if(equipment.getState().equals(OaConstant.NO.toString())
//                            ||equipment.getState().equals(OaConstant.NUM_2.toString())){
//                        equipmentTmp.setState(OaConstant.NUM_3.toString());
//                    }else {
//                        equipmentTmp.setState(OaConstant.NO.toString());
//                    }
//                    mesEquipmentService.update(equipmentTmp,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,eqpIdTmp));
//                    ack.acknowledge();
//                    return;
//                }
//
//                //2#混合机油泵电机只有一台开
//                if(eqp.getId().equals(1552858842006159360L)){
//                    mesEquipmentService.update(equipment,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,"1552858842006159360"));
//                    Long eqpIdTmp = 1554749911603871744L;
//                    MesEquipment equipmentTmp = new MesEquipment();
//                    if(equipment.getState().equals(OaConstant.NO.toString())
//                            ||equipment.getState().equals(OaConstant.NUM_2.toString())){
//                        equipmentTmp.setState(OaConstant.NUM_3.toString());
//                    }else {
//                        equipmentTmp.setState(OaConstant.NO.toString());
//                    }
//                    mesEquipmentService.update(equipmentTmp,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,eqpIdTmp));
//                    ack.acknowledge();
//                    return;
//                } else if(eqp.getId().equals(1554749911603871744L)){
//                    mesEquipmentService.update(equipment,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,"1554749911603871744"));
//                    Long eqpIdTmp = 1552858842006159360L;
//                    MesEquipment equipmentTmp = new MesEquipment();
//                    if(equipment.getState().equals(OaConstant.NO.toString())
//                            ||equipment.getState().equals(OaConstant.NUM_2.toString())){
//                        equipmentTmp.setState(OaConstant.NUM_3.toString());
//                    }else {
//                        equipmentTmp.setState(OaConstant.NO.toString());
//                    }
//                    mesEquipmentService.update(equipmentTmp,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,eqpIdTmp));
//                    ack.acknowledge();
//                    return;
//                }
//                //1、一停(开)下级设备全停(停)
//                //称量皮带 窑头布料小车
//                Set<Long> eqpId =Sets.newHashSet(1548573570782908416L,1793533390992564224L);
//                if(eqpId.contains(eqp.getId())){
//                    List<MesEquipment> childrenList = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId, eqp.getParentCode())
//                            .eq(MesEquipment::getStatus, OaConstant.NO.toString())
//                            .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
//                    if(CollUtil.isNotEmpty(childrenList)){
//                        idSet.addAll(childrenList.stream().map(MesEquipment::getId).collect(Collectors.toSet()));
//                    }
//                    idSet.add(eqp.getId());
//                    mesEquipmentService.update(equipment,new LambdaQueryWrapper<MesEquipment>()
//                            .in(MesEquipment::getId,idSet)
//                            .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                    ack.acknowledge();
//                    return;
//                }
//                //2、一个停需要看他上级以及下级(2层)
//                //1#强制式混合机 2#强制式混合机
//                // 1#硅砂喂料皮带 2#硅砂喂料皮带
//                // 破碎玻璃喂料 缓冲仓闸门
//                // 复合澄清剂螺旋给料机	 铝粉螺旋给料机 小料螺旋给料机
//                //硅砂2#排料电磁振动给料机 白云石排料电磁振动给料机 纯碱排料电磁振动给料机 石灰石排料电磁振动给料机
//                //硅砂1#活化料斗 硅砂2#活化料斗 纯碱活化料斗 复合澄清剂活化料斗 小料活化料斗
//                Set<Long> eqpId2 =Sets.newHashSet(1543243396975095808L,1543243870138724352L,
//                        1548571027763810304L,1548572780831887360L,
//                        1548607109662429184L,1548869435598753792L,1548869791401562112L,1548869874788519936L,
//                        1551474111896023040L,1551474428196876288L,1551474615883591680L,
//                        1552943093476143104L,1592324192488906752L,1592326468393754624L,1592327036235407360L,
//                        1555023219150815232L,1555026576456286208L,1555026819172270080L,1555028579710402560L,1555029030027657216L);
//                //3层
//                //白云石大螺旋 白云石小螺旋 纯碱大螺旋 石灰石大螺旋 石灰石小螺旋
//                // 配合料1#皮带 配合料3#皮带 碎玻璃7#皮带
//                // 配合料2#皮带 配合料4#皮带 碎玻璃8#皮带
//
//                //碎玻璃7#皮带 碎玻璃8#皮带 关联CH35和CH36
//                if(eqp.getId().equals(1548589026927366144L)||eqp.getId().equals(1548590147603451904L)){
//                    Long eqpTmp = 1552936564324741120L;
//                    if(!eqp.getId().equals(1548589026927366144L)){
//                        eqpTmp = 1552938001553338368L;
//                    }
//                    List<MesEquipment> childrenList = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getParentCode, eqpTmp)
//                            .eq(MesEquipment::getStatus, OaConstant.NO.toString())
//                            .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
//                    Set<Long> eqpIdTmp2 = Sets.newHashSet();
//                    if(CollUtil.isNotEmpty(childrenList)){
//                        eqpIdTmp2.addAll(childrenList.stream().map(MesEquipment::getId).collect(Collectors.toSet()));
//                    }
//                    eqpIdTmp2.add(eqpTmp);
//                    mesEquipmentService.update(equipment,new LambdaQueryWrapper<MesEquipment>()
//                            .in(MesEquipment::getId,eqpIdTmp2)
//                            .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                    //校验父级
//                    List<MesEquipment> brotherList = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getParentCode, "1592305077745344512")
//                            .eq(MesEquipment::getStatus, OaConstant.NO.toString())
//                            .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
//                    int offNum = (int)brotherList.stream().filter(v->v.getState().equals(OaConstant.NUM_3.toString())).count();
//                    MesEquipment equipment1 = new MesEquipment();
//                    if(brotherList.size()==offNum){
//                        equipment1.setState(OaConstant.NUM_3.toString());
//                    }else {
//                        equipment1.setState(OaConstant.NO.toString());
//                    }
//                    mesEquipmentService.update(equipment1,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,brotherList.get(0).getParentCode())
//                            .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                }
//                Set<Long> eqpId3 =Sets.newHashSet(1731573403747045376L,1731573432738074624L,1731579657513029632L,1731579686487281664L,1731580128638226432L,1731580172787470336L
//                ,1548575683441250304L,1548583156571099136L,1548589026927366144L
//                ,1548582353395437568L,1548584532931956736L,1548590147603451904L);
//                if(eqpId2.contains(eqp.getId())||eqpId3.contains(eqp.getId())){
//                    //修改其下级设备
//                    List<MesEquipment> childrenList = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
//                            .like(MesEquipment::getParentCodes, eqp.getParentCode())
//                            .eq(MesEquipment::getStatus, OaConstant.NO.toString())
//                            .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
//                    if(CollUtil.isNotEmpty(childrenList)){
//                        idSet.addAll(childrenList.stream().map(MesEquipment::getId).collect(Collectors.toSet()));
//                    }
//                    idSet.add(eqp.getId());
//                    //对油泵电机进行操作 1#
//                    if(equipment.getState().equals(OaConstant.NUM_3.toString())&&idSet.contains(1543243396975095808L)){
//                        idSet.add(1554749971972489216L);
//                        idSet.add(1554750034203377664L);
//
//                    } //2#
//                    if(equipment.getState().equals(OaConstant.NUM_3.toString())&&idSet.contains(1543243870138724352L)){
//                        idSet.add(1554749911603871744L);
//                        idSet.add(1552858842006159360L);
//
//                    }
//                    mesEquipmentService.update(equipment,new LambdaQueryWrapper<MesEquipment>()
//                            .in(MesEquipment::getId,idSet)
//                            .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                    List<MesEquipment> brotherList = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getParentCode, eqp.getParentCode())
//                            .eq(MesEquipment::getStatus, OaConstant.NO.toString())
//                            .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
//                    //如果都一致如果状态都一致那么其上级设备也改变
//                    if(CollUtil.isNotEmpty(brotherList)){
//                        int offNum = (int)brotherList.stream().filter(v->v.getState().equals(OaConstant.NUM_3.toString())).count();
//                        //窑头布料皮带与混合机一致
//                        Set<Long> eqpIdSetTmp = Sets.newHashSet();
//                        eqpIdSetTmp.add(Long.parseLong(eqp.getParentCode()));
//                        if(eqp.getParentCode().equals("1590633171971465216")){
//                            eqpIdSetTmp.addAll(Sets.newHashSet(1548585834533539840L,1678677285362327552L,
//                                    1548586975526834176L));
//                        }
//                        if(offNum == brotherList.size()){
//                            MesEquipment eqpTmp1 = new MesEquipment();
//                            eqpTmp1.setState(OaConstant.NUM_3.toString());
//                            mesEquipmentService.update(eqpTmp1,new LambdaQueryWrapper<MesEquipment>()
//                                    .in(MesEquipment::getId,eqpIdSetTmp)
//                                    .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                        }else {
//                            MesEquipment eqpTmp1 = new MesEquipment();
//                            eqpTmp1.setState(OaConstant.NO.toString());
//                            mesEquipmentService.update(eqpTmp1,new LambdaQueryWrapper<MesEquipment>()
//                                    .in(MesEquipment::getId,eqpIdSetTmp)
//                                    .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                        }
//                    }
//                    //操作第三层
//                    if(eqpId3.contains(eqp.getId())){
//                        //定制螺旋给料机
//                        if(eqp.getEquipmentName().contains("螺旋给料机")){
//                            List<MesEquipment> luoList = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
//                                    .like(MesEquipment::getEquipmentName,"螺旋给料机")
//                                    .eq(MesEquipment::getStatus, OaConstant.NO.toString())
//                                    .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
//                                    .eq(MesEquipment::getState,OaConstant.NUM_3.toString()));
//                            luoList = luoList.stream().filter(v->!v.getEquipmentName().equals("螺旋给料机")).collect(Collectors.toList());
//                            if(CollUtil.isNotEmpty(luoList)&&luoList.size()==6){
//                                MesEquipment eqpTmp1 = new MesEquipment();
//                                eqpTmp1.setState(OaConstant.NUM_3.toString());
//                                mesEquipmentService.update(eqpTmp1,new LambdaQueryWrapper<MesEquipment>()
//                                        .eq(MesEquipment::getId,"1592083143237558272")
//                                        .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                            }else {
//                                MesEquipment eqpTmp1 = new MesEquipment();
//                                eqpTmp1.setState(OaConstant.NO.toString());
//                                mesEquipmentService.update(eqpTmp1,new LambdaQueryWrapper<MesEquipment>()
//                                        .eq(MesEquipment::getId,"1592083143237558272")
//                                        .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                            }
//                            ack.acknowledge();
//                            return;
//                        }
//                        //找到他的父亲节点
//                        MesEquipment parent = mesEquipmentService.getOne(new LambdaQueryWrapper<MesEquipment>()
//                                .eq(MesEquipment::getId, eqp.getParentCode())
//                                .eq(MesEquipment::getStatus, OaConstant.NO.toString())
//                                .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
//                                .last("limit 1"));
//                        if(Objects.nonNull(parent)){
//                            //找到兄弟节点
//                            List<MesEquipment> pBrotherList = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
//                                    .eq(MesEquipment::getParentCode, parent.getParentCode())
//                                    .eq(MesEquipment::getStatus, OaConstant.NO.toString())
//                                    .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
//                            if(CollUtil.isNotEmpty(pBrotherList)){
//                                int offNum = (int)pBrotherList.stream().filter(v->v.getState().equals(OaConstant.NUM_3.toString())).count();
//                                if(offNum == brotherList.size()){
//                                    MesEquipment eqpTmp1 = new MesEquipment();
//                                    eqpTmp1.setState(OaConstant.NUM_3.toString());
//                                    mesEquipmentService.update(eqpTmp1,new LambdaQueryWrapper<MesEquipment>()
//                                            .eq(MesEquipment::getId,pBrotherList.get(0).getParentCode())
//                                            .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                                }else {
//                                    MesEquipment eqpTmp1 = new MesEquipment();
//                                    eqpTmp1.setState(OaConstant.NO.toString());
//                                    mesEquipmentService.update(eqpTmp1,new LambdaQueryWrapper<MesEquipment>()
//                                            .in(MesEquipment::getId,pBrotherList.get(0).getParentCode())
//                                            .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
//                                }
//                            }
//                        }
//                    }
//                    ack.acknowledge();
//                    return;
//                }
//                //1#混合机油泵电机只有一台开
//                if(eqp.getId().equals(1554749971972489216L)){
//                    Long eqpIdTmp = 1554750034203377664L;
//                    MesEquipment equipmentTmp = new MesEquipment();
//                    if(equipment.getState().equals(OaConstant.NO.toString())
//                            ||equipment.getState().equals(OaConstant.NUM_2.toString())){
//                        equipmentTmp.setState(OaConstant.NUM_3.toString());
//                    }else {
//                        equipmentTmp.setState(OaConstant.NO.toString());
//                    }
//                    mesEquipmentService.update(equipmentTmp,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,eqpIdTmp));
//                    ack.acknowledge();
//                    return;
//                } else if(eqp.getId().equals(1554750034203377664L)){
//                    Long eqpIdTmp = 1554749971972489216L;
//                    MesEquipment equipmentTmp = new MesEquipment();
//                    if(equipment.getState().equals(OaConstant.NO.toString())
//                            ||equipment.getState().equals(OaConstant.NUM_2.toString())){
//                        equipmentTmp.setState(OaConstant.NUM_3.toString());
//                    }else {
//                        equipmentTmp.setState(OaConstant.NO.toString());
//                    }
//                    mesEquipmentService.update(equipmentTmp,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,eqpIdTmp));
//                    ack.acknowledge();
//                    return;
//                }
//
//                //2#混合机油泵电机只有一台开
//                if(eqp.getId().equals(1552858842006159360L)){
//                    Long eqpIdTmp = 1554749911603871744L;
//                    MesEquipment equipmentTmp = new MesEquipment();
//                    if(equipment.getState().equals(OaConstant.NO.toString())
//                            ||equipment.getState().equals(OaConstant.NUM_2.toString())){
//                        equipmentTmp.setState(OaConstant.NUM_3.toString());
//                    }else {
//                        equipmentTmp.setState(OaConstant.NO.toString());
//                    }
//                    mesEquipmentService.update(equipmentTmp,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,eqpIdTmp));
//                    ack.acknowledge();
//                    return;
//                } else if(eqp.getId().equals(1552858842006159360L)){
//                    Long eqpIdTmp = 1552858842006159360L;
//                    MesEquipment equipmentTmp = new MesEquipment();
//                    if(equipment.getState().equals(OaConstant.NO.toString())
//                            ||equipment.getState().equals(OaConstant.NUM_2.toString())){
//                        equipmentTmp.setState(OaConstant.NUM_3.toString());
//                    }else {
//                        equipmentTmp.setState(OaConstant.NO.toString());
//                    }
//                    mesEquipmentService.update(equipmentTmp,new LambdaQueryWrapper<MesEquipment>()
//                            .eq(MesEquipment::getId,eqpIdTmp));
//                    ack.acknowledge();
//                    return;
//                }
//            }
            //只有原料车间的设备才需要考虑待机停机
//            if(!eqp.getParentCodes().contains("1501738403312599040")){
//                equipment.setState(OaConstant.NUM_3.toString());
//
//            }
            idSet.add(eqp.getId());
            //混合机和电机一致
            if(eqp.getEquipmentCode().equals("TC2_YL1_PL_01_01")||eqp.getEquipmentCode().equals("TC2_YL1_PL_02_03")){
                MesEquipment parent = mesEquipmentService.getOne(new LambdaQueryWrapper<MesEquipment>()
                        .eq(MesEquipment::getId, eqp.getParentCode())
                        .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                        .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
                        .last("limit 1"));
                idSet.add(parent.getId());
                mesEquipmentService.update(equipment,new LambdaQueryWrapper<MesEquipment>()
                        .in(MesEquipment::getId,idSet)
                        .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
                ack.acknowledge();
                return;
            }
            //所有下级设备改变状态
            List<MesEquipment> childrenList = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
                    .like(MesEquipment::getParentCodes, eqp.getId().toString()+",")
                    .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                    .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
            if(CollUtil.isNotEmpty(childrenList)){
                //仅对设备信号外的设备有效
                childrenList.forEach(tmp->{
                    idSet.add(tmp.getId());
                });
            }

            //混合机开则电机开
            if(idSet.contains(1554749971972489216L) || idSet.contains(1554750034203377664L)){
                if(equipment.getState().equals(OaConstant.NO.toString())){
                    idSet.add(1731569543175426048L);
                }

            }else if(idSet.contains(1552858842006159360L) || idSet.contains(1554749911603871744L)){
                if(equipment.getState().equals(OaConstant.NO.toString())){
                    idSet.add(1554750094693629952L);
                }
            }
            mesEquipmentService.update(equipment,new LambdaQueryWrapper<MesEquipment>()
                    .in(MesEquipment::getId,idSet)
                    .eq(MesEquipment::getStatus, OaConstant.NO.toString()));

            if(eqp.getParentCodes().contains("1501738403312599040")){
                //定制化螺旋给料机
                if(eqp.getEquipmentCode().contains("TC2_YL1_PL_19")){
                    updateScrewEqp(eqp.getParentCode());
                }else if(eqp.getEquipmentCode().contains("TC2_YL1_PL_20")){
                    updateScrewEqp(eqp.getParentCode());
                }else if(eqp.getEquipmentCode().contains("TC2_YL1_PL_21")){
                    updateScrewEqp(eqp.getParentCode());
                }else if(eqp.getEquipmentCode().contains("TC2_YL1_PL_22")){
                    updateScrewEqp(eqp.getParentCode());
                }else if(eqp.getEquipmentCode().contains("TC2_YL1_PL_23")){
                    updateScrewEqp(eqp.getParentCode());
                }else if(eqp.getEquipmentCode().contains("TC2_YL1_PL_24")){
                    updateScrewEqp(eqp.getParentCode());
                }
                else if(eqp.getEquipmentCode().contains("TC2_YL1_PL_01_02")){
                    updateScrewEqp(eqp.getParentCode());
                }else if(eqp.getEquipmentCode().contains("TC2_YL1_PL_01_03")){
                    updateScrewEqp(eqp.getParentCode());
                }else if(eqp.getEquipmentCode().contains("TC2_YL1_PL_02_01")){
                    updateScrewEqp(eqp.getParentCode());
                }else if(eqp.getEquipmentCode().contains("TC2_YL1_PL_02_02")){
                    updateScrewEqp(eqp.getParentCode());
                }else {
                    //处理该设备的设备树
                    String rootEqpCode = cutString(eqp.getEquipmentCode());
                    MesEquipment eqpRoot = mesEquipmentService.getOne(new LambdaQueryWrapper<MesEquipment>()
                            .eq(MesEquipment::getEquipmentCode, rootEqpCode)
                            .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                            .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
                            .last("limit 1"));
                    if(Objects.nonNull(eqpRoot)){
                        List<MesEquipment> eqpTree = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
                                .like(MesEquipment::getParentCodes, eqpRoot.getId().toString()+",")
                                .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                                .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
                        eqpTree.add(eqpRoot);
                        //对整颗设备树进行处理
                        if(CollUtil.isNotEmpty(eqpTree)){
                            // 构建树状结构
                            Map<String, MesEquipment> equipmentMap = Maps.newHashMap();
                            for (MesEquipment equipmentTmp : eqpTree) {
                                equipmentMap.put(equipmentTmp.getId().toString(), equipmentTmp);
                            }
                            for (MesEquipment equipmentTmp : eqpTree) {
                                if (equipment.getParentCode() != null) {
                                    MesEquipment parent = equipmentMap.get(equipmentTmp.getParentCode());
                                    if (parent != null) {
                                        parent.getChildren().add(equipment);
                                    }
                                }
                            }
                            MesEquipment root = equipmentMap.get(rootEqpCode);
                            // 更新节点状态
                            // map用于记录被更新的节点ID和状态
                            Map<String,Set<Long>> updatedIds = Maps.newHashMap();
                            updateState(root,updatedIds);
                            if(CollUtil.isNotEmpty(updatedIds)){
                                for(Map.Entry<String,Set<Long>> entry:updatedIds.entrySet()){
                                    MesEquipment eqpTmp = new MesEquipment();
                                    eqpTmp.setState(entry.getKey());
                                    mesEquipmentService.update(equipment,new LambdaQueryWrapper<MesEquipment>()
                                            .in(MesEquipment::getId,entry.getValue())
                                            .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
                                }
                            }
                        }else {
                            log.error(rootEqpCode+"设备树不存在");
                        }
                    }else {
                        log.error(rootEqpCode+"设备不存在");
                    }

                }

            }



            /*
            MesEquipment parent = mesEquipmentService.getOne(new LambdaQueryWrapper<MesEquipment>()
                    .eq(MesEquipment::getEquipmentCode, eqp.getEquipmentCode())
                    .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                    .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
                    .last("limit 1"));
            //设备改变状态时，例如1)若下级设备全部停止 2)同级是否都停止，若都停止则上级也停止
            //1)下级设备全部停止
            List<MesEquipment> childrenList = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
                    .eq(MesEquipment::getParentCode, config.getRefContent())
                    .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                    .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
            if(CollUtil.isNotEmpty(childrenList)){
                //仅对设备信号外的设备有效
                childrenList.forEach(tmp->{
                    if(!signalCode.contains(tmp.getEquipmentCode())){
                        idSet.add(tmp.getId());
                    }
                });
            }
            2)同级是否是否都停止，若都停止则上级也停止
            List<MesEquipment> platList = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
                    .eq(MesEquipment::getParentCode, eqp.getParentCode())
                    .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                    .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
            if(CollUtil.isNotEmpty(platList)){
                //判断同级设备是否都停止
                int size = platList.size();
                platList = platList.stream().filter(v->v.getState().equals(equipment.getState())).collect(Collectors.toList());
                if(size == platList.size()&&!signalCode.contains(parent.getEquipmentCode())){
                    idSet.add(parent.getId());
                }
            }

            mesEquipmentService.update(equipment,new LambdaQueryWrapper<MesEquipment>()
                    .in(MesEquipment::getId,idSet)
                    .eq(MesEquipment::getStatus, OaConstant.NO.toString()));
             */
        }
        //找到对应关系
//        //然后发送到websocket中
//        if (CollUtil.isNotEmpty(msgPushDTO.getUserIdList())) {
//            if (WebsocketConstant.WsPushLevelEnum.LEVEL_MORE.getType().equals(msgPushDTO.getPushLevel())) {
//                Set<Long> userIdList = msgPushDTO.getUserIdList();
//                msgPushDTO.setUserIdList(null);
//                WebSocketManager.sendMoreMessage(Lists.newArrayList(userIdList), JSONUtil.toJsonStr(msgPushDTO));
//            } else if (WebsocketConstant.WsPushLevelEnum.LEVEL_ONLINE_ALL.getType().equals(msgPushDTO.getPushLevel())) {
//                WebSocketManager.sendMessageToOnlineUser(record.value());
//            }
//        }
        ack.acknowledge();
    }

    private void updateScrewEqp(String parentCode){
        //找到父级
        MesEquipment parent = mesEquipmentService.getOne(new LambdaQueryWrapper<MesEquipment>()
                .eq(MesEquipment::getId, parentCode)
                .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                .eq(MesEquipment::getIsEqp, OaConstant.NO.toString())
                .last("limit 1"));
        //找到子集
        List<MesEquipment> childrenTmp = mesEquipmentService.list(new LambdaQueryWrapper<MesEquipment>()
                .eq(MesEquipment::getParentCode, parent.getId())
                .eq(MesEquipment::getStatus, OaConstant.NO.toString())
                .eq(MesEquipment::getIsEqp, OaConstant.NO.toString()));
        //有一个开机则开机,否则关机
        String state = "0";
        for(MesEquipment tmp:childrenTmp){
            if(tmp.getState().equals(OaConstant.NO.toString())||tmp.getState().equals(OaConstant.NO.toString())){
                state = tmp.getState();
                break;
            }else {
                state = tmp.getState();
            }
        }
        MesEquipment tmp = new MesEquipment();
        tmp.setId(parent.getId());
        tmp.setState(state);
        mesEquipmentService.updateById(tmp);
    };

    protected String cutString(String input) {
        // 找到第三个和第四个_符号的位置
        int firstUnderscore = input.indexOf('_');
        int secondUnderscore = input.indexOf('_', firstUnderscore + 1);
        int thirdUnderscore = input.indexOf('_', secondUnderscore + 1);
        int fourthUnderscore = input.indexOf('_', thirdUnderscore + 1);

        // 如果找到第四个_符号
        if (fourthUnderscore != -1) {
            // 截取从开头到第三个_符号之后第四个_符号之前的字符串
            return input.substring(0, fourthUnderscore);
        } else {
            // 如果没有第四个_符号，就返回原字符串
            return input;
        }
    }

    private static void updateState(MesEquipment node,Map<String,Set<Long>> changeEqp) {
        if (node == null) return;
        for (MesEquipment child : node.getChildren()) {
            updateState(child,changeEqp);
        }

        if (!node.getChildren().isEmpty()) {
            String firstChildState = node.getChildren().get(0).getState();
            boolean allSame = node.getChildren().stream().allMatch(c -> c.getState().equals(firstChildState));
            if (allSame) {
                node.setState(firstChildState);
                changeEqp.computeIfAbsent(firstChildState, k -> new HashSet<>()).add(node.getId());
            }
        }
    }
}
