package com.steccn.mes.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
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
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
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
    @KafkaListener(topics = "opc_ua_all_topic", concurrency = "2")
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
                            sysOffices = jsSysOfficeService.list(new LambdaQueryWrapper<JsSysOffice>()
                                    .select(JsSysOffice::getOfficeCode)
                                    .like(JsSysOffice::getParentCodes, departmentStr)
                                    .eq(JsSysOffice::getStatus,OaConstant.NO));
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
            //只有原料车间的设备才需要考虑待机停机
            if(!eqp.getParentCodes().contains("1501738403312599040")){
                equipment.setState(OaConstant.NUM_3.toString());
            }
            if(Objects.isNull(eqp)){
                log.error(config.getRefContent()+"设备不存在");
                throw new BusinessException(config.getRefContent()+"设备不存在");
            }
            idSet.add(eqp.getId());
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
            //2)同级是否是否都停止，若都停止则上级也停止
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
}
