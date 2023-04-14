package com.steccn.data.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Lists;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.util.SplitListUtil;
import com.steccn.data.domain.EpMeterCollect;
import com.steccn.data.domain.PmStationCollect;
import com.steccn.data.domain.QtDefectCollect;
import com.steccn.data.domain.QtDefectCollectDetail;
import com.steccn.data.dto.*;
import com.steccn.data.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;

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
                model.setErrorCode(dto.getData().getErrcode());
                model.setProductTime(dto.getData().getProduct_time());
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
    @Transactional(rollbackFor = Exception.class)
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
}
