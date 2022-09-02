package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.qms.domain.QtCheckItem;
import com.steccn.qms.service.QtCheckItemService;
import com.steccn.qms.mapper.QtCheckItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yequan
 * @description 针对表【qt_check_item(检验项表)】的数据库操作Service实现
 * @createDate 2022-09-02 08:54:09
 */
@Service
public class QtCheckItemServiceImpl extends ServiceImpl<QtCheckItemMapper, QtCheckItem>
        implements QtCheckItemService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String removeMy(Long id) {
        //事务验证没有问题
        removeById(1);
        int i = 1 / 0;
        removeById(2);
        return "success";
    }
}




