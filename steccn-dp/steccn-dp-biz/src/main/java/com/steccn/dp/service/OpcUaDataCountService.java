package com.steccn.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.TripleDTO;
import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.domain.OpcUaDataCount;
import java.time.LocalDateTime;
import java.util.List;

/**
* @author Admin
* @description 针对表【opc_ua_data_count】的数据库操作Service
* @createDate 2024-08-21 14:36:31
*/
public interface OpcUaDataCountService extends IService<OpcUaDataCount> {

    /**
     * 根据时间统计条数
     * @param time 时间窗口
     * @return
     */
    List<TupleDTO<String,Integer>> category(LocalDateTime time);

    /**
     * 大类明细集合
     * @param time 时间窗口
     * @param categoryCode 大类code
     * @return
     */
    List<TripleDTO<String, String, Integer>> detail(LocalDateTime time, String categoryCode);
}
