package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsBarcodePrintRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 34872
* @description 针对表【pms_barcode_print_record(打印记录流水信息)】的数据库操作Mapper
* @createDate 2023-08-25 11:11:32
* @Entity com.steccn.pms.domain.PmsBarcodePrintRecord
*/
@Mapper
public interface PmsBarcodePrintRecordMapper extends BaseMapper<PmsBarcodePrintRecord> {

}




