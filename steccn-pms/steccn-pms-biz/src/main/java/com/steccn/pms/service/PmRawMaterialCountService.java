package com.steccn.pms.service;

import com.steccn.common.dto.TupleDTO;
import com.steccn.pms.domain.PmRawMaterialCount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.vo.PmRawMaterialVO;

import java.time.LocalDate;
import java.util.List;

/**
* @author Admin
* @description 针对表【pm_raw_material_count(原料配料统计表)】的数据库操作Service
* @createDate 2023-12-19 16:18:55
*/
public interface PmRawMaterialCountService extends IService<PmRawMaterialCount> {
    /**
     * 根据日期获取数据
     * @param dto f1开始时间 f2结束时间
     * @return
     */
    List<PmRawMaterialVO> getListByDate(TupleDTO<LocalDate,LocalDate> dto);
}
