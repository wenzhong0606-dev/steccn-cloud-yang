package com.steccn.qms.service;

import com.steccn.qms.domain.QtCheckTaskCopy;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.vo.QtCheckTaskCopyVO;

import java.time.LocalDate;
import java.util.List;

/**
* @author 86178
* @description 针对表【qt_check_task_copy】的数据库操作Service
* @createDate 2022-11-10 16:25:47
*/
public interface QtCheckTaskCopyService extends IService<QtCheckTaskCopy> {

    List<QtCheckTaskCopyVO> getLocalDateList();

}
