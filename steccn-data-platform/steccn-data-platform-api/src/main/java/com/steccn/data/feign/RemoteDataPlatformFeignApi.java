package com.steccn.data.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.vo.R;
import com.steccn.data.dto.*;
import com.steccn.data.vo.DwDefectCollectVO;
import com.steccn.data.vo.DwEpMeterCollectVO;
import com.steccn.data.vo.DwStationCollectVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @desc 数据中台服务
 */
@FeignClient(contextId = "remoteDataPlatformFeignApi", value = FeignConstant.STECCN_DATA_PLATFORM_SERVICE_NAME)
public interface RemoteDataPlatformFeignApi {


    /**
     * 能源绩效系统-抄表归档
     *
     * @param dto 数仓条件列表
     * @return list
     */
    @PostMapping("/feign/data/warehouse/meter/read")
    @ApiOperation("能源绩效系统-抄表归档")
    R<DwEpMeterCollectVO> meterRead(@RequestBody DwMeterReadDTO dto);

    @PostMapping("/feign/data/warehouse/meter/read/seValue")
    @ApiOperation("能源绩效系统-抄表归档-同一个code的2个值")
    R<List<BigDecimal>> seValue(@RequestBody TripleDTO<String,LocalDateTime,LocalDateTime> tripleDTO);

    @PostMapping("/feign/data/warehouse/meter/read/diff")
    @ApiOperation(value = "数仓条件列表-一块仪表，2个时间点的差值")
    R<BigDecimal> meterReadDiff(@RequestBody DwMeterReadDiffDTO dto);

    /**
     * 生产管理-进出站计数归档
     *
     * @param dto 查询条件
     * @return list
     */
    @PostMapping("/feign/data/warehouse/station/collect")
    @ApiOperation("生产管理-进出站计数归档")
    R<List<DwStationCollectVO>> stationCollect(@RequestBody DwStationCollectDTO dto);

    /**
     * 最近抄表时间
     * @param distantCodeSet 远传仪表编码
     * @return map
     */
    @PostMapping("/feign/data/warehouse/meter/last/read/time")
    @ApiOperation("能源绩效-最近抄表时间")
    R<Map<String, LocalDateTime>> lastReadTime(@RequestBody Set<String> distantCodeSet);

    /**
     * 质量管理-缺陷项
     *
     * @param dto dto
     * @return
     */
    @PostMapping("/feign/data/warehouse/defect")
    @ApiOperation("质量管理-缺陷项")
    R<List<DwDefectCollectVO>> collectDetail(@RequestBody TripleDTO<LocalDateTime, LocalDateTime, Set<String>> dto);

    /**
     * 生产管理-报工数值预填
     *
     * @param dto dto
     * @return 预填数值
     */
    @PostMapping("/feign/data/warehouse/report/work/preload")
    @ApiOperation("生产管理-报工数值预填")
    R<BigDecimal> preload(@RequestBody PmReportWorkPreloadDTO dto);


    /**
     * 迁移数据
     *
     * @param dto dto
     * @return 迁移数据
     */
    @PostMapping("/feign/data/warehouse/deal/data")
    @ApiOperation("迁移数据")
    R<Boolean> dealData(@RequestBody List<EpMeterCollectDTO> list);


    /**
     * 迁移数据
     *
     * @param list dto
     * @return 迁移数据 pm_station_collect
     */
    @PostMapping("/feign/data/warehouse/psc/deal/data")
    @ApiOperation("迁移数据")
    R<Boolean> pmStationCollectDealData(@RequestBody List<PmStationCollectDTO> list);

}
