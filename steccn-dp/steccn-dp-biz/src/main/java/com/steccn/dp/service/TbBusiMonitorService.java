package com.steccn.dp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.TupleDTO;
import com.steccn.dp.domain.TbBusiMonitor;
import com.steccn.dp.dto.DpBusiMonitorDTO;
import java.util.List;

/**
 * @author yequan
 * @description 针对表【tb_busi_monitor】的数据库操作Service
 * @createDate 2024-10-21 16:20:05
 */
public interface TbBusiMonitorService extends IService<TbBusiMonitor> {

    /**
     * 保存 - 存在删除、更新、新增
     *
     * @param dataList
     * @return
     */
    Boolean saveList(List<DpBusiMonitorDTO> dataList);

    /**
     * 显示所有的业务监控配置
     * @return
     */
    List<TupleDTO<Integer, List<TupleDTO<String, List<DpBusiMonitorDTO>>>>> showAll();
}
