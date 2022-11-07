package com.steccn.pms.service;

import com.steccn.pms.domain.PmsPullForce;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.dto.PmsProducePlanDetailsDTO;
import com.steccn.pms.dto.PmsPullForceDTO;
import com.steccn.pms.dto.PmsPullForceDetailsDTO;
import com.steccn.pms.dto.PmsPullForceEditDTO;
import com.steccn.pms.vo.PmsPullForceDetailsVO;
import com.steccn.pms.vo.PmsPullForceVO;

import java.util.List;

/**
* @author yequan
* @description 针对表【pms_pull_force(拉引力表)】的数据库操作Service
* @createDate 2022-11-02 10:10:08
*/
public interface PmsPullForceService extends IService<PmsPullForce> {

    /**
     * 拉引量列表
     *
     * @param dto
     * @return
     */
    List<PmsPullForceVO> list(PmsPullForceDTO dto);

    /**
     * 拉引量详情
     *
     * @param dto
     * @return
     */
    PmsPullForceDetailsVO details(PmsPullForceDetailsDTO dto);

    /**
     * 拉引量批量编辑
     *
     * @param dtoList
     * @return
     */
    Boolean editBatch(List<PmsPullForceEditDTO> dtoList);

}
