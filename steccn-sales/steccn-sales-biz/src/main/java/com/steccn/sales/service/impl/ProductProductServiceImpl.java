package com.steccn.sales.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.sales.domain.ProductProduct;
import com.steccn.sales.service.ProductProductService;
import com.steccn.sales.mapper.ProductProductMapper;
import org.springframework.stereotype.Service;

/**
* @author Admin
* @description 针对表【product_product(Product Variant)】的数据库操作Service实现
* @createDate 2023-11-29 15:38:09
*/
@Service
public class ProductProductServiceImpl extends ServiceImpl<ProductProductMapper, ProductProduct>
    implements ProductProductService{

}




