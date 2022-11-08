package com.steccn.qms.service;

import com.steccn.qms.domain.QtReportGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtReportGroupDTO;
import com.steccn.qms.vo.QtReportVO;

import java.util.List;

/**
* @author 86178
* @description 针对表【qt_report_sort】的数据库操作Service
* @createDate 2022-11-08 11:29:13
*/
public interface QtReportGroupService extends IService<QtReportGroup> {

    Boolean add(QtReportGroupDTO dto);

    /**
     * 根据分组id查询报表卡片列表
     * @param id
     * @return
     */
    List<QtReportVO> getQtReportList(Integer id);
}
