package com.steccn.platform.strategy;

import com.steccn.platform.dto.OaProcessNotifyDTO;

/**
 * @author yequan
 * @desc 抽象策略
 * @date 2023/4/12
 */
public abstract class AbstractOaProcessStrategy {

    /**
     * 执行方法
     *
     * @param dto
     */
    public abstract void execute(OaProcessNotifyDTO dto);

    /**
     * 支持这个策略
     *
     * @param dto dto
     * @return
     */
    public final boolean support(OaProcessNotifyDTO dto) {
        return dto.getData().getWorkflowId().equals(getWorkFlow());
    }

    /**
     * 获取这个workFlowId
     *
     * @return
     */
    public abstract String getWorkFlow();

}
