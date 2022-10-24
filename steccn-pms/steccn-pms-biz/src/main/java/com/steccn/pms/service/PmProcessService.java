package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.vo.R;
import com.steccn.pms.domain.PmProcess;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmExcelDTO;
import com.steccn.pms.dto.PmProcessDTO;
import com.steccn.pms.dto.PmsProcessInStationDTO;
import com.steccn.pms.dto.PmsProcessOutStationPageDTO;
import com.steccn.pms.vo.BarcodePrintRecordVO;
import com.steccn.pms.vo.PmProcessVO;
import com.steccn.pms.vo.PmsProcessInStationVO;
import com.steccn.pms.vo.PmsProcessOutStationVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author yequan
 * @description 针对表【pm_process(生产工序表)】的数据库操作Service
 * @createDate 2022-09-02 17:01:20
 */

public interface PmProcessService extends IService<PmProcess> {


    /**
     * 保存工序
     * @param dto
     * @return
     */
    R<Boolean> saveProcess(PmProcessDTO dto);


    /**
     * 分页
     * @param dto
     * @return
     */
    Page<PmProcessVO> pageList(PmProcessDTO dto);

    /**
     * 删除
     * @param id
     * @return
     */
    Boolean delete(Integer id);

    /**
     * 编辑
     * @param dto
     * @return
     */
    R<Boolean> edit(PmProcessDTO dto);

    /**
     * 根据id
     * @param processIds
     * @return
     */
    Map<Long, PmProcessVO> listById(List<Long> processIds);

    /**
     * 工序列表
     * @return
     */
    List<PmProcessVO> listData();

    /**
     * EXCEL数据导入
     * @param dto
     * @return
     * @throws IOException
     */
    Boolean excelImportData(PmExcelDTO dto) throws IOException;

    /**
     * 下载模板
     * @param response
     * @throws IOException
     */
    void download(HttpServletResponse response) throws IOException;

    /**
     * 工序excel导出
     * @param response
     * @throws IOException
     */
    void excelExportData(HttpServletResponse response) throws IOException;

}
