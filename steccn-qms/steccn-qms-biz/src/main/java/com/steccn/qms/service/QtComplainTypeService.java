package com.steccn.qms.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.qms.domain.QtComplainType;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtComplainTypeDTO;
import com.steccn.qms.vo.QtComplainTypeVO;

import java.util.List;

/**
* @author yequan
* @description 针对表【qt_complain_type(投诉类型)】的数据库操作Service
* @createDate 2023-03-15 10:16:53
*/
public interface QtComplainTypeService extends IService<QtComplainType> {

    /**
     * 投诉类型集合
     * @return
     */
    List<QtComplainTypeVO> typeList();

    /**
     * 保存投诉类型
     * @param dto dto
     * @return
     */
    Boolean saveType(QtComplainTypeDTO dto);
}
