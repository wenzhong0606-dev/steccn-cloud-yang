package com.steccn.mes.service;

import com.steccn.common.dto.TupleDTO;
import com.steccn.mes.domain.JsSysOffice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.mes.dto.ErpCustomerDTO;
import com.steccn.mes.vo.MesCutomerVO;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author Admin
* @description 针对表【js_sys_office(组织机构表)】的数据库操作Service
* @createDate 2023-07-26 14:25:51
*/
public interface JsSysOfficeService extends IService<JsSysOffice> {

    /**
     * 同步客户
     * @param dto
     * @return
     */
    Boolean synchronousCustomer(ErpCustomerDTO dto);

    /**
     * 汉得获取客户数据
     * @param dto
     * @return
     */
    List<MesCutomerVO> getAllCustomer(ErpCustomerDTO dto);

    /**
     * 同步供应商
     * @param dto
     * @return
     */
    Boolean synchronousSupplier(ErpCustomerDTO dto);
}
