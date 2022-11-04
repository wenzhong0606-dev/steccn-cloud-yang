package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.pms.domain.PmsMrp;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.pms.domain.PmsProducePlan;
import com.steccn.pms.dto.PmsMrpPageDTO;
import com.steccn.pms.vo.PmsMrpVO;

import java.util.List;

/**
* @author yequan
* @description 针对表【pms_mrp(物料需求计划)】的数据库操作Service
* @createDate 2022-11-02 10:10:08
*/
public interface PmsMrpService extends IService<PmsMrp> {

    /**
     * 物料需求计划分页列表
     * @param dto
     * @return
     */
    Page<PmsMrpVO> pageList(PmsMrpPageDTO dto);

    /**
     * 添加物料需求计划信息
     * @param list
     * @return
     */
    Boolean add(List<PmsMrp> list);
}
