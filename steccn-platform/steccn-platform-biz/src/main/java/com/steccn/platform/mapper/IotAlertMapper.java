package com.steccn.platform.mapper;

import com.steccn.platform.domain.IotAlert;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Admin
* @description 针对表【iot_alert(告警表)】的数据库操作Mapper
* @createDate 2025-12-10 14:27:19
* @Entity com.steccn.platform.domain.IotAlert
*/
@Mapper
public interface IotAlertMapper extends BaseMapper<IotAlert> {

}




