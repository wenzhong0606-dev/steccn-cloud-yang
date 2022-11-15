package com.steccn.qms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtChartTargetValue;
import com.steccn.qms.dto.QtChatTargetValueDTO;
import com.steccn.qms.service.QtChartTargetValueService;
import com.steccn.qms.mapper.QtChatTargetValueMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author AndrewLuo
* @description 针对表【qt_chat_target_value】的数据库操作Service实现
* @createDate 2022-11-08 18:34:41
*/
@Service
public class QtChartTargetValueServiceImpl extends ServiceImpl<QtChatTargetValueMapper, QtChartTargetValue>
    implements QtChartTargetValueService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(QtChatTargetValueDTO dto) {
        //先删除key相关数据
        remove(new LambdaQueryWrapper<QtChartTargetValue>().eq(QtChartTargetValue::getTarget,dto.getTarget()));
        //校验 如果是基片缺陷总数 深加工缺陷总数 数值为自然数
        if(dto.getTarget().equals(QtManagerConstant.QtChatTargetValue.JPQX.getCode()) || dto.getTarget().equals(QtManagerConstant.QtChatTargetValue.SJGQX.getCode())){
            if(dto.getValue().scale()!=0){
                throw new BusinessException("请输入自然数");
            }
            if(dto.getValue().intValue()<0){
                throw new BusinessException("请输入自然数");
            }
        }
        QtChartTargetValue model = new QtChartTargetValue();
        BeanUtils.copyProperties(dto,model);
        return save(model);
    }
}




