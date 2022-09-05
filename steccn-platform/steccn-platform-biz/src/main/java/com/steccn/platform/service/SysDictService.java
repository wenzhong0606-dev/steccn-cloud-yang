package com.steccn.platform.service;

import com.setccn.platfrom.vo.SysDictVO;
import com.steccn.platform.domain.SysDict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author yequan
* @description 针对表【sys_dict(数据字典表)】的数据库操作Service
* @createDate 2022-09-02 08:54:09
*/
public interface SysDictService extends IService<SysDict> {

    /**
     * 根据数据字典code获取所有选项
     * @param type 类型
     * @return list
     */
    List<SysDictVO> sysList(String type);
}
