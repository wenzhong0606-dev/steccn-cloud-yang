package com.steccn.platform.service;

import com.steccn.platform.vo.SysDictVO;
import com.steccn.platform.domain.SysDict;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

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

    /**
     * 过滤数据字典版本
     * @param type 数据字典type
     * @param name 数据字段name
     * @return
     */
    List<SysDictVO> sysListFilter(String type, String name) ;

    /**
     * 根据主键id查询所有的数据字典数据
     * @param idList 主键集合
     * @return map key为主键id，value为值
     */
    Map<Long, SysDictVO> listByIdList(List<Long> idList);

    /**
     * 是否存在
     * @param type 类型
     * @param id 主键id
     * @return 是否
     */
    Boolean exist(String type, Long id);

    SysDictVO getByCode(String code,String type);
}
