package com.steccn.qms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.WriteContext;
import com.alibaba.excel.metadata.data.ClientAnchorData;
import com.alibaba.excel.util.FileTypeUtils;
import com.alibaba.excel.util.MapUtils;
import com.alibaba.excel.util.StyleUtil;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.fill.FillConfig;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.common.dto.BiStackGraphDTO;
import com.steccn.common.dto.QtLineChartBiBarDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.common.util.*;
import com.steccn.common.vo.BaseSelectVO;
import com.steccn.common.vo.QtTaskClassCalculate;
import com.steccn.common.vo.R;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.constant.OaManagerConstant;
import com.steccn.oa.dto.OaGroupCountDTO;
import com.steccn.oa.dto.OaRosterRecordDTO;import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.oa.vo.OaGroupIdAndNameVO;
import com.steccn.oa.vo.OaRosterRecordVO;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.constant.SysOfficeConstant;
import com.steccn.platform.dto.SysMsgSendDTO;import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.feign.RemoteSpareFeignApi;
import com.steccn.platform.util.LogicalPageUtil;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.pms.constant.PmManagerConstant;
import com.steccn.pms.feign.RemotePmsFeignApi;
import com.steccn.pms.vo.PmsProduceOrderVO;
import com.steccn.pms.vo.PmsWorkOrderBatchVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.*;
import com.steccn.qms.dto.*;
import com.steccn.qms.mapper.QtCheckTaskCopyMapper;
import com.steccn.qms.mapper.QtCheckTaskDestoryInfoMapper;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.service.*;
import com.steccn.qms.util.CodeGenerateUtil;
import com.steccn.qms.util.DownloadModelUtil;
import com.steccn.qms.util.DownloadUtil;
import com.steccn.qms.vo.*;
import com.steccn.websocket.constant.WebsocketConstant;
import com.steccn.websocket.dto.MsgPushDTO;
import com.steccn.websocket.feign.RemoteWsFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author AndrewLuo
 * @description 针对表【qt_check_task(巡检任务表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QtCheckTaskServiceImpl extends ServiceImpl<QtCheckTaskMapper, QtCheckTask>
        implements QtCheckTaskService {

    private final QtCheckTaskMapper qtCheckTaskMapper;

    private final RemoteOfficeFeign remoteOfficeFeign;

    @Autowired
    private  QtMaterialService qtMaterialService;

    @Autowired
    private QtCheckPlanService qtCheckPlanService;

    @Autowired
    private QtCheckTaskDetailService qtCheckTaskDetailService;

    private final QtRecheckTaskService qtRecheckTaskService;

    private final QtCheckTaskCopyMapper qtCheckTaskCopyMapper;

    private final RemotePmsFeignApi remotePmsFeignApi;

    private final QtCheckTaskDestoryInfoService qtCheckTaskDestoryInfoService;

    private final QtCheckTaskCopyService qtCheckTaskCopyService;

    private final QtCheckTaskDestoryInfoMapper qtCheckTaskDestoryInfoMapper;

    private final RemoteSpareFeignApi remoteSpareFeignApi;
    private final RemoteWsFeignApi remoteWsFeignApi;

    private final OaInfoService oaInfoService;

    private final RemoteMsgFeignApi remoteMsgFeignApi;

    /**
     * 新增
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(QtAddCheckTaskDTO dto) {
        //巡检抽检时间必填
        if(dto.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())){
            Assert.notNull(dto.getSpotTime(),"巡检任务的抽检时间不能为空");
        }
        //来料检到货日期为必填
        if(dto.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode())){
            Assert.notNull(dto.getArrivalTime(),"来料检任务到货日期不能为空");
        }
        //在线检校验缺陷开始时间参数
        if(dto.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())){
            QtCheckOnlineTaskVO qtCheckOnlineTaskVO = checkOnline(dto);
            //只有重复才会报错
            if(qtCheckOnlineTaskVO.getIsChecked().equals(OaConstant.NUM_1)){
                throw new BusinessException(qtCheckOnlineTaskVO.getMsg());
            }
        }
        //1）仅当生产车间为深加工车间且检验类型为在线检时，默认值为已选中生产产线的“钢化工位”，只读
        if(dto.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                &&dto.getProductWorkshop()!=null
                &&dto.getProductWorkshop().getName().contains(QtManagerConstant.DEEP_OFFICE_NAME)){
            if(dto.getProductLine()!=null
                    &&dto.getProductLine().getId()!=null){
                SysOfficeParentDTO officeDto = new SysOfficeParentDTO();
                officeDto.setId(dto.getProductLine().getId());
                officeDto.setOfficeType(SysOfficeConstant.STATION);
                List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if(CollUtil.isEmpty(sysOfficeVOS)){
                    throw new BusinessException("该产线下没有工位");
                }
                SysOfficeVO sysOfficeVO = sysOfficeVOS.stream().filter(v -> v.getOfficeName().equals(QtManagerConstant.STELL_STATION)).findFirst().orElse(null);
                if(Objects.isNull(sysOfficeVO)){
                    throw new BusinessException("该产线下没有钢化工位");
                }
                dto.setStationId(sysOfficeVO.getId().longValue());
                dto.setStationName(sysOfficeVO.getOfficeName());
                QtBaseIdNameDTO station = new QtBaseIdNameDTO();
                station.setId(sysOfficeVO.getId());
                station.setName(sysOfficeVO.getOfficeName());
                dto.setStation(station);
            }
        }else if(dto.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode())
                ||dto.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())){
            //2）仅当生产车间为深加工车间且检验类型为首检/巡检时，默认值为已选中生产产线的“下片工位”，只读
            if(dto.getProductWorkshop()!=null
                    &&dto.getProductWorkshop().getName().contains(QtManagerConstant.DEEP_OFFICE_NAME)){
                if(dto.getProductLine()!=null
                        &&dto.getProductLine().getId()!=null){
                    SysOfficeParentDTO officeDto = new SysOfficeParentDTO();
                    officeDto.setId(dto.getProductLine().getId());
                    officeDto.setOfficeType(SysOfficeConstant.STATION);
                    List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    if(CollUtil.isEmpty(sysOfficeVOS)){
                        throw new BusinessException("该产线下没有工位");
                    }
                    SysOfficeVO sysOfficeVO = sysOfficeVOS.stream().filter(v -> v.getOfficeName().equals(QtManagerConstant.DOWN_STATION)).findFirst().orElse(null);
                    if(Objects.isNull(sysOfficeVO)){
                        throw new BusinessException("该产线下没有下片工位");
                    }
                    dto.setStationId(sysOfficeVO.getId().longValue());
                    dto.setStationName(sysOfficeVO.getOfficeName());
                    QtBaseIdNameDTO station = new QtBaseIdNameDTO();
                    station.setId(sysOfficeVO.getId());
                    station.setName(sysOfficeVO.getOfficeName());
                    dto.setStation(station);
                }
            }
        }else if(dto.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                &&dto.getProductWorkshop()!=null
                &&dto.getProductWorkshop().getName().contains(QtManagerConstant.JIPIAN)){
            //3）仅当生产车间为基片车间且检验类型为在线检时，默认值为已选中生产产线的“成型工位”，只读
            if(dto.getProductLine()!=null
                    &&dto.getProductLine().getId()!=null){
                SysOfficeParentDTO officeDto = new SysOfficeParentDTO();
                officeDto.setId(dto.getProductLine().getId());
                officeDto.setOfficeType(SysOfficeConstant.STATION);
                List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if(CollUtil.isEmpty(sysOfficeVOS)){
                    throw new BusinessException("该产线下没有工位");
                }
                SysOfficeVO sysOfficeVO = sysOfficeVOS.stream().filter(v -> v.getOfficeName().equals(QtManagerConstant.FORMING_STATION)).findFirst().orElse(null);
                if(Objects.isNull(sysOfficeVO)){
                    throw new BusinessException("该产线下没有成型工位");
                }
                dto.setStationId(sysOfficeVO.getId().longValue());
                dto.setStationName(sysOfficeVO.getOfficeName());
                QtBaseIdNameDTO station = new QtBaseIdNameDTO();
                station.setId(sysOfficeVO.getId());
                station.setName(sysOfficeVO.getOfficeName());
                dto.setStation(station);
            }

        }else if(dto.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode())
                &&dto.getProductWorkshop()!=null
                &&dto.getProductWorkshop().getName().contains(QtManagerConstant.JIPIAN)){
            //4）仅当生产车间为基片车间且检验类型为化验抽检时，默认值为已选中生产产线的“下片工位”，只读
            if(dto.getProductLine()!=null
                    &&dto.getProductLine().getId()!=null){
                SysOfficeParentDTO officeDto = new SysOfficeParentDTO();
                officeDto.setId(dto.getProductLine().getId());
                officeDto.setOfficeType(SysOfficeConstant.STATION);
                List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                if(CollUtil.isEmpty(sysOfficeVOS)){
                    throw new BusinessException("该产线下没有工位");
                }
                SysOfficeVO sysOfficeVO = sysOfficeVOS.stream().filter(v -> v.getOfficeName().equals(QtManagerConstant.DOWN_STATION)).findFirst().orElse(null);
                if(Objects.isNull(sysOfficeVO)){
                    throw new BusinessException("该产线下没有下片工位");
                }
                dto.setStationId(sysOfficeVO.getId().longValue());
                dto.setStationName(sysOfficeVO.getOfficeName());
                QtBaseIdNameDTO station = new QtBaseIdNameDTO();
                station.setId(sysOfficeVO.getId());
                station.setName(sysOfficeVO.getOfficeName());
                dto.setStation(station);
            }
        }


        //首检、在线检、巡检班组必填
        if(dto.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
            ||dto.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode())
            ||dto.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())){
            Assert.notNull(dto.getClassGroupName(),"首检、在线检、巡检班组不能为空");
            if(dto.getClassGroupName().contains(PmManagerConstant.METHYL_CLASS_NAME)){
                dto.setClassGroupId(QtManagerConstant.NUM_0);
            }else if(dto.getClassGroupName().contains(PmManagerConstant.ETHYL_CLASS_NAME)){
                dto.setClassGroupId(QtManagerConstant.NUM_1);
            }else if(dto.getClassGroupName().contains(PmManagerConstant.PROPYLENE_CLASS_NAME)){
                dto.setClassGroupId(QtManagerConstant.NUM_2);
            }
        }
        QtCheckTask qtCheckTask = new QtCheckTask();
        BeanUtils.copyProperties(dto, qtCheckTask);
        QtBaseIdNameDTO qtBaseIdNameDTO = new QtBaseIdNameDTO();
        qtBaseIdNameDTO.setId(dto.getMaterialWorkshopId());
        qtBaseIdNameDTO.setName(dto.getMaterialWorkshopName());
        qtCheckTask.setMaterialWorkshop(qtBaseIdNameDTO);
        //如果新建检验类型为在线检，缺陷记录时段不允许跨班次;即8:00、20:00两个时间点
        if (QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode().equals(dto.getTaskType())) {
            if (dto.getDefectStartTime() != null && dto.getDefectEndTime() != null) {
                //是否跨班次
                Boolean flag = CalculateDateUtil.isSameClassType(dto.getDefectStartTime(),dto.getDefectEndTime());
                QtTaskClassCalculate defectStart = CalculateDateUtil.calculateClassTime(dto.getDefectStartTime());
//                QtTaskClassCalculate defectEnd = CalculateDateUtil.calculateClassTime(dto.getDefectEndTime());
                //判断是否跨班次和跨时间维度
                if(flag){
                    qtCheckTask.setClassesId(defectStart.getClassType());
                }else {
                    throw new BusinessException("缺陷记录时段不允许跨班次");
                }
            }
            Assert.notNull("缺陷记录时段不能为空");
        }
//        //产线为空校验
//        if (dto.getProductLine() == null || "null".equals(dto.getProductLine().getName() )){
//            throw  new BusinessException("产线不能为空");
//        }

        //状态待检验
        qtCheckTask.setStatus(0);
        if (dto.getOriginalId() != null) {
            //复检任务
            qtCheckTask.setType(1);
        } else {
            //正常任务
            qtCheckTask.setType(0);
        }
        //获取最大的自增编码
        qtCheckTask.setCode(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_JYRW, CodeGenerateUtil.NUM_BIT_8, currentMaxCode()));
        /**
         * 判断是否需要创建检验方案快照【方案+分组+检验项+缺陷项】
         *      plan1 v1 -> T1 需要创建快照，plan1' v1
         *               -> T2 去查询最新版本是v1 不需要创建快照
         *               -> 分组，缺陷项和检验项变化会造成方法版本更新
         *      plan2 v2 -> T3 去查询最新版本是v2 需要创建快照 plan2' v2
         *               -> T4 去查询最新版本是v2 不需要创建快照
         */
        //获取检验方案id
        Integer checkPlanId = qtCheckTask.getCheckPlanId();
        //判断检验方案是否存在
        QtCheckPlan qtCheckPlan = qtCheckPlanService.getById(checkPlanId);
        if (Objects.isNull(qtCheckPlan)) {
            throw new BusinessException("检验方案不存在");
        }
        //fix bug 检验方案为快照，无法创建任务
        if (MpConstant.NO.equals(qtCheckPlan.getShowFlag())) {
            throw new BusinessException("检验方案是快照，不可以用于创建任务");
        }
        //获取最大版本号的快照数据
        QtCheckPlan maxSnapshot = qtCheckPlanService.getOne(new LambdaQueryWrapper<QtCheckPlan>()
                .eq(QtCheckPlan::getOriginalId, checkPlanId)
                .eq(QtCheckPlan::getShowFlag, MpConstant.NO)
                .orderByDesc(QtCheckPlan::getVersion)
                .last(MpConstant.STR_LIMIT_1));
        //如果第一次进来没有快照，或者快照版本已比当前版本低则重建快照，绑定最新的数据
        if (Objects.isNull(maxSnapshot) || maxSnapshot.getVersion() < qtCheckPlan.getVersion()) {
            QtCheckPlan planSnapshot = qtCheckPlanService.planSnapshot(checkPlanId);
            qtCheckTask.setCheckPlanId(planSnapshot.getId());
        } else {
            //取旧的快照
            qtCheckTask.setCheckPlanId(maxSnapshot.getId());
        }
        //复检任务需要置空
        if (dto.getOriginalId() != null) {
            qtCheckTask.setFinishedTime(null);
            qtCheckTask.setFinishedName(null);
            qtCheckTask.setFinishedBy(null);
            qtCheckTask.setCancelTime(null);
            qtCheckTask.setCancelBy(null);
            qtCheckTask.setCancelName(null);
            qtCheckTask.setStartTime(null);
            qtCheckTask.setStartBy(null);
            qtCheckTask.setStartName(null);
            qtCheckTask.setClassesId(null);
            qtCheckTask.setClassTime(null);
        }
        save(qtCheckTask);
        //如果有原始任务id,则表示为复检任务
        if (dto.getOriginalId() != null) {
            //复检任务
            QtCheckTask originalTask = getById(dto.getOriginalId());
            QtRecheckTask recheckModel = new QtRecheckTask();
            recheckModel.setOriginalId(dto.getOriginalId());
            recheckModel.setRecheckId(qtCheckTask.getId());
            recheckModel.setOriginalCode(originalTask.getCode());
            qtRecheckTaskService.save(recheckModel);
        }
        return qtCheckTask.getId();
    }

    @Override
    public QtCheckOnlineTaskVO checkOnline(QtAddCheckTaskDTO dto) {
        Assert.notNull(dto.getProductWorkshop().getId(),"车间不能为空");
        Assert.notNull(dto.getProductLine().getId(),"产线不能为空");
        Assert.notNull(dto.getDefectStartTime(),"缺陷开始时间不能为空");
        Assert.notNull(dto.getDefectStartTime(),"缺陷结束时间不能为空");
        QtCheckOnlineTaskVO result = new QtCheckOnlineTaskVO();
        //基片二车间id为76
        if(!dto.getProductWorkshop().getId().equals(76)){
            result.setIsChecked(OaConstant.NO);
            return result;
        }
        //新建任务，缺陷记录时段选中t1~t2，点击“保存”时，若选中的产线在t1~t2已存在已检验的在线检任务，则无法提交，toast提示“t1~t2已完成在线检，请更改缺陷记录时段”（t1、t2的格式均为hh:mm）
        QtCheckTask taskOne = getOne(new LambdaQueryWrapper<QtCheckTask>()
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .eq(QtCheckTask::getProductLineId, dto.getProductLine().getId())
                .eq(QtCheckTask::getDefectStartTime,dto.getDefectStartTime())
                .eq(QtCheckTask::getDefectEndTime,dto.getDefectEndTime())
                .last("LIMIT 1"));
        if(Objects.nonNull(taskOne)){
            result.setMsg(dto.getDefectStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))+"~"
                    +dto.getDefectEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    +"存在已完成在线检,请更改缺陷记录时段");
            result.setIsChecked(OaConstant.NUM_1);
            return result;
        }else {
            //仅当“生产车间”选中“基片二车间”且任务类型为“在线检”时，进行如下校验避免“缺陷记录时段”遗漏：
            //新建任务，缺陷记录时段选中t1~t2，t1所在的班次日期为D，以下12个时段在区间[t1所在班次的开始时间, t1]内的有n个
            LocalDate classDate = LocalDateUtil.getClassDate(dto.getDefectStartTime(), false);
            LocalDateTime classDateTime = classDate.atTime(LocalTime.of(8,0,0,0));
            List<QtCheckTask> taskList = list(new LambdaQueryWrapper<QtCheckTask>()
                    .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                    .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                    .eq(QtCheckTask::getProductLineId, dto.getProductLine().getId())
                    .ge(QtCheckTask::getDefectStartTime,classDateTime)
                    .le(QtCheckTask::getDefectEndTime,dto.getDefectStartTime()));
            for(int i = 0; i <12;i++){
                LocalDateTime timeBeforeTmp = classDateTime.plusHours(i*2);
                LocalDateTime timeAfterTmp = timeBeforeTmp.plusHours(2L);
                QtCheckTask qtCheckTask = taskList.stream().filter(v -> v.getDefectStartTime().equals(timeBeforeTmp)
                        && v.getDefectEndTime().equals(timeAfterTmp)).findFirst().orElse(null);
                if(Objects.isNull(qtCheckTask)){
                    result.setDefectStartTime(timeBeforeTmp);
                    result.setDefectEndTime(timeAfterTmp);
                    result.setIsChecked(OaConstant.NUM_2);
                    result.setMsg(timeBeforeTmp.format(DateTimeFormatter.ofPattern("HH:mm"))+"~"
                            +timeAfterTmp.format(DateTimeFormatter.ofPattern("HH:mm"))
                            +"尚未检验，是否为此时段新建任务？");
                    return result;
                }
            }
        }

        return result;
    }

    @Override
    public Long checkFinishedOnline(QtAddCheckTaskDTO dto) {
        Assert.notNull(dto.getProductLine().getId(),"产线不能为空");
        Assert.notNull(dto.getDefectStartTime(),"缺陷开始时间不能为空");
        Assert.notNull(dto.getDefectStartTime(),"缺陷结束时间不能为空");
        QtCheckTask taskOne = getOne(new LambdaQueryWrapper<QtCheckTask>()
                .select(QtCheckTask::getId)
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .eq(QtCheckTask::getProductLineId, dto.getProductLine().getId())
                .eq(QtCheckTask::getDefectStartTime,dto.getDefectStartTime())
                .eq(QtCheckTask::getDefectEndTime,dto.getDefectEndTime())
                .last("LIMIT 1"));
        if(Objects.isNull(taskOne)){
            return 0L;
        }
        return taskOne.getId();
    }

    /**
     * 任务列表
     *
     * @param dto
     * @return
     */
    @Override
    public List<QtCheckTaskListVO> checkTaskList(QtCheckTaskListDTO dto) {
        Assert.notNull(dto.getCheckTypeCode(), "检验类型不能为空");
        Assert.notNull(dto.getStatus(), "任务状态不能为空");
        List<QtCheckTask> qtCheckTasks = qtCheckTaskMapper.selectByTaskType(dto.getCheckTypeCode(), dto.getStatus());
        List<QtCheckTaskListVO> vos = new ArrayList<>();
        if (qtCheckTasks != null && qtCheckTasks.size() > 0) {
            qtCheckTasks.forEach(tmp -> {
                QtCheckTaskListVO qtCheckTaskListVo = new QtCheckTaskListVO();
                qtCheckTaskListVo.setId(tmp.getId());
                qtCheckTaskListVo.setStatus(tmp.getStatus());
                if(tmp.getSpotTime()!=null){
                    qtCheckTaskListVo.setSpotTime(tmp.getSpotTime());
                }
                //返回名字
                qtCheckTaskListVo.setMaterialName(qtMaterialService.getById(tmp.getMaterialId()).getName());
                qtCheckTaskListVo.setProductWorkshopName(remoteOfficeFeign.getNameById(tmp.getProductWorkshop().getId()).getMsg());
                qtCheckTaskListVo.setProductLineName(remoteOfficeFeign.getNameById(tmp.getProductLine().getId()).getMsg());
                vos.add(qtCheckTaskListVo);
            });
        }
        return vos;
    }

    /**
     * 任务详情
     *
     * @param id
     * @param isPc
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public QtCheckTaskDetailVO taskDetail(Long id, Boolean isPc) {
        QtCheckTask qtCheckTask = getById(id);
        //转换一下班组
        if(StrUtil.isNotBlank(qtCheckTask.getClassGroupName())){
            if(qtCheckTask.getClassGroupName().contains(PmManagerConstant.METHYL_CLASS_NAME)){
                qtCheckTask.setClassGroupName(PmManagerConstant.METHYL_CLASS_NAME);
            }else if(qtCheckTask.getClassGroupName().contains(PmManagerConstant.ETHYL_CLASS_NAME)){
                qtCheckTask.setClassGroupName(PmManagerConstant.ETHYL_CLASS_NAME);
            }else if(qtCheckTask.getClassGroupName().contains(PmManagerConstant.PROPYLENE_CLASS_NAME)){
                qtCheckTask.setClassGroupName(PmManagerConstant.PROPYLENE_CLASS_NAME);
            }
        }
        Assert.notNull(qtCheckTask, "检验记录不存在");
        QtCheckTaskListVO qtCheckTaskListVo = new QtCheckTaskListVO();
        QtCheckPlanDetailsVO qtCheckPlanDetailsVO = new QtCheckPlanDetailsVO();
        if (qtCheckTask != null) {
            //计划id
            qtCheckTaskListVo.setCheckPlanName(qtCheckPlanService.getById(qtCheckTask.getCheckPlanId()).getName());
            if (qtCheckTask.getMaterialWorkshop() != null) {
                qtCheckTask.setMaterialWorkshopName(qtCheckTask.getMaterialWorkshop().getName());
            }
            if (qtCheckTask.getSupplier() != null) {
                qtCheckTask.setSupplierName(qtCheckTask.getSupplier().getName());
            }
            if (qtCheckTask.getCustomer() != null) {
                qtCheckTask.setCustomerName(qtCheckTask.getCustomer().getName());
            }
            if (qtCheckTask.getProductWorkshop() != null) {
                qtCheckTask.setProductWorkshopName(qtCheckTask.getProductWorkshop().getName());
            }
            if (qtCheckTask.getProductLine() != null) {
                qtCheckTask.setProductLineName(qtCheckTask.getProductLine().getName());
            }
            //如果是暂存状态，则返回检验中
            if (qtCheckTask.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.TEMPORARY.getCode())) {
                qtCheckTask.setStatus(QtManagerConstant.QtCheckTaskStatusEnum.CHECKING.getCode());
            }
            BeanUtils.copyProperties(qtCheckTask, qtCheckTaskListVo);
            //json名字
            QtMaterial qtMaterial = qtMaterialService.getById(qtCheckTask.getMaterialId());
            if(Objects.nonNull(qtMaterial)){
                qtCheckTaskListVo.setMaterialName(qtMaterial.getName());
                qtCheckTaskListVo.setMaterialCode(qtMaterial.getCode());
            }
            if(qtCheckTask.getMaterialWorkshop() != null) qtCheckTaskListVo.setMaterialWorkshopName(qtCheckTask.getMaterialWorkshop().getName());
            //如果为复检任务则查询复检任务
            if (qtCheckTask.getType() == 1) {
                QtRecheckTask recheckTask = qtRecheckTaskService.getOne(new LambdaQueryWrapper<QtRecheckTask>().eq(QtRecheckTask::getRecheckId, id),false);
                qtCheckTaskListVo.setOriginalTaskCode(recheckTask.getOriginalCode());
            }
        }
        //设置工位字段
        qtCheckTaskListVo.setStation(qtCheckTask.getStation());
        if (qtCheckTask.getStationId() != null) {
            qtCheckTaskListVo.setStationId(Math.toIntExact(qtCheckTask.getStationId()));
        }
        qtCheckTaskListVo.setStationName(qtCheckTask.getStationName());
        if(qtCheckTask.getProductLine() !=null) {
            //生产产线code
            if(qtCheckTask.getProductLine().getId() !=null) {
                qtCheckTaskListVo.setProductLineId(qtCheckTask.getProductLine().getId().toString());
                R<String> productLineCode = remoteOfficeFeign.getByIdSysOfficeInfo(Long.parseLong(qtCheckTask.getProductLine().getId().toString()));
                if (org.apache.commons.lang3.StringUtils.isNotBlank(productLineCode.getMsg()))
                    qtCheckTaskListVo.setProductLineCode(productLineCode.getMsg());
            }
        }
        if(qtCheckTask.getProductWorkshop() !=null) {
            //生产车间code
            if(qtCheckTask.getProductWorkshop().getId() !=null) {
                qtCheckTaskListVo.setProductWorkshopId(qtCheckTask.getProductWorkshop().getId().toString());
                R<String> productWorkshopCode = remoteOfficeFeign.getByIdSysOfficeInfo(Long.parseLong(qtCheckTask.getProductWorkshop().getId().toString()));
                if (org.apache.commons.lang3.StringUtils.isNotBlank(productWorkshopCode.getMsg()))
                    qtCheckTaskListVo.setProductWorkshopCode(productWorkshopCode.getMsg());
            }
        }

        QtCheckTaskDetailVO qtCheckTaskDetailVo = new QtCheckTaskDetailVO();
        qtCheckTaskDetailVo.setQtCheckTaskListVo(qtCheckTaskListVo);
        //处理样本数
        //选择任务详情
        QueryWrapper<QtCheckTaskDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DISTINCT copy").eq("task_id", id).orderByAsc("copy");
        List<QtCheckTaskDetail> qtCheckTaskDetails = qtCheckTaskDetailService.list(queryWrapper);
        //手机端copy
        List<QtCheckTaskDetailCopyVO> copy = Lists.newArrayList();
        if (!qtCheckTaskDetails.isEmpty()) {
            //包装好group和copy
            for (QtCheckTaskDetail tmp : qtCheckTaskDetails) {
                QtCheckPlanDetailsVO qtCheckPlanDetailsVOTmp = qtCheckPlanService.info(qtCheckTask.getCheckPlanId(), id, tmp.getCopy());
                //如果有值则表示在线检，跳出循环即可
                if (CollUtil.isNotEmpty(qtCheckPlanDetailsVOTmp.getOnlineItem())) {
                    qtCheckPlanDetailsVO.setOnlineItem(qtCheckPlanDetailsVOTmp.getOnlineItem());
                    break;
                }
                //查看是否有details
                QtCheckTaskCopy qtCheckTaskCopy = qtCheckTaskCopyMapper.selectOne(new LambdaQueryWrapper<QtCheckTaskCopy>()
                        .eq(QtCheckTaskCopy::getTaskId, id)
                        .eq(QtCheckTaskCopy::getCopy, tmp.getCopy()).last("limit 1"));
                QtCheckTaskDetailCopyVO qtCheckTaskDetailCopyVO = new QtCheckTaskDetailCopyVO();
                qtCheckTaskDetailCopyVO.setNum(tmp.getCopy());
                qtCheckTaskDetailCopyVO.setCheckGroup(qtCheckPlanDetailsVOTmp.getCheckGroup());
                //放入copyDetails
                QtCheckTaskCopyDetailsDTO copyDetailsVO = new QtCheckTaskCopyDetailsDTO();
                if (qtCheckTaskCopy != null) {
                    BeanUtils.copyProperties(qtCheckTaskCopy, copyDetailsVO);
                    copyDetailsVO.setAnalystTime(qtCheckTaskCopy.getAnalystTime()==null?null: qtCheckTaskCopy.getAnalystTime().toLocalDate());
                }
                qtCheckTaskDetailCopyVO.setCopyDetails(copyDetailsVO);
                copy.add(qtCheckTaskDetailCopyVO);
                //放入落板记录模块数据setQtCheckTaskDestoryInfoVOList
                List<QtCheckTaskDestoryInfo> qtCheckTaskDestoryInfoList = qtCheckTaskDestoryInfoMapper.selectList(new LambdaQueryWrapper<QtCheckTaskDestoryInfo>().eq(QtCheckTaskDestoryInfo::getTaskId, id));
                List<QtCheckTaskDestoryInfoVO> qtCheckTaskDestoryInfoVOList = BeanUtil.copyToList(qtCheckTaskDestoryInfoList, QtCheckTaskDestoryInfoVO.class);
                //处理落板工位
                if(CollUtil.isNotEmpty(qtCheckTaskDestoryInfoVOList)){
                    List<QtCheckTaskDestoryInfoVO> collect = qtCheckTaskDestoryInfoVOList.stream().filter(ele -> StrUtil.isNotBlank(ele.getDestoryReason())).map(destoryVOInfo -> {
                        Integer workStation = destoryVOInfo.getDestoryWorkStation();
                        if (workStation != null) {
                            if (workStation.equals(QtManagerConstant.NUM_3)) {
                                Set<String> checkList = Sets.newHashSet(QtManagerConstant.NUM_1.toString(), QtManagerConstant.NUM_2.toString());
                                destoryVOInfo.setCheckList(checkList);
                            } else {
                                destoryVOInfo.setCheckList(Sets.newHashSet(workStation.toString()));
                            }
                        }
                        return destoryVOInfo;
                    }).collect(Collectors.toList());
                    qtCheckTaskDetailCopyVO.setQtCheckTaskDestoryInfo(collect);
                }
            }

        }
        //details没有值说明没有输入详细值
        else {
            qtCheckPlanDetailsVO = qtCheckPlanService.info(qtCheckTask.getCheckPlanId(), id, null);
            QtCheckTaskDetailCopyVO qtCheckTaskDetailCopyVO = new QtCheckTaskDetailCopyVO();
            qtCheckTaskDetailCopyVO.setNum(1);
            qtCheckTaskDetailCopyVO.setCheckGroup(qtCheckPlanDetailsVO.getCheckGroup());
            copy.add(qtCheckTaskDetailCopyVO);
        }

        //pc端对数据进行处理
        if (isPc) {
            //copy没有值是在线检
            if (copy.size() > 0) {
                //先拿到checkGroup
                List<QtCheckPlanGroupVO> checkGroup = copy.get(0).getCheckGroup();
                //如果没有group则说明是在线检
                if (checkGroup != null) {
                    //对每个item进行赋值
                    checkGroup.forEach(groupTmp -> {
                        groupTmp.getItems().forEach(itemTmp -> {
                            Map<String, String> result = new HashMap<>();
                            Set<String> unQualifiedSet = new HashSet<>();
                            //对每一层copy进行遍历
                            for (QtCheckTaskDetailCopyVO copyTmp : copy) {
                                String num = String.valueOf(copyTmp.getNum());
                                //需要对每一个copy下的分组和项进行校验，匹配则放入值
                                copyTmp.getCheckGroup().forEach(copyGroupTmp -> {
                                    if (copyGroupTmp.getId().equals(groupTmp.getId())) {
                                        copyGroupTmp.getItems().forEach(itemGroupTmp -> {
                                            if (itemGroupTmp.getItemId().equals(itemTmp.getItemId())) {
                                                if (StringUtils.isNotBlank(itemGroupTmp.getCheckResult())) {
                                                    result.put("checkResult" + num, itemGroupTmp.getCheckResult());
                                                }
                                                if (itemGroupTmp.getDefectId() != null) {
                                                    result.put("defect" + num, itemGroupTmp.getDefectId().toString());
                                                    result.put("defectName" + num, itemGroupTmp.getDefectName());
                                                }
                                                if (itemGroupTmp.getIsQualified() != null && itemGroupTmp.getIsQualified().equals(0)) {
                                                    unQualifiedSet.add("checkResult" + num);
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                            itemTmp.setResult(result);
                            List<String> unQualifiedList = new ArrayList<>();
                            unQualifiedList.addAll(unQualifiedSet);
                            itemTmp.setIsQualifiedList(unQualifiedList);
                        });
                    });
                    qtCheckTaskDetailVo.setCheckGroup(checkGroup);
                    if (qtCheckTask.getStation() != null) {
                        qtCheckTask.setStationId(qtCheckTask.getStationId());
                        qtCheckTask.setStationName(qtCheckTask.getStationName());
                        if(qtCheckTask.getStationId() != null){
                            qtCheckTaskListVo.setStationId(Math.toIntExact(qtCheckTask.getStationId()));
                        }
                        qtCheckTaskListVo.setStationName(qtCheckTask.getStation().getName());
                        qtCheckTaskListVo.setStation(qtCheckTask.getStation());
                    }
                    //设置样本概况模块数据
                    //先删除样本概况表中可能存在的数据

                    String[] strings = {"sampleNum","licensePlate","analyst","analystTime","analystName","ingredientNum","samplingTime","samplingTimeMix"};
                    List<QtCheckTaskCopy> qtCheckTaskCopies = qtCheckTaskCopyService.list(new LambdaQueryWrapper<QtCheckTaskCopy>().eq(QtCheckTaskCopy::getTaskId, id));
                    List<QtCheckTaskSimpleDTO> qtCheckTaskSimpleDTOS = Lists.newArrayList();
                    if (!qtCheckTaskCopies.isEmpty()) {
                        QtCheckTaskSimpleDTO qtCheckTaskSimpleDTO = new QtCheckTaskSimpleDTO();
                        for (String s: strings) {
                            qtCheckTaskSimpleDTO = new QtCheckTaskSimpleDTO();
                            qtCheckTaskSimpleDTO.setId(s);
                            Map<String, Object> simpleMap = new HashMap<>();

                            for (QtCheckTaskCopy qtCheckTaskCopy : qtCheckTaskCopies) {
                                Map<String, Map<String, Object>> result = new HashMap<>();
                                if (QtManagerConstant.SAMPLENUM.equals(s)){
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getSampleNum());
                                } else if (QtManagerConstant.LICENSEPLATE.equals(s)) {
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getLicensePlate());
                                } /*else if ("analyst".equals(s)) {
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getAnalyst());
                                } */else if (QtManagerConstant.ANALYSTIME.equals(s)) {
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getAnalystTime());
                                } else if (QtManagerConstant.ANALYSTNAME.equals(s)) {
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getAnalystName());
                                    simpleMap.put("analyst" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getAnalyst());
                                } else if (QtManagerConstant.INGREDIENTNUM.equals(s)) {
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getIngredientNum());
                                } else if (QtManagerConstant.SAMPLINGTIME.equals(s)) {
                                    simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getSamplingTimeArray());
                                }
                                else if (QtManagerConstant.SAMPLINGTIMEMIX.equals(s)) {
                                    //配料抽检专用时间
                                    if(qtCheckTask.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode())){
                                        simpleMap.put("checkResult" + qtCheckTaskCopy.getCopy(), qtCheckTaskCopy.getSamplingTime());
                                    }
                                }
                            }
                            qtCheckTaskSimpleDTO.setResult(simpleMap);
                            qtCheckTaskSimpleDTOS.add(qtCheckTaskSimpleDTO);

                        }
                        qtCheckTaskDetailVo.setSampleInfoList(qtCheckTaskSimpleDTOS);
                    }
                    //样本概况模块返回数据设置完成
                } else {
                    qtCheckTaskDetailVo.setQtCheckPlanDetailsVO(qtCheckPlanDetailsVO);
                }
            } else {
                qtCheckTaskDetailVo.setQtCheckPlanDetailsVO(qtCheckPlanDetailsVO);
            }
        } else {
            qtCheckPlanDetailsVO.setCopy(copy);
            qtCheckTaskDetailVo.setQtCheckPlanDetailsVO(qtCheckPlanDetailsVO);
        }
        //放入落板记录模块数据setQtCheckTaskDestoryInfoVOList
        List<QtCheckTaskDestoryInfo> qtCheckTaskDestoryInfoList = qtCheckTaskDestoryInfoMapper.selectList(new LambdaQueryWrapper<QtCheckTaskDestoryInfo>().eq(QtCheckTaskDestoryInfo::getTaskId, id));
        if (!qtCheckTaskDestoryInfoList.isEmpty()) {
            List<QtCheckTaskDestoryInfoVO> qtCheckTaskDestoryInfoVOList1 = BeanUtil.copyToList(qtCheckTaskDestoryInfoList, QtCheckTaskDestoryInfoVO.class);
            //处理落板工位
            List<QtCheckTaskDestoryInfoVO> collect = qtCheckTaskDestoryInfoVOList1.stream().filter(ele -> StrUtil.isNotBlank(ele.getDestoryReason())).map(destoryVOInfo -> {
                Integer workStation = destoryVOInfo.getDestoryWorkStation();
                if (workStation != null) {
                    if(workStation.equals(QtManagerConstant.NUM_3)) {
                        Set<String> checkList = Sets.newHashSet(QtManagerConstant.NUM_1.toString(), QtManagerConstant.NUM_2.toString());
                        destoryVOInfo.setCheckList(checkList);
                    } else {
                        destoryVOInfo.setCheckList(Sets.newHashSet(workStation.toString()));
                    }
                }
                return destoryVOInfo;
            }).collect(Collectors.toList());
            qtCheckTaskDetailVo.setQtCheckTaskDestoryInfo(collect);
            qtCheckTaskDetailVo.setDestoryInfoRemark(qtCheckTaskDestoryInfoVOList1.get(0).getDestoryInfoRemark());
        }

        return qtCheckTaskDetailVo;
    }

    /**
     * 修改任务状态
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(QtCheckTaskStatusDTO dto) {
        QtCheckTask qtCheckTask = new QtCheckTask();
        QtCheckTask oldModel = getById(dto.getId());
        qtCheckTask.setId(dto.getId());
        qtCheckTask.setStatus(dto.getStatus());
        //开始任务
        if (dto.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.CHECKING.getCode())) {
            log.debug("老任务",oldModel.getStatus());
            if (oldModel.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())) {
                throw new BusinessException("已完成的任务不能开始");
            }
            qtCheckTask.setStartBy(SecurityUtils.getUser().getUserId());
            qtCheckTask.setStartName(SecurityUtils.getUser().getName());
            qtCheckTask.setStartTime(LocalDateTime.now());
        }
        //取消任务
        if (dto.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.CANCELED.getCode())) {
            //不是待检验状态则无法修改
            if (!oldModel.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.WAITED.getCode())) {
                throw new BusinessException("只有待检验状态才能取消");
            }
            qtCheckTask.setCancelBy(SecurityUtils.getUser().getUserId());
            qtCheckTask.setCancelName(SecurityUtils.getUser().getName());
            qtCheckTask.setCancelTime(LocalDateTime.now());
        }
        //完成任务
        if (dto.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())) {
            qtCheckTask.setFinishedBy(SecurityUtils.getUser().getUserId());
            //修改完成任务显示拼音的bug
            //qtCheckTask.setFinishedName(SecurityUtils.getUser().getUsername());
            qtCheckTask.setFinishedName(SecurityUtils.getUser().getName());
            qtCheckTask.setFinishedTime(LocalDateTime.now());
            //计算班次时间
            //如果是在线检则根据缺陷开始时间
            QtTaskClassCalculate taskClassCalculate = new QtTaskClassCalculate();
            if(oldModel.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())&&oldModel.getDefectStartTime()!=null){
                taskClassCalculate = CalculateDateUtil.calculateClassTime(oldModel.getDefectStartTime());
            }else {
                taskClassCalculate = CalculateDateUtil.calculateClassTime(qtCheckTask.getFinishedTime());
            }
            qtCheckTask.setClassesId(taskClassCalculate.getClassType());
            qtCheckTask.setClassTime(taskClassCalculate.getClassDate().toLocalDate());
        }
        return updateById(qtCheckTask);
    }

    /**
     * 添加值
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean itemValue(QtCheckTaskSubmitDTO dto) {
        List<QtCheckTaskDetail> valueModels = new ArrayList<>();
        QtCheckTask task = getById(dto.getId());
        //校验落版记录时段
        if(CollUtil.isNotEmpty(dto.getQtCheckTaskDestoryInfo())){
            dto.getQtCheckTaskDestoryInfo().forEach(tmp->{
                if(tmp.getDestoryStartTime()!=null && tmp.getDestoryEndTime()!=null){
                    if(!CalculateDateUtil.isSameClassType(tmp.getDestoryStartTime(),tmp.getDestoryEndTime())){
                        throw new BusinessException("落版时段不允许跨班次");
                    }
                }
            });
        }
        //先删除可能暂存的值
        Map<String, Object> valueCondition = new HashMap<>();
        valueCondition.put("task_id", dto.getId());
        qtCheckTaskDetailService.removeByMap(valueCondition);
        //根据任务id删除落板记录表的暂存的数据
        qtCheckTaskDestoryInfoService.removeByMap(valueCondition);
        //插入值
        if (dto.getValues() != null && dto.getValues().size() > 0) {
            dto.getValues().forEach(valueTmp -> {
                //value下的list
                if (valueTmp.getList() != null && valueTmp.getList().size() > 0) {
                    valueTmp.getList().forEach(listTmp -> {
                        //list下checkGroup
                        if (listTmp.getCheckGroup() != null && listTmp.getCheckGroup().size() > 0) {
                            listTmp.getCheckGroup().forEach(checkGroupTmp -> {
                                Map<String, Object> condition = new HashMap<>();
                                QtCheckTaskDetail qtCheckTaskDetail = new QtCheckTaskDetail();
                                qtCheckTaskDetail.setCopy(valueTmp.getCopy());
                                condition.put("copy", valueTmp.getCopy());
                                qtCheckTaskDetail.setCheckResult(checkGroupTmp.getCheckResult());
                                if (checkGroupTmp.getDefectId() != null) {
                                    qtCheckTaskDetail.setDefectId(checkGroupTmp.getDefectId());
                                }
                                qtCheckTaskDetail.setTaskId(dto.getId());
                                condition.put("task_id", dto.getId());
                                qtCheckTaskDetail.setItemId(checkGroupTmp.getItemId());
                                condition.put("item_id", checkGroupTmp.getItemId());
                                qtCheckTaskDetail.setGroupId(listTmp.getGroupId());
                                condition.put("group_id", listTmp.getGroupId());
                                //需要先删除之前保存的值
                                qtCheckTaskDetailService.removeByMap(condition);
                                qtCheckTaskDetail.setIsQualified(checkGroupTmp.getIsQualified());
                                valueModels.add(qtCheckTaskDetail);
                            });
                        }
                    });
                }

                //操作样本详情 当样本类型为来料检、化验抽检、配料抽检时，有样本模块
                if ((task.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode()) || task.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode()) || task.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode())) && !Objects.isNull(valueTmp.getCopyDetails())) {
                    QtCheckTaskCopy copyDetailModel = new QtCheckTaskCopy();
                    Map<String, Object> deleteCondition = new HashMap<>();
                    deleteCondition.put("copy", valueTmp.getCopy());
                    copyDetailModel.setCopy(valueTmp.getCopy());
                    deleteCondition.put("task_id", dto.getId());
                    copyDetailModel.setTaskId(dto.getId());
                    BeanUtils.copyProperties(valueTmp.getCopyDetails(), copyDetailModel);
                    copyDetailModel.setAnalystTime(valueTmp.getCopyDetails().getAnalystTime().atStartOfDay());
                    //如果是配料抽检则加入取样时间，字段，便于统计
//                    if(task.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode())){
//                        cn.hutool.json.JSONArray jsonArray = new cn.hutool.json.JSONArray(copyDetailModel.getSamplingTimeArray());
//                        String str = jsonArray.get(0).toString();
//                        copyDetailModel.setSamplingTime(LocalDateTime.parse(str,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//                    }
                    //先删除冗余数据
                    qtCheckTaskCopyMapper.deleteByMap(deleteCondition);
                    qtCheckTaskCopyMapper.insert(copyDetailModel);
                }
            });
            qtCheckTaskDetailService.saveBatch(valueModels);
        }
        //插入在线检数据
        else if (task.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode()) && dto.getOnlineItem() != null) {
            List<QtCheckTaskDetail> model = Lists.newArrayList();
            dto.getOnlineItem().forEach(tmp -> {
                QtCheckTaskDetail detail = new QtCheckTaskDetail();
                detail.setItemId(tmp.getItemId());
                detail.setGroupId(tmp.getGroupId());
                detail.setCopy(0);
                detail.setTaskId(dto.getId());
                List<QtOnlineCheckDefectDTO> defectList = new ArrayList<>();
                if (CollUtil.isNotEmpty(tmp.getCategory())) {
                    tmp.getCategory().forEach(category -> {
                        category.getDefect().forEach(categoryTmp -> {
                            defectList.add(categoryTmp);
                        });
                    });
                }
                detail.setDefectNum(defectList);
                model.add(detail);
            });
            qtCheckTaskDetailService.saveBatch(model);
        }
        QtCheckTaskStatusDTO statusDTO = new QtCheckTaskStatusDTO();
        statusDTO.setId(dto.getId());
        statusDTO.setStatus(dto.getStatus());
        //组装落板记录详情数据
        if (CollUtil.isNotEmpty(dto.getQtCheckTaskDestoryInfo())) {
            List<QtCheckTaskDestoryInfo> qtCheckTaskDestoryInfoList = Lists.newArrayList();
            dto.getQtCheckTaskDestoryInfo().forEach(destoryInfoVO -> {
                if (dto.getId() != null) {
                    QtCheckTaskDestoryInfoDTO qtCheckTaskDestoryInfoDTO = new QtCheckTaskDestoryInfoDTO();
                    QtCheckTaskDestoryInfo qtCheckTaskDestoryInfo = new QtCheckTaskDestoryInfo();
                    if (qtCheckTaskDetailService.getById(dto.getId()) != null) {
                        qtCheckTaskDestoryInfoDTO.setCopy(qtCheckTaskDetailService.getById(dto.getId()).getCopy());
                    }
                    qtCheckTaskDestoryInfoDTO.setTaskId(dto.getId());
                    qtCheckTaskDestoryInfoDTO.setDestoryDetail(destoryInfoVO.getDestoryDetail());
                    qtCheckTaskDestoryInfoDTO.setDestoryReason(destoryInfoVO.getDestoryReason());
                    qtCheckTaskDestoryInfoDTO.setDelFlag(0);
                    qtCheckTaskDestoryInfoDTO.setDestoryInfoRemark(dto.getDestoryInfoRemark());
                    qtCheckTaskDestoryInfoDTO.setCreateBy(SecurityUtils.getUser().getUserId());
                    qtCheckTaskDestoryInfoDTO.setDestoryStartTime(destoryInfoVO.getDestoryStartTime());
                    qtCheckTaskDestoryInfoDTO.setDestoryEndTime(destoryInfoVO.getDestoryEndTime());
                    qtCheckTaskDestoryInfoDTO.setCreateName(SecurityUtils.getUser().getName());
                    if (qtCheckTaskCopyService.getById(dto.getId()) != null) {
                        qtCheckTaskDestoryInfoDTO.setSampleNum(qtCheckTaskCopyService.getById(dto.getId()).getSampleNum());
                    }
                    BeanUtils.copyProperties(qtCheckTaskDestoryInfoDTO, qtCheckTaskDestoryInfo);
                    //落板工位
                    if(CollUtil.isNotEmpty(destoryInfoVO.getCheckList())){
                        //选一个
                        if(destoryInfoVO.getCheckList().size() == 1){
                            Integer tmp = Integer.valueOf(destoryInfoVO.getCheckList().iterator().next());
                            if(!tmp.equals(QtManagerConstant.NUM_1) && !tmp.equals(QtManagerConstant.NUM_2)){
                                throw new BusinessException("落板工位格式错误");
                            }
                            qtCheckTaskDestoryInfo.setDestoryWorkStation(tmp);
                        }else {
                            qtCheckTaskDestoryInfo.setDestoryWorkStation(QtManagerConstant.NUM_3);
                        }
                    }
                    qtCheckTaskDestoryInfoList.add(qtCheckTaskDestoryInfo);
                }
            });
            qtCheckTaskDestoryInfoService.saveBatch(qtCheckTaskDestoryInfoList);
        }else{
            saveCheckTaskDestoryInfoRemark(dto.getId(),dto.getDestoryInfoRemark());
        }
        return updateStatus(statusDTO);
    }

    /**
     * pc端添加值
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean pcItemValue(QtPcCheckTaskSubmitDTO dto) {
        if (CollUtil.isEmpty(dto.getCheckGroup()) && dto.getOnlineItem() == null) {
            throw new BusinessException("传的值为空");
        }
        //校验落版记录时段
        if(CollUtil.isNotEmpty(dto.getQtCheckTaskDestoryInfo())){
            dto.getQtCheckTaskDestoryInfo().forEach(tmp->{
                if(tmp.getDestoryStartTime()!=null && tmp.getDestoryEndTime()!=null){
                    if(!CalculateDateUtil.isSameClassType(tmp.getDestoryStartTime(),tmp.getDestoryEndTime())){
                        throw new BusinessException("落版时段不允许跨班次");
                    }
                }
            });
        }
        List<QtCheckTaskDetail> valueModels = new ArrayList<>();
        QtCheckTask tasKDetail = getById(dto.getId());
        //先删除可能暂存的值
        Map<String, Object> valueCondition = new HashMap<>();
        valueCondition.put("task_id", dto.getId());
        qtCheckTaskDetailService.removeByMap(valueCondition);
        //处理样本概况数据
        //样本概况在来料捡,化验抽捡,配料抽检时显示
        if (tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode()) || tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode()) || tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode())) {
            if (CollUtil.isNotEmpty(dto.getSampleInfoList())) {
                Map<String, Object> deleteCondition = new HashMap<>();
                deleteCondition.put("task_id", dto.getId());
                //先删除可能暂存的值
                qtCheckTaskCopyMapper.deleteByMap(deleteCondition);
                List<QtCheckTaskCopy> qtCheckTaskCopies2 = Lists.newArrayList();
                Map<String, QtCheckTaskCopy> objectMap = new HashMap<>();
                dto.getSampleInfoList().forEach(sampleInfo -> {
                    //遍历Map
                    for (Map.Entry<String, String> entry : sampleInfo.getResult().entrySet()) {
                        String checkResultNum = "";
                        if(entry.getKey().contains(QtManagerConstant.QT_CHECK_TASK_FIELD_CHECK_RESULT))
                              checkResultNum = entry.getKey().substring(entry.getKey().length()==12?(entry.getKey().length() - 1)
                                    :(entry.getKey().length() - 2));
                        QtCheckTaskCopy qtCheckTaskCopy = new QtCheckTaskCopy();
                        if (Objects.nonNull(objectMap.get(entry.getKey()))) {
                            if (QtManagerConstant.SAMPLENUM.equals(sampleInfo.getId())) {
                                objectMap.get(entry.getKey()).setSampleNum(entry.getValue());
                            } else if (QtManagerConstant.LICENSEPLATE.equals(sampleInfo.getId())) {
                                objectMap.get(entry.getKey()).setLicensePlate(entry.getValue());
                            }else if (QtManagerConstant.ANALYST.equals(sampleInfo.getId())) {
                                if (StringUtils.isNotBlank(entry.getKey())) {
                                    objectMap.get(entry.getKey()).setAnalyst(Integer.valueOf(entry.getValue()));
                                }
                            }else if (QtManagerConstant.ANALYSTNAME.equals(sampleInfo.getId())) {
                              //  objectMap.get(entry.getKey()).setAnalystName(entry.getValue());
                                if(entry.getKey().contains(QtManagerConstant.QT_CHECK_TASK_FIELD_CHECK_RESULT)){
                                    objectMap.get(entry.getKey()).setAnalystName(sampleInfo.getResult()
                                            .get(QtManagerConstant.QT_CHECK_TASK_FIELD_CHECK_RESULT+checkResultNum));
                                    objectMap.get(entry.getKey()).setAnalyst(Integer.valueOf(sampleInfo.getResult()
                                            .get(QtManagerConstant.ANALYST+checkResultNum)));
                                }
                            }else if (QtManagerConstant.SAMPLINGTIME.equals(sampleInfo.getId())) {
                               /* if (StringUtils.isNotBlank(entry.getKey())) {
                                    objectMap.get(entry.getKey()).setSamplingTime(LocalDateTime.parse(entry.getValue(),DatePattern.NORM_DATETIME_FORMATTER));
                                }*/
                                if (StringUtils.isNotBlank(entry.getValue())) {
                                    objectMap.get(entry.getKey()).setSamplingTimeArray( getSamplingTimeArray(entry.getValue().split(",") ));
                                }
                            }else if (QtManagerConstant.ANALYSTIME.equals(sampleInfo.getId())) {
                                if (StringUtils.isNotBlank(entry.getKey())) {
                                    try {
                                        objectMap.get(entry.getKey()).setAnalystTime(entry.getKey().length()>10?
                                                LocalDateTime.parse(entry.getValue(),DatePattern.NORM_DATETIME_FORMATTER):
                                                new SimpleDateFormat("yyyy-MM-dd").
                                                        parse(entry.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                                    } catch (Exception e) {
                                        log.info("-->>转换异常！Exception:"+e.getLocalizedMessage());
                                    }
                                }
                            }else if (QtManagerConstant.INGREDIENTNUM.equals(sampleInfo.getId())) {
                                if (StringUtils.isNotBlank(entry.getKey())) {
                                    if(StringUtils.isNotBlank(entry.getValue())){
                                        objectMap.get(entry.getKey()).setIngredientNum(Integer.valueOf(entry.getValue()));
                                    }
                                }
                            }else if (QtManagerConstant.SAMPLINGTIMEMIX.equals(sampleInfo.getId())) {
                                //配料抽检专用localDateTime
                                if (StringUtils.isNotBlank(entry.getValue()) && tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode())) {
                                    objectMap.get(entry.getKey()).setSamplingTime(LocalDateTime.parse(entry.getValue(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                                }
                            }
                        } else {
                                if (QtManagerConstant.SAMPLENUM.equals(sampleInfo.getId())) {
                                    qtCheckTaskCopy.setSampleNum(entry.getValue());
                                } else if (QtManagerConstant.LICENSEPLATE.equals(sampleInfo.getId())) {
                                    qtCheckTaskCopy.setLicensePlate(entry.getValue());
                                } else if (QtManagerConstant.ANALYST.equals(sampleInfo.getId())) {
                                    if (entry.getKey() != null) {
                                        qtCheckTaskCopy.setAnalyst(Integer.valueOf(entry.getValue()));
                                    }
                                } else if (QtManagerConstant.ANALYSTNAME.equals(sampleInfo.getId())) {
                                    if(entry.getKey().contains(QtManagerConstant.QT_CHECK_TASK_FIELD_CHECK_RESULT)){
                                        qtCheckTaskCopy.setAnalystName(entry.getValue());
                                        qtCheckTaskCopy.setAnalyst(Integer.valueOf(sampleInfo.getResult().get(QtManagerConstant.ANALYST+checkResultNum)));
                                    }
                                } else if (QtManagerConstant.SAMPLINGTIME.equals(sampleInfo.getId())) {
//                                    if (StringUtils.isNotBlank(entry.getKey())) {
//                                        qtCheckTaskCopy.setSamplingTime(LocalDateTime.parse(entry.getKey()));
//                                    }
                                    if (StringUtils.isNotBlank(entry.getValue())) {
                                        qtCheckTaskCopy.setSamplingTimeArray(getSamplingTimeArray(entry.getValue().split(",") ));
                                    }
                                } else if (QtManagerConstant.ANALYSTIME.equals(sampleInfo.getId())) {
                                    if (StringUtils.isNotBlank(entry.getKey())) {
                                        qtCheckTaskCopy.setAnalystTime(LocalDateTime.parse(entry.getValue()));
                                    }
                                } else if (QtManagerConstant.INGREDIENTNUM.equals(sampleInfo.getId())) {
                                    if (StringUtils.isNotBlank(entry.getKey())) {
                                        qtCheckTaskCopy.setIngredientNum(Integer.valueOf(entry.getKey()));
                                    }
                                } else if (QtManagerConstant.SAMPLINGTIMEMIX.equals(sampleInfo.getId())) {
                                    //配料抽检专用localDateTime
                                    if (StringUtils.isNotBlank(entry.getValue()) && tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode())) {
                                        qtCheckTaskCopy.setSamplingTime(LocalDateTime.parse(entry.getValue(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                                    }
                                }
                            if(!entry.getKey().contains(QtManagerConstant.ANALYST)){
                                objectMap.put(entry.getKey(),qtCheckTaskCopy);
                            }
                        }
                    }
                });
                objectMap.forEach((key,qtCheckTaskCopyInfo)->{
                    QtCheckTaskCopy qtCheckTaskCopy1 = new QtCheckTaskCopy();
                    BeanUtils.copyProperties(qtCheckTaskCopyInfo,qtCheckTaskCopy1);
                    //如果是配料抽检，方便取报表，传参
                    if(tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode())&&qtCheckTaskCopyInfo.getSamplingTime()!=null){
                        qtCheckTaskCopy1.setSamplingTimeArray(getSamplingTimeArray(new String[]{qtCheckTaskCopyInfo.getSamplingTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}));
                    }
                    qtCheckTaskCopy1.setTaskId(dto.getId());
                    //不是在线检，设置copy值
                    if(key.length() != 0) {
                        Integer copy = Integer.valueOf(key.contains(QtManagerConstant.QT_CHECK_TASK_FIELD_CHECK_RESULT)
                                ?key.substring(key.length()==12?(key.length() - 1):(key.length() - 2))
                                :key.substring(key.length() - 1));
                        qtCheckTaskCopy1.setCopy(copy);
                    }
                    qtCheckTaskCopies2.add(qtCheckTaskCopy1);
                });
                //先删除可能暂存的值
                qtCheckTaskCopyService.removeById(dto.getId());
                qtCheckTaskCopyService.saveBatch(qtCheckTaskCopies2);
            }
        }
        //往样本概况表设置数据完成

        //处理数据
        if (CollUtil.isNotEmpty(dto.getCheckGroup())) {
            dto.getCheckGroup().forEach(group -> {
                if (CollUtil.isNotEmpty(group.getItems())) {
                    group.getItems().forEach(item -> {
                        //遍历Map
                        for (Map.Entry<String, String> entry : item.getResult().entrySet()) {
                            String key = entry.getKey();
                            if(item.getItemId() != null) {
                                //如果map值等于checkResult
                                if (key.contains(QtManagerConstant.QT_CHECK_TASK_FIELD_CHECK_RESULT)) {
                                    //得到checkResult 后面追加参数 (只保证到100内 )
                                    String checkResultNum = key.substring(key.length()==12?(key.length() - 1):(key.length() - 2));
                                    QtCheckTaskDetail qtCheckTaskDetail = new QtCheckTaskDetail();
                                    //detail设置Result
                                    qtCheckTaskDetail.setCheckResult(item.getResult().get(key));
                                    //如果是在线检则设置copy为0
                                    if (tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())) {
                                        qtCheckTaskDetail.setCopy(0);
                                    } else {
                                        //否则设置copy的值为末尾
                                        Integer copy = Integer.valueOf(checkResultNum);
                                        qtCheckTaskDetail.setCopy(copy);
                                    }
                                    String defectId = QtManagerConstant.QT_CHECK_TASK_FIELD_CHECK_DEFECTTEM + checkResultNum;
                                    if (item.getResult().get(defectId) != null) {
                                        qtCheckTaskDetail.setDefectId(Integer.valueOf(item.getResult().get(defectId)));
                                    }
                                    qtCheckTaskDetail.setTaskId(dto.getId());
                                    qtCheckTaskDetail.setItemId(item.getItemId());
                                    qtCheckTaskDetail.setGroupId(group.getId());
                                    //在该list中的数据是不合格数据的值
                                    if (CollUtil.isNotEmpty(item.getIsQualifiedList()) && item.getIsQualifiedList().contains(key)) {
                                        qtCheckTaskDetail.setIsQualified(0);
                                    } else {
                                        qtCheckTaskDetail.setIsQualified(1);
                                    }
                                    valueModels.add(qtCheckTaskDetail);
                                }
                            }
                        }
                    });
                } else {
                    throw new BusinessException("检验项数据为空");
                }
            });
            qtCheckTaskDetailService.saveBatch(valueModels);
        }
        //组装落板记录详情数据
        if (CollUtil.isNotEmpty(dto.getQtCheckTaskDestoryInfo())) {
            List<QtCheckTaskDestoryInfo> qtCheckTaskDestoryInfoList = Lists.newArrayList();
            //删除样本概况中暂存的值
             qtCheckTaskDestoryInfoService.remove(new LambdaQueryWrapper<QtCheckTaskDestoryInfo>().eq(QtCheckTaskDestoryInfo::getTaskId,dto.getId()));

            dto.getQtCheckTaskDestoryInfo().forEach(destoryInfoVO -> {
                QtCheckTaskDestoryInfoDTO qtCheckTaskDestoryInfoDTO = new QtCheckTaskDestoryInfoDTO();
                QtCheckTaskDestoryInfo qtCheckTaskDestoryInfo = new QtCheckTaskDestoryInfo();
                if (dto.getId() != null) {
                    //qtCheckTaskDestoryInfoDTO.setCopy(qtCheckTaskDetailService.getById(dto.getId()).getCopy());
                    qtCheckTaskDestoryInfoDTO.setTaskId(dto.getId());
                    qtCheckTaskDestoryInfoDTO.setDestoryDetail(destoryInfoVO.getDestoryDetail());
                    qtCheckTaskDestoryInfoDTO.setDestoryReason(destoryInfoVO.getDestoryReason());
                    qtCheckTaskDestoryInfoDTO.setDelFlag(0);
                    qtCheckTaskDestoryInfoDTO.setDestoryInfoRemark(destoryInfoVO.getDestoryInfoRemark());
                    qtCheckTaskDestoryInfoDTO.setCreateBy(SecurityUtils.getUser().getUserId());
                    qtCheckTaskDestoryInfoDTO.setDestoryStartTime(destoryInfoVO.getDestoryStartTime());
                    qtCheckTaskDestoryInfoDTO.setDestoryEndTime(destoryInfoVO.getDestoryEndTime());
                    if (qtCheckTaskCopyService.getById(dto.getId()) != null) {
                        qtCheckTaskDestoryInfoDTO.setCreateName(SecurityUtils.getUser().getName());
                        qtCheckTaskDestoryInfoDTO.setSampleNum(qtCheckTaskCopyService.getById(dto.getId()).getSampleNum());
                    }

                }
                qtCheckTaskDestoryInfoDTO.setDestoryInfoRemark(dto.getDestoryInfoRemark());
                BeanUtils.copyProperties(qtCheckTaskDestoryInfoDTO, qtCheckTaskDestoryInfo);
                //落板工位
                if(CollUtil.isNotEmpty(destoryInfoVO.getCheckList())){
                    //选一个
                    if(destoryInfoVO.getCheckList().size() == 1){
                        Integer tmp = Integer.valueOf(destoryInfoVO.getCheckList().iterator().next());
                        if(!tmp.equals(QtManagerConstant.NUM_1) && !tmp.equals(QtManagerConstant.NUM_2)){
                            throw new BusinessException("落板工位格式错误");
                        }
                        qtCheckTaskDestoryInfo.setDestoryWorkStation(tmp);
                    }else {
                        qtCheckTaskDestoryInfo.setDestoryWorkStation(QtManagerConstant.NUM_3);
                    }
                }
                qtCheckTaskDestoryInfoList.add(qtCheckTaskDestoryInfo);
            });
            qtCheckTaskDestoryInfoService.saveBatch(qtCheckTaskDestoryInfoList);
        }else{
            saveCheckTaskDestoryInfoRemark(dto.getId(),dto.getDestoryInfoRemark());
        }
        //插入在线检数据
        if (tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode()) && dto.getOnlineItem() != null) {
            List<QtCheckTaskDetail> model = Lists.newArrayList();
            dto.getOnlineItem().forEach(tmp -> {
               if(tmp.getItemId() !=null) {
                   QtCheckTaskDetail detail = new QtCheckTaskDetail();
                   detail.setTaskId(dto.getId());
                   detail.setItemId(tmp.getItemId());
                   detail.setGroupId(tmp.getGroupId());
                   detail.setCopy(0);
                   List<QtOnlineCheckDefectDTO> defectList = new ArrayList<>();
                   if (CollUtil.isNotEmpty(tmp.getCategory())) {
                       tmp.getCategory().forEach(category -> {
                           category.getDefect().forEach(categoryTmp -> {
                               defectList.add(categoryTmp);
                           });
                       });
                   }
                   detail.setDefectNum(defectList);
                   model.add(detail);
               }
            });
            qtCheckTaskDetailService.saveBatch(model);
        }
        //pc可能会存任务信息
        QtCheckTask taskModel = new QtCheckTask();
        if (dto.getBadNum() != null) {
            taskModel.setBadNum(dto.getBadNum());
        }
        if (dto.getGoodNum() != null) {
            taskModel.setGoodNum(dto.getGoodNum());
        }
        if (dto.getCheckNum() != null) {
            taskModel.setCheckNum(dto.getCheckNum());
        }
        if (dto.getNote() != null) {
            taskModel.setNote(dto.getNote());
        }
        if (StringUtils.isNotBlank(dto.getResult())) {
            taskModel.setResult(dto.getResult());
        }
        if (dto.getDefectNum() != null) {
            taskModel.setDefectNum(dto.getDefectNum());
        }
        taskModel.setId(dto.getId());
        taskModel.setStatus(dto.getStatus());
        if (dto.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())) {
            taskModel.setFinishedBy(SecurityUtils.getUser().getUserId());
            taskModel.setFinishedName(SecurityUtils.getUser().getName());
            taskModel.setFinishedTime(LocalDateTime.now());
            //计算班次时间
            QtTaskClassCalculate taskClassCalculate = new QtTaskClassCalculate();
            if(tasKDetail.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())&&tasKDetail.getDefectStartTime()!=null){
                taskClassCalculate = CalculateDateUtil.calculateClassTime(tasKDetail.getDefectStartTime());
            }else {
                taskClassCalculate = CalculateDateUtil.calculateClassTime(taskModel.getFinishedTime());
            }
            taskModel.setClassesId(taskClassCalculate.getClassType());
            taskModel.setClassTime(taskClassCalculate.getClassDate().toLocalDate());
        }

        //如果是配料抽检，则pushtype为SPOT_CHECK
        if(QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode().equals(tasKDetail.getTaskType())&&dto.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())){
            //推送刷新消息
            MsgPushDTO msgPushDTO = new MsgPushDTO();
            msgPushDTO.setType(WebsocketConstant.WsPushTypeEnum.SPOT_CHECK.getType());
            msgPushDTO.setMsgType(WebsocketConstant.WsPushTypeEnum.SPOT_CHECK.getType());
            FeignOptional.of(() -> remoteWsFeignApi.pushRefreshMsg(msgPushDTO), FeignConstant.STECCN_WEBSOCKET_SERVICE_NAME).get();
            sendMessage();
        }
        return updateById(taskModel);
    }

    /****
     * 检验时间字符串转json格式字符串
     * @param timeArray
     * @return
     */
    public String getSamplingTimeArray(String[] timeArray){
        List<String> dates = new ArrayList<>();
        for (String vo:timeArray) {
            dates.add(vo);
        }
        JSONArray jsonArray = new JSONArray(dates);
        return jsonArray.toString();
    }
    /****
     * 保存备注
     * @param id
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveCheckTaskDestoryInfoRemark(Long id ,String destoryInfoRemark){
        if(org.apache.commons.lang3.StringUtils.isNotBlank(destoryInfoRemark)) {
            QtCheckTaskDestoryInfo qtCheckTaskDestoryInfo = new QtCheckTaskDestoryInfo();
            if (qtCheckTaskDetailService.getById(id) != null)
                qtCheckTaskDestoryInfo.setCopy(qtCheckTaskDetailService.getById(id).getCopy());
            qtCheckTaskDestoryInfo.setTaskId(id);
            if (qtCheckTaskCopyService.getById(id) != null)
                qtCheckTaskDestoryInfo.setSampleNum(qtCheckTaskCopyService.getById(id).getSampleNum());
            qtCheckTaskDestoryInfo.setCreateName(SecurityUtils.getUser().getName());
            qtCheckTaskDestoryInfo.setCreateBy(SecurityUtils.getUser().getUserId());
            qtCheckTaskDestoryInfo.setDestoryInfoRemark(destoryInfoRemark);
            qtCheckTaskDestoryInfoService.save(qtCheckTaskDestoryInfo);
        }
    }
    /**
     * 完成任务
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean finished(QtCheckResultDTO dto) {
        QtCheckTask qtCheckTask = new QtCheckTask();
        BeanUtils.copyProperties(dto, qtCheckTask);
        qtCheckTask.setStatus(2);
        qtCheckTask.setFinishedBy(SecurityUtils.getUser().getUserId());
        qtCheckTask.setFinishedName(SecurityUtils.getUser().getName());
        qtCheckTask.setFinishedTime(LocalDateTime.now());
        QtCheckTask task = getById(dto.getId());
        //计算班次时间
        QtTaskClassCalculate taskClassCalculate = new QtTaskClassCalculate();
        if(task.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())&&task.getDefectStartTime()!=null){
            taskClassCalculate = CalculateDateUtil.calculateClassTime(task.getDefectStartTime());
        }else {
            taskClassCalculate = CalculateDateUtil.calculateClassTime(qtCheckTask.getFinishedTime());
        }
        qtCheckTask.setClassesId(taskClassCalculate.getClassType());
        qtCheckTask.setClassTime(taskClassCalculate.getClassDate().toLocalDate());
        //配料抽检任务完成,发送消息给岗位为"配料工"的当前班次人员;
        if (QtManagerConstant.NUM_7.equals(task.getTaskType())){
            sendMessage();
        }
        return updateById(qtCheckTask);
    }

    /**
     * 复检任务
     *
     * @param dto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long retask(QtReTaskDTO dto) {
        //复检操作后添加 新的复核任务
        QtCheckTask qtCheckTask = new QtCheckTask();
        //获取原始任务信息
        LambdaQueryWrapper<QtCheckTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QtCheckTask::getCode,dto.getOriginalTaskCode());
        QtCheckTask originalQtCheckTask = qtCheckTaskMapper.selectOne(queryWrapper);
        BeanUtils.copyProperties(dto, qtCheckTask);
        qtCheckTask.setStation(originalQtCheckTask.getStation());
        qtCheckTask.setClassTime(originalQtCheckTask.getClassTime());
        qtCheckTask.setFinishedBy(originalQtCheckTask.getFinishedBy());
        qtCheckTask.setFinishedName(originalQtCheckTask.getFinishedName());
        qtCheckTask.setStartBy(originalQtCheckTask.getStartBy());
        qtCheckTask.setStartName(originalQtCheckTask.getStartName());
        qtCheckTask.setSpotTime(originalQtCheckTask.getSpotTime());
        qtCheckTask.setId(null);
        qtCheckTask.setType(1);//任务类型 0原始任务  1复检任务
        qtCheckTask.setCode(CodeGenerateUtil.getNextCode(CodeGenerateUtil.PREFIX_JYRW, CodeGenerateUtil.NUM_BIT_8, currentMaxCode()));
        qtCheckTask.setCreateTime(LocalDateTime.now());
        qtCheckTask.setUpdateTime(LocalDateTime.now());
        qtCheckTask.setNote(null);
        qtCheckTask.setBadNum(null);
        qtCheckTask.setCheckNum(null);
        qtCheckTask.setGoodNum(null);
        qtCheckTask.setStatus(0);
        save(qtCheckTask);
        //保存巡检任务与复检任务关联数据
        QtRecheckTask recheckModel = new QtRecheckTask();
        recheckModel.setRecheckId(qtCheckTask.getId());
        recheckModel.setOriginalCode(originalQtCheckTask.getCode());
        recheckModel.setOriginalId(originalQtCheckTask.getId());
        recheckModel.setCreateBy(originalQtCheckTask.getCreateBy());
        recheckModel.setCreateName(originalQtCheckTask.getCreateName());
        recheckModel.setUpdateBy(originalQtCheckTask.getUpdateBy());
        recheckModel.setUpdateName(originalQtCheckTask.getUpdateName());
        recheckModel.setUpdateTime(originalQtCheckTask.getUpdateTime());
        recheckModel.setDelFlag(originalQtCheckTask.getDelFlag());
        qtRecheckTaskService.save(recheckModel);
        return  qtCheckTask.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean refreshGroupClass() {
        List<QtCheckTask> taskList = list(new LambdaQueryWrapper<QtCheckTask>()
                .select(QtCheckTask::getId,QtCheckTask::getStartBy)
                .and(v->v.between(QtCheckTask::getId,24491,30000))
                .and(v->v.eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode())
                        .or().eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                        .or().eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())));
        for(QtCheckTask tmp:taskList){
            QtCheckTask taskModel = new QtCheckTask();
            taskModel.setId(tmp.getId());
            if(tmp.getStartBy()==null){
                taskModel.setClassGroupName("车间倒班-甲班");
                taskModel.setClassesId(1);
            }else {
                Set<Long> idList = Sets.newHashSet();
                idList.add(tmp.getStartBy());
                List<OaGroupIdAndNameVO> groupByUser = oaInfoService.getGroupByUser(idList).getData();
                if(CollUtil.isEmpty(groupByUser)){
                    taskModel.setClassGroupName("车间倒班-甲班");
                    taskModel.setClassesId(1);
                }else {
                    taskModel.setClassGroupId(groupByUser.get(0).getClassGroupId());
                    taskModel.setClassGroupName(groupByUser.get(0).getClassGroupName());
                }
            }
            updateById(taskModel);

        }
        return true;
    }

    @Override
    public Boolean refreshWorkStation() {
        List<QtCheckTask> taskList = list(new LambdaQueryWrapper<QtCheckTask>().between(QtCheckTask::getId,24491,30000));
        for(QtCheckTask tmp:taskList){
            QtCheckTask taskModel = new QtCheckTask();
            //1）仅当生产车间为深加工车间且检验类型为在线检时，默认值为已选中生产产线的“钢化工位”，只读
            if(tmp.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                    &&tmp.getProductWorkshop()!=null
                    &&tmp.getProductWorkshop().getName().contains(QtManagerConstant.DEEP_OFFICE_NAME)){
                if(tmp.getProductLine()!=null
                        &&tmp.getProductLine().getId()!=null){
                    SysOfficeParentDTO officeDto = new SysOfficeParentDTO();
                    officeDto.setId(tmp.getProductLine().getId());
                    officeDto.setOfficeType(SysOfficeConstant.STATION);
                    List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    if(CollUtil.isEmpty(sysOfficeVOS)){
                        continue;
                    }
                    SysOfficeVO sysOfficeVO = sysOfficeVOS.stream().filter(v -> v.getOfficeName().equals(QtManagerConstant.STELL_STATION)).findFirst().orElse(null);
                    if(Objects.isNull(sysOfficeVO)){
                        continue;
                    }
                    taskModel.setStationId(sysOfficeVO.getId().longValue());
                    taskModel.setStationName(sysOfficeVO.getOfficeName());
                    QtBaseIdNameDTO station = new QtBaseIdNameDTO();
                    station.setId(sysOfficeVO.getId());
                    station.setName(sysOfficeVO.getOfficeName());
                    taskModel.setStation(station);
                }
            }else if(tmp.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode())
                    ||tmp.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())){
                //2）仅当生产车间为深加工车间且检验类型为首检/巡检时，默认值为已选中生产产线的“下片工位”，只读
                if(tmp.getProductWorkshop()!=null
                        &&tmp.getProductWorkshop().getName().contains(QtManagerConstant.DEEP_OFFICE_NAME)){
                    if(tmp.getProductLine()!=null
                            &&tmp.getProductLine().getId()!=null){
                        SysOfficeParentDTO officeDto = new SysOfficeParentDTO();
                        officeDto.setId(tmp.getProductLine().getId());
                        officeDto.setOfficeType(SysOfficeConstant.STATION);
                        List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                        if(CollUtil.isEmpty(sysOfficeVOS)){
                            continue;
                        }
                        SysOfficeVO sysOfficeVO = sysOfficeVOS.stream().filter(v -> v.getOfficeName().equals(QtManagerConstant.DOWN_STATION)).findFirst().orElse(null);
                        if(Objects.isNull(sysOfficeVO)){
                            continue;
                        }
                        taskModel.setStationId(sysOfficeVO.getId().longValue());
                        taskModel.setStationName(sysOfficeVO.getOfficeName());
                        QtBaseIdNameDTO station = new QtBaseIdNameDTO();
                        station.setId(sysOfficeVO.getId());
                        station.setName(sysOfficeVO.getOfficeName());
                        taskModel.setStation(station);
                    }
                }
            }else if(tmp.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode())
                    &&tmp.getProductWorkshop()!=null
                    &&tmp.getProductWorkshop().getName().contains(QtManagerConstant.JIPIAN)){
                //3）仅当生产车间为基片车间且检验类型为在线检时，默认值为已选中生产产线的“成型工位”，只读
                if(tmp.getProductLine()!=null
                        &&tmp.getProductLine().getId()!=null){
                    SysOfficeParentDTO officeDto = new SysOfficeParentDTO();
                    officeDto.setId(tmp.getProductLine().getId());
                    officeDto.setOfficeType(SysOfficeConstant.STATION);
                    List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    if(CollUtil.isEmpty(sysOfficeVOS)){
                        continue;
                    }
                    SysOfficeVO sysOfficeVO = sysOfficeVOS.stream().filter(v -> v.getOfficeName().equals(QtManagerConstant.FORMING_STATION)).findFirst().orElse(null);
                    if(Objects.isNull(sysOfficeVO)){
                        continue;
                    }
                    taskModel.setStationId(sysOfficeVO.getId().longValue());
                    taskModel.setStationName(sysOfficeVO.getOfficeName());
                    QtBaseIdNameDTO station = new QtBaseIdNameDTO();
                    station.setId(sysOfficeVO.getId());
                    station.setName(sysOfficeVO.getOfficeName());
                    taskModel.setStation(station);
                }

            }else if(tmp.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode())
                    &&tmp.getProductWorkshop()!=null
                    &&tmp.getProductWorkshop().getName().contains(QtManagerConstant.JIPIAN)){
                //4）仅当生产车间为基片车间且检验类型为化验抽检时，默认值为已选中生产产线的“下片工位”，只读
                if(tmp.getProductLine()!=null
                        &&tmp.getProductLine().getId()!=null){
                    SysOfficeParentDTO officeDto = new SysOfficeParentDTO();
                    officeDto.setId(tmp.getProductLine().getId());
                    officeDto.setOfficeType(SysOfficeConstant.STATION);
                    List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(() -> remoteOfficeFeign.listByParentId(officeDto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    if(CollUtil.isEmpty(sysOfficeVOS)){
                        continue;
                    }
                    SysOfficeVO sysOfficeVO = sysOfficeVOS.stream().filter(v -> v.getOfficeName().equals(QtManagerConstant.DOWN_STATION)).findFirst().orElse(null);
                    if(Objects.isNull(sysOfficeVO)){
                        continue;
                    }
                    taskModel.setStationId(sysOfficeVO.getId().longValue());
                    taskModel.setStationName(sysOfficeVO.getOfficeName());
                    QtBaseIdNameDTO station = new QtBaseIdNameDTO();
                    station.setId(sysOfficeVO.getId());
                    station.setName(sysOfficeVO.getOfficeName());
                    taskModel.setStation(station);
                }
            }
            taskModel.setId(tmp.getId());
            if(taskModel.getStation()!=null){
                updateById(taskModel);
            }
        }
        return true;
    }

    @Override
    public List<QtCheckTaskTypeCountVO> countTaskByType() {
        List<QtCheckTaskTypeCountVO> vos = Lists.newArrayList();
        //检验方案编码:来料检1/首检2/在线检3/巡检4/出货检5/化验抽检6/配料抽检7
        for (int i = 1; i <= 7; i++) {
            QtCheckTaskTypeCountVO vo = new QtCheckTaskTypeCountVO();
            Long count = count(new LambdaQueryWrapper<QtCheckTask>().eq(QtCheckTask::getStatus, 0).eq(QtCheckTask::getTaskType, i));
            vo.setCount(count);
            vo.setTypeCode(i);
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public Page<QtCheckTaskListVO> pageList(QtCheckTaskListDTO dto) {
        //如果是进行中则默认是暂存和进行中状态
        if (dto.getStatus() != null && dto.getStatus().equals(QtManagerConstant.QtCheckTaskStatusEnum.CHECKING.getCode())) {
            dto.setStatus(null);
            List<Integer> statusList = new ArrayList<>();
            statusList.add(QtManagerConstant.QtCheckTaskStatusEnum.CHECKING.getCode());
            statusList.add(QtManagerConstant.QtCheckTaskStatusEnum.TEMPORARY.getCode());
            dto.setStatusList(statusList);
        }
        //如果传了进行中,则包括暂存状态
        if (CollUtil.isNotEmpty(dto.getStatusList())) {
            if (dto.getStatusList().contains(QtManagerConstant.QtCheckTaskStatusEnum.CHECKING.getCode())) {
                dto.getStatusList().add(QtManagerConstant.QtCheckTaskStatusEnum.TEMPORARY.getCode());
            }
        }
        //1、配置分页信息
        MPJLambdaWrapper<QtCheckTask> condition = new MPJLambdaWrapper<QtCheckTask>()
                .select(QtCheckTask::getId,QtCheckTask::getType,QtCheckTask::getMaterialId,QtCheckTask::getMaterialWorkshop,
                        QtCheckTask::getProductLine,QtCheckTask::getProductOrder,QtCheckTask::getProductBatch,
                        QtCheckTask::getCheckPlanId,QtCheckTask::getStatus,QtCheckTask::getCheckNum,QtCheckTask::getGoodNum,
                        QtCheckTask::getBadNum,QtCheckTask::getResult,QtCheckTask::getNote,QtCheckTask::getCreateBy,QtCheckTask::getCreateName,
                        QtCheckTask::getCreateTime,QtCheckTask::getUpdateBy,QtCheckTask::getUpdateName,QtCheckTask::getUpdateTime,
                        QtCheckTask::getDelFlag,QtCheckTask::getStartTime,QtCheckTask::getFinishedTime,QtCheckTask::getArrivalTime,
                        QtCheckTask::getDeliverTime,QtCheckTask::getSupplier,QtCheckTask::getCustomer,QtCheckTask::getDefectStartTime,
                        QtCheckTask::getDefectEndTime,QtCheckTask::getDeliverOrder,QtCheckTask::getTaskType,QtCheckTask::getDeliverNum,
                        QtCheckTask::getCode,QtCheckTask::getProductWorkshop,QtCheckTask::getStartBy,QtCheckTask::getStartName,
                        QtCheckTask::getCancelBy,QtCheckTask::getCancelName,QtCheckTask::getFinishedBy,QtCheckTask::getFinishedName,
                        QtCheckTask::getCancelTime,QtCheckTask::getSendOrder,QtCheckTask::getDefectNum,QtCheckTask::getClassesId,QtCheckTask::getStationId,
                        QtCheckTask::getClassTime,QtCheckTask::getSpotTime,QtCheckTask::getClassGroupId,QtCheckTask::getClassGroupName)
                //关联表映射
                .selectAs(QtMaterial::getName, QtCheckTaskListVO::getMaterialName)
                .selectAs(QtRecheckTask::getOriginalCode, QtCheckTaskListVO::getOriginalTaskCode)
                //json映射
                .selectAs(QtCheckTask::getMaterialWorkshopName, QtCheckTaskListVO::getMaterialWorkshopName)
                .selectAs(QtCheckTask::getProductWorkshopName, QtCheckTaskListVO::getProductWorkshopName)
                .selectAs(QtCheckTask::getProductLineName, QtCheckTaskListVO::getProductLineName)
                .selectAs(QtCheckTask::getSupplierName, QtCheckTaskListVO::getSupplierName)
                .selectAs(QtCheckTask::getCustomerName, QtCheckTaskListVO::getCustomerName)
                .selectAs(QtCheckTask::getStation, QtCheckTaskListVO::getStation)
                .selectAs(QtCheckTask::getStationName, QtCheckTaskListVO::getStationName)
                .selectAs(QtCheckTask::getProductLineId, QtCheckTaskListVO::getProductLineId)
                .selectAs(QtCheckTask::getProductWorkshopId, QtCheckTaskListVO::getProductWorkshopId)
                //关联
                .leftJoin(QtMaterial.class, QtMaterial::getId, QtCheckTask::getMaterialId)
                .leftJoin(QtRecheckTask.class, QtRecheckTask::getRecheckId, QtCheckTask::getId)
                //查询
                //物料名称或者编码
                .and((dto.getMaterialNameOrCode() != null && !dto.getMaterialNameOrCode().equals("")), wrapper -> wrapper.like(QtMaterial::getName, dto.getMaterialNameOrCode()).or().like(QtMaterial::getCode, dto.getMaterialNameOrCode()))
                //状态
                .in(CollUtil.isNotEmpty(dto.getStatusList()), QtCheckTask::getStatus, dto.getStatusList())
                //状态
                .eq(dto.getStatus() != null, QtCheckTask::getStatus, dto.getStatus())
                //供应商
                .apply(dto.getSupplierId() != null, "JSON_CONTAINS(supplier, JSON_OBJECT('id', {0}))", dto.getSupplierId())
                //到货时间大于等于
                .between(dto.getArrivalTimeStart() != null && dto.getArrivalTimeEnd() != null, QtCheckTask::getArrivalTime, dto.getArrivalTimeStart(), dto.getArrivalTimeEnd())
                //物料使用车间
                .apply(dto.getMaterialWorkshopId() != null, "JSON_CONTAINS(material_workshop, JSON_OBJECT('id', {0}))", dto.getMaterialWorkshopId())
                //检验结果
                .eq(StringUtils.isNotBlank(dto.getResult()), QtCheckTask::getResult, dto.getResult())
                //生产订单号
                .like(StringUtils.isNotBlank(dto.getProductOrder()), QtCheckTask::getProductOrder, dto.getProductOrder())
                //生产车间
                .apply(dto.getProductWorkshopId() != null, "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkshopId())
                //产线
                .apply(dto.getProductLineId() != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())
                //生产批次
                .like(StringUtils.isNotBlank(dto.getProductBatch()), QtCheckTask::getProductBatch, dto.getProductBatch())
                //原始任务单号
                .like(StringUtils.isNotBlank(dto.getOriginalTaskCode()), QtRecheckTask::getOriginalCode, dto.getOriginalTaskCode())
                //发货日期
                .between(dto.getDeliverTimeStart() != null && dto.getArrivalTimeEnd() != null, QtCheckTask::getDeliverTime, dto.getDeliverTimeStart(), dto.getArrivalTimeEnd())
                //客户
                .apply(dto.getCustomerId() != null, "JSON_CONTAINS(customer, JSON_OBJECT('id', {0}))", dto.getCustomerId())
                //创建时间
                .between(dto.getCreateTimeStart() != null && dto.getCreateTimeEnd() != null, QtCheckTask::getCreateTime, dto.getCreateTimeStart(), dto.getCreateTimeEnd())
                //抽检时间
                .between(dto.getSpotTimeStart() != null && dto.getSpotTimeEnd() != null, QtCheckTask::getSpotTime, dto.getSpotTimeStart(), dto.getSpotTimeEnd())
                //创建人
                .eq(dto.getCreateBy() != null, QtCheckTask::getCreateBy, dto.getCreateBy())
                //检验类型
                .eq(dto.getCheckTypeCode() != null, QtCheckTask::getTaskType, dto.getCheckTypeCode());
        //2.解析排序规则F
        if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CODE.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getCode);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_STATUS.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY FIELD(t.status,0,1,4,2,3)");
            } else {
                condition.last("ORDER BY FIELD(t.status,3,2,4,1,0)");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(materialName using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(materialName using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_ARRIVAL_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getArrivalTime);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_SUPPLIER_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.supplier ->> '$.name' using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.supplier ->> '$.name' using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_WORKSHOP_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.material_workshop ->> '$.name' using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.material_workshop ->> '$.name' using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_ORDER.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getProductOrder);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_WORKSHOP_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.product_workshop ->> '$.name' using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.product_workshop ->> '$.name' using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_LINE_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.product_line ->> '$.name' using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.product_line ->> '$.name' using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_BATCH.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.product_batch using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.product_batch using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_START_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getDefectStartTime);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_ORIGINAL_CODE.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtRecheckTask::getOriginalCode);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_DELIVER_ORDER.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getDeliverOrder);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_DELIVER_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getDeliverTime);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CUSTOMER_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.customer ->> '$.name' using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.customer ->> '$.name' using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_DELIVER_NUM.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getDeliverNum);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_RESULT.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getResult);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_SPOT_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getSpotTime);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CREATE_NAME.equals(dto.getSortField())) {
            if (dto.getAscFlag()) {
                condition.last("ORDER BY CONVERT(t.create_name using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(t.create_name using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CREATE_TIME.equals(dto.getSortField())) {
            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getCreateTime);
        } else {
            //默认按照创建时间降序
            condition.orderByDesc(true, QtCheckTask::getCreateTime);
        }
        IPage<QtCheckTaskListVO> iPage = qtCheckTaskMapper.selectJoinPage(new Page<>(dto.getCurrent(), dto.getSize()), QtCheckTaskListVO.class, condition);
        //3、处理返回数据
        if (CollUtil.isNotEmpty(iPage.getRecords())) {
            for (QtCheckTaskListVO vo : iPage.getRecords()) {
                //若为暂存状态，则是检验中
                if (vo.getStatus() == 4)  vo.setStatus(1);
                if (vo.getMaterialWorkshopName() == null || ("null").equals(vo.getMaterialWorkshopName()))  vo.setMaterialWorkshopName("");
                if (vo.getProductWorkshopName() == null || ("null").equals(vo.getProductWorkshopName()))   vo.setProductWorkshopName(null);
                if (vo.getProductLineName() == null || ("null").equals(vo.getProductLineName()))   vo.setProductLineName(null);
                if (vo.getCustomerName() == null || ("null").equals(vo.getCustomerName()))   vo.setCustomerName(null);
                if (vo.getStationName() == null || ("null").equals(vo.getStationName()))   vo.setStationName(null);
                QtCheckPlan planNameTmp = qtCheckPlanService.getById(vo.getCheckPlanId());
                if (planNameTmp != null) {
                    vo.setCheckPlanName(planNameTmp.getName());
                }
                if(org.apache.commons.lang3.StringUtils.isNotBlank(vo.getProductLineId()) && !vo.getProductLineId().equals("null")) {
                    //生产产线code
                    R<String> productLineCode = remoteOfficeFeign.getByIdSysOfficeInfo(Long.parseLong(vo.getProductLineId()));
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(productLineCode.getMsg()))
                        vo.setProductLineCode(productLineCode.getMsg());
                }else{
                    vo.setProductLineId(null);
                }
                if(org.apache.commons.lang3.StringUtils.isNotBlank(vo.getProductWorkshopId()) && !vo.getProductWorkshopId().equals("null")) {
                    //生产车间code
                    R<String> productWorkshopCode = remoteOfficeFeign.getByIdSysOfficeInfo(Long.parseLong(vo.getProductWorkshopId()));
                    if (org.apache.commons.lang3.StringUtils.isNotBlank(productWorkshopCode.getMsg()))
                        vo.setProductWorkshopCode(productWorkshopCode.getMsg());
                }else{
                    vo.setProductWorkshopId(null);
                }
                //处理取样日期
                //来料检有取样日期
                if(vo.getTaskType().equals(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode())){
                    Set<String> dateSet = Sets.newTreeSet();
                    //找到样本信息
                    List<QtCheckTaskCopy> qtCheckTaskCopyList = qtCheckTaskCopyService.list(new LambdaQueryWrapper<QtCheckTaskCopy>().eq(QtCheckTaskCopy::getTaskId, vo.getId()));
                    if(CollUtil.isNotEmpty(qtCheckTaskCopyList)){
                        qtCheckTaskCopyList.forEach(tmp->{
                            if(Objects.nonNull(tmp.getSamplingTime())){
                                dateSet.add(tmp.getSamplingTime().toLocalDate().toString());
                            }
                            if(Objects.nonNull(tmp.getSamplingTimeArray())){
                                //obj转LocalDateTime
                                List<LocalDateTime> localDateTimeList = JSONObject.parseArray(tmp.getSamplingTimeArray().toString(), LocalDateTime.class);
                                localDateTimeList.forEach(timeStr->{
                                    dateSet.add(timeStr.toLocalDate().toString());
                                });
                            }
                        });
                    }
                    //dateSet转string
                    if(CollUtil.isNotEmpty(dateSet)){
                        StringBuilder sb = new StringBuilder();
                        for(String str:dateSet){
                            sb.append(str);
                            sb.append(",");
                        }
                        //去掉最后一个,
                        sb.deleteCharAt(sb.length() - 1);
                        vo.setSamplingTime(sb.toString());
                    }
                }
                log.debug("vo:" + vo);
            }
        }
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        BeanUtils.copyProperties(iPage, page);
        return page;
    }

//TODO

    /**
     * 根据生产订单号查询产线，批次,车间等信息
     *
     * @param code
     * @return
     */
    @Override
    public QtProduceOrderLineVO productOrderInfo(String code) {
        QtProduceOrderLineVO qtProduceOrderLineVO = new QtProduceOrderLineVO();
        R<PmsProduceOrderVO> pmsProduceOrderVO = remotePmsFeignApi.code(code);

        if (pmsProduceOrderVO.getData() != null) {
            SysOfficeParentDTO sysOfficeParentDTO = new SysOfficeParentDTO();
            //如果根据生产订单号能查询到当前订单信息，则根据订单表中的id查询批次信息
            R<PmsWorkOrderBatchVO> pmsWorkOrderBatchVO = remotePmsFeignApi.getByOrderId(pmsProduceOrderVO.getData().getId());
            if (pmsProduceOrderVO.getData().getProductionLineId() != null) {
                //根据产线id查询产线名称
                R<String> productionLineName = remoteOfficeFeign.getNameById(pmsProduceOrderVO.getData().getProductionLineId());
                //设置产线名称
                qtProduceOrderLineVO.setProductionLineName(productionLineName.getData());
            }
            String batchCode = "";
            if (pmsWorkOrderBatchVO.getData() != null){
                batchCode = pmsWorkOrderBatchVO.getData().getCode();
            }
            qtProduceOrderLineVO.setBatchNo(batchCode);
            sysOfficeParentDTO.setId(pmsProduceOrderVO.getData().getProductionLineId());
            sysOfficeParentDTO.setOfficeType(PmManagerConstant.STATE_9);
            Integer productionLineId = pmsProduceOrderVO.getData().getProductionLineId();


            BeanUtils.copyProperties(pmsProduceOrderVO.getData(),qtProduceOrderLineVO);
            if (productionLineId != null){
                //根据产线查询父级车间
                SysOfficeVO sysOfficeVO = FeignOptional.of(()->remoteOfficeFeign.parent(productionLineId), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                qtProduceOrderLineVO.setWorkshopId(sysOfficeVO.getId());
                qtProduceOrderLineVO.setWorkshopName(sysOfficeVO.getOfficeName());
            }
        }

        return qtProduceOrderLineVO;
    }

    /**
     * 根据产线查询生产订单号,批次等信息
     *
     * @param id
     * @return
     */
    @Override
    public QtProduceOrderLineVO selectProductOrder(Integer id) {

        QtProduceOrderLineVO qtProduceOrderLineVO = new QtProduceOrderLineVO();
        PmsProduceOrderVO pmsProduceOrderVO = remotePmsFeignApi.listByProduceLineId(id).getData();
        Assert.notNull(pmsProduceOrderVO,"该产线的生产订单尚未开始,请通知生产人员");
        BeanUtils.copyProperties(pmsProduceOrderVO, qtProduceOrderLineVO);

        return qtProduceOrderLineVO;
    }

    @Override
    public Page<QtCheckProduceTaskVO> pageProductOrder(QtCheckaProductTaskListDTO dto) {
        Objects.requireNonNull(dto.getProductOrder(), "生产订单号不能为空");
        List<Long> stationIds = dto.getStationIds();
        List<Integer> checkTypeCodeList = Arrays.asList(
                QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode(),
                QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode(),
                QtManagerConstant.QtCheckTaskTypeEnum.FIRST.getCode(),
                QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode()
        );
        MPJLambdaWrapper<QtCheckTask> productOrder = new MPJLambdaWrapper<QtCheckTask>()
                .selectAll(QtCheckTask.class)
                .leftJoin(QtRecheckTask.class, QtRecheckTask::getRecheckId, QtCheckTask::getId)
                .leftJoin(QtMaterial.class, QtMaterial::getId, QtCheckTask::getMaterialId)
                .selectAs(QtRecheckTask::getOriginalCode, QtCheckTaskListVO::getOriginalTaskCode)
                .selectAs(QtMaterial::getName, QtCheckTask::getMaterialName)
                .eq(QtCheckTask::getProductOrder, dto.getProductOrder())
                .in(QtCheckTask::getTaskType, checkTypeCodeList)
                .and(wrapper -> wrapper.apply("JSON_CONTAINS(JSON_ARRAY(" + String.join(",", stationIds.stream().map(Object::toString).collect(Collectors.toList())) + "), station->'$.id')"));
        if (Objects.nonNull(dto.getSortField())) {
            switch (dto.getSortField()) {
                case QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CODE:
                    productOrder.orderBy(true, dto.getAscFlag(), QtCheckTask::getCode);
                    break;
                case QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_STATUS:
                    if (dto.getAscFlag()) {
                        productOrder.last("ORDER BY FIELD(t.status,0,1,4,2,3)");
                    } else {
                        productOrder.last("ORDER BY FIELD(t.status,3,2,4,1,0)");
                    }
                    break;
                case QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME:
                    if (dto.getAscFlag()) {
                        productOrder.last("ORDER BY CONVERT(materialName using gbk) ASC");
                    } else {
                        productOrder.last("ORDER BY CONVERT(materialName using gbk) DESC");
                    }
                    break;
                case QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_ARRIVAL_TIME:
                    productOrder.orderBy(true, dto.getAscFlag(), QtCheckTask::getArrivalTime);
                    break;
                case QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_SUPPLIER_NAME:
                    if (dto.getAscFlag()) {
                        productOrder.last("ORDER BY CONVERT(t.supplier ->> '$.name' using gbk) ASC");
                    } else {
                        productOrder.last("ORDER BY CONVERT(t.supplier ->> '$.name' using gbk) DESC");
                    }
                    break;
                case QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_ORDER:
                    productOrder.orderBy(true, dto.getAscFlag(), QtCheckTask::getProductOrder);
                    break;
                case QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_BATCH:
                    if (dto.getAscFlag()) {
                        productOrder.last("ORDER BY CONVERT(t.product_batch using gbk) ASC");
                    } else {
                        productOrder.last("ORDER BY CONVERT(t.product_batch using gbk) DESC");
                    }
                    break;
                case QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_START_TIME:
                    productOrder.orderBy(true, dto.getAscFlag(), QtCheckTask::getDefectStartTime);
                    break;
                case QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_DEFECT_ORIGINAL_CODE:
                    productOrder.orderBy(true, dto.getAscFlag(), QtRecheckTask::getOriginalCode);
                    break;
                case QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CREATE_TIME:
                    productOrder.orderBy(true, dto.getAscFlag(), QtCheckTask::getCreateTime);
                    break;
                default:
                    productOrder.orderByDesc(true, QtCheckTask::getCreateTime);
                    break;
            }
        }
        Page<QtCheckTask> resPage = page(new Page<>(dto.getCurrent(), dto.getSize()), productOrder);
        List<QtCheckProduceTaskVO> resRecords = resPage.getRecords().stream().map(qtCheckTask -> {
            QtCheckProduceTaskVO vo = new QtCheckProduceTaskVO();
            BeanUtils.copyProperties(qtCheckTask, vo);
            if (qtCheckTask.getTaskType() != QtManagerConstant.QtCheckTaskTypeEnum.ONLINE.getCode()) {
                vo.setDefectStartTime(null);
                vo.setDefectEndTime(null);
            }
            return vo;
        }).collect(Collectors.toList());
        Page<QtCheckProduceTaskVO> produceTaskpage = new Page<>(dto.getCurrent(), dto.getSize());
        produceTaskpage.setRecords(resRecords);
        produceTaskpage.setTotal(resPage.getTotal());
        return produceTaskpage;
    }

    @Override
    public List<QtCheckTask> getSpcCheckTask(QtReport report) {
        //设置查询条件
        List<QtCheckTask> dbList = list(new QueryWrapper<QtCheckTask>()
                .lambda()
                .eq(QtCheckTask::getMaterialId, report.getMaterialId())
                .eq(QtCheckTask::getTaskType, report.getCheckType())
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .ge(QtCheckTask::getFinishedTime, report.getTimeScopeStart())
                .lt(QtCheckTask::getFinishedTime, report.getTimeScopeEnd()));

        //根据条件先low出所有的检验任务数据
        return Objects.nonNull(dbList) ? dbList : Lists.newArrayList();
    }

    @Override
    public List<QtCheckTask> getSpcCheckTaskSpotTime(QtReport report) {
        //设置查询条件
        List<QtCheckTask> dbList = list(new QueryWrapper<QtCheckTask>()
                .lambda()
                .eq(QtCheckTask::getMaterialId, report.getMaterialId())
                .eq(QtCheckTask::getTaskType, report.getCheckType())
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .ge(QtCheckTask::getSpotTime, report.getTimeScopeStart())
                .lt(QtCheckTask::getSpotTime, report.getTimeScopeEnd()));

        //根据条件先low出所有的检验任务数据
        return Objects.nonNull(dbList) ? dbList : Lists.newArrayList();
    }


    /**
     * 深加工/基片巡检合格率趋势明细分页表
     *
     * @param dto
     * @return
     */
    @Override
    public Page<QtDeepOrSubAcceptabilityDetailVO> getQtDeepOrSubAcceptabilityDetailVO(String keyword, QtSubOrDeepAcceptabilityDTO dto) {
        Page<QtDeepOrSubAcceptabilityDetailVO> page = new Page<>(dto.getCurrent(), dto.getSize());
        //根据条件获取数据
        List<QtCheckTaskReportVO> rateDataList = rateDataList(keyword, dto);
        rateDataList = rateDataList.stream().filter(v -> Objects.nonNull(v.getClassesId()) &&
                Objects.nonNull(v.getClassTime()) &&
                Objects.nonNull(v.getProductWorkshopId()) &&
                Objects.nonNull(v.getProductLineId()) &&
                Objects.nonNull(v.getMaterialId())).collect(Collectors.toList());
        if (CollUtil.isEmpty(rateDataList)) {
            page.setTotal(0L);
            page.setRecords(Lists.newArrayList());
            return page;
        }
        //车间，产线，物料名称，客户
        Set<Integer> officeIdSet = Sets.newHashSet();
        Set<Long> materialIdSet = Sets.newHashSet();
        rateDataList.forEach(ele -> {
            officeIdSet.add(ele.getProductWorkshopId());
            officeIdSet.add(ele.getProductLineId());
            officeIdSet.add(ele.getCustomerId());
            materialIdSet.add(ele.getMaterialId());
        });
        Map<Integer, String> officeIdNameMap = FeignOptional.of(()->remoteOfficeFeign.listByIdCh(officeIdSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, String> materialIdNameMap = FeignOptional.of(()->remoteSpareFeignApi.listByIdCh(materialIdSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        Map<LocalDate, List<QtCheckTaskReportVO>> classDateMap = rateDataList.stream().collect(Collectors.groupingBy(v -> v.getClassTime()));
        //定义返回的集合
        List<QtDeepOrSubAcceptabilityDetailVO> resList = Lists.newArrayList();

        for (Map.Entry<LocalDate, List<QtCheckTaskReportVO>> classDateEntry : classDateMap.entrySet()) {
            Map<Integer, List<QtCheckTaskReportVO>> classMap = classDateEntry.getValue().stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getClassesId));
            for (Map.Entry<Integer, List<QtCheckTaskReportVO>> classEntry : classMap.entrySet()) {
                Map<Integer, List<QtCheckTaskReportVO>> produceWorkshopMap = classEntry.getValue().stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getProductWorkshopId));
                for (Map.Entry<Integer, List<QtCheckTaskReportVO>> pwsEntry : produceWorkshopMap.entrySet()) {
                    Map<Integer, List<QtCheckTaskReportVO>> lineMap = pwsEntry.getValue().stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getProductLineId));
                    for (Map.Entry<Integer, List<QtCheckTaskReportVO>> lineEntry : lineMap.entrySet()) {
                        Map<Long, List<QtCheckTaskReportVO>> materialMap = lineEntry.getValue().stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getMaterialId));
                        for (Map.Entry<Long, List<QtCheckTaskReportVO>> materialEntry : materialMap.entrySet()) {
                            Map<String, List<QtCheckTaskReportVO>> customerMap = materialEntry.getValue().stream().collect(Collectors.groupingBy(v -> v.getCustomerId() + ""));
                            for (Map.Entry<String, List<QtCheckTaskReportVO>> customerEntry : customerMap.entrySet()) {
                                QtDeepOrSubAcceptabilityDetailVO resVO = new QtDeepOrSubAcceptabilityDetailVO();
                                resVO.setClassesDate(classDateEntry.getKey());
                                QtManagerConstant.QtClassesEnum classEnum = QtManagerConstant.QtClassesEnum.code(classEntry.getKey());
                                resVO.setClassName(Objects.nonNull(classEnum) ? classEnum.getName() : Strings.EMPTY);
                                resVO.setProductWorkshopName(officeIdNameMap.getOrDefault(pwsEntry.getKey(), Strings.EMPTY));
                                resVO.setProductLineName(officeIdNameMap.getOrDefault(lineEntry.getKey(), Strings.EMPTY));
                                resVO.setMaterialName(materialIdNameMap.getOrDefault(materialEntry.getKey(), Strings.EMPTY));
                                if(!"null".equals(customerEntry.getKey())){
                                    Integer customerId = Integer.parseInt(customerEntry.getKey());
                                    resVO.setCustomerId(customerId);
                                    resVO.setCustomerName(officeIdNameMap.getOrDefault(customerId, Strings.EMPTY));
                                }
                                List<QtSubOrDeepFPYTaskDetailsVO> taskDetails = Lists.newArrayList();
                                List<BigDecimal> rateList = Lists.newArrayList();
                                materialEntry.getValue().forEach(ele -> {
                                    QtSubOrDeepFPYTaskDetailsVO detail = new QtSubOrDeepFPYTaskDetailsVO();
                                    detail.setCode(ele.getCode());
                                    detail.setDetailAcceptability(BigDecimalUtil.divide(BigDecimal.valueOf(ele.getGoodNum()).multiply(BigDecimalUtil.HUNDRED), BigDecimal.valueOf(ele.getCheckNum())));
                                    detail.setFinishedTime(ele.getFinishedTime());
                                    detail.setSpotTime(ele.getSpotTime());
                                    detail.setCode(ele.getCode());
                                    detail.setResult(ele.getResult());
                                    detail.setTaskId(ele.getTaskId());
                                    rateList.add(detail.getDetailAcceptability());
                                    taskDetails.add(detail);
                                });
                                BigDecimal reduce = BigDecimalUtil.sum(rateList);
                                BigDecimal divide = BigDecimalUtil.divide(reduce, BigDecimal.valueOf(rateList.size()), 2);
                                resVO.setAcceptability(divide);
                                resVO.setTaskDetails(taskDetails);
                                resList.add(resVO);
                            }
                        }
                    }
                }
            }
        }
        //过滤图例
        legendFilter(dto,resList);
        //分页
        Page<QtDeepOrSubAcceptabilityDetailVO> page1 = LogicalPageUtil.page(resList, dto.getCurrent(), dto.getSize());
        return page1;
    }

    /**
     * 图例过滤
     * @param dto
     * @param pageList
     * @return
     */
    private List<QtDeepOrSubAcceptabilityDetailVO> legendFilter(QtSubOrDeepAcceptabilityDTO dto, List<QtDeepOrSubAcceptabilityDetailVO> pageList) {
        //对比模式下考虑到图例的过滤
        if (QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode().equals(dto.getCompare())) {
            pageList = pageList.stream().filter(v -> CollUtil.isNotEmpty(dto.getFilters()) && dto.getFilters().contains(v.getProductWorkshopName())).collect(Collectors.toList());
        } else if (QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode().equals(dto.getCompare())) {
            pageList = pageList.stream().filter(v -> CollUtil.isNotEmpty(dto.getFilters()) && dto.getFilters().contains(v.getProductLineName())).collect(Collectors.toList());
        } else if (QtManagerConstant.QtChartCompareEnum.WORK.getCode().equals(dto.getCompare())) {
            pageList = pageList.stream().filter(v -> CollUtil.isNotEmpty(dto.getFilters()) && dto.getFilters().contains(v.getClassName())).collect(Collectors.toList());
        }
        pageList.forEach(detailVO -> detailVO.setClassesDateString(LocalDateUtil.localDateFormat(detailVO.getClassesDate(), DatePattern.NORM_DATE_PATTERN)));
        return pageList;
    }

    private LambdaQueryWrapper<QtCheckTask> getQtDeepOrSubAcceptabilityWrapper(QtSubOrDeepAcceptabilityDTO dto) {
        //先根据开始时间和结束时间查出这段时间内所有完成的符合条件的检验项
         LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>().select("any_value(type) type , any_value(material_id) material_id , " +
                        "any_value(material_workshop) material_workshop , any_value(product_line) product_line , any_value(product_order) product_order , " +
                        "any_value(product_batch) product_batch , any_value(check_plan_id) check_plan_id , any_value(status) status , sum(check_num) check_num , " +
                        "sum(good_num) good_num , sum(bad_num) bad_num , any_value(result) result , any_value(del_flag) del_flag , " +
                        "any_value(supplier) supplier , any_value(customer) customer , any_value(deliver_order) deliver_order , any_value(task_type) task_type ," +
                        "any_value(deliver_num) deliver_num , any_value(product_workshop) product_workshop , any_value(classes_id) classes_id , any_value ( code ) code , " +
                         "finished_time").lambda()
                //车间
                .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkShopId())
                //产线
                .apply(Objects.nonNull(dto.getProductLineId()), "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())
                //物料
                .and(Objects.nonNull(dto.getMaterialId()), wrapper -> wrapper.eq(QtCheckTask::getMaterialId, dto.getMaterialId()))
                //客户
                .apply(Objects.nonNull(dto.getCustomer()), "JSON_CONTAINS(customer, JSON_OBJECT('id', {0}))", dto.getCustomer());


        //判断是深加工车间还是基片车间
        if (dto.getName().equals(QtManagerConstant.DEEP_OFFICE_NAME)) {
            condition.apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.DEEP_OFFICE_NAME_ONE)
                    .or()
                    .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.DEEP_OFFICE_NAME_TWO)
                    .or()
                    .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.DEEP_OFFICE_NAME_THREE);
        }
        if (dto.getName().equals(QtManagerConstant.SUB_OFFICE_NAME)) {
            condition.apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.SUB_OFFICE_NAME_ONE)
                    .or()
                    .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('name', {0}))", QtManagerConstant.SUB_OFFICE_NAME_TWO);
        }

        //判断是否筛选白班夜班
        if (QtManagerConstant.QtClassesEnum.DAY.getCode().equals(dto.getWorkingOrder())) {
            condition.eq(QtCheckTask::getClassesId, QtManagerConstant.QtClassesEnum.DAY);
        } else if (QtManagerConstant.QtClassesEnum.NIGHT.equals(dto.getWorkingOrder())) {
            condition.eq(QtCheckTask::getClassesId, QtManagerConstant.QtClassesEnum.NIGHT);
        }


        //根据完成时间筛选
        List<SFunction<QtCheckTask,?>> functionList = new ArrayList<>();

        functionList.add(QtCheckTask::getFinishedTime);
        if (QtManagerConstant.QtChartCompareEnum.WORK.getCode()==dto.getCompare()){
            functionList.add(QtCheckTask::getClassesId);
        }else if (QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode()==dto.getCompare()){
            functionList.add(QtCheckTask::getProductWorkshopId);
        }else if (QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode()==dto.getCompare()){
            functionList.add(QtCheckTask::getProductLineId);
        }


        condition.ge(QtCheckTask::getFinishedTime, dto.getDateStart())
                .le(QtCheckTask::getFinishedTime, dto.getDateEnd())
                .groupBy(functionList);
        //2.解析排序规则
        if ((QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CLASS_ID.equals(dto.getSortField()))) {
            if (dto.getAscFlag()) {
                //使用last把sql用于排序的中文函数代码语句拼接到最后
                condition.last("ORDER BY CONVERT(class_id using gbk) ASC");
            } else {
                condition.last("ORDER BY CONVERT(class_id using gbk) DESC");
            }
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_WORKSHOP_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getProductWorkshopName);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_PRODUCT_LINE_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getProductLine);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getMaterialId);
        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_CUSTOMER_NAME.equals(dto.getSortField())) {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getCustomerName);
        } else {
            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getFinishedTime);
        }
        return condition;
    }

    /**
     * 深加工或基片合格率图表
     *
     * @param dto
     * @return
     */
    @Override
    public QtLineChartBiBarDTO getQtChartDeepOrSubBarVO(String keyword, QtSubOrDeepAcceptabilityDTO dto) {
        List<QtCheckTaskReportVO> flatDataList  = rateDataList(keyword, dto);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无报表");
        //定义数据集合
        List<BiStackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();

        //将时间范围根据粒度分片
        List<LocalDateTime> cutDate = ShardTimeUtil.cutDateRangeNew(QtManagerConstant.QtGranularityEnum.DAY.getType()
                , LocalDateUtil.getMorning(dto.getDateStart())
                , LocalDateUtil.getMorning(dto.getDateEnd().plusDays(1)));
        Assert.notEmpty(cutDate, "时间范围根据粒度分片为空");

        //考虑对比
        TupleDTO<Map<Integer, List<QtCheckTaskReportVO>>, Map<Integer, String>> mapMapTupleDTO = commonGroup(dto.getCompare(), flatDataList);
        //获取对比数据集和对比维度
        Map<Integer, List<QtCheckTaskReportVO>> contrastDataMap = mapMapTupleDTO.getField1();
        Map<Integer, String> idNameMap = mapMapTupleDTO.getField2();

        List<QtCheckTaskReportVO> allSelectList = Lists.newArrayList();
        for (int i = 1; i < cutDate.size(); i++) {
            LocalDateTime start = cutDate.get(i - 1);
            LocalDateTime end = cutDate.get(i);
            //获取时间范围的集合
            List<QtCheckTaskReportVO> conditionDataList = flatDataList.stream().filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                LocalDate classDate = LocalDateUtil.getClassDate(start, false);
                if (classDate.isEqual(v.getClassTime())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());
            //加入所有下拉选项数据
            allSelectList.addAll(conditionDataList);
            BiStackGraphDTO<String, BigDecimal> biStackGraphDTO = new BiStackGraphDTO<>();
            biStackGraphDTO.setXAxis(LocalDateTimeUtil.format(start, DatePattern.NORM_DATE_PATTERN));
            biStackGraphDTO.setData(Lists.newArrayList());

            //默认集合
            List<QtCheckTaskReportVO> defaultQtCheckTaskList = Lists.newArrayList();
            //对比的结果
            for (Map.Entry<Integer, String> entry : idNameMap.entrySet()) {
                //获取对比数据
                List<QtCheckTaskReportVO> contrastData = contrastDataMap.getOrDefault(entry.getKey(), defaultQtCheckTaskList);
                Set<Long> conditionIdList = conditionDataList.stream().map(QtCheckTaskReportVO::getId).collect(Collectors.toSet());
                contrastData = contrastData.stream().filter(v -> conditionIdList.contains(v.getId())).collect(Collectors.toList());
                TupleDTO<String, BigDecimal> tupleDTO = getTupleDTO(contrastData, entry.getValue());
                biStackGraphDTO.getData().add(Lists.newArrayList(tupleDTO));
            }
            stackGraphDTOList.add(biStackGraphDTO);
        }
        //自定义转换工具
        QtLineChartBiBarDTO qtLineChartBiBarDTO = StackGraphUtil.biConvertData(stackGraphDTOList, Boolean.FALSE);

        //基片巡检合格率趋势-柱状图 全量数据表查询车间，产线，班次，物料
        Set<Integer> classIdSet = Sets.newTreeSet();
        Set<Integer> groupIdSet = Sets.newTreeSet();
        Set<Integer> workShopIdSet = Sets.newTreeSet();
        Set<Integer> lineIdSet = Sets.newTreeSet();
        Set<Long> materialIdSet = Sets.newHashSet();
        Set<Integer> customerIdSet = Sets.newHashSet();
        for (QtCheckTaskReportVO data : flatDataList) {
            if(Objects.nonNull(data.getClassesId())){
                classIdSet.add(data.getClassesId());
            }
            if(Objects.nonNull(data.getProductWorkshopId())){
                workShopIdSet.add(data.getProductWorkshopId());
            }
            if(Objects.nonNull(data.getProductLineId())){
                lineIdSet.add(data.getProductLineId());
            }
            if(Objects.nonNull(data.getMaterialId())){
                materialIdSet.add(data.getMaterialId());
            }
            if(Objects.nonNull(data.getCustomerId())){
                customerIdSet.add(data.getCustomerId());
            }
            if(Objects.nonNull(data.getClassGroupId())){
                groupIdSet.add(data.getClassGroupId());
            }
        }
        BaseScreenVO baseScreenVO = new BaseScreenVO();
        baseScreenVO.setWorkingOrderScreen(conditionData(classIdSet, QtManagerConstant.QtClassesEnum.allClass()));
        baseScreenVO.setProductWorkShopScreen(conditionData(workShopIdSet, FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(workShopIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get()));
        baseScreenVO.setProductLineScreen(conditionData(lineIdSet, FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(lineIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get()));
        baseScreenVO.setMaterialScreen(conditionData(materialIdSet, FeignOptional.of(() -> remoteSpareFeignApi.listByIdCh(materialIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get()));
        baseScreenVO.setCustomerScreen(conditionData(customerIdSet, FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(customerIdSet), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get()));
        baseScreenVO.setClassGroupScreen(conditionData(groupIdSet, OaManagerConstant.OaTeamGroupMapping.allGroup()));
        qtLineChartBiBarDTO.setOther(baseScreenVO);
        return qtLineChartBiBarDTO;
    }

    /**
     * 获取全量数据表的条件集合
     * @param idSet
     * @param idNameMap
     * @param <T>
     * @return
     */
    private <T> List<BaseSelectVO<T>> conditionData(Set<T> idSet, Map<T, String> idNameMap) {
        List<BaseSelectVO<T>> resList = org.apache.commons.compress.utils.Lists.newArrayList();
        for (T id : idSet) {
            resList.add(new BaseSelectVO<T>(id, idNameMap.get(id)));
        }
        return resList;
    }

    /**
     * 拷贝数据
     *
     * @param sourceData 原始数据
     * @return
     */
    private List<QtCheckTaskReportVO> copyQtCheckTask(List<QtCheckTask> sourceData) {
        //班組信息
        Optional<QtCheckTask> minOp = sourceData.stream().filter(v -> Objects.nonNull(v.getSpotTime())).min(Comparator.comparing(QtCheckTask::getSpotTime));
        Optional<QtCheckTask> maxOp = sourceData.stream().filter(v -> Objects.nonNull(v.getSpotTime())).max(Comparator.comparing(QtCheckTask::getSpotTime));
        Map<String, OaGroupCountDTO> groupMap;
        if(minOp.isPresent() && maxOp.isPresent()){
            LocalDate start = LocalDateUtil.getClassDate(minOp.get().getSpotTime(), Boolean.FALSE);
            LocalDate end = LocalDateUtil.getClassDate(maxOp.get().getSpotTime(), Boolean.TRUE);
            TupleDTO<LocalDate,LocalDate> dateDTO = new TupleDTO<>(start, end);
            groupMap = FeignOptional.of(() -> oaInfoService.getGroupInfo(dateDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        }else{
            groupMap = Maps.newHashMap();
        }

        List<QtCheckTaskReportVO> resList = Lists.newArrayList();
        Map<String, OaGroupCountDTO> finalGroupMap = groupMap;
        OaGroupCountDTO defaultOaGroupCountDTO = new OaGroupCountDTO();
        sourceData.forEach(ele -> {
            QtCheckTaskReportVO vo = new QtCheckTaskReportVO();
            BeanUtils.copyProperties(ele, vo);
            if(Objects.nonNull(vo.getProductLine())){
                vo.setProductLineId(ele.getProductLine().getId());
            }
            if(Objects.nonNull(ele.getProductWorkshop())){
                vo.setProductWorkshopId(ele.getProductWorkshop().getId());
            }
            if(Objects.nonNull(ele.getMaterialWorkshop())){
                vo.setMaterialWorkshopId(ele.getMaterialWorkshop().getId());
            }
            if(Objects.nonNull(ele.getSupplier())){
                vo.setSupplierId(ele.getSupplier().getId());
            }
            if(Objects.nonNull(ele.getCustomer())){
                vo.setCustomerId(ele.getCustomer().getId());
            }
            vo.setTaskId(ele.getId());
            if(Objects.nonNull(vo.getSpotTime())){
                QtTaskClassCalculate qtTaskClassCalculate = CalculateDateUtil.calculateClassTime(vo.getSpotTime());
                vo.setClassesId(qtTaskClassCalculate.getClassType());
                vo.setClassTime(qtTaskClassCalculate.getClassDate().toLocalDate());
                //班組信息
                QtManagerConstant.QtClassesEnum code = QtManagerConstant.QtClassesEnum.code(vo.getClassesId());
                if (Objects.nonNull(code)) {
                    OaGroupCountDTO oaGroupCountDTO = finalGroupMap.getOrDefault(vo.getClassTime() + ":" + code.getName(), defaultOaGroupCountDTO);
                    vo.setClassGroupId(oaGroupCountDTO.getGroupId());
                    vo.setClassGroupName(oaGroupCountDTO.getGroupName());
                }
            }
            resList.add(vo);
        });
        return resList;
    }

    /**
     * 公共分组
     *
     * @param compare      对比
     * @param flatDataList 数据集合
     */
    private TupleDTO<Map<Integer, List<QtCheckTaskReportVO>>, Map<Integer, String>> commonGroup(Integer compare, List<QtCheckTaskReportVO> flatDataList) {
        Map<Integer, List<QtCheckTaskReportVO>> targetMap;
        Map<Integer, String> idNameMap;
        List<QtCheckTaskReportVO> flatDataCopyList ;
        if (QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode().equals(compare)) {
            flatDataCopyList = flatDataList.stream().filter(v -> Objects.nonNull(v.getProductWorkshopId())).collect(Collectors.toList());
            flatDataList.clear();
            flatDataList.addAll(flatDataCopyList);
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getProductWorkshopId));
            Set<Integer> workshopIdSet = flatDataList.stream().filter(v -> Objects.nonNull(v.getProductWorkshopId())).map(QtCheckTaskReportVO::getProductWorkshopId).collect(Collectors.toSet());
            Map<Integer, SysOfficeVO> dataMap = FeignOptional.of(()->remoteOfficeFeign.listById(workshopIdSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            idNameMap =  officeContrastSort(dataMap);
        } else if (QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode().equals(compare)) {
            flatDataCopyList = flatDataList.stream().filter(v -> Objects.nonNull(v.getProductLineId())).collect(Collectors.toList());
            flatDataList.clear();
            flatDataList.addAll(flatDataCopyList);
            targetMap = flatDataList.stream().filter(v -> Objects.nonNull(v.getProductLineId())).collect(Collectors.groupingBy(QtCheckTaskReportVO::getProductLineId));
            Set<Integer> lineIdSet = flatDataList.stream().filter(v -> Objects.nonNull(v.getProductLineId())).map(QtCheckTaskReportVO::getProductLineId).collect(Collectors.toSet());
            Map<Integer, SysOfficeVO> dataMap = FeignOptional.of(()->remoteOfficeFeign.listById(lineIdSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            idNameMap =  officeContrastSort(dataMap);
        } else if (QtManagerConstant.QtChartCompareEnum.MATERIALWORKSHOP.getCode().equals(compare)) {
            flatDataCopyList = flatDataList.stream().filter(v -> Objects.nonNull(v.getMaterialWorkshopId())).collect(Collectors.toList());
            flatDataList.clear();
            flatDataList.addAll(flatDataCopyList);
            targetMap = flatDataList.stream().filter(v -> Objects.nonNull(v.getMaterialWorkshopId())).collect(Collectors.groupingBy(QtCheckTaskReportVO::getMaterialWorkshopId));
            Set<Integer> idSet = flatDataList.stream().filter(v -> Objects.nonNull(v.getMaterialWorkshopId())).map(QtCheckTaskReportVO::getMaterialWorkshopId).collect(Collectors.toSet());
            Map<Integer, SysOfficeVO> dataMap = FeignOptional.of(()->remoteOfficeFeign.listById(idSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            idNameMap =  officeContrastSort(dataMap);
        }else if(QtManagerConstant.QtChartCompareEnum.WORK.getCode().equals(compare)){
            flatDataCopyList = flatDataList.stream().filter(v -> Objects.nonNull(v.getClassesId())).collect(Collectors.toList());
            flatDataList.clear();
            flatDataList.addAll(flatDataCopyList);
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getClassesId));
            Set<Integer> classIdSet = flatDataList.stream().map(QtCheckTaskReportVO::getClassesId).collect(Collectors.toSet());
            idNameMap = Maps.newHashMap();
            for (Integer classId : classIdSet) {
                QtManagerConstant.QtClassesEnum code = QtManagerConstant.QtClassesEnum.code(classId);
                idNameMap.put(code.getCode(), code.getName());
            }
        }else if(QtManagerConstant.QtChartCompareEnum.CUSTOMER.getCode().equals(compare)){
          //对比客户
            flatDataCopyList = flatDataList.stream().filter(v -> Objects.nonNull(v.getCustomerId())).collect(Collectors.toList());
            flatDataList.clear();
            flatDataList.addAll(flatDataCopyList);
            targetMap = flatDataList.stream().filter(v -> Objects.nonNull(v.getCustomerId())).collect(Collectors.groupingBy(QtCheckTaskReportVO::getCustomerId));
            Set<Integer> idSet = flatDataList.stream().filter(v -> Objects.nonNull(v.getCustomerId())).map(QtCheckTaskReportVO::getCustomerId).collect(Collectors.toSet());
            Map<Integer, SysOfficeVO> dataMap = FeignOptional.of(() -> remoteOfficeFeign.listById(idSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            idNameMap =  officeContrastSort(dataMap);
        }else if(QtManagerConstant.QtChartCompareEnum.CLASSGROUP.getCode().equals(compare)){
            //对比班组
            targetMap = flatDataList.stream().filter(v ->Objects.nonNull(v.getClassGroupId())).collect(Collectors.groupingBy(QtCheckTaskReportVO::getClassGroupId));
            Set<Integer> groupIdSet = flatDataList.stream().filter(v ->Objects.nonNull(v.getClassGroupId())).map(QtCheckTaskReportVO::getClassGroupId).collect(Collectors.toSet());
            idNameMap = Maps.newHashMap();
            List<Integer> groupIdList = groupIdSet.stream().sorted(Comparator.comparingInt(x -> x)).collect(Collectors.toList());
            for (Integer groupId : groupIdList) {
                OaManagerConstant.OaTeamGroupMapping code = OaManagerConstant.OaTeamGroupMapping.code(groupId);
                if (Objects.nonNull(code)) {
                    idNameMap.put(code.getCode(), code.getName());
                }
            }
        } else{
            //不对比
            targetMap = Maps.newHashMap();
            targetMap.put(0, flatDataList);
            idNameMap = Maps.newHashMap();
            idNameMap.put(0, StackGraphUtil.STR_DEFAULT);
        }
        return new TupleDTO<>(targetMap, idNameMap);
    }

    /**
     * 组织架构对比排序
     * @param dataMap 数据map
     * @return 排序后的map
     */
    private Map<Integer, String> officeContrastSort(Map<Integer, SysOfficeVO> dataMap) {
        Map<Integer,String> idNameMap = Maps.newLinkedHashMap();
        if(CollUtil.isNotEmpty(dataMap)){
            List<SysOfficeVO> collect = dataMap.values().stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
            collect.forEach(ele -> {
                idNameMap.put(ele.getId(),ele.getOfficeName());
            });
        }
        return idNameMap;
    }

    /**
     * 求和，返回二元组数据
     *
     * @param tDataList 原始数据
     * @param key       元素显示
     * @return
     */
    private TupleDTO<String, BigDecimal> getTupleDTO(List<QtCheckTaskReportVO> tDataList, String key) {
        List<Integer> checkSumList = tDataList.stream().filter(v -> Objects.nonNull(v.getCheckNum())).map(QtCheckTaskReportVO::getCheckNum).collect(Collectors.toList());
        List<Integer> goodSumList = tDataList.stream().filter(v -> Objects.nonNull(v.getGoodNum())).map(QtCheckTaskReportVO::getGoodNum).collect(Collectors.toList());
        Integer checkSum = IntegerUtil.sum(checkSumList);
        Integer goodSum = IntegerUtil.sum(goodSumList);
        return new TupleDTO<>(key, BigDecimalUtil.percent(BigDecimalUtil.valueOf(goodSum), BigDecimalUtil.valueOf(checkSum)));
    }


    /**
     * 获取合格率基础数据集合
     * @param keyword 关键字，深加工||基片
     * @param dto dto
     * @return list
     */
    private List<QtCheckTaskReportVO> rateDataList(String keyword, QtSubOrDeepAcceptabilityDTO dto) {
        dto.setDateTimeStart(LocalDateUtil.getMorning(dto.getDateStart()));
        dto.setDateTimeEnd(LocalDateUtil.getMorning(dto.getDateEnd().plusDays(1)));
        List<QtCheckTaskReportVO> resList = Lists.newArrayList();
        Set<Integer> produceLineSet = null;
        if(StrUtil.isNotBlank(keyword)){
            SysOfficeDTO sysOfficeDTO = new SysOfficeDTO();
            sysOfficeDTO.setOfficeName(keyword);
            sysOfficeDTO.setOfficeType(QtManagerConstant.OFFICE_PRODUCE_LINE_TYPE);
            List<SysOfficeVO> sysOfficeVOList = FeignOptional.of(()->remoteOfficeFeign.getByTypeAndLine(sysOfficeDTO),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            Assert.notEmpty(sysOfficeVOList, "产线集合不能为空");
            produceLineSet = sysOfficeVOList.stream().map(SysOfficeVO::getId).collect(Collectors.toSet());
        }
        List<QtCheckTask> qtCheckTasks = this.baseMapper.rateDataList(produceLineSet, dto);
        if(CollUtil.isNotEmpty(qtCheckTasks)){
            resList = copyQtCheckTask(qtCheckTasks);
            if(QtManagerConstant.QtCheckTaskTypeEnum.INCOMING.getCode().equals(dto.getTaskType())){
                //获取时间
                Set<Long> taskIdSet = resList.stream().map(QtCheckTaskReportVO::getId).collect(Collectors.toSet());
                //任务id集合
                List<QtCheckTaskCopy> copyList = qtCheckTaskCopyService.list(new QueryWrapper<QtCheckTaskCopy>()
                        .select("task_id , min(sampling_time) as sampling_time")
                        .lambda()
                        .in(QtCheckTaskCopy::getTaskId, taskIdSet)
                        .groupBy(QtCheckTaskCopy::getTaskId));
                Map<Long,LocalDateTime> copyMap = Maps.newHashMap();
                if(CollUtil.isNotEmpty(copyList)){
                    for (QtCheckTaskCopy qtCheckTaskCopy : copyList) {
                        copyMap.put(qtCheckTaskCopy.getTaskId(),qtCheckTaskCopy.getSamplingTime());
                    }
                }
                resList.forEach(ele -> ele.setSamplingTime(copyMap.get(ele.getId())));
                //来料检需要过滤取样日期
                if(Objects.nonNull(dto.getSamplingTime())){
                    resList = resList.stream().filter(v -> Objects.nonNull(v.getSamplingTime()) && dto.getSamplingTime().equals(v.getSamplingTime().toLocalDate())).collect(Collectors.toList());
                }

            }
        }
        return resList;
    }

    @Override
    public List<QtCheckTask> conditionTaskList(QtCheckTaskConditionDTO dto) {
        //根据已知条件查询任务表
        String join = null;
        if(CollUtil.isNotEmpty(dto.getProductLineIdList())){
            join = String.join(",", dto.getProductLineIdList().stream().map(Object::toString).collect(Collectors.toList()));
        }

        LambdaQueryWrapper<QtCheckTask> taskCondition = new LambdaQueryWrapper<QtCheckTask>()
                //任务完成
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                //完成时间范围
                .ge(Objects.nonNull(dto.getStartTime()),QtCheckTask::getDefectStartTime, dto.getStartTime())
                .le(Objects.nonNull(dto.getEndTime()), QtCheckTask::getDefectEndTime, dto.getEndTime())
                //班次
                .eq(Objects.nonNull(dto.getWorkOrderId()), QtCheckTask::getClassesId, dto.getWorkOrderId())
                //检验类型
                .eq(Objects.nonNull(dto.getTaskType()), QtCheckTask::getTaskType, dto.getTaskType())
                //生产批次
                .in(CollUtil.isNotEmpty(dto.getProductBatchList()), QtCheckTask::getProductBatch, dto.getProductBatchList())
                //车间
                .apply(Objects.nonNull(dto.getProductWorkShopId()), "JSON_CONTAINS(product_workshop, JSON_OBJECT('id', {0}))", dto.getProductWorkShopId())
                //产线
                .apply(Objects.nonNull(dto.getProductLineId()), "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())
                .apply(CollUtil.isNotEmpty(dto.getProductLineIdList()), "JSON_CONTAINS( JSON_ARRAY(" + join + ") ,product_line->'$.id')", dto.getProductLineIdList())
                .orderByAsc(QtCheckTask::getFinishedTime);
        return list(taskCondition);
    }

    /**
     * 配料抽检合格率的明细分页
     *
     * @param dto
     * @return
     */
    @Override
    public Page<QtAccessoryAcceptabilityDetailVO> getQtAccessoryAcceptabilityDetailVO(QtAccessoryAcceptabilityDetailDTO dto) {
        //查询时间范围为当天的8点到晚上八点
        LocalDateTime start = LocalDateTime.of(dto.getDateTime(), LocalTime.of(8, 0, 0));
        LocalDateTime end = LocalDateTime.of(dto.getDateTime(), LocalTime.of(20, 0, 0));

        //3.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());

        if (Objects.nonNull(dto.getCompare()) && CollectionUtils.isEmpty(dto.getWorkshopNameList())) {
            return page;
        }
        //4.查询分页数据
        IPage<QtCheckTask> resPage = page(page, new LambdaQueryWrapper<QtCheckTask>()
                .ge(QtCheckTask::getFinishedTime, start)
                .le(QtCheckTask::getFinishedTime, end)
                .eq(QtCheckTask::getTaskType,QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode()));

        //5.拷贝page信息
        BeanUtils.copyProperties(resPage, page);
        if (CollectionUtils.isEmpty(resPage.getRecords())) {
            return page;
        }
        //6.拷贝返回的数据
        List<QtCheckTask> qtCheckTasks = BeanUtil.copyToList(resPage.getRecords(), QtCheckTask.class);
        if (Objects.nonNull(dto.getCompare()) && CollectionUtils.isNotEmpty(dto.getWorkshopNameList())) {
            qtCheckTasks = qtCheckTasks.stream().filter(qtCheckTask -> dto.getWorkshopNameList().contains(qtCheckTask.getMaterialWorkshop().getName())).collect(Collectors.toList());
        }
        Set<Long> taskIds = qtCheckTasks.stream().map(QtCheckTask::getId).collect(Collectors.toSet());
        Map<Long,List<QtIQCAcceptabilityCopyVO>> taskInfoMap = qtCheckTaskDetailService.listByTaskId(taskIds);
        List<QtIQCAcceptabilityCopyVO> orCopyList =Lists.newArrayList();

        //创建返回数据的类
        List<QtAccessoryAcceptabilityDetailVO> vos = Lists.newArrayList();
        qtCheckTasks.forEach(q -> {
            QtAccessoryAcceptabilityDetailVO vo = new QtAccessoryAcceptabilityDetailVO();

            BigDecimal qCheckBig = BigDecimal.valueOf(Objects.isNull(q.getCheckNum())?0:q.getCheckNum());
            BigDecimal qGoodBig = BigDecimal.valueOf(Objects.isNull(q.getGoodNum())?0:q.getGoodNum());
            BigDecimal rate = BigDecimalUtil.divide(qGoodBig, qCheckBig,4);
            BeanUtils.copyProperties(q, vo);
//            List<QtCheckTaskCopy> qtCheckTaskCopies = qtCheckTaskCopyService.list(new LambdaQueryWrapper<QtCheckTaskCopy>()
//                    .eq(QtCheckTaskCopy::getTaskId, q.getId()));
//            List<QtAccessoryAcceptablityCopyVO> qtAccessoryAcceptablityCopyVOS = Lists.newArrayList();
//            qtCheckTaskCopies.forEach(qtCheckTaskCopy -> {
//                QtAccessoryAcceptablityCopyVO copyVO = new QtAccessoryAcceptablityCopyVO();
//                BeanUtils.copyProperties(qtCheckTaskCopy, copyVO);
//                qtAccessoryAcceptablityCopyVOS.add(copyVO);
//            });
            List<QtIQCAcceptabilityCopyVO> orDefault = taskInfoMap.getOrDefault(q.getId(), orCopyList);
            vo.setAccessoryDetail(orDefault);
            vo.setAcceptability(rate);
            vo.setFinishedTime(LocalDateUtil.format(q.getFinishedTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
            vos.add(vo);
        });
        page.setTotal(vos.size());
        return page.setRecords(vos);
    }

    /**
     * 配料抽检合格率柱状图
     *
     * @param dto
     * @return
     */
    @Override
    public QtChartBarVO getAccessoryChartDefectVO(QtAccessoryAcceptabilityDTO dto) {
        //查询时间范围为当天的8点到晚上八点
        LocalDateTime start = LocalDateTime.of(dto.getDateTime(), LocalTime.of(8, 0, 0));
        LocalDateTime end = LocalDateTime.of(dto.getDateTime(), LocalTime.of(20, 0, 0));

        //根据条件查询
        LambdaQueryWrapper<QtCheckTask> wrapper = accessoryAcceptabilityQtCheckTasks(dto, start, end);
        List<QtCheckTask> qtCheckTasks = list(wrapper);
        //创建返回数据对象
        QtChartBarVO qtChartBarVO = new QtChartBarVO();

        //遍历筛选出来的检验任务
        List<QtCheckTask> subOneCheckTasks = qtCheckTasks.stream().filter(s -> s.getProductWorkshopName().equals(QtManagerConstant.SUB_OFFICE_NAME_ONE))
                .collect(Collectors.toList());
        List<QtCheckTask> subTwoCheckTasks = qtCheckTasks.stream().filter(s -> s.getProductWorkshopName().equals(QtManagerConstant.SUB_OFFICE_NAME_TWO))
                .collect(Collectors.toList());

        //把集合容量大的作为x坐标
        List<String> count = Lists.newArrayList();
        List<QtChartBarSeriesVO> qtChartBarSeriesVOS = Lists.newArrayList();
        if (subOneCheckTasks.size() >= subTwoCheckTasks.size()) {
            for (int i = 1; i <= subOneCheckTasks.size(); i++) {
                List<BigDecimal> resutOne = Lists.newArrayList();
                QtChartBarSeriesVO qtOneChartBarSeriesVO = new QtChartBarSeriesVO();
                QtChartBarSeriesVO qtTwoChartBarSeriesVO = new QtChartBarSeriesVO();
                qtOneChartBarSeriesVO.setName(QtManagerConstant.SUB_OFFICE_NAME_ONE);
                QtCheckTask qtCheckTaskOne = subOneCheckTasks.get(i - 1);
                BigDecimal bigDecimalCheck1 = new BigDecimal(qtCheckTaskOne.getGoodNum());
                BigDecimal bigDecimalCheck2 = new BigDecimal(qtCheckTaskOne.getCheckNum());
                resutOne.add(BigDecimalUtil.divide(bigDecimalCheck1, bigDecimalCheck2, BigDecimalUtil.SCALE_2));
                qtOneChartBarSeriesVO.setData(resutOne);

                List<BigDecimal> resutTwo = Lists.newArrayList();
                if (i <= subTwoCheckTasks.size()) {
                    QtCheckTask qtCheckTaskTwo = subTwoCheckTasks.get(i - 1);
                    BigDecimal bigDecimal3 = new BigDecimal(qtCheckTaskTwo.getGoodNum());
                    BigDecimal bigDecimal4 = new BigDecimal(qtCheckTaskTwo.getCheckNum());
                    resutTwo.add(BigDecimalUtil.divide(bigDecimal3, bigDecimal4, BigDecimalUtil.SCALE_2));

                    qtTwoChartBarSeriesVO.setName(QtManagerConstant.SUB_OFFICE_NAME_TWO);
                    qtTwoChartBarSeriesVO.setData(resutTwo);
                }
                Collections.addAll(qtChartBarSeriesVOS, qtOneChartBarSeriesVO, qtTwoChartBarSeriesVO);
                count.add(i + "");
            }
        } else {
            for (int i = 1; i <= subTwoCheckTasks.size(); i++) {
                List<BigDecimal> resutOne = Lists.newArrayList();
                QtChartBarSeriesVO qtOneChartBarSeriesVO = new QtChartBarSeriesVO();
                QtChartBarSeriesVO qtTwoChartBarSeriesVO = new QtChartBarSeriesVO();
                if (i <= subOneCheckTasks.size()) {
                    qtOneChartBarSeriesVO.setName(QtManagerConstant.SUB_OFFICE_NAME_ONE);
                    QtCheckTask qtCheckTaskOne = subOneCheckTasks.get(i - 1);
                    BigDecimal bigDecimalCheck1 = new BigDecimal(qtCheckTaskOne.getGoodNum());
                    BigDecimal bigDecimalCheck2 = new BigDecimal(qtCheckTaskOne.getCheckNum());
                    resutOne.add(BigDecimalUtil.divide(bigDecimalCheck1, bigDecimalCheck2, BigDecimalUtil.SCALE_2));
                    qtOneChartBarSeriesVO.setData(resutOne);
                }
                List<BigDecimal> resutTwo = Lists.newArrayList();
                QtCheckTask qtCheckTaskTwo = subTwoCheckTasks.get(i - 1);
                BigDecimal bigDecimal3 = new BigDecimal(qtCheckTaskTwo.getGoodNum());
                BigDecimal bigDecimal4 = new BigDecimal(qtCheckTaskTwo.getCheckNum());
                resutTwo.add(BigDecimalUtil.divide(bigDecimal3, bigDecimal4, BigDecimalUtil.SCALE_2));
                qtTwoChartBarSeriesVO.setName(QtManagerConstant.SUB_OFFICE_NAME_TWO);
                qtTwoChartBarSeriesVO.setData(resutTwo);

                Collections.addAll(qtChartBarSeriesVOS, qtOneChartBarSeriesVO, qtTwoChartBarSeriesVO);
                count.add(i + "");
            }
        }
        qtChartBarVO.setXAis(count);
        qtChartBarVO.setSeries(qtChartBarSeriesVOS);
        return qtChartBarVO;
    }

    private LambdaQueryWrapper<QtCheckTask> accessoryAcceptabilityQtCheckTasks(QtAccessoryAcceptabilityDTO dto, LocalDateTime start, LocalDateTime end) {
        List<String> type = Lists.newArrayList();
        type.add(QtManagerConstant.OFFICE_WORKSHOP_TYPE);
        SysOfficeTypeKeyDTO sysOfficeTypeKeyDTO = new SysOfficeTypeKeyDTO();
        sysOfficeTypeKeyDTO.setKey(QtManagerConstant.SUB_OFFICE_NAME);
        sysOfficeTypeKeyDTO.setType(type);
        List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(()->remoteOfficeFeign.getListByTypeAndKey(sysOfficeTypeKeyDTO),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<String> officeNames = sysOfficeVOS.stream().map(SysOfficeVO::getOfficeName).collect(Collectors.toList());


        //根据条件查询
        LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>().select("any_value(code) code , any_value(result) result , " +
                        "any_value(finished_time) finished_time , any_value(product_workshop) product_workshop , " +
                        "any_value(material_workshop) material_workshop").lambda()
                .in(QtCheckTask::getMaterialWorkshopName, officeNames)
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode())
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .ge(QtCheckTask::getFinishedTime, start)
                .le(QtCheckTask::getFinishedTime, end);
        //如果选了物料车间id名称就加上物料车间条件
        if (Objects.nonNull(dto.getMaterialWorkshopId())) {
            condition.apply("JSON_CONTAINS(material_workshop, JSON_OBJECT('id', {0}))", dto.getMaterialWorkshopId());
        }
        condition.groupBy(QtCheckTask::getMaterialWorkshop)
                .orderByAsc(QtCheckTask::getFinishedTime);
        //获取符合条件的任务集合
        return condition;
    }


    /**
     * 基片化验抽检合格率趋势明细分页
     *
     * @param dto
     * @return
     */
    @Override
    public Page<QtCheckTaskReportVO> getQtSubLaboratoryAcceptabilityDetailVO(QtSubOrDeepAcceptabilityDTO dto) {
        List<QtCheckTaskReportVO> flatDataList  = rateDataList("基片", dto);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无报表");
        //筛选车间
        if(dto.getProductLineId()!=null){
            flatDataList = flatDataList.stream().filter(s->s.getProductWorkshopId().equals(dto.getProductLineId())).collect(Collectors.toList());
        }
        //筛选filters
        if(dto.getFilters()!=null){
            flatDataList = flatDataList.stream().filter(s->dto.getFilters().contains(s.getProductWorkshop().getName())).collect(Collectors.toList());
        }
        //计算每条任务的合格率
        flatDataList.forEach(tmp->{
            BigDecimal result = BigDecimalUtil.divide(new BigDecimal(tmp.getGoodNum()),new BigDecimal(tmp.getCheckNum()));
            result = BigDecimalUtil.multiply(result,new BigDecimal(100));
            tmp.setPassRate(new DecimalFormat("0.00").format(result));
        });

        //根据检验完成时间升序排列
        flatDataList = flatDataList.stream().sorted(Comparator.comparing(QtCheckTaskReportVO::getFinishedTime)).collect(Collectors.toList());

        Set<Long> taskIds = flatDataList.stream().map(QtCheckTaskReportVO::getId).collect(Collectors.toSet());
        Map<Long,List<QtIQCAcceptabilityCopyVO>> taskInfoMap = qtCheckTaskDetailService.listByTaskId(taskIds);
        Page<QtCheckTaskReportVO> page = LogicalPageUtil.page(flatDataList, dto.getCurrent(), dto.getSize());
        page.getRecords().forEach(flatData ->{
            flatData.setList(taskInfoMap.get(flatData.getId()));
        });
        return page;
    }

    @Override
    public Page<QtIQCAcceptabilityDetailVO> getQtIQCAcceptabilityDetailVO(QtSubOrDeepAcceptabilityDTO dto) {
        //1.处理时间数据
        if (Objects.nonNull(dto.getDateStart())) {
            dto.setDateTimeStart(LocalDateTime.of(dto.getDateStart(), LocalTime.of(8, 0, 0)));
        }
        if (Objects.nonNull(dto.getDateEnd())) {
            dto.setDateTimeEnd(LocalDateTime.of(dto.getDateEnd().plusDays(1), LocalTime.of(8, 0, 0)));
        }
        if (Objects.nonNull(dto.getSamplingTime())) {
            dto.setSamplingStartTime(LocalDateTime.of(dto.getSamplingTime(), LocalTime.MIN));
            dto.setSamplingEndTime(LocalDateTime.of(dto.getSamplingTime(), LocalTime.MAX));
        }
        //2.自定义sql查询数据
        Page<QtIQCAcceptabilityDetailVO> resPage = this.baseMapper.pageList(new Page<>(dto.getCurrent(), dto.getSize()), dto);
        //3.处理数据
        Set<Long> taskIds = Sets.newHashSet();
        Set<Long> materialIds = Sets.newHashSet();
        List<QtIQCAcceptabilityDetailVO> resList = resPage.getRecords();

        //根据taskId获取最小取样时间
        Set<Long> taskIdSet = resList.stream().map(QtIQCAcceptabilityDetailVO::getId).collect(Collectors.toSet());
        Map<Long, LocalDateTime> minSamplingTimeMap = qtCheckTaskCopyService.minSamplingTimeMap(taskIdSet);

        resList.forEach(task -> {
                    //处理时间
                    LocalDateTime samplingTime = minSamplingTimeMap.get(task.getId());
                    if (Objects.nonNull(samplingTime)) {
                        task.setSamplingTime(samplingTime.toLocalDate());
                    }
                    taskIds.add(task.getId());
                    materialIds.add(task.getMaterialId());
                    //计算合格
                    BigDecimal checkNum = new BigDecimal(task.getCheckNum());
                    BigDecimal goodNum = new BigDecimal(task.getGoodNum());
                    task.setAcceptability(BigDecimalUtil.divide(goodNum, checkNum, BigDecimalUtil.SCALE_2));
                }
        );
        List<QtIQCAcceptabilityDetailVO> voList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resList)) {
            List<QtIQCAcceptabilityDetailVO> filterList = resList;
            //根据对比类型过滤数据
            if (Objects.nonNull(dto.getCompare())) {
                filterList = resList.stream().filter(predicate(dto.getCompare(), dto.getCompareContent(), resList)).collect(Collectors.toList());
            }
            //获取物料数据
            Map<Long, String> materialMap = FeignOptional.of(()->remoteSpareFeignApi.listByIdCh(materialIds),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取详情数据
            Map<Long,List<QtIQCAcceptabilityCopyVO>> taskInfoMap = qtCheckTaskDetailService.listByTaskId(taskIds);
            List<QtIQCAcceptabilityCopyVO> orCopyList =Lists.newArrayList();
            //处理详情数据
            filterList.forEach(task -> {
                List<QtIQCAcceptabilityCopyVO> orDefault = taskInfoMap.getOrDefault(task.getId(), orCopyList);
                if(CollUtil.isNotEmpty(orDefault)){
                    QtIQCAcceptabilityCopyVO qtIQCAcceptabilityCopyVO = orDefault.get(0);
                    List<String> headList = qtIQCAcceptabilityCopyVO.getList().stream().map(v -> v.getName()).collect(Collectors.toList());
                    task.setDetailTableHead(headList);

                    orDefault.forEach(ele ->{
                        List<String> eleList = Lists.newArrayList();
                        ele.getList().forEach(e ->{
                            eleList.add(e.getValue());
                            e.setSamplingTime(minSamplingTimeMap.get(e.getTaskId()));
                        });
                        ele.setEleList(eleList);
                        ele.setSamplingTime(minSamplingTimeMap.get(ele.getTaskId()));
                    });
                }
                task.setDetail(orDefault);
                task.setMaterialName(materialMap.get(task.getMaterialId()));
            });
            //根据规则排序
            voList = sortList(filterList);
        }
        return resPage.setRecords(voList);
    }

    @Override
    public Page<QtIQCAcceptabilityDetailVO> getQtIQCAcceptabilityDetail(QtSubOrDeepAcceptabilityDTO dto) {
        //1.处理时间数据
        if (Objects.nonNull(dto.getDateStart())) {
            dto.setDateTimeStart(LocalDateTime.of(dto.getDateStart(), LocalTime.of(8, 0, 0)));
        }
        if (Objects.nonNull(dto.getDateEnd())) {
            dto.setDateTimeEnd(LocalDateTime.of(dto.getDateEnd().plusDays(1), LocalTime.of(8, 0, 0)));
        }
        if (Objects.nonNull(dto.getSamplingTime())) {
            dto.setSamplingStartTime(LocalDateTime.of(dto.getSamplingTime(), LocalTime.MIN));
            dto.setSamplingEndTime(LocalDateTime.of(dto.getSamplingTime(), LocalTime.MAX));
        }
        //2.自定义sql查询数据
        List<QtIQCAcceptabilityDetailVO> resList = this.baseMapper.list(dto);
        //3.处理数据
        Set<Long> taskIds = Sets.newHashSet();
        Set<Long> materialIds = Sets.newHashSet();
        resList.forEach(task -> {
                    //处理时间
//            task.setFinishedTime(LocalDate.from(task.getFinishedTime()));
//            task.setSamplingTime(LocalDateTime.from(task.get));
                    taskIds.add(task.getId());
                    materialIds.add(task.getMaterialId());
                    //计算合格
                    BigDecimal checkNum = new BigDecimal(task.getCheckNum());
                    BigDecimal goodNum = new BigDecimal(task.getGoodNum());
                    task.setAcceptability(BigDecimalUtil.divide(goodNum, checkNum, BigDecimalUtil.SCALE_2));
                }
        );
        List<QtIQCAcceptabilityDetailVO> voList = Lists.newArrayList();
        if (CollUtil.isNotEmpty(resList)) {
            List<QtIQCAcceptabilityDetailVO> filterList = resList;
            //根据对比类型过滤数据
            if (Objects.nonNull(dto.getCompare())) {
                filterList = resList.stream().filter(predicate(dto.getCompare(), dto.getCompareContent(), resList)).collect(Collectors.toList());
            }
            //获取物料数据
            Map<Long, String> materialMap = FeignOptional.of(()->remoteSpareFeignApi.listByIdCh(materialIds),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            //获取详情数据
            Map<Long,List<QtIQCAcceptabilityCopyVO>> taskInfoMap = qtCheckTaskDetailService.listByTaskId(taskIds);
            List<QtIQCAcceptabilityCopyVO> orCopyList =Lists.newArrayList();
            //处理详情数据
            filterList.forEach(task -> {
                List<QtIQCAcceptabilityCopyVO> orDefault = taskInfoMap.getOrDefault(task.getId(), orCopyList);
                task.setDetail(orDefault);
                task.setMaterialName(materialMap.get(task.getMaterialId()));
            });
            //根据规则排序
            voList = sortList(filterList);
        }
        return new Page<QtIQCAcceptabilityDetailVO>().setRecords(voList);
    }

//    /**
//     * 来料检合格率明细分页
//     *
//     * @param dto
//     * @return
//     */
//    @Override
//    public Page<QtIQCAcceptabilityDetailVO> getQtIQCAcceptabilityDetailVO(QtIQCAcceptabilityDTO dto) {
//        MPJLambdaWrapper<QtCheckTask> condition = getQtIQCCondition(dto);
//        //3.定义分页对象
//        Page page = new Page<>(dto.getCurrent(), dto.getSize());
//        //4.查询分页数据
//        IPage<QtCheckTask> resPage = page(page, condition);
//        //5.拷贝page信息
//        BeanUtils.copyProperties(resPage, page);
//        //6.拷贝返回的数据
//        List<QtIQCAcceptabilityDetailVO> qtIQCAcceptabilityDetailVOS = Lists.newArrayList();
//        List<QtCheckTask> qtCheckTasks = BeanUtil.copyToList(resPage.getRecords(), QtCheckTask.class);
//
//        qtCheckTasks.forEach(q -> {
//
//            QtIQCAcceptabilityDetailVO qtIQCAcceptabilityDetailVO = new QtIQCAcceptabilityDetailVO();
//            List<QtIQCAcceptabilityCopyVO> qtIQCAcceptabilityCopyVOS = Lists.newArrayList();
//            List<QtCheckTaskCopy> checkTaskCopies = qtCheckTaskCopyService.list(new LambdaQueryWrapper<QtCheckTaskCopy>().eq(QtCheckTaskCopy::getTaskId, q.getId()));
//            checkTaskCopies.forEach(checkTaskCopy -> {
//                QtIQCAcceptabilityCopyVO qtIQCAcceptabilityCopyVO = new QtIQCAcceptabilityCopyVO();
//                BeanUtils.copyProperties(checkTaskCopy, qtIQCAcceptabilityCopyVO);
//                qtIQCAcceptabilityCopyVOS.add(qtIQCAcceptabilityCopyVO);
//            });
//            qtIQCAcceptabilityDetailVO.setDetail(qtIQCAcceptabilityCopyVOS);
//            BeanUtils.copyProperties(q, qtIQCAcceptabilityDetailVO);
//            BigDecimal checkNum = new BigDecimal(q.getCheckNum());
//            BigDecimal goodNum = new BigDecimal(q.getGoodNum());
//            BigDecimal result = BigDecimalUtil.divide(goodNum, checkNum, BigDecimalUtil.SCALE_2);
//            qtIQCAcceptabilityDetailVO.setAcceptability(result);
//            qtIQCAcceptabilityDetailVOS.add(qtIQCAcceptabilityDetailVO);
//        });
//        page.setRecords(qtIQCAcceptabilityDetailVOS);
//        page.setTotal(qtIQCAcceptabilityDetailVOS.size());
//        return page;
//    }

//        MPJLambdaWrapper<QtCheckTask> condition = getQtIQCCondition(dto);
//        //3.定义分页对象
//        Page page = new Page<>(dto.getCurrent(), dto.getSize());
//        //4.查询分页数据
//        IPage<QtCheckTask> resPage = page(page, condition);
//        //5.拷贝page信息
//        BeanUtils.copyProperties(resPage, page);
//        //6.拷贝返回的数据
//        List<QtIQCAcceptabilityDetailVO> qtIQCAcceptabilityDetailVOS = Lists.newArrayList();
//        List<QtCheckTask> qtCheckTasks = BeanUtil.copyToList(resPage.getRecords(), QtCheckTask.class);
//        qtCheckTasks.forEach(q -> {
//            QtIQCAcceptabilityDetailVO qtIQCAcceptabilityDetailVO = new QtIQCAcceptabilityDetailVO();
//            List<QtIQCAcceptabilityCopyVO> qtIQCAcceptabilityCopyVOS = Lists.newArrayList();
//            List<QtCheckTaskCopy> checkTaskCopies = qtCheckTaskCopyService.list(new LambdaQueryWrapper<QtCheckTaskCopy>().eq(QtCheckTaskCopy::getTaskId, q.getId()));
//            checkTaskCopies.forEach(checkTaskCopy -> {
//                QtIQCAcceptabilityCopyVO qtIQCAcceptabilityCopyVO = new QtIQCAcceptabilityCopyVO();
//                BeanUtils.copyProperties(checkTaskCopy, qtIQCAcceptabilityCopyVO);
//                qtIQCAcceptabilityCopyVOS.add(qtIQCAcceptabilityCopyVO);
//            });
//            qtIQCAcceptabilityDetailVO.setDetail(qtIQCAcceptabilityCopyVOS);
//            BeanUtils.copyProperties(q, qtIQCAcceptabilityDetailVO);
//            BigDecimal checkNum = new BigDecimal(q.getCheckNum());
//            BigDecimal goodNum = new BigDecimal(q.getGoodNum());
//            BigDecimal result = BigDecimalUtil.divide(goodNum, checkNum, BigDecimalUtil.SCALE_2);
//            qtIQCAcceptabilityDetailVO.setAcceptability(result);
//            qtIQCAcceptabilityDetailVOS.add(qtIQCAcceptabilityDetailVO);
//        });
//        page.setRecords(qtIQCAcceptabilityDetailVOS);
//        return page;
//    }

//    private MPJLambdaWrapper<QtCheckTask> getQtIQCCondition(QtSubOrDeepAcceptabilityDTO dto) {
//        //1、配置分页信息
//        MPJLambdaWrapper<QtCheckTask> condition = new MPJLambdaWrapper<QtCheckTask>()
//                .selectAll(QtCheckTask.class)
//                //关联表映射
//                .selectAs(QtMaterial::getName, QtCheckTaskListVO::getMaterialName)
//                .selectAs(QtCheckTaskCopy::getTaskId, QtCheckTaskListVO::getId)
//                //json映射
//                .selectAs(QtCheckTask::getMaterialWorkshopName, QtCheckTaskListVO::getMaterialWorkshopName)
//                .selectAs(QtCheckTask::getProductWorkshopName, QtCheckTaskListVO::getProductWorkshopName)
//                .selectAs(QtCheckTask::getProductLineName, QtCheckTaskListVO::getProductLineName)
//                .selectAs(QtCheckTask::getSupplierName, QtCheckTaskListVO::getSupplierName)
//                .selectAs(QtCheckTask::getCustomerName, QtCheckTaskListVO::getCustomerName)
//                //关联
//                .leftJoin(QtCheckTaskCopy.class, QtCheckTaskCopy::getTaskId, QtCheckTask::getId)
//                .leftJoin(QtMaterial.class, QtMaterial::getId, QtCheckTask::getMaterialId)
//                //查询
//                .and(Objects.nonNull(dto.getSamplingTime()), wrapper -> wrapper.eq(QtCheckTaskCopy::getSamplingTime, dto.getSamplingTime()))
//                .and(Objects.nonNull(dto.getMaterialId()), wrapper -> wrapper.eq(QtCheckTask::getMaterialId, dto.getMaterialId()))
//                .and(Objects.nonNull(dto.getMaterialWorkshopId()), wrapper -> wrapper.eq(QtCheckTask::getMaterialWorkshopId, dto.getMaterialWorkshopId()))
//                .and(Objects.nonNull(dto.getSupplierId()), wrapper -> wrapper.eq(QtCheckTask::getSupplierId, dto.getSupplierId()));
//
//        if (QtManagerConstant.QtChartCompareEnum.MATERIALWORKSHOP.getCode().equals(dto.getCompare())){
//            condition.groupBy(QtCheckTask::getMaterialId);
//        }else
//        if (QtManagerConstant.QtChartCompareEnum.SAMPLINGTIME.getCode().equals(dto.getCompare())){
//            condition.groupBy(QtCheckTaskCopy::getSamplingTime);
//        }
//        condition.groupBy(QtCheckTask::getSupplierId)
//                .gt(QtCheckTask::getFinishedTime,LocalDateTime.of(dto.getDateTime(),LocalTime.of(8,0,0)))
//                .le(QtCheckTask::getFinishedTime,LocalDateTime.of(dto.getDateTime().plusDays(1),LocalTime.of(8,0,0)));
//       //2.解析排序规则
//        if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_NAME.equals(dto.getSortField())) {
//            if (dto.getAscFlag()) {
//                condition.last("ORDER BY CONVERT(name using gbk) ASC");
//            } else {
//                condition.last("ORDER BY CONVERT(name using gbk) DESC");
//            }
//        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_FINISHED_TIME.equals(dto.getSortField())) {
//            condition.orderBy(true, !dto.getAscFlag(), QtCheckTask::getFinishedTime);
//        } else if (QtManagerConstant.QT_CHECK_TASK_COPY__SORT_FIELD.equals(dto.getSortField())) {
//            condition.orderBy(true, dto.getAscFlag(), QtCheckTaskCopy::getSamplingTime);
//        } else if (QtManagerConstant.QT_CHECK_TASK_SORT_FIELD_MATERIAL_WORKSHOP_NAME.equals(dto.getSortField())) {
//            condition.orderBy(true, dto.getAscFlag(), QtCheckTask::getMaterialWorkshopName);
//        } else {
//            //默认按创建时间降序排列
//            condition.orderBy(true, false, QtCheckTask::getSupplierName);
//        }
//        return condition;
//    }

    /**
     * 来料检合格率柱状图表
     *
     * @param dto
     * @return
     */
    @Override
    public QtLineChartBiBarDTO accessoryChart(QtSubOrDeepAcceptabilityDTO dto) {
        List<QtCheckTaskReportVO> flatDataList  = rateDataList(null, dto);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无报表");

        //定义数据集合
        List<BiStackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();

        flatDataList = flatDataList.stream().sorted(Comparator.comparing(QtCheckTaskReportVO::getFinishedTime)).collect(Collectors.toList());
        LinkedHashMap<LocalDateTime, List<QtCheckTaskReportVO>> linkedMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getFinishedTime, LinkedHashMap::new, Collectors.toList()));

        //考虑对比
        Map<Integer, List<QtCheckTaskReportVO>> contrastDataMap = Maps.newHashMap();
        Map<Integer, String> idNameMap = Maps.newHashMap();
        if(Objects.nonNull(dto.getCompare())){
             TupleDTO<Map<Integer, List<QtCheckTaskReportVO>>, Map<Integer, String>> mapMapTupleDTO = commonGroup(dto.getCompare(), flatDataList);
             //获取对比数据集和对比维度
             contrastDataMap = mapMapTupleDTO.getField1();
             idNameMap = mapMapTupleDTO.getField2();
             if(CollUtil.isEmpty(idNameMap)) return null;
        }
        //次数
        int idx = 1;
        for (Map.Entry<LocalDateTime, List<QtCheckTaskReportVO>> entry : linkedMap.entrySet()) {
            //获取时间范围的集合
            List<QtCheckTaskReportVO> conditionDataList = flatDataList.stream().filter(v -> {
                //数据范围应该是大于等于开始时间  小于结束时间
                if (entry.getKey().isEqual(v.getFinishedTime())) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }).collect(Collectors.toList());

            BiStackGraphDTO<String, BigDecimal> biStackGraphDTO = new BiStackGraphDTO<>();
            biStackGraphDTO.setXAxis("第" + idx + "抽检");
            biStackGraphDTO.setData(Lists.newArrayList());

            //默认集合
            List<QtCheckTaskReportVO> defaultQtCheckTaskList = Lists.newArrayList();
            //对比的结果
            if(Objects.nonNull(dto.getCompare())){
                for (Map.Entry<Integer, String> entry1 : idNameMap.entrySet()) {
                    //获取对比数据
                    List<QtCheckTaskReportVO> contrastData = contrastDataMap.getOrDefault(entry1.getKey(), defaultQtCheckTaskList);
                    Set<Long> conditionIdList = conditionDataList.stream().map(QtCheckTaskReportVO::getId).collect(Collectors.toSet());
                    contrastData = contrastData.stream().filter(v -> conditionIdList.contains(v.getId())).collect(Collectors.toList());
                    TupleDTO<String, BigDecimal> tupleDTO = getTupleDTO(contrastData, entry1.getValue());
                    biStackGraphDTO.getData().add(Lists.newArrayList(tupleDTO));
                }
            }else{
                TupleDTO<String, BigDecimal> tupleDTO = getTupleDTO(entry.getValue(), StackGraphUtil.STR_DEFAULT);
                biStackGraphDTO.getData().add(Lists.newArrayList(tupleDTO));
            }
            stackGraphDTOList.add(biStackGraphDTO);
            idx++;
        }
        //自定义转换工具
        QtLineChartBiBarDTO qtLineChartBiBarDTO = StackGraphUtil.biConvertData(stackGraphDTOList, Boolean.FALSE);
        return qtLineChartBiBarDTO;
    }

    @Override
    public QtLineChartBiBarDTO getQtIQCAcceptabilityChart(QtSubOrDeepAcceptabilityDTO dto) {
        List<QtCheckTaskReportVO> flatDataList = rateDataList(null, dto);
        SteccnTableAssertUtil.notEmpty(flatDataList, "数据为空，暂无报表");

        //定义数据集合
        List<BiStackGraphDTO<String, BigDecimal>> stackGraphDTOList = Lists.newArrayList();

        //获取物料id
        Set<Long> materialIdSet = flatDataList.stream().map(QtCheckTaskReportVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, String> materialIdNameMap = FeignOptional.of(()->remoteSpareFeignApi.listByIdCh(materialIdSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        flatDataList = flatDataList.stream().sorted(Comparator.comparing(QtCheckTaskReportVO::getFinishedTime).reversed()).collect(Collectors.toList());
        LinkedHashMap<Long, List<QtCheckTaskReportVO>> linkedMap = flatDataList.stream().collect(Collectors.groupingBy(QtCheckTaskReportVO::getMaterialId, LinkedHashMap::new, Collectors.toList()));

        //考虑对比
        Map<String, List<QtCheckTaskReportVO>> contrastDataMap = Maps.newHashMap();
        Map<String, String> idNameMap = Maps.newHashMap();
        if(Objects.nonNull(dto.getCompare())){
            TupleDTO<Map<String, List<QtCheckTaskReportVO>>, Map<String, String>> mapMapTupleDTO = rateGroup(dto.getCompare(), flatDataList);
            //获取对比数据集和对比维度
            contrastDataMap = mapMapTupleDTO.getField1();
            idNameMap = mapMapTupleDTO.getField2();
            if(CollUtil.isEmpty(idNameMap)) return null;
        }
        //根据物料id去分组
        for (Map.Entry<Long, List<QtCheckTaskReportVO>> entry : linkedMap.entrySet()) {
            BiStackGraphDTO<String, BigDecimal> biStackGraphDTO = new BiStackGraphDTO<>();
            biStackGraphDTO.setXAxis(materialIdNameMap.getOrDefault(entry.getKey(), Strings.EMPTY));
            biStackGraphDTO.setData(Lists.newArrayList());
            //默认集合
            List<QtCheckTaskReportVO> defaultQtCheckTaskList = Lists.newArrayList();
            //对比的结果
            if(Objects.nonNull(dto.getCompare())){
                for (Map.Entry<String, String> entry1 : idNameMap.entrySet()) {
                    //获取对比数据
                    List<QtCheckTaskReportVO> contrastData = contrastDataMap.getOrDefault(entry1.getKey(), defaultQtCheckTaskList);
                    Set<Long> conditionIdList = entry.getValue().stream().map(QtCheckTaskReportVO::getId).collect(Collectors.toSet());
                    contrastData = contrastData.stream().filter(v -> conditionIdList.contains(v.getId())).collect(Collectors.toList());
                    TupleDTO<String, BigDecimal> tupleDTO = getTupleDTO(contrastData, entry1.getValue());
                    biStackGraphDTO.getData().add(Lists.newArrayList(tupleDTO));
                }
            }else{
                TupleDTO<String, BigDecimal> tupleDTO = getTupleDTO(entry.getValue(), StackGraphUtil.STR_DEFAULT);
                biStackGraphDTO.getData().add(Lists.newArrayList(tupleDTO));
            }
            stackGraphDTOList.add(biStackGraphDTO);
        }
        //自定义转换工具
        QtLineChartBiBarDTO qtLineChartBiBarDTO = StackGraphUtil.biConvertData(stackGraphDTOList, Boolean.FALSE);
        BaseScreenVO baseScreenVO = new BaseScreenVO();
        baseScreenVO.setMaterialScreen(materialSelect(flatDataList));
        baseScreenVO.setSampleTimeScreen(sampleTimeSelect(flatDataList));
        qtLineChartBiBarDTO.setOther(baseScreenVO);
        return qtLineChartBiBarDTO;
    }


    /**
     * 获取物料所有下拉选项
     * @param allSelectList 所有数据
     * @return
     */
    private List<BaseSelectVO<Long>> materialSelect( List<QtCheckTaskReportVO> allSelectList){
        //获取所有的下拉选项
        Set<Long> materialIdSet = allSelectList.stream().map(QtCheckTaskReportVO::getMaterialId).collect(Collectors.toSet());
        Map<Long, String> materialMap = FeignOptional.of(()->remoteSpareFeignApi.listByIdCh(materialIdSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        List<BaseSelectVO<Long>> materialSelectList = Lists.newArrayList();
        materialMap.entrySet().forEach(entry -> materialSelectList.add(new BaseSelectVO<>(entry.getKey(), entry.getValue())));
        return materialSelectList;
    }

    /**
     * 获取取样时间下拉选项
     * @param allSelectList 所有数据
     * @return
     */
    private List<BaseSelectVO<LocalDate>> sampleTimeSelect( List<QtCheckTaskReportVO> allSelectList){
        //获取所有的下拉选项
        Set<LocalDate> sampleTimeIdSet = allSelectList.stream().filter(v -> Objects.nonNull(v.getSamplingTime())).map(v -> v.getSamplingTime().toLocalDate()).collect(Collectors.toSet());

        List<LocalDate> list = new ArrayList<>(sampleTimeIdSet);
        Collections.sort(list);
        List<BaseSelectVO<LocalDate>> sampleTimeSelectList = Lists.newArrayList();
        list.forEach(e -> sampleTimeSelectList.add(new BaseSelectVO<>(e, LocalDateTimeUtil.format(e, DatePattern.NORM_DATE_PATTERN))));
        return sampleTimeSelectList;
    }


    @SneakyThrows
    @Override
    public void downloadAccessoryAcceptability(AccessoryAcceptabilityDownDTO dto, HttpServletResponse response) {

        LocalDateTime start = LocalDateTime.of(dto.getDate(), LocalTime.of(8, 0, 0));
        LocalDateTime end = LocalDateTime.of(dto.getDate(), LocalTime.of(20, 0, 0));


        Map<String, Object> map = MapUtils.newHashMap();
        if (Objects.isNull(dto.getWorkshopId())) {
            map.put("materialWorkshopName","");
        } else {
            String workshopName = remoteOfficeFeign.getNameById(dto.getWorkshopId()).getData();
            map.put("materialWorkshopName",workshopName);
        }
        if (Objects.isNull(dto.getCompare())) {
            map.put("materialWorkshop","");
        } else {
            map.put("materialWorkshop",QtManagerConstant.QtChartCompareEnum.MATERIALWORKSHOP.getName());
        }
        if (Objects.isNull(dto.getDate())) {
            map.put("date","");
        } else {
            map.put("date",LocalDateUtil.localDateFormat(dto.getDate(),DatePattern.NORM_DATE_PATTERN));
        }
        //查询数据
        List<QtCheckTask> qtCheckTaskList = list(new LambdaQueryWrapper<QtCheckTask>()
                        .ge(QtCheckTask::getFinishedTime, start)
                        .le(QtCheckTask::getFinishedTime, end)
                        .eq(QtCheckTask::getTaskType,QtManagerConstant.QtCheckTaskTypeEnum.MIXED.getCode()));

        if (Objects.nonNull(dto.getCompare()) && CollectionUtils.isNotEmpty(dto.getWorkshopNameList())) {
            //过滤数据
            qtCheckTaskList = qtCheckTaskList.stream().filter(qtCheckTask -> dto.getWorkshopNameList().contains(qtCheckTask.getMaterialWorkshop().getName())).collect(Collectors.toList());
        } else if (Objects.nonNull(dto.getCompare()) && CollectionUtils.isEmpty(dto.getWorkshopNameList())) {
            qtCheckTaskList = Lists.newArrayList();
        }

        //创建返回数据的类
        List<AccessoryAcceptabilityDownVO> acceptabilityDownVOList = Lists.newArrayList();


        qtCheckTaskList.forEach(q -> {
            AccessoryAcceptabilityDownVO acceptabilityDownVO = new AccessoryAcceptabilityDownVO();

            BigDecimal qCheckBig = BigDecimal.valueOf(Objects.isNull(q.getCheckNum())?0:q.getCheckNum());
            BigDecimal qGoodBig = BigDecimal.valueOf(Objects.isNull(q.getGoodNum())?0:q.getGoodNum());
            BigDecimal rate = BigDecimalUtil.divide(qGoodBig, qCheckBig,4);
            BeanUtils.copyProperties(q, acceptabilityDownVO);

            acceptabilityDownVO.setMaterialWorkshopName(q.getMaterialWorkshop().getName());
            acceptabilityDownVO.setAcceptability(new DecimalFormat("0.00%").format(rate));
            acceptabilityDownVO.setFinishedTime(LocalDateUtil.format(q.getFinishedTime(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));
            acceptabilityDownVOList.add(acceptabilityDownVO);
        });
        //下载合格率
        DownloadUtil.downloadTemplate(response,QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_PLCJ,QtManagerConstant.QT_CHART_ACCESSORY_PROCESS_NAME,map,acceptabilityDownVOList,dto.getBase64());

    }

    /**
     * 下载基片化验抽检详情
     * @param dto
     * @param response
     */
    @SneakyThrows
    @Override
    public void downloadQtSubLaboratoryAcceptabilityDetailVO(QtSubOrDeepAcceptabilityDTO dto, HttpServletResponse response) {
        List<QtCheckTaskReportVO> flatDataList  = rateDataList("基片", dto);
        Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        //筛选车间
        if(dto.getProductLineId()!=null){
            flatDataList = flatDataList.stream().filter(s->s.getProductLineId().equals(dto.getProductLineId())).collect(Collectors.toList());
        }
        //筛选filters
        if(dto.getFilters()!=null){
            flatDataList = flatDataList.stream().filter(s->dto.getFilters().contains(s.getProductWorkshop().getName())).collect(Collectors.toList());
        }
        //计算每条任务的合格率
        flatDataList.forEach(tmp->{
            BigDecimal result = BigDecimalUtil.divide(new BigDecimal(tmp.getGoodNum()),new BigDecimal(tmp.getCheckNum()));
            tmp.setPassRate(new DecimalFormat("0.00%").format(result));
            if(Objects.nonNull(tmp.getProductWorkshop().getName())){
                tmp.setProductWorkshopName(tmp.getProductWorkshop().getName());
            }
        });

        Map<String, Object> map = MapUtils.newHashMap();
        map.put("dateStart",dto.getDateStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        map.put("dateEnd",dto.getDateEnd().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        if(dto.getProductWorkShopId()!=null){
            R<String> nameById = remoteOfficeFeign.getNameById(dto.getProductWorkShopId());
            if(nameById!=null){
                map.put("productWorkshopName",nameById.getData());
            }
        }
        if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORK.getCode())){
            map.put("workOrderNameCondition",QtManagerConstant.QtChartCompareEnum.WORK.getName());
        }
        if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getCode())){
            map.put("workOrderNameCondition",QtManagerConstant.QtChartCompareEnum.PRODUCTLINE.getName());
        }
        if(dto.getCompare().equals(QtManagerConstant.QtChartCompareEnum.WORKSHOP.getCode())){
            map.put("workOrderNameCondition",QtManagerConstant.QtChartCompareEnum.WORKSHOP.getName());
        }
        DownloadUtil.downloadTemplate(response,QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_JPHY,QtManagerConstant.QT_CHART_SUB_JPCJ,map,flatDataList,dto.getBase64());
    }

    @SneakyThrows
    @Override
    public void downloadAcceptability(QtSubOrDeepAcceptabilityDTO dto, HttpServletResponse response) {
        Map<String, Object> map = MapUtils.newHashMap();
        Map<Long, String> materialIdNameMap = FeignOptional.of(()->remoteSpareFeignApi.listByIdCh(Collections.singleton(dto.getMaterialId())),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();

        map.put("dateStart",LocalDateUtil.localDateFormat(dto.getDateStart(),DatePattern.NORM_DATE_PATTERN));
        map.put("dateEnd",LocalDateUtil.localDateFormat(dto.getDateEnd(),DatePattern.NORM_DATE_PATTERN));
        map.put("samplingTime","全部");
        map.put("materialName","");
        map.put("materialWorkshopName","全部");
        map.put("compare","");
        if (Objects.nonNull(dto.getCompare())) {
            map.put("compare",QtManagerConstant.QtChartCompareEnum.getNameByCode(dto.getCompare()));
        }
        if (Objects.nonNull(dto.getSamplingTime())) {
            map.put("samplingTime",LocalDateUtil.localDateFormat(dto.getSamplingTime(),DatePattern.NORM_DATE_PATTERN));
        }
        if (Objects.nonNull(dto.getMaterialWorkshopId())) {
            String workshopName = remoteOfficeFeign.getNameById(dto.getProductWorkShopId()).getData();
            map.put("materialWorkshopName",workshopName);
        }
        if (Objects.nonNull(dto.getMaterialId())) {
            map.put("materialName",materialIdNameMap.get(dto.getMaterialId()));
        }

        downloadTemplate(response, map,dto.getBase64(),dto.getDetailBase64(),dto.getLength());
    }

     private void downloadTemplate(HttpServletResponse response, Map<String, Object> fixMap, String base64, String detailBase64, Integer length) throws IOException {
        //1、从服务器获取模板数据
        InputStream modelIn = DownloadModelUtil.getModelInputStream(QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_LLJ);
        //2、设置下载头
         response.setContentType(QtManagerConstant.QT_CHART_EXCEL_CONTENT_TYPE);
         response.setCharacterEncoding(QtManagerConstant.QT_CHART_EXCEL_ENCODE);
         // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
         String fileName = URLEncoder.encode(QtManagerConstant.QT_CHART_ACCEPTABILITY_LLJ, QtManagerConstant.QT_CHART_EXCEL_ENCODE).replaceAll("\\+", "%20");
         response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).autoCloseStream(Boolean.FALSE).withTemplate(modelIn).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet().build();
            // 这里注意 入参用了forceNewRow 代表在写入list的时候不管list下面有没有空行 都会创建一行，然后下面的数据往后移动。默认 是false，会直接使用下一行，如果没有则创建。
            // forceNewRow 如果设置了true,有个缺点 就是他会把所有的数据都放到内存了，所以慎用
            // 简单的说 如果你的模板有list,且list不是最后一行，下面还有数据需要填充 就必须设置 forceNewRow=true 但是这个就会把所有数据放到内存 会很耗内存
            // 如果数据量大 list不是最后一行 参照下一个
            FillConfig fillConfig = FillConfig.builder().forceNewRow(Boolean.FALSE).build();
            excelWriter.fill(fillConfig, writeSheet);
            if(StrUtil.isNotBlank(base64)){
                //BASE64Decoder decoder = new BASE64Decoder();
                Base64.Decoder decoder =  Base64.getDecoder();
                byte[] bytes = decoder.decode(base64.replace("data:image/png;base64,",""));
                //byte[] bytes = decoder.decodeBuffer(base64.replace("data:image/png;base64,",""));
                for (int i = 0; i < bytes.length; ++i) {
                    if (bytes[i] < 0) {
                        // 调整异常数据
                        bytes[i] += 1024;
                    }
                }
                if (bytes != null) {
                    //拿到context数据
                    WriteContext writeContext = excelWriter.writeContext();
                    Workbook workbook = writeContext.writeWorkbookHolder().getWorkbook();
                    Sheet sheet = writeContext.writeSheetHolder().getCachedSheet();
                    Drawing<?> drawing = sheet.getDrawingPatriarch();
                    if (drawing == null) {
                        drawing = sheet.createDrawingPatriarch();
                    }
                    CreationHelper helper = sheet.getWorkbook().getCreationHelper();
                    int index = workbook.addPicture(bytes,
                            FileTypeUtils.getImageTypeFormat(bytes));
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setDx2(-StyleUtil.getCoordinate(5));
                    anchor.setDx1(StyleUtil.getCoordinate(5));
                    // 图片位置设置代码
                    anchor.setRow1(StyleUtil.getCellCoordinate(0, 0, 7));
                    anchor.setCol1(StyleUtil.getCellCoordinate(0, 0, 0));
                    anchor.setRow2(StyleUtil.getCellCoordinate(0, 0, 10) + 1);
                    anchor.setCol2(StyleUtil.getCellCoordinate(0, 0, 6) + 1);
                    anchor.setAnchorType(ClientAnchorData.AnchorType.MOVE_AND_RESIZE.getValue());
                    drawing.createPicture(anchor, index);
                }
            }
            if (StringUtils.isNotBlank(detailBase64)) {
                //BASE64Decoder decoder = new BASE64Decoder();
                //byte[] bytes = decoder.decodeBuffer(detailBase64.replace("data:image/png;base64,",""));
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] bytes = decoder.decode(detailBase64.replace("data:image/png;base64,",""));
                for (int i = 0; i < bytes.length; ++i) {
                    if (bytes[i] < 0) {
                        // 调整异常数据
                        bytes[i] += 1024;
                    }
                }
                if (bytes != null) {
                    //拿到context数据
                    WriteContext writeContext = excelWriter.writeContext();
                    Workbook workbook = writeContext.writeWorkbookHolder().getWorkbook();
                    Sheet sheet = writeContext.writeSheetHolder().getCachedSheet();
                    Drawing<?> drawing = sheet.getDrawingPatriarch();
                    if (drawing == null) {
                        drawing = sheet.createDrawingPatriarch();
                    }
                    CreationHelper helper = sheet.getWorkbook().getCreationHelper();
                    int high = 20;
                    if (length > 10) {
                        high =length/2+16;
                    }
                    int index = workbook.addPicture(bytes,
                            FileTypeUtils.getImageTypeFormat(bytes));
                    ClientAnchor anchor = helper.createClientAnchor();
                    anchor.setDx2(-StyleUtil.getCoordinate(5));
                    anchor.setDx1(StyleUtil.getCoordinate(5));
                    // 图片位置设置代码
                    anchor.setRow1(StyleUtil.getCellCoordinate(0, 0, 14));
                    anchor.setCol1(StyleUtil.getCellCoordinate(0, 0, 0));
                    anchor.setRow2(StyleUtil.getCellCoordinate(0, 0, high) + 1);
                    anchor.setCol2(StyleUtil.getCellCoordinate(0, 0, 6) + 2);
                    anchor.setAnchorType(ClientAnchorData.AnchorType.MOVE_AND_RESIZE.getValue());
                    drawing.createPicture(anchor, index);
                }
            }
            excelWriter.fill(fixMap, writeSheet);
            // bytes为要导出的图像文件字节数组

            response.getOutputStream().flush();
        }
    }

    @SneakyThrows
    @Override
    public void downloadAccessoryDeepOrSubstrateProcessing(QtSubOrDeepAcceptabilityDTO dto, HttpServletResponse response) {
        //获取表格明细数据
        dto.setDateTimeStart(LocalDateTime.of(dto.getDateStart(), LocalTime.MIN));
        dto.setDateTimeEnd(LocalDateTime.of(dto.getDateEnd(), LocalTime.MAX));
        dto.setCurrent(1);
        dto.setSize(10000);
        dto.setTaskType(QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode());
        List<QtDeepOrSubAcceptabilityDetailVO> flatDataList  = getQtDeepOrSubAcceptabilityDetailVO(dto.getInspectionType()==1?
                QtManagerConstant.DEEP_OFFICE_NAME:QtManagerConstant.SUB_OFFICE_NAME,  dto).getRecords();
        for (QtDeepOrSubAcceptabilityDetailVO vo:flatDataList) {
            vo.setClassesDateString(LocalDateUtil.localDateFormat(vo.getClassesDate(),DatePattern.NORM_DATE_PATTERN));
        }
       // Assert.notEmpty(flatDataList, "数据为空，暂无报表");
        //声明excel表格数据
        Map<String, Object> map = MapUtils.newHashMap();
        map.put("dateStart",LocalDateUtil.localDateFormat(dto.getDateStart(),DatePattern.NORM_DATE_PATTERN));
        map.put("dateEnd",LocalDateUtil.localDateFormat(dto.getDateEnd(),DatePattern.NORM_DATE_PATTERN));
        //车间，产线，物料名称，客户
        Set<Integer> officeIdSet = Sets.newHashSet();
        Set<Long> materialIdSet = Sets.newHashSet();
        materialIdSet.add(dto.getMaterialId());//物料
        officeIdSet.add(dto.getProductWorkShopId());//生产车间
        officeIdSet.add(dto.getProductLineId());//产线
        officeIdSet.add(dto.getCustomer());//客户
        Map<Integer, String> officeIdNameMap = FeignOptional.of(()->remoteOfficeFeign.listByIdCh(officeIdSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        Map<Long, String> materialIdNameMap = FeignOptional.of(()->remoteSpareFeignApi.listByIdCh(materialIdSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        if(dto.getProductWorkShopId()!=null){
//            R<String> nameById = remoteOfficeFeign.getNameById(dto.getProductWorkShopId());
//            if(nameById!=null){
//                map.put("productWorkShopName",nameById.getData());
//            }
            map.put("productWorkShopName",officeIdNameMap.getOrDefault(dto.getProductWorkShopId(), Strings.EMPTY));
        }
        if(dto.getProductLineId()!=null) map.put("productLineName", officeIdNameMap.getOrDefault(dto.getProductLineId(), Strings.EMPTY));
        if(dto.getWorkingOrder()!=null){
            QtManagerConstant.QtClassesEnum classEnum = QtManagerConstant.QtClassesEnum.code(dto.getWorkingOrder());
            map.put("className",Objects.nonNull(classEnum) ? classEnum.getName() : Strings.EMPTY);
        }
        if(dto.getMaterialId()!=null) map.put("materialName",materialIdNameMap.getOrDefault(dto.getMaterialId(), Strings.EMPTY));
        if(dto.getCustomer()!=null)  map.put("customerName",officeIdNameMap.getOrDefault(dto.getCustomer(), Strings.EMPTY));
        if(dto.getCompare()!=null)  map.put("compareName",QtManagerConstant.QtChartCompareEnum.getNameByCode(dto.getCompare()));
        DownloadUtil.downloadTemplate(response,dto.getInspectionType()==1?QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_SJGXJHGLQS:QtManagerConstant.QT_CHART_EXCEL_MODEL_NAME_JPXJHGLQS,
                dto.getInspectionType()==1?QtManagerConstant.QT_CHART_DEEP_PROCESS_NAME:QtManagerConstant.QT_CHART_SUB_PROCESS_NAME,map,flatDataList,dto.getBase64());
    }

    /**
     * 删除任务
     * @param id 主键id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteTask(Long id) {
        //先删除任务id
        this.removeById(id);
        //任务详情
        qtCheckTaskDetailService.remove(new LambdaQueryWrapper<QtCheckTaskDetail>().eq(QtCheckTaskDetail::getTaskId,id));
        //样本
        qtCheckTaskCopyService.remove(new LambdaQueryWrapper<QtCheckTaskCopy>().eq(QtCheckTaskCopy::getTaskId,id));
        //落版
        qtCheckTaskDestoryInfoService.remove(new LambdaQueryWrapper<QtCheckTaskDestoryInfo>().eq(QtCheckTaskDestoryInfo::getTaskId,id));
        return true;
    }

    /**
     * 获取所有完成任务的编码
     * @param taskCode
     * @return
     */
    @Override
    public List<QtCheckTaskCodeIdVO> getAllFinishedTaskCode(String taskCode) {
        LambdaQueryWrapper condition = new LambdaQueryWrapper<QtCheckTask>()
                .eq(QtCheckTask::getStatus,QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .select(QtCheckTask::getCode,QtCheckTask::getId)
                .like(StrUtil.isNotBlank(taskCode),QtCheckTask::getCode,taskCode)
                .orderByDesc(QtCheckTask::getCreateTime)
                .last("LIMIT 100");
        List<QtCheckTask> result = list(condition);
        List<QtCheckTaskCodeIdVO> codes = Lists.newArrayList();
        if(CollUtil.isNotEmpty(result)){
            result.forEach(tmp -> {
                QtCheckTaskCodeIdVO vo = new QtCheckTaskCodeIdVO();
                vo.setCode(tmp.getCode());
                vo.setId(tmp.getId());
                codes.add(vo);
            });
        }
        return codes;
    }

    @Override
    public List<QtIncomingCheckDTO> comingList(Set<Integer> itemIdSet) {
        List<QtIncomingCheckDTO> resList = null;
        if (CollUtil.isNotEmpty(itemIdSet)) {
            resList = baseMapper.comingList(itemIdSet);
        }
        return Objects.nonNull(resList) ? resList : Lists.newArrayList();
    }

    @Override
    public List<QtOtherCheckDTO> otherList(Integer checkType, Set<Integer> itemIdSet, LocalDateTime start, LocalDateTime end) {
        List<QtOtherCheckDTO> resList = null;
        if(CollUtil.isNotEmpty(itemIdSet)){
            resList = baseMapper.otherList(checkType, itemIdSet, start, end);
        }
        return Objects.nonNull(resList) ? resList : Lists.newArrayList();
    }

    @Override
    public QtCheckTaskListVO checkPatrol(QtCheckTaskListDTO dto) {
        Assert.notNull(dto.getProductLineId(),"产线不能为空");
        QtCheckTask one = getOne(new LambdaQueryWrapper<QtCheckTask>()
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.PATROL.getCode())
                .eq(dto.getStationId() != null, QtCheckTask::getStationId, dto.getStationId())
                .apply(dto.getProductLineId() != null, "JSON_CONTAINS(product_line, JSON_OBJECT('id', {0}))", dto.getProductLineId())
                .orderByDesc(QtCheckTask::getFinishedTime).last("LIMIT 1"));
        QtCheckTaskListVO result = new QtCheckTaskListVO();
        BeanUtil.copyProperties(one,result);
        return result;
    }


    /**
     * 合格率分组
     *
     * @param compare      对比
     * @param flatDataList 数据集合
     */
    private TupleDTO<Map<String, List<QtCheckTaskReportVO>>, Map<String, String>> rateGroup(Integer compare, List<QtCheckTaskReportVO> flatDataList) {
        Map<String, List<QtCheckTaskReportVO>> targetMap;
        Map<String, String> idNameMap = Maps.newHashMap();
        List<QtCheckTaskReportVO> flatDataCopyData = null;
        if (QtManagerConstant.QtChartCompareEnum.SAMPLINGTIME.getCode().equals(compare)) {
            flatDataCopyData = flatDataList.stream().filter(v -> Objects.nonNull(v.getSamplingTime())).collect(Collectors.toList());
            flatDataList.clear();
            flatDataList.addAll(flatDataCopyData);
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(x -> String.valueOf(x.getSamplingTime().toLocalDate())));
            for (Map.Entry<String, List<QtCheckTaskReportVO>> e : targetMap.entrySet()) {
                idNameMap.put(e.getKey(), e.getKey() + "取样");
            }
        } else if (QtManagerConstant.QtChartCompareEnum.MATERIALWORKSHOP.getCode().equals(compare)) {
            flatDataCopyData = flatDataList.stream().filter(v -> Objects.nonNull(v.getMaterialWorkshopId())).collect(Collectors.toList());
            flatDataList.clear();
            flatDataList.addAll(flatDataCopyData);
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(v -> String.valueOf(v.getMaterialWorkshopId())));
            Set<Integer> idSet = flatDataList.stream().filter(v -> Objects.nonNull(v.getMaterialWorkshopId())).map(QtCheckTaskReportVO::getMaterialWorkshopId).collect(Collectors.toSet());
            Map<Integer, String> officeMap = FeignOptional.of(()->remoteOfficeFeign.listByIdCh(idSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            for (Map.Entry<String, List<QtCheckTaskReportVO>> e : targetMap.entrySet()) {
                idNameMap.put(e.getKey(),officeMap.get(Integer.parseInt(e.getKey())));
            }
        } else {
            //供应商
            flatDataCopyData = flatDataList.stream().filter(v -> Objects.nonNull(v.getSupplierId())).collect(Collectors.toList());
            flatDataList.clear();
            flatDataList.addAll(flatDataCopyData);
            targetMap = flatDataList.stream().collect(Collectors.groupingBy(v -> String.valueOf(v.getSupplierId())));
            Set<Integer> idSet = flatDataList.stream().filter(v -> Objects.nonNull(v.getSupplierId())).map(QtCheckTaskReportVO::getSupplierId).collect(Collectors.toSet());
            Map<Integer, String> officeMap = FeignOptional.of(()->remoteOfficeFeign.listByIdCh(idSet),FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            for (Map.Entry<String, List<QtCheckTaskReportVO>> e : targetMap.entrySet()) {
                idNameMap.put(e.getKey(),officeMap.get(Integer.parseInt(e.getKey())));
            }
        }
        return new TupleDTO<>(targetMap, idNameMap);
    }


    /**
     * 基片化验抽检合格率趋势查询条件
     *
     * @param dto
     * @return
     */
    private LambdaQueryWrapper<QtCheckTask> getQtCheckTaskSubLaboratoryCondition(QtSubLaboratoryAcceptabilityDTO dto) {
        //定义一个接口,查询所有的符合条件的车间和产线
        //①先调用下拉选项接口,查询所有的机构
        //②根据关键字过滤想要的机构
        List<String> type = Lists.newArrayList();
        type.add(QtManagerConstant.OFFICE_WORKSHOP_TYPE);
        SysOfficeTypeKeyDTO sysOfficeTypeKeyDTO = new SysOfficeTypeKeyDTO();
        sysOfficeTypeKeyDTO.setKey(QtManagerConstant.SUB_OFFICE_NAME);
        sysOfficeTypeKeyDTO.setType(type);
        R<List<SysOfficeVO>> listByTypeAndKey = remoteOfficeFeign.getListByTypeAndKey(sysOfficeTypeKeyDTO);
        List<String> officeNames = listByTypeAndKey.getData().stream().map(SysOfficeVO::getOfficeName).collect(Collectors.toList());

        //根据条件查询
        LambdaQueryWrapper<QtCheckTask> condition = new QueryWrapper<QtCheckTask>().select("any_value(type) type , any_value(material_id) material_id , " +
                        "any_value(material_workshop) material_workshop , any_value(product_line) product_line , any_value(product_order) product_order , " +
                        "any_value(product_batch) product_batch , any_value(check_plan_id) check_plan_id , any_value(status) status , any_value(check_num) check_num , " +
                        "any_value(good_num) good_num , any_value(bad_num) bad_num , any_value(result) result , any_value(del_flag) del_flag , " +
                        "any_value(supplier) supplier , any_value(customer) customer , any_value(deliver_order) deliver_order , any_value(task_type) task_type ," +
                        "any_value(deliver_num) deliver_num , any_value(product_workshop) product_workshop , any_value(classes_id) classes_id")
                .lambda()
                //找出所有基片车间的数据
                .in(QtCheckTask::getMaterialWorkshopName, officeNames)
                //任务类型为化验检
                .eq(QtCheckTask::getTaskType, QtManagerConstant.QtCheckTaskTypeEnum.CHEMICAL.getCode())
                //任务状态为完成的
                .eq(QtCheckTask::getStatus, QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode());

        //如果基片生产车间id不为空根据车间id筛选
        if (Objects.nonNull(dto.getProductWorkshopId())) {
            condition.eq(QtCheckTask::getProductWorkshopId, dto.getProductWorkshopId());
        }

        //按生产车间分组
        condition
                //时间范围
                .ge(QtCheckTask::getFinishedTime, dto.getStartTime())
                .lt(QtCheckTask::getFinishedTime, dto.getEndTime())
                .groupBy(QtCheckTask::getProductWorkshopId)
                .orderByAsc(QtCheckTask::getFinishedTime);

        return condition;
    }

    private Long currentMaxCode() {
        //1.定义返回值
        Long currentMax = 0L;
        //2.获取系统最大code
        QtCheckTask maxCodeModel = getOne(new LambdaQueryWrapper<QtCheckTask>().orderByDesc(QtCheckTask::getCode).last(MpConstant.STR_LIMIT_1));
        //3.如果最大code不为空
        if (Objects.nonNull(maxCodeModel)) {
            try {
                //4.解析数值部分
                String substringVal = maxCodeModel.getCode().substring(CodeGenerateUtil.PREFIX_JYRW.length());
                //赋值
                long l = Long.parseLong(substringVal);
                currentMax = l;
            } catch (Exception e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return currentMax;
    }

    private static Predicate<QtIQCAcceptabilityDetailVO> predicate(Integer compare, List<String> filterList, List<QtIQCAcceptabilityDetailVO> list) {
        Predicate<QtIQCAcceptabilityDetailVO> predicate = new Predicate<QtIQCAcceptabilityDetailVO>() {
            @Override
            public boolean test(QtIQCAcceptabilityDetailVO o) {
                List<QtIQCAcceptabilityDetailVO> resList = null;
                if (QtManagerConstant.QtChartCompareEnum.WORKSHOP.equals(compare)) {
                    resList = list.stream().filter(v -> filterList.contains(v.getProductWorkshop().getName())).collect(Collectors.toList());
                } else {
                    resList = list;
                }
                return Boolean.TRUE;
            }
        };
        return predicate;
    }

    private static List<QtIQCAcceptabilityDetailVO> sortList(List<QtIQCAcceptabilityDetailVO> voList) {
        List<QtIQCAcceptabilityDetailVO> list = Lists.newArrayList();
        if (CollUtil.isNotEmpty(voList)) {
            List<QtIQCAcceptabilityDetailVO> valueIsNullList = Lists.newArrayList();
            List<QtIQCAcceptabilityDetailVO> sortList = Lists.newArrayList();
            QtBaseIdNameDTO qtBaseIdNameDTO = new QtBaseIdNameDTO();
            voList.forEach(detail -> {
                QtBaseIdNameDTO materialWorkshop = detail.getMaterialWorkshop();
                QtBaseIdNameDTO supplier = detail.getSupplier();
                if (Objects.isNull(detail.getSamplingTime()) || Objects.isNull(detail.getFinishedTime())
                        || Objects.isNull(materialWorkshop) || StringUtils.isBlank(materialWorkshop.getName()) || StringUtils.isBlank(supplier.getName()) || Objects.isNull(supplier) || StringUtils.isBlank(detail.getMaterialName())) {
                    valueIsNullList.add(detail);
                } else {
                    sortList.add(detail);
                }
            });
            //根据规则排序
            list = sortList.stream().sorted(Comparator.comparing(QtIQCAcceptabilityDetailVO::getMaterialName, Comparator.nullsLast(String::compareTo))
                    .thenComparing(QtIQCAcceptabilityDetailVO::getFinishedTime, Comparator.nullsLast(LocalDate::compareTo))
                    .thenComparing(QtIQCAcceptabilityDetailVO::getSamplingTime, Comparator.nullsLast(LocalDate::compareTo))
                    .thenComparing(Comparator.nullsLast(Comparator.comparing(x -> x.getMaterialWorkshop().getName())))
                    .thenComparing(Comparator.nullsLast(Comparator.comparing(x -> x.getSupplier().getName()))))
                    .collect(Collectors.toList());
            list.addAll(valueIsNullList);
            list = list.stream()
                    .sorted(Comparator.comparing(QtIQCAcceptabilityDetailVO::getMaterialName, Comparator.nullsLast(String::compareTo)))
                    .collect(Collectors.toList());
        }
        return list;
    }


    protected void sendMessage() {
        Set<Long> longIds = Sets.newHashSet();
        //发送给配料工
        QtTaskClassCalculate qtTaskClassCalculate = CalculateDateUtil.calculateClassTime(LocalDateTime.now());
        OaRosterRecordDTO rosterDto = new OaRosterRecordDTO();
        rosterDto.setStartdate(qtTaskClassCalculate.getClassDate().toLocalDate().toString());
        rosterDto.setEnddate(qtTaskClassCalculate.getClassDate().toLocalDate().toString());
        rosterDto.setJobtitle(PmManagerConstant.BATCHER_ID);
        List<OaRosterRecordVO> oaRosterRecordVOS = FeignOptional.of(() -> oaInfoService.getRosterRecord(rosterDto), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        //过滤当前班次
        String classStr = "";
        if (qtTaskClassCalculate.getClassType().equals(0)) {
            classStr = "白班";
        } else {
            classStr = "夜班";
        }
        String finalClassStr = classStr;
        oaRosterRecordVOS = oaRosterRecordVOS.stream().filter(v -> v.getSerial().equals(finalClassStr)).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(oaRosterRecordVOS)) {
            List<String> workCodes = Lists.newArrayList();
            oaRosterRecordVOS.forEach(tmp -> {
                workCodes.add(tmp.getWorkcode());
            });
            //找到配料工id
            if (CollUtil.isNotEmpty(workCodes)) {
                List<OaUserIdAndName> oaUserIdAndNames = FeignOptional.of(() -> oaInfoService.getInfoByWorkCode(workCodes), FeignConstant.STECCN_OA_SERVICE_NAME).get();
                if (CollUtil.isNotEmpty(oaUserIdAndNames)) {
                    oaUserIdAndNames.forEach(tmp -> {
                        longIds.add(tmp.getId().longValue());
                    });
                }
            }
        }
        //发给岗位为二线配料副段长
//        OaHrmDTO hrm = new OaHrmDTO();
//        hrm.setJobtitle(PmManagerConstant.PEILIAO_DUANZHANG);
//        List<OaEmpVO> oaEmpVOS = FeignOptional.of(() -> oaInfoService.getUserByIdByJobTitle(hrm), FeignConstant.STECCN_OA_SERVICE_NAME).get();
//        if (CollUtil.isNotEmpty(oaEmpVOS)) {
//            oaEmpVOS.forEach(tmp -> {
//                longIds.add(tmp.getId().longValue());
//            });
//        }
        //部门为“原料二车间”/“配料工段”且岗位为“配料副段长”的人员
        List<OaUserIdAndName> oaUserIdAndNames1 = FeignOptional.of(() -> oaInfoService.getUserByIdByJobTitleAndDepartmentId(PmManagerConstant.DEPARTMENT_YUANLIAO_2_PEILIAO_ID, PmManagerConstant.DIRECTOR_ID_LINE2), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        if(CollUtil.isNotEmpty(oaUserIdAndNames1)){
            oaUserIdAndNames1.forEach(tmp -> {
                longIds.add(tmp.getId().longValue());
            });
        }
        //一定发给配料班长
        List<OaUserIdAndName> oaUserIdAndNames2 = FeignOptional.of(() -> oaInfoService.getUserByIdByJobTitleAndDepartmentId(PmManagerConstant.DEPARTMENT_YUANLIAO_2_PEILIAO_ID, PmManagerConstant.CLASS_LEADER_PEILIAO), FeignConstant.STECCN_OA_SERVICE_NAME).get();
        if(CollUtil.isNotEmpty(oaUserIdAndNames2)){
            oaUserIdAndNames2.forEach(tmp -> {
                longIds.add(tmp.getId().longValue());
            });
        }
        if (CollUtil.isNotEmpty(longIds)) {
            SysMsgSendDTO sysMsgDTO = new SysMsgSendDTO();
            sysMsgDTO.setType(SysMsgConstant.MsgTypeEnum.BATCH_CHECK_UPDATE.getCode());
            sysMsgDTO.setTitle(SysMsgConstant.MsgTypeEnum.BATCH_CHECK_UPDATE.getTitle());
            sysMsgDTO.setWarnFlag(SysMsgConstant.MsgTypeEnum.BATCH_CHECK_UPDATE.getWarnFlag());
            //不给自己发消息
//            longIds.remove(SecurityUtils.getUser().getUserId());
            sysMsgDTO.setUserList(longIds);
            String content = SysMsgConstant.MsgTypeEnum.BATCH_CHECK_UPDATE.getTemplate();
            sysMsgDTO.setContent(String.format(content, SecurityUtils.getUser().getName()));
            sysMsgDTO.setPushType(WebsocketConstant.WsPushTypeEnum.MSG_SYS_REMIND.getType());
            FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        }
    }
}




