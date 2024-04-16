package com.steccn.dp.service;

import com.steccn.dp.domain.DpColdEndCheckpointValue;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.dp.vo.DpColdEndCheckpointValueVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
* @author Admin
* @description 针对表【dp_cold_end_checkpoint_value(冷端各线测点数据)】的数据库操作Service
* @createDate 2024-03-01 16:21:20
*/
public interface DpColdEndCheckpointValueService extends IService<DpColdEndCheckpointValue> {

    /**
     * 获取所有冷端测点最新数据
     * @return
     */
    List<DpColdEndCheckpointValueVO> getColdEndValue();

    /**
     * 获取屏幕数据 1 KLW 2 NOX 3 SO2
     * @param num
     * @return
     */
    BigDecimal getScreenData(Integer num);

    /**
     * 删除date时间前的数据
     * @param date
     * @return
     */
    Boolean deleteData(LocalDateTime date);

}
