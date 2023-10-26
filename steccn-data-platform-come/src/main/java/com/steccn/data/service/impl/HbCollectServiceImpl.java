package com.steccn.data.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.authserver.util.SecurityUtils;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.ValidList;
import com.steccn.common.util.CalculateDateUtil;
import com.steccn.common.util.SplitListUtil;
import com.steccn.data.constant.HbThirdPartyConstant;
import com.steccn.data.domain.*;
import com.steccn.data.dto.*;
import com.steccn.data.service.*;
import com.steccn.oa.constant.OaConstant;
import com.steccn.oa.dto.OaRosterRecordDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.oa.vo.OaRosterRecordVO;
import com.steccn.platform.constant.SysMsgConstant;
import com.steccn.platform.dto.SysMsgSendDTO;
import com.steccn.platform.feign.RemoteMsgFeignApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yequan
 * @desc 湖北博华采样数据接收接口
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HbCollectServiceImpl implements HbCollectService {

    private final QtDefectCollectService qtDefectCollectService;
    private final QtDefectCollectDetailService qtDefectCollectDetailService;
    private final EpMeterCollectService epMeterCollectService;
    private final PmStationCollectService pmStationCollectService;
    private final MesEqpSignalService mesEqpSignalService;
    private final QtScreenCollectService qtScreenCollectService;
    private final RemoteMsgFeignApi remoteMsgFeignApi;
    private final OaInfoService oaInfoService;
    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(3, 5, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000), Executors.defaultThreadFactory());

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveDefective(QtThirdPartyDefectiveDTO dto) {
        //1.获取当前时间,定义标识
        LocalDateTime now = LocalDateTime.now();
        boolean flag = Boolean.FALSE;
        try {
            //2.保存主表
            QtDefectCollect model = new QtDefectCollect();
            long mainId = IdUtil.getSnowflake().nextId();
            model.setId(mainId);
            model.setMsgType(dto.getMsgEchoType());
            if (Objects.nonNull(dto.getData())) {
                model.setLine(dto.getData().getLine());
                model.setNewLine(Integer.parseInt(model.getLine()));
                model.setErrorCode(dto.getData().getErrcode());
                model.setProductTime(dto.getData().getProduct_time());
                LocalDate date = model.getProductTime().toLocalDate();
                int hour = model.getProductTime().getHour();
                model.setStartTime(LocalDateTime.of(date, LocalTime.of(hour, 0, 0)));
            }
            model.setCreateTime(now);
            qtDefectCollectService.save(model);
            //3.保存子表
            if (Objects.nonNull(dto.getData())) {
                QtThirdPartyDefectiveDataDTO data = dto.getData();
                List<QtThirdPartyDefectContentDTO> defect_content = data.getDefect_Content();
                if (CollUtil.isNotEmpty(defect_content)) {
                    List<QtDefectCollectDetail> itemList = Lists.newArrayList();
                    for (QtThirdPartyDefectContentDTO child : defect_content) {
                        QtDefectCollectDetail item = new QtDefectCollectDetail();
                        item.setId(IdUtil.getSnowflake().nextId());
                        item.setDefectType(child.getDefect_type());
                        item.setLevel(child.getLevel());
                        item.setDefectCount(Integer.parseInt(child.getDefect_count()));
                        item.setCreateTime(now);
                        item.setPushId(mainId);
                        itemList.add(item);
                    }
                    qtDefectCollectDetailService.saveBatch(itemList);
                }
            }
            flag = Boolean.TRUE;
            return flag;
        } catch (Exception e) {
            log.error("请求的dto数据接收 -> {}", dto);
            log.error(e.getLocalizedMessage());
            throw new BusinessException("请求参数解析错误");
        }
    }

    @Override
    public Boolean epMeter(CollectCommonDTO<EpMeterCollectDTO> dto) {
        List<EpMeterCollectDTO> dataList = dto.getData();
        LocalDateTime now = LocalDateTime.now();
        List<EpMeterCollect> batchList = Lists.newArrayList();
        for (EpMeterCollectDTO data : dataList) {
            EpMeterCollect epMeterCollect = new EpMeterCollect();
            BeanUtils.copyProperties(data, epMeterCollect);
            epMeterCollect.setCreateTime(now);
            epMeterCollect.setRecordTime(data.getTimestamp());
            batchList.add(epMeterCollect);
        }
        return epMeterCollectService.saveBatch(batchList);
    }

    @Override
    public Boolean pmStation(CollectCommonDTO<PmStationCollectDTO> dto) {
        List<PmStationCollectDTO> dataList = dto.getData();
        LocalDateTime now = LocalDateTime.now();
        List<PmStationCollect> batchList = Lists.newArrayList();
        for (PmStationCollectDTO data : dataList) {
            PmStationCollect pmStationCollect = new PmStationCollect();
            BeanUtils.copyProperties(data, pmStationCollect);
            pmStationCollect.setCreateTime(now);
            pmStationCollect.setRecordTime(data.getTimestamp());
            batchList.add(pmStationCollect);
        }
        return pmStationCollectService.saveBatch(batchList);
    }

    @Override
    public Boolean mesEqpSignal(CollectCommonValidDTO<MesEqpSignalDTO> dto) {
        List<MesEqpSignalDTO> data = dto.getData();
        List<MesEqpSignal> models = Lists.newArrayList();
        data.forEach(tmp -> {
            MesEqpSignal model = new MesEqpSignal();
            BeanUtils.copyProperties(tmp,model);
            //校验是否发消息
            if(HbThirdPartyConstant.SIGNAL_CODE_LIST.contains(tmp.getCode())){
                if(tmp.getValue().equals(OaConstant.YES)){
                    poolExecutor.execute(()->{
                        SysMsgSendDTO sysMsgDTO = new SysMsgSendDTO();
                        sysMsgDTO.setType(SysMsgConstant.MsgTypeEnum.OFFLINE_NOTICE.getCode());
                        sysMsgDTO.setTitle(SysMsgConstant.MsgTypeEnum.OFFLINE_NOTICE.getTitle());
                        sysMsgDTO.setWarnFlag(SysMsgConstant.MsgTypeEnum.OFFLINE_NOTICE.getWarnFlag());
                        //发给深加工车间当班主任
                        Set<Long> longIds = Sets.newHashSet(574L,576L,500L,501L,509L,527L,532L);
                        //获取深加工值班主任
//                        OaRosterRecordDTO rosterDto = new OaRosterRecordDTO();
//                        rosterDto.setStartdate(CalculateDateUtil.calculateClassTime(LocalDateTime.now()).getClassDate().toLocalDate().toString());
//                        rosterDto.setEnddate(CalculateDateUtil.calculateClassTime(LocalDateTime.now()).getClassDate().toLocalDate().toString());
//                        //获取深加工值班主任
//                        rosterDto.setJobtitle(HbThirdPartyConstant.SHIFT_SUPERVISOR_1);
//                        List<OaRosterRecordVO> oaRosterRecordVOS = Lists.newArrayList();
//                        oaRosterRecordVOS.addAll(FeignOptional.of(() -> oaInfoService.getRosterRecord(rosterDto), FeignConstant.STECCN_OA_SERVICE_NAME).get());
//                        rosterDto.setJobtitle(HbThirdPartyConstant.SHIFT_SUPERVISOR_2);
//                        oaRosterRecordVOS.addAll(FeignOptional.of(() -> oaInfoService.getRosterRecord(rosterDto), FeignConstant.STECCN_OA_SERVICE_NAME).get());
//                        rosterDto.setJobtitle(HbThirdPartyConstant.SHIFT_SUPERVISOR_3);
//                        oaRosterRecordVOS.addAll(FeignOptional.of(() -> oaInfoService.getRosterRecord(rosterDto), FeignConstant.STECCN_OA_SERVICE_NAME).get());
//                        rosterDto.setJobtitle(HbThirdPartyConstant.SHIFT_SUPERVISOR_4);
//                        oaRosterRecordVOS.addAll(FeignOptional.of(() -> oaInfoService.getRosterRecord(rosterDto), FeignConstant.STECCN_OA_SERVICE_NAME).get());
//                        rosterDto.setJobtitle(HbThirdPartyConstant.SHIFT_SUPERVISOR_5);
//                        oaRosterRecordVOS.addAll(FeignOptional.of(() -> oaInfoService.getRosterRecord(rosterDto), FeignConstant.STECCN_OA_SERVICE_NAME).get());
                        sysMsgDTO.setUserList(longIds);
                        String content = String.format(SysMsgConstant.MsgTypeEnum.OFFLINE_NOTICE.getTemplate(),HbThirdPartyConstant.SIGNAL_CODE_MAPPING.getNameByCode(tmp.getCode()), tmp.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        sysMsgDTO.setPushType(OaConstant.NUM_1);
                        sysMsgDTO.setContent(content);
                        FeignOptional.of(() -> remoteMsgFeignApi.sendMsg(sysMsgDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                    });
                }
            }
            model.setCreateTime(LocalDateTime.now());
            models.add(model);
        });
        return mesEqpSignalService.saveBatch(models);
    }

    @Override
    public Boolean saveScreenData(CollectCommonValidDTO<ScreenDataDTO> dto) {
        List<ScreenDataDTO> data = dto.getData();
        List<QtScreenCollect> models = Lists.newArrayList();
        data.forEach(tmp->{
            QtScreenCollect model = new QtScreenCollect();
            BeanUtils.copyProperties(tmp,model);
            model.setCreateTime(LocalDateTime.now());
            models.add(model);
        });
        return qtScreenCollectService.saveBatch(models);
    }
}
