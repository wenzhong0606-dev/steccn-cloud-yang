package com.steccn.sales.mapper;

import com.steccn.sales.domain.ProductProduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author Admin
* @description 针对表【product_product(Product Variant)】的数据库操作Mapper
* @createDate 2023-11-29 15:38:09
* @Entity com.steccn.sales.domain.ProductProduct
*/
@Mapper
public interface ProductProductMapper extends BaseMapper<ProductProduct> {

}




