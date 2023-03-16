package com.steccn.qms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.pms.dto.CustomerComplainPageDTO;
import com.steccn.qms.domain.QtCustomerComplain;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.qms.dto.QtCustomerComplainDTO;
import com.steccn.qms.vo.QtCustomerComplainVO;

/**
* @author yequan
* @description 针对表【qms_customer_complain(客诉记录表)】的数据库操作Service
* @createDate 2023-03-15 10:16:54
*/
public interface QtCustomerComplainService extends IService<QtCustomerComplain> {

    /**
     * 新增客诉记录
     * @param dto
     * @return
     */
    Boolean add(QtCustomerComplainDTO dto);


    /**
     * 客诉记录列表
     * @param dto
     * @return
     */
    Page<QtCustomerComplainVO> listPage(CustomerComplainPageDTO dto);


    /**
     * 获取详情内容
     * @param id
     * @return
     */
    QtCustomerComplainVO getInfo(Long id);

    /**
     * 编辑
     * @param qtCustomerComplainDTO
     * @return
     */
    Boolean edit(QtCustomerComplainDTO qtCustomerComplainDTO);
}
