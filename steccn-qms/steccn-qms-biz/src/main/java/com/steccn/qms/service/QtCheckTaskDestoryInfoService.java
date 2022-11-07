package com.steccn.qms.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.domain.QtCheckTaskDestoryInfo;
import com.steccn.qms.dto.QtCheckTaskDestoryInfoDTO;
import com.steccn.qms.vo.QtCheckTaskDestoryInfoVO;

import java.util.List;

/**
* @author 86187
* @description 针对表【qt_check_task_destory_info】的数据库操作Service
* @createDate 2022-11-03 10:29:18
*/
public interface QtCheckTaskDestoryInfoService extends IService<QtCheckTaskDestoryInfo> {

    //添加落板记录单条
    Boolean add(QtCheckTaskDestoryInfoDTO dto);
    //删除落板记录
    Boolean delete(QtCheckTaskDestoryInfoDTO dto);
    //查询落板记录
    List<QtCheckTaskDestoryInfoVO> select(QtCheckTaskDestoryInfoDTO dto);
    //添加落板记录多条
    Boolean addList(List<QtCheckTaskDestoryInfoDTO> dtoList);
}
