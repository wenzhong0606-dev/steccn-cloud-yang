package com.steccn.oa.service;

import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.vo.OaClassVO;

import java.time.LocalDate;

/**
 * 人员班组班次信息service
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
public interface HrmClassInfoService {
    /**
     * 根据用户id获取班次和班组
     * @param dto
     * @return
     */
    OaClassVO getClassByUserId(TupleDTO<LocalDate,Integer> dto);
}
