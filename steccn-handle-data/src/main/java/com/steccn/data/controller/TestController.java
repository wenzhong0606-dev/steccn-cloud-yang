package com.steccn.data.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.dto.TupleDTO;
import com.steccn.data.domain.EpMeterCollect;
import com.steccn.data.domain.TbMoveData;
import com.steccn.data.dto.EpMeterCollectDTO;
import com.steccn.data.feign.RemoteDataPlatformFeignApi;
import com.steccn.data.service.EpMeterCollectService;
import com.steccn.data.service.TbMoveDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yequan
 * @desc
 * @date 2023/3/31
 */
@Api("湖北博华数据采集")
@RestController
@RequiredArgsConstructor
@RequestMapping("/handle/")
@Slf4j
public class TestController {

    private final EpMeterCollectService epMeterCollectService;

    private final TbMoveDataService tbMoveDataService;

    private final RemoteDataPlatformFeignApi remoteDataPlatformFeignApi;

    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 20, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), Executors.defaultThreadFactory());

    @GetMapping("/data")
    @ApiOperation(value = "迁移数据")
    @Transactional(rollbackFor = Exception.class)
    public void data(@RequestParam("start") LocalDateTime start, @RequestParam("end") LocalDateTime end) {
        //分20个线程去跑任务
        List<TupleDTO<LocalDateTime, LocalDateTime>> shard = shard(start, end);
        for (TupleDTO<LocalDateTime, LocalDateTime> tupleDTO : shard) {
            pool.execute(() -> {
                try {
                    log.info("开始时间 -> {} 结束时间 -> {} START",tupleDTO.getField1(),tupleDTO.getField2());
                    List<EpMeterCollect> resList = epMeterCollectService.list(new LambdaQueryWrapper<EpMeterCollect>()
                            .ge(EpMeterCollect::getRecordTime, tupleDTO.getField1())
                            .lt(EpMeterCollect::getRecordTime, tupleDTO.getField2()));
                    if (CollUtil.isNotEmpty(resList)) {
                        List<EpMeterCollectDTO> epMeterCollects = BeanUtil.copyToList(resList, EpMeterCollectDTO.class);
                        Boolean flag = FeignOptional.of(() -> remoteDataPlatformFeignApi.dealData(epMeterCollects), "服务不可用").get();
                        if (flag) {
                            //保存结果到存储中
                            TbMoveData tbMoveData = new TbMoveData();
                            tbMoveData.setStartTime(tupleDTO.getField1());
                            tbMoveData.setEndTime(tupleDTO.getField2());
                            tbMoveData.setFlag(1);
                            tbMoveData.setCountSum(resList.size());
                            tbMoveDataService.save(tbMoveData);
                        }
                    }
                    log.info("开始时间 -> {} 结束时间 -> {} END",tupleDTO.getField1(),tupleDTO.getField2());
                }catch (Exception e){
                    log.error(e.getLocalizedMessage());
                }
            });
        }
    }

    /**
     * 根据开始时间和结束时间分片
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 分片
     */
    private static List<TupleDTO<LocalDateTime, LocalDateTime>> shard(LocalDateTime start, LocalDateTime end) {
        List<TupleDTO<LocalDateTime, LocalDateTime>> resList = Lists.newArrayList();
        while (start.isBefore(end)) {
            LocalDateTime resStart = LocalDateTime.from(start);
            start = start.plusMinutes(1);
            LocalDateTime resEnd = LocalDateTime.from(start);
            TupleDTO<LocalDateTime, LocalDateTime> tupleDTO = new TupleDTO<>(resStart, resEnd);
            resList.add(tupleDTO);
        }
        return resList;
    }
}
