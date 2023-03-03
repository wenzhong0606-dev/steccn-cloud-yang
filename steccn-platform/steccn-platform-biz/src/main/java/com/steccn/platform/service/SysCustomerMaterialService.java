package com.steccn.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.common.constant.ResultCode;
import com.steccn.platform.domain.SysCustomerMaterial;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.platform.dto.SysCustomerMaterialDTO;
import com.steccn.platform.vo.SysCustomerMaterialVO;

/**
* @author yequan
* @description 针对表【sys_customer_material(客户物料表)】的数据库操作Service
* @createDate 2023-03-03 10:04:02
*/
public interface SysCustomerMaterialService extends IService<SysCustomerMaterial> {

    /**
     * 分页列表
     * @param dto dto
     * @return 分页数据
     */
    Page<SysCustomerMaterialVO> pageList(SysCustomerMaterialDTO dto);

    /**
     * 根据客户物料主键查询客户物料明细数据
     * @param id 客户物料主键
     * @return
     */
    SysCustomerMaterialVO info(Long id);
}
