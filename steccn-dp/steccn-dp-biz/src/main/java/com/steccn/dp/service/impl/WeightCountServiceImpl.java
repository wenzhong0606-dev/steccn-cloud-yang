package com.steccn.dp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.dp.domain.WeightCount;
import com.steccn.dp.service.WeightCountService;
import com.steccn.dp.mapper.WeightCountMapper;
import org.springframework.stereotype.Service;

/**
* @author Admin
* @description 针对表【weight_count(生产工序表)】的数据库操作Service实现
* @createDate 2024-08-27 15:44:50
*/
@Service
public class WeightCountServiceImpl extends ServiceImpl<WeightCountMapper, WeightCount>
    implements WeightCountService{

}




