package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.platform.domain.OaResponse;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.*;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.OaMaterialVO;

/**
* @author Admin
* @description 针对表【oa_response】的数据库操作Service
* @createDate 2022-12-26 13:10:08
*/
public interface OaResponseService extends IService<OaResponse> {

    /**
     * 新流程推送接口
     * @param dto
     * @return
     */
    Boolean addProcessPush(OaProcessPushDTO dto);

    /**
     * 新消息推送接口
     * @param dto
     * @return
     */
    Boolean addMessagePush(OaMessagePushDTO dto);

    /**
     * 流程审批返回接口
     * @param dto
     * @return
     */
    Boolean addProcessNotify(OaProcessNotifyDTO dto);

    /**
     * OA查询物料分页
     *
     * @param dto
     * @return
     */
    Page<OaMaterialVO> listPage(OaMaterialDTO dto);
}
