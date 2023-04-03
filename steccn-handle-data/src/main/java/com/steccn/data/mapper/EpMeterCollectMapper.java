package com.steccn.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.data.domain.EpMeterCollect;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【ep_meter_collect(能源绩效-远程仪表采样表)】的数据库操作Mapper
* @createDate 2022-12-16 13:23:55
* @Entity com.steccn.data.domain.EpMeterCollect
*/
@Mapper
public interface EpMeterCollectMapper extends BaseMapper<EpMeterCollect> {

}




