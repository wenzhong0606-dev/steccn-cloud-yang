package com.steccn.qms.mapper;

import com.steccn.qms.domain.QmsDefectProduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【qms_defect_product(不合格品处理表)】的数据库操作Mapper
* @createDate 2023-03-15 10:16:54
* @Entity com.steccn.qms.domain.QmsDefectProduct
*/
@Mapper
public interface QmsDefectProductMapper extends BaseMapper<QmsDefectProduct> {

}




