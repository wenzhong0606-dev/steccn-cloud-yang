package com.steccn.ems.scheduled;

import com.steccn.common.constant.LockConstant;
import com.steccn.common.util.BigDecimalUtil;
import com.steccn.ems.domain.EpMeterRead;
import com.steccn.ems.mapper.EpMeterReadMapper;
import com.steccn.ems.service.EpMeterReadService;
import com.steccn.ems.vo.ReadCorrectVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenhuan
 * @date 2022/12/5 11:14
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ReadCorrectScheduledTask {

    private final EpMeterReadMapper epMeterReadMapper;

    private final EpMeterReadService epMeterReadService;

    private final RedissonClient redissonClient;

    @Async
    @Scheduled(cron = "0 3 9 * * ?")
//    @Scheduled(cron = "0/20 * * * * ? ")
    public void execute() {
        RLock lock = redissonClient.getLock(LockConstant.READ_CORRECT_SCHEDULED_TASK);
        boolean b = lock.tryLock();
        if(!b){
            log.info("获取锁失败");
            return ;
        }
        log.info("数据开始纠偏");
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
                    log.info("当前仪表只有一次抄表记录");
                    continue;
                }
                //管理员操作不计算纠偏值
                if (entry.getValue().get(0).getAdminFlag().equals(1)) {
                    log.info("管理员操作不计算纠偏值");
                    continue;
                }
                //不是今天的数据不计算纠偏值
                if (entry.getValue().get(0).getReadTime().isAfter(startDateTime) && entry.getValue().get(0).getReadTime().isBefore(endDateTime)) {
                    log.info("不是今天的数据不计算纠偏值");
                    continue;
                }
                //抄表时间等于8点不纠偏
                if (entry.getValue().get(0).getReadTime().isEqual(localDateTime)) {
                    log.info("抄表时间等于8点不纠偏");
                    continue;
                }

                ReadCorrectVO readCorrect1 = entry.getValue().get(0);
                ReadCorrectVO readCorrect2 = entry.getValue().get(1);
                BigDecimal readValue1 = readCorrect1.getReadValue();
                BigDecimal readValue2 = readCorrect1.getReadValue();
                //和上次抄表相差的秒数
                long seconds = ChronoUnit.SECONDS.between(readCorrect1.getReadTime(), readCorrect2.getReadTime());
                //平均流速
                BigDecimal vMean = BigDecimalUtil.divide(readValue1.subtract(readValue2),BigDecimal.valueOf(seconds));
                //和当天8点相差的秒数
                long differSecond = ChronoUnit.SECONDS.between(readCorrect1.getReadTime(), localDateTime);
                //纠偏值
                BigDecimal correctValue = readCorrect1.getReadValue().add(BigDecimal.valueOf(differSecond).multiply(vMean));

                EpMeterRead epMeterRead = new EpMeterRead();
                epMeterRead.setId(readCorrect1.getId());
                epMeterRead.setCorrectValue(correctValue);
                epMeterRead.setAdminValue(correctValue);
                epMeterReadList.add(epMeterRead);
            }
            epMeterReadService.updateBatchById(epMeterReadList);
        }catch (Exception e) {
            log.error("数据纠偏异常",e);
        } finally {
            log.info("数据纠偏结束");
            lock.unlock();
        }
    }
}
