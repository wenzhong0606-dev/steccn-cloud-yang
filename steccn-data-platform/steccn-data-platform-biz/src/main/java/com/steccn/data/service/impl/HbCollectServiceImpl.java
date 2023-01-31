package com.steccn.data.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.IdUtil;
import com.google.common.collect.Lists;
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

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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

    private final ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(3, 5, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100), Executors.defaultThreadFactory());

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
                Date date = new Date(dto.getData().getProduct_time());
                model.setProductTime(LocalDateTimeUtil.of(date));
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
        } catch (Exception e) {
            log.error("请求的dto数据接收 -> {}", dto);
            log.error(e.getLocalizedMessage());
        }
        return flag;
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
        poolExecutor.execute(()-> epMeterCollectService.saveBatch(batchList));
        return Boolean.TRUE;
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
        poolExecutor.execute(() -> pmStationCollectService.saveBatch(batchList));
        return Boolean.TRUE;
    }
}
