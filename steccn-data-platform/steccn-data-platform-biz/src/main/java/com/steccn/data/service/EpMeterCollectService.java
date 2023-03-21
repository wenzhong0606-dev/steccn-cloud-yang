package com.steccn.data.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.data.domain.EpMeterCollect;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.data.dto.DwMeterReadDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【ep_meter_collect(能源绩效-远程仪表采样表)】的数据库操作Service
 * @createDate 2022-12-16 13:23:55
 */
public interface EpMeterCollectService extends IService<EpMeterCollect> {

    /**
     * 数仓条件列表
     *
     * @param dto
     * @return
     */
    DwEpMeterCollectVO meterRead(DwMeterReadDTO dto);

    /**
     * 最近抄表时间
     *
     * @param distantCodeSet 远传仪表编码集合
     * @return
     */
    Map<String, LocalDateTime> lastReadTime(Set<String> distantCodeSet);

    /**
     * 获取最近抄表时间点
     *
     * @return
     */
    LocalDateTime getLastRecordTime();

    /**
     * 刷新最近抄表时间
     *
     * @param curTime 大于这个时间
     * @return
     */
    Boolean refreshLastReadTime(LocalDateTime curTime);
}
