package com.steccn.pms.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.pms.domain.PmsMrp;
import com.steccn.pms.dto.PmsMrpPageDTO;
import com.steccn.pms.service.PmsMrpService;
import com.steccn.pms.mapper.PmsMrpMapper;
import com.steccn.pms.vo.PmsMrpVO;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【pms_mrp(物料需求计划)】的数据库操作Service实现
* @createDate 2022-11-02 10:10:08
*/
@Service
public class PmsMrpServiceImpl extends ServiceImpl<PmsMrpMapper, PmsMrp>
    implements PmsMrpService{

    @Override
    public Page<PmsMrpVO> pageList(PmsMrpPageDTO dto) {
        return null;
    }
}




