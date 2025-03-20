package com.steccn.pms.service;

import com.steccn.common.dto.TupleDTO;
import com.steccn.pms.domain.PmsMeltingConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsMeltingConfigDTO;
import com.steccn.pms.vo.PmsMeltingConfigVO;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
* @author Admin
* @description 针对表【pms_melting_config】的数据库操作Service
* @createDate 2025-03-20 15:00:05
*/
public interface PmsMeltingConfigService extends IService<PmsMeltingConfig> {
    /**
     * 修改熔化率
     * @param dto
     * @return
     */
    Boolean add(PmsMeltingConfigDTO dto);

    /**
     * 获取每日熔化率列表
     * @param workshopId
     * @param monthDate
     * @return
     */
    List<PmsMeltingConfigVO> list(String monthDate);

    /**
     * 自动设置熔化率
     * @return
     */
    Boolean addMeltingRate();
}
