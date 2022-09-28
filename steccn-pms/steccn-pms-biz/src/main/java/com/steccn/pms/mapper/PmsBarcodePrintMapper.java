package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsBarcodePrint;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author yequan
* @description 针对表【pms_barcode_print(条码打印)】的数据库操作Mapper
* @createDate 2022-09-28 15:18:51
* @Entity com.steccn.pms.domain.PmsBarcodePrint
*/
@Mapper
public interface PmsBarcodePrintMapper extends BaseMapper<PmsBarcodePrint> {

}




