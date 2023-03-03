package com.steccn.platform.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.platform.domain.SysCustomerMaterial;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.platform.dto.SysCustomerMaterialDTO;
import com.steccn.platform.vo.SysCustomerMaterialVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author yequan
* @description 针对表【sys_customer_material(客户物料表)】的数据库操作Mapper
* @createDate 2023-03-03 10:04:01
* @Entity com.steccn.platform.domain.SysCustomerMaterial
*/
@Mapper
public interface SysCustomerMaterialMapper extends BaseMapper<SysCustomerMaterial> {

    Page<SysCustomerMaterialVO> pageList(@Param("page") Page<SysCustomerMaterialVO> page , @Param("dto") SysCustomerMaterialDTO dto);
}




