package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.IotTrendGroupDetail;
import com.steccn.platform.dto.IotTrendDetailDTO;
import com.steccn.platform.service.IotTrendGroupDetailService;
import com.steccn.platform.mapper.IotTrendGroupDetailMapper;
import com.steccn.platform.vo.IotTrendDetailVO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
* @author Admin
* @description 针对表【iot_trend_group_detail(趋势组详情表)】的数据库操作Service实现
* @createDate 2025-11-12 16:06:04
*/
@Service
public class IotTrendGroupDetailServiceImpl extends ServiceImpl<IotTrendGroupDetailMapper, IotTrendGroupDetail>
    implements IotTrendGroupDetailService{

    @Override
    public Boolean saveInfo(List<IotTrendDetailDTO> dto) {
        //1、删除信息
        remove(new LambdaQueryWrapper<IotTrendGroupDetail>().eq(IotTrendGroupDetail::getTrendId,dto.get(0).getTrendId()));
        //2、保存
        List<IotTrendGroupDetail> model = BeanUtil.copyToList(dto, IotTrendGroupDetail.class);
        saveBatch(model);
        return true;
    }

    @Override
    public List<IotTrendDetailVO> infos(Integer id) {
        List<IotTrendGroupDetail> list = list(new LambdaQueryWrapper<IotTrendGroupDetail>()
                .eq(IotTrendGroupDetail::getTrendId, id)
                .orderByDesc(IotTrendGroupDetail::getSort));
        List<IotTrendDetailVO> results = BeanUtil.copyToList(list, IotTrendDetailVO.class);
        return results;
    }
}




