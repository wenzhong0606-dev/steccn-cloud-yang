package com.steccn.ems.scheduled;

import com.steccn.common.constant.LockConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.mapper.EpMeterReadMapper;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.vo.ReadCorrectVO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenhuan
 * @date 2022/12/5 11:14
 */
@RequiredArgsConstructor
@Component
public class ReadCorrectScheduledTask {

    private final EpMeterReadMapper epMeterReadMapper;

    private final EpMeterReadService epMeterReadService;

    private final RedissonClient redissonClient;

    @Async
    @Scheduled(cron = "0 3 9 * * ?")
    public void execute() {
        RLock lock = redissonClient.getLock(LockConstant.READ_CORRECT_SCHEDULED_TASK);
        lock.lock();
        try {
            LocalDate now = LocalDate.now();
            LocalTime startTime = LocalTime.of(7, 0, 0);
            LocalTime endTime = LocalTime.of(9, 0, 0);
            LocalTime time = LocalTime.of(8,0,0);
            LocalDateTime startDateTime = LocalDateTime.of(now, startTime);
            LocalDateTime endDateTime = LocalDateTime.of(now, endTime);
            LocalDateTime localDateTime = LocalDateTime.of(now,time);

            List<ReadCorrectVO> meterReadDetailsVOList = epMeterReadMapper.selectByMeterId();
            Map<Long, List<ReadCorrectVO>> groupBy = meterReadDetailsVOList
                    .stream()
                    .collect(Collectors.groupingBy(ReadCorrectVO::getMeterId));

            List<EpMeterRead> epMeterReadList = new ArrayList<>();
            for (Map.Entry<Long, List<ReadCorrectVO>> entry : groupBy.entrySet()) {
                //没有两条数据不计算纠偏值
                if (entry.getValue().size() < 2) {
                    continue;
                }
                //如果是管理员操作不计算纠偏值
                if (entry.getValue().get(0).getAdminFlag().equals(1)) {
                    continue;
                }
                //不是今天的数据不计算纠偏值
                if (entry.getValue().get(0).getReadTime().isAfter(startDateTime) && entry.getValue().get(0).getReadTime().isBefore(endDateTime)) {
                    continue;
                }
                //抄表时间等于8点不纠偏
                if (entry.getValue().get(0).getReadTime().isEqual(localDateTime)) {
                    continue;
                }

                ReadCorrectVO readCorrect1 = entry.getValue().get(0);
                ReadCorrectVO readCorrect2 = entry.getValue().get(1);
                BigDecimal readValue1 = readCorrect1.getReadValue();
                BigDecimal readValue2 = readCorrect1.getReadValue();
                LocalDateTime readTime1 = readCorrect1.getReadTime();
                LocalDateTime readTime2 = readCorrect2.getReadTime();
                Long second1 = readTime1.toEpochSecond(ZoneOffset.of("+8"));
                Long second2 = readTime2.toEpochSecond(ZoneOffset.of("+8"));
                //8点的时间戳
                Long second = localDateTime.toEpochSecond(ZoneOffset.of("+8"));
                //平均流速
                BigDecimal vMean = BigDecimalUtil.divide(readValue1.subtract(readValue2),BigDecimal.valueOf(second1-second2));
                //纠偏值
                BigDecimal correctValue;
                //如果小于八点
                if (readTime1.isBefore(localDateTime)) {
                    //抄表时间和8点相差的秒数
                    long differSecond = second - second1;
                    correctValue = readCorrect1.getReadValue().add(BigDecimal.valueOf(differSecond).multiply(vMean));
                } else {
                    //抄表时间和8点相差的秒数
                    long differSecond = second1 - second;
                    correctValue = readCorrect1.getReadValue().subtract(BigDecimal.valueOf(differSecond).multiply(vMean));
                }
                EpMeterRead epMeterRead = new EpMeterRead();
                epMeterRead.setId(readCorrect1.getId());
                epMeterRead.setCorrectValue(correctValue);
                epMeterReadList.add(epMeterRead);
            }
            epMeterReadService.saveBatch(epMeterReadList);
        } finally {
            lock.unlock();
        }
    }
}
