package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.pms.domain.PmsLaborProtectionRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.dto.PmsLaborGerReqDTO;
import com.steccn.pms.dto.PmsLaborRecordPageDTO;
import com.steccn.pms.vo.PmsLaborRecordVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
* @author Admin
* @description 针对表【pms_labor_protection_record(劳保记录表)】的数据库操作Service
* @createDate 2024-04-15 10:59:25
*/
public interface PmsLaborProtectionRecordService extends IService<PmsLaborProtectionRecord> {
    /**
     * 获取分页数据
     * @param dto
     * @return
     */
    Page<PmsLaborRecordVO> pageList(PmsLaborRecordPageDTO dto);

    /**
     * 生成领用单
     * @param dto
     * @return
     */
    Boolean generateReq(PmsLaborGerReqDTO dto);

    void download(HttpServletResponse response) throws IOException;
    SteccnTableDataDTO<Map<String, String>> excelImportData(PmExcelDTO dto) throws IOException;


}
