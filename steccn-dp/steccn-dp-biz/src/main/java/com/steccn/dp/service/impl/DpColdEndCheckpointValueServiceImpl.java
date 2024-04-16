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
import com.steccn.oa.constant.OaConstant;
import com.steccn.platform.feign.RemoteDictFeignApi;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Map<String, String> codeNameMap = FeignOptional.of(() -> remoteDictFeignApi.getCheckPointName(Sets.newHashSet(dpColdEndCheckpointValues
                .stream().map(DpColdEndCheckpointValue::getCheckPointCode).collect(Collectors.toSet()))), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        dpColdEndCheckpointValues.forEach(tmp->{
            //查询各测点检查时间
            DpColdEndCheckpointValue one = getOne(new LambdaQueryWrapper<DpColdEndCheckpointValue>()
                    .eq(DpColdEndCheckpointValue::getCheckPointCode, tmp.getCheckPointCode())
                    .eq(DpColdEndCheckpointValue::getTimeStamp, tmp.getTimeStamp()).last("LIMIT 1"));
            DpColdEndCheckpointValueVO vo = new DpColdEndCheckpointValueVO();
            BeanUtil.copyProperties(one,vo);
            vo.setCheckPointName(codeNameMap.get(one.getCheckPointCode()));
            results.add(vo);
        });
        return results;
    }

    @Override
    public BigDecimal getScreenData(Integer num) {
        // 1 KLW 2 NOX 3 SO2
        BigDecimal result = BigDecimal.ZERO;
        String code = null;
        if(num.equals(OaConstant.NUM_1)){
            code = "OA.YR_TX.yibiao.KLW_INPUT";
        }else if(num.equals(OaConstant.NUM_2)){
            code = "OA.YR_TX.yibiao.NOX_INPUT";
        }else if (num.equals(OaConstant.NUM_3)){
            code = "OA.YR_TX.yibiao.SO2_INPUT";
        }
        DpColdEndCheckpointValue one = getOne(new LambdaQueryWrapper<DpColdEndCheckpointValue>()
                .select(DpColdEndCheckpointValue::getValue)
                .eq(DpColdEndCheckpointValue::getCheckPointCode, code)
                .orderByDesc(DpColdEndCheckpointValue::getTimeStamp)
                .last("LIMIT 1"));
        result = one.getValue();
        return result;
    }

    @Override
    public Boolean deleteData(LocalDateTime date) {
        dpColdEndCheckpointValueMapper.deleteData(date);
        return true;
    }
}




