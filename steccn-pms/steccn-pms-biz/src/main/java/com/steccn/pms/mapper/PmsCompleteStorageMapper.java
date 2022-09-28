package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsCompleteStorage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【pms_complete_storage(生产订单-完工入库)】的数据库操作Mapper
* @createDate 2022-09-28 15:18:51
* @Entity com.steccn.pms.domain.PmsCompleteStorage
*/
@Mapper
public interface PmsCompleteStorageMapper extends BaseMapper<PmsCompleteStorage> {

}




