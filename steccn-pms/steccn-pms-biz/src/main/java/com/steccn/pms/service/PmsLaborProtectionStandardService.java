package com.steccn.pms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.common.dto.SteccnTableDataDTO;
import com.steccn.pms.domain.PmsLaborProtectionStandard;
import com.steccn.pms.dto.PmsLaborStandardDTO;
import com.steccn.pms.vo.PmsLaborStandardVO;
import com.steccn.qms.dto.QtCheckTaskStatusDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author Admin
* @description 针对表【pms_labor_protection_standard(劳保标准表)】的数据库操作Service
* @createDate 2024-04-03 10:21:46
*/
public interface PmsLaborProtectionStandardService extends IService<PmsLaborProtectionStandard> {

    /**
     * 新建检验项
     *
     * @param dto
     * @return
     */
    Boolean add(PmsLaborStandardDTO dto);

    /**
     * 修改
     * @param dto
     * @return
     */
    Boolean updateData(PmsLaborStandardDTO dto);

    /**
     * 详情
     * @param id
     * @return
     */
    PmsLaborStandardVO info(Integer id);

    /**
     * 通过id批量删除
     * @param id
     * @return
     */
    Boolean batchDeleteById(Set<Integer> id);

    /**
     * 表单数据
     * @return
     */
    SteccnTableDataDTO<Map<String, String>> listInfo();

}
