package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.qms.domain.QmsDefectProduct;
import com.steccn.qms.service.QmsDefectProductService;
import com.steccn.qms.mapper.QmsDefectProductMapper;
import org.springframework.stereotype.Service;

/**
* @author yequan
* @description 针对表【qms_defect_product(不合格品处理表)】的数据库操作Service实现
* @createDate 2023-03-15 10:16:54
*/
@Service
public class QmsDefectProductServiceImpl extends ServiceImpl<QmsDefectProductMapper, QmsDefectProduct>
    implements QmsDefectProductService{

}




