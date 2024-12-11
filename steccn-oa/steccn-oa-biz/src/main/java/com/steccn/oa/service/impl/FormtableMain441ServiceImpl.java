package com.steccn.oa.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.steccn.oa.domain.FormtableMain441;
import com.steccn.oa.domain.FormtableMain441Dt1;
import com.steccn.oa.service.FormtableMain441Dt1Service;
import com.steccn.oa.service.FormtableMain441Service;
import com.steccn.oa.mapper.FormtableMain441Mapper;
import com.steccn.oa.service.WorkflowRequestlogService;
import com.steccn.oa.vo.OaFormTableDT441VO;
import com.steccn.oa.vo.OaFormTableMain441VO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author Admin
* @description 针对表【formtable_main_441】的数据库操作Service实现
* @createDate 2024-12-11 10:11:25
*/
@Service
@RequiredArgsConstructor
public class FormtableMain441ServiceImpl extends ServiceImpl<FormtableMain441Mapper, FormtableMain441>
    implements FormtableMain441Service{
    private final WorkflowRequestlogService workflowRequestlogService;
    private final FormtableMain441Dt1Service formtableMain441Dt1Service;

    @Override
    public List<OaFormTableMain441VO> synPmsPlan(String yearMonth) {
        List<OaFormTableMain441VO> results = Lists.newArrayList();
        List<FormtableMain441> list = list(new LambdaQueryWrapper<FormtableMain441>()
                .eq(FormtableMain441::getFhyf, yearMonth));
        for(FormtableMain441 tmp:list){
            Boolean finished = workflowRequestlogService.workflowFinished(tmp.getRequestid());
            if(!finished){
                continue;
            }
            List<FormtableMain441Dt1> detail = formtableMain441Dt1Service.list(new LambdaQueryWrapper<FormtableMain441Dt1>()
                    .eq(FormtableMain441Dt1::getMainid, tmp.getId()));
            List<OaFormTableDT441VO> details = BeanUtil.copyToList(detail,OaFormTableDT441VO.class);
            OaFormTableMain441VO result = new OaFormTableMain441VO();
            BeanUtil.copyProperties(tmp,result);
            result.setDetailList(details);
            results.add(result);
        }
        return results;
    }
}




