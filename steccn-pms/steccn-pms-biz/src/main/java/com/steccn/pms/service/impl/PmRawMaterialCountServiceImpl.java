package com.steccn.pms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.steccn.common.dto.TupleDTO;
import com.steccn.pms.domain.PmRawMaterialCount;
import com.steccn.pms.service.PmRawMaterialCountService;
import com.steccn.pms.mapper.PmRawMaterialCountMapper;
import com.steccn.pms.vo.PmRawMaterialVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
* @author Admin
* @description 针对表【pm_raw_material_count(原料配料统计表)】的数据库操作Service实现
* @createDate 2023-12-19 16:18:55
*/
@Service
public class PmRawMaterialCountServiceImpl extends ServiceImpl<PmRawMaterialCountMapper, PmRawMaterialCount>
    implements PmRawMaterialCountService{

    @Override
    public List<PmRawMaterialVO> getListByDate(TupleDTO<LocalDate,LocalDate> dto) {
        List<PmRawMaterialCount> list = list(new LambdaQueryWrapper<PmRawMaterialCount>().between(PmRawMaterialCount::getClassDate,dto.getField1(),dto.getField2()));
        List<PmRawMaterialVO> results = Lists.newArrayList();
        if(CollUtil.isNotEmpty(list)){
            list.forEach(tmp->{
                PmRawMaterialVO vo = new PmRawMaterialVO();
                BeanUtil.copyProperties(tmp,vo);
                results.add(vo);
            });
        }
        return results;
    }
}




