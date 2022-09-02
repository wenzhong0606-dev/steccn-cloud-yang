package com.steccn.qms.service;

import com.steccn.qms.domain.QtCheckItem;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author yequan
* @description 针对表【qt_check_item(检验项表)】的数据库操作Service
* @createDate 2022-09-02 08:54:09
*/
public interface QtCheckItemService extends IService<QtCheckItem> {

    String removeMy(Long id);
}
