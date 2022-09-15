package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmProcess;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.dto.PmProcessDTO;
import com.steccn.pms.vo.PmProcessVO;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @description 针对表【pm_process(生产工序表)】的数据库操作Service
 * @createDate 2022-09-02 17:01:20
 */

public interface PmProcessService extends IService<PmProcess> {

    R<Boolean> saveProcess(PmProcessDTO dto);

    Page<PmProcessVO> pageList(PmProcessDTO dto);

    Boolean delete(Integer id);

    R<Boolean> edit(PmProcessDTO dto);

    Map<Long, PmProcessVO> listById(List<Long> processIds);

    List<PmProcessVO> listData();

    Boolean excelImportData(PmExcelDTO dto) throws IOException;
}
