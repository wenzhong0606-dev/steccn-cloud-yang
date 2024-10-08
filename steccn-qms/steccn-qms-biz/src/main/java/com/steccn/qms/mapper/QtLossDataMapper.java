package com.steccn.qms.mapper;

import com.steccn.qms.domain.QtLossData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Admin
* @description 针对表【qt_loss_data(损耗全量数据表)】的数据库操作Mapper
* @createDate 2024-10-08 11:00:29
* @Entity com.steccn.qms.domain.QtLossData
*/
@Mapper
public interface QtLossDataMapper extends BaseMapper<QtLossData> {

}
