package com.steccn.platform.service.impl;

import com.steccn.platform.dto.OaProcessNotifyDTO;
import com.steccn.platform.service.OaProcessHandler;
import com.steccn.platform.strategy.AbstractOaProcessStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * @author yequan
 * @desc OA流程处理
 * @date 2023/4/12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OaProcessHandlerImpl implements OaProcessHandler {

    private final List<AbstractOaProcessStrategy> strategyList;

    @Override
    public void handler(OaProcessNotifyDTO dto) {
        strategyList.forEach(ele -> {
            ele.execute(dto);
        });
    }
}
