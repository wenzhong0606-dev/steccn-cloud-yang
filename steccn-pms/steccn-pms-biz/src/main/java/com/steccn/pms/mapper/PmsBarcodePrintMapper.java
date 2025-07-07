package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmsBarcodePrint;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.pms.vo.PmsCaseNoHistoryVO;
import com.steccn.wms.vo.WmsCaseNoHistoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author yequan
* @description 针对表【pms_barcode_print(条码打印)】的数据库操作Mapper
* @createDate 2022-09-28 15:18:51
* @Entity com.steccn.pms.domain.PmsBarcodePrint
*/
@Mapper
public interface PmsBarcodePrintMapper extends BaseMapper<PmsBarcodePrint> {
    /**
     * 箱号操作日志
     * @param caseNo 箱号
     * @return
     */
    List<PmsCaseNoHistoryVO> caseNoHistory(@Param("caseNo") String caseNo);
}




