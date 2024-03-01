package com.steccn.dp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.dp.domain.DpColdEndCheckpointValue;
import com.steccn.dp.service.DpColdEndCheckpointValueService;
import com.steccn.dp.mapper.DpColdEndCheckpointValueMapper;
import com.steccn.dp.vo.DpColdEndCheckpointValueVO;
import com.steccn.platform.feign.RemoteDictFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
* @author Admin
* @description 针对表【dp_cold_end_checkpoint_value(冷端各线测点数据)】的数据库操作Service实现
* @createDate 2024-03-01 16:21:20
*/
@Service
@RequiredArgsConstructor
public class DpColdEndCheckpointValueServiceImpl extends ServiceImpl<DpColdEndCheckpointValueMapper, DpColdEndCheckpointValue>
    implements DpColdEndCheckpointValueService{
    private final DpColdEndCheckpointValueMapper dpColdEndCheckpointValueMapper;
    private final RemoteDictFeignApi remoteDictFeignApi;

    @Override
    public List<DpColdEndCheckpointValueVO> getColdEndValue() {
        List<DpColdEndCheckpointValueVO> results = Lists.newArrayList();
        //获取各测点最新的检查时间
        List<DpColdEndCheckpointValue> dpColdEndCheckpointValues = dpColdEndCheckpointValueMapper.selectLatestTime();
        Map<String, String> codeNameMap = FeignOptional.of(() -> remoteDictFeignApi.getCheckPointName(Sets.newHashSet(key)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        dpColdEndCheckpointValues.forEach(tmp->{
            //查询各测点检查时间
            DpColdEndCheckpointValue one = getOne(new LambdaQueryWrapper<DpColdEndCheckpointValue>()
                    .eq(DpColdEndCheckpointValue::getCheckPointCode, tmp.getCheckPointCode())
                    .eq(DpColdEndCheckpointValue::getTimestamp, tmp.getTimestamp()).last("LIMIT 1"));
            DpColdEndCheckpointValueVO vo = new DpColdEndCheckpointValueVO();
            BeanUtil.copyProperties(one,vo);
            vo.setCheckPointName(codeNameMap.get(one.getCheckPointCode()));
            results.add(vo);
        });
        return results;
    }
}




