package com.steccn.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.sales.domain.ResPartner;
import com.steccn.sales.dto.KsSaleIdAndNameDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
* @author Admin
* @description 针对表【res_partner】的数据库操作Mapper
* @createDate 2023-11-29 15:28:25
* @Entity com.steccn.sales.domain.ResPartner
*/
@Mapper
public interface ResPartnerMapper extends BaseMapper<ResPartner> {
    /**
     * 获取采购员名称
     * @param saleIdSet 采购员id
     * @return
     */
    List<KsSaleIdAndNameDTO> selectSaleName(@Param("saleIdSet") Set<Integer> saleIdSet);
}




