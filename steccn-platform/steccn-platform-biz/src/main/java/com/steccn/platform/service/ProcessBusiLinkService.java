package com.steccn.platform.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.platform.domain.ProcessBusiLink;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.ProcessBusiLinkDTO;

/**
* @author W10
* @description 针对表【process_busi_link(流程业务关联)】的数据库操作Service
* @createDate 2023-04-03 14:19:42
*/
public interface ProcessBusiLinkService extends IService<ProcessBusiLink> {

    /**
     * 保存
     * @param processBusiLinkDTO
     * @return
     */
    Boolean add(ProcessBusiLinkDTO processBusiLinkDTO);


    /**
     * 根据流程实例id获取详情
     * @param instanceId
     * @return
     */
    ProcessBusiLinkDTO get(Integer instanceId);


    /**
     * 根据业务id获取详情
     * @param busiId
     * @return
     */
    ProcessBusiLinkDTO getByBusiId(Long busiId);

    /**
     * 根据业务id以及类型获取流程信息
     * @param busiId
     * @param busiType
     * @return
     */
    ProcessBusiLinkDTO getByTypeAndBusiId(Long busiId, Integer busiType);
}
