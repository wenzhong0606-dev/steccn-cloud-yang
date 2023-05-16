package com.steccn.qms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.oa.dto.OaGroupDTO;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.qms.constant.QtManagerConstant;
import com.steccn.qms.domain.QtCheckItem;
import com.steccn.qms.domain.QtCheckTask;
import com.steccn.qms.domain.QtCheckTaskDetail;
import com.steccn.qms.dto.QtCheckTaskDtDTO;
import com.steccn.qms.mapper.QtCheckTaskMapper;
import com.steccn.qms.service.QtCheckItemService;
import com.steccn.qms.service.QtCheckTaskDtService;
import com.steccn.qms.service.QtCheckTaskService;
import com.steccn.qms.vo.QtCheckTaskDtVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数字孪生实现类
 *
 * @author AndrewLuo
 * @date 2023/05/16
 */
@Service
@RequiredArgsConstructor
public class QtCheckTaskDtServiceImpl implements QtCheckTaskDtService {

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final QtCheckItemService qtCheckItemService;

    private final QtCheckTaskMapper qtCheckTaskMapper;
    @Override
    public List<SysOfficeVO> getOfficeList() {
        SysOfficeTypeKeyDTO dto = new SysOfficeTypeKeyDTO();
        dto.setType(Lists.newArrayList(QtManagerConstant.NUM_9.toString()));
        dto.setKey(QtManagerConstant.JIPIAN);
        List<SysOfficeVO> sysOfficeVOS = FeignOptional.of(() -> remoteOfficeFeign.getListByTypeAndKey(dto), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        sysOfficeVOS = sysOfficeVOS.stream().sorted(Comparator.comparing(SysOfficeVO::getTreeSort)).collect(Collectors.toList());
        return sysOfficeVOS;
    }

    @Override
    public Map<String, List<QtCheckTaskDtVO>> getDtData(QtCheckTaskDtDTO dto) {
        Map<String, List<QtCheckTaskDtVO>> result = Maps.newHashMap();
        //获取officeId和String
        Map<Integer,String> officeMap = FeignOptional.of(() -> remoteOfficeFeign.listByIdCh(dto.getProductLineId()), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        //先找到需要的数据 (横向,纵向,毛板宽)
        List<QtCheckItem> qtCheckItems = qtCheckItemService.list(new LambdaQueryWrapper<QtCheckItem>()
                .select(QtCheckItem::getId, QtCheckItem::getName)
                .likeRight(QtCheckItem::getName, QtManagerConstant.HENGXIANG)
                .or().likeRight(QtCheckItem::getName, QtManagerConstant.ZONGXIANG)
                .or().eq(QtCheckItem::getName, QtManagerConstant.MAOBAN));
        dto.getProductLineId().forEach(tmp -> {
            List<QtCheckTaskDtVO> value = Lists.newArrayList();
            //开始查找车间下数据
            for(int i = 0; i <= QtManagerConstant.NUM_24;i++){
                String itemName;
                if(i==0){
                    itemName = QtManagerConstant.MAOBAN;
                }else if(i<QtManagerConstant.NUM_11){
                    itemName = QtManagerConstant.HENGXIANG + i;
                }else {
                    itemName = QtManagerConstant.ZONGXIANG + i;
                }
                //收集该检验项相关数据
                List<Integer> itemId = qtCheckItems.stream().filter(v->v.getName().equals(itemName))
                        .map(QtCheckItem::getId).collect(Collectors.toList());
                //关联查询数据
                if(CollUtil.isNotEmpty(itemId)){
                    MPJLambdaWrapper mpjLambdaWrapper = new MPJLambdaWrapper<QtCheckTaskDtVO>()
                            .selectAs(QtCheckTaskDetail::getCheckResult,QtCheckTaskDtVO::getValue)
                            .leftJoin(QtCheckTaskDetail.class,QtCheckTaskDetail::getTaskId,QtCheckTask::getId)
                            .isNotNull(QtCheckTaskDetail::getCheckResult)
                            .ne(QtCheckTaskDetail::getCheckResult,"")
                            .ne(QtCheckTaskDetail::getCheckResult,"-")
                            .eq(QtCheckTask::getStatus,QtManagerConstant.QtCheckTaskStatusEnum.FINISHED.getCode())
                            .in(QtCheckTaskDetail::getItemId,itemId)
                            .apply("JSON_CONTAINS(t.product_line, JSON_OBJECT('id', {0}))",tmp)
                            .orderByDesc(QtCheckTaskDetail::getUpdateTime)
                            .last("LIMIT 1");
                    QtCheckTaskDtVO qtCheckTaskDtVO = qtCheckTaskMapper.selectJoinOne(QtCheckTaskDtVO.class, mpjLambdaWrapper);
                    //判断是否有值
                    if(Objects.isNull(qtCheckTaskDtVO)){
                        QtCheckTaskDtVO noValue = new QtCheckTaskDtVO();
                        noValue.setItemName(itemName);
                        value.add(noValue);
                    }else {
                        qtCheckTaskDtVO.setItemName(itemName);
                        value.add(qtCheckTaskDtVO);
                    }
                }
            }
            result.put(officeMap.get(tmp),value);
        });
        return result;
    }
}
