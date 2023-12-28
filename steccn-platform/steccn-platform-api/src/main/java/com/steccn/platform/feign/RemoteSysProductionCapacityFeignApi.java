package com.steccn.platform.feign;

import com.steccn.common.constant.FeignConstant;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.vo.R;
import com.steccn.platform.vo.SysOfficeCollectVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @desc 标准产能
 * @date 2023/12/28
 */
@FeignClient(contextId = "remoteSysProductionCapacityFeignApi", value = FeignConstant.STECCN_PLATFORM_SERVICE_NAME)
public interface RemoteSysProductionCapacityFeignApi {

    /**
     * 查询工位指定日期区间的标准产能
     *
     * @param tripleDTO 类型
     * @return
     */
    @PostMapping("/feign/platform/sys/production/capacity/station")
    R<List<SysOfficeCollectVO>> listOnlyStation(@RequestBody TripleDTO<Set<Integer>, LocalDate, LocalDate> tripleDTO);

}
