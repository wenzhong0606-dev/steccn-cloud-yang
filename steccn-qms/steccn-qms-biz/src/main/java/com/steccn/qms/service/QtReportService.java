package com.steccn.qms.service;

import com.steccn.common.vo.R;
import com.steccn.qms.domain.QtReport;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtAddReportDTO;
import com.steccn.qms.vo.QtCheckItemListVO;

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
    Integer add(QtAddReportDTO dto);

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
}
