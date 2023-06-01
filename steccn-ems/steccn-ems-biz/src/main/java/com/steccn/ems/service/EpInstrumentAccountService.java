package com.steccn.ems.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.SteccnTableAssertUtil;
import com.steccn.ems.domain.EpInstrumentAccount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.ems.dto.*;
import com.steccn.ems.vo.EpInstrumentAccountPageVO;
import com.steccn.ems.vo.EpInstrumentAccountVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author yequan
* @description 针对表【ep_instrument_account(仪表台账)】的数据库操作Service
* @createDate 2022-11-29 19:27:28
*/
public interface EpInstrumentAccountService extends IService<EpInstrumentAccount> {

    /**
     * 新增仪表台账
     * @param dto
     * @return
     */
    Boolean addOrEdit(EpInstrumentAccountDTO dto);

    /**
     * 根据仪表名称模糊查询以及所属分区节点id查询分页列表
     * @param dto
     * @return
     */
    Page<EpInstrumentAccountPageVO> getEpInstrumentAccountVOPage(EpInstrumentAccountPageDTO dto);

    /**
     * 根据仪表类型(即能源类型)获取所有未删除仪表所属分区节点id
     * @return
     */
    List<Long> getPartitionNodes (Integer energyType);

    /**
     * 修改仪表台账状态
     * @param dto
     * @return
     */
    Boolean reviseStatus(EpInstrumentAccountStatusDTO dto);

    /**
     * 根据id查询仪表台账详情
     * @param id
     * @return
     */
    EpInstrumentAccountVO getEpInstrumentAccountVO(Integer id);

    /**
     * 根据id删除仪表台账
     * @param id
     * @return
     */
    Boolean delete(Integer id);

    /**
     * 获取能耗分区节点挂载的仪表
     * @param partitionNodeIdSet 能耗分区节点id
     * @return
     */
    Map<Long, EpInstrumentAccount> listById(Set<Long> partitionNodeIdSet);

    /**
     * 下载资产卡片图片
     * @param response
     * @param request
     */
//    void downloadSubstrate(HttpServletResponse response, HttpServletRequest request)throws Exception;

    /**
     * 查询节点关联仪表
     * @param energyType 能源类型
     * @return
     */
    Map<Long, EpInstrumentAccountDTO> queryNodeLinkMeter(Integer energyType);

}
