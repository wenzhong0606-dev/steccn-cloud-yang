package com.steccn.qms.service;

import com.steccn.qms.domain.QtReport;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtReportDTO;
import com.steccn.qms.vo.QtCheckItemListVO;
import com.steccn.qms.vo.QtReportVO;

import java.util.List;

/**
* @author 86178
* @description 针对表【qt_report】的数据库操作Service
* @createDate 2022-11-01 16:42:01
*/
public interface QtReportService extends IService<QtReport> {

    /**
     * 新建报表任务
     * @param dto
     * @return
     */
    Integer add(QtReportDTO dto);

    /**
     * 根据物料id以及检验类型选择检验分组
     * @param materialId
     * @param checkType
     * @return
     */
    List<Integer> getGroupIdList(Long materialId, Integer checkType);

    /**
     * 根据检验分组id选择检验项
     * @param groupid
     * @return
     */
    List<QtCheckItemListVO> getQtCheckItemList(Integer groupid);

    /**
     * 根据id删除spc控制图卡片
     * @param id
     * @return
     */
    Boolean reportDelete(Integer id);

    /**
     * 根据id查询Spc控制图卡片
     * @param id
     * @return
     */
    QtReportVO getQtReport(Integer id);

    /**
     * 编辑SPC报表
     * @param qtReportDTO
     * @return
     */
    Boolean edit(QtReportDTO qtReportDTO);
}
