package com.steccn.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.steccn.wms.domain.WmsCheckOrder;
import com.steccn.wms.dto.WmsCheckOrderDTO;
import com.steccn.wms.vo.WmsCheckOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author W10
* @description 针对表【wms_check_order(盘点单)】的数据库操作Mapper
* @createDate 2022-11-30 09:45:17
* @Entity com.steccn.wms.domain.WmsCheckOrder
*/
@Mapper
public interface WmsCheckOrderMapper extends BaseMapper<WmsCheckOrder> {


    List<WmsCheckOrderVO> getCheckOrderList(Page<WmsCheckOrderDTO> iPage, @Param("form") WmsCheckOrderDTO dto);


}




