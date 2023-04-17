package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.domain.QtDealWay;
import com.steccn.qms.dto.QtDealWayDTO;
import com.steccn.qms.vo.QtDealWayVO;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【qms_deal_way(处理方式表)】的数据库操作Service
 * @createDate 2023-03-15 10:16:54
 */
public interface QtDealWayService extends IService<QtDealWay> {

    /**
     * 处理方式-列表
     *
     * @param type 类型
     * @return
     */
    List<QtDealWayVO> wayList(Integer type);

    /**
     * 处理方式-保存
     *
     * @param dto dto
     * @return
     */
    Boolean saveWay(QtDealWayDTO dto);

    /**
     * 处理方式-删除
     *
     * @param id 主键
     * @return
     */
    Boolean delete(Integer id);


    /**
     * 获取处理方式名称
     * @param ids
     * @return
     */
    Map<Integer,String> listByIdCh(Set<Integer> ids);
}
