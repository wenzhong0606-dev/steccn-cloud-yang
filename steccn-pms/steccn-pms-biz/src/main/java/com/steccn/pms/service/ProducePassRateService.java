package com.steccn.pms.service;

import com.steccn.pms.dto.ProducePassRateDTO;
import com.steccn.pms.vo.ProducePassRateVO;

import java.util.List;

/**
 * @author chenhuan
 * @date 2022/11/9 16:27
 */
public interface ProducePassRateService {

    /**
     * 根据类型获取合格率
     *
     * @param dto
     * @return
     */
    List<ProducePassRateVO> getProducePassRateGroup(ProducePassRateDTO dto);
}
