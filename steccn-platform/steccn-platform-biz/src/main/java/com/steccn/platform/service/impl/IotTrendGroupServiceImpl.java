package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.platform.domain.IotTrendGroup;
import com.steccn.platform.domain.IotTrendGroupDetail;
import com.steccn.platform.mapper.IotTrendGroupDetailMapper;
import com.steccn.platform.service.IotTrendGroupService;
import com.steccn.platform.mapper.IotTrendGroupMapper;
import com.steccn.platform.vo.IotPointVO;
import com.steccn.platform.vo.IotTrendGroupVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Admin
* @description 针对表【iot_trend_group(趋势组表)】的数据库操作Service实现
* @createDate 2025-11-12 14:02:30
*/
@Service
@RequiredArgsConstructor
public class IotTrendGroupServiceImpl extends ServiceImpl<IotTrendGroupMapper, IotTrendGroup>
    implements IotTrendGroupService{
    private final IotTrendGroupDetailMapper iotTrendGroupDetailMapper;

    @Override
    public Boolean add(String name) {
        IotTrendGroup model = new IotTrendGroup();
        model.setName(name);
        save(model);
        return true;
    }

    @Override
    public List<IotTrendGroupVO> IotTrendGroup() {
        List<IotTrendGroup> list = list();
        List<IotTrendGroupVO> iotPointVOS = BeanUtil.copyToList(list, IotTrendGroupVO.class);
        return iotPointVOS;
    }

    @Override
    public Boolean deleteGroup(Integer id) {
        removeById(id);
        iotTrendGroupDetailMapper.delete(new LambdaQueryWrapper<IotTrendGroupDetail>()
                .eq(IotTrendGroupDetail::getTrendId,id));
        return true;
    }
}




