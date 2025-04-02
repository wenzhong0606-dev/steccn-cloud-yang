package com.steccn.dp.service;

import com.steccn.dp.domain.RawNum;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Admin
* @description 针对表【raw_num(原料每天的值)】的数据库操作Service
* @createDate 2025-04-02 14:11:41
*/
public interface RawNumService extends IService<RawNum> {
    Boolean test();

    /**
     * 插入当天的值
     * @return
     */
    Boolean insertToday();
}
