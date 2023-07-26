package com.steccn.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.steccn.authserver.exception.BusinessException;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.domain.Hrmresource;
import com.steccn.oa.domain.KqGroup;
import com.steccn.oa.domain.KqGroupmember;
import com.steccn.oa.dto.OaRosterRecordDTO;
import com.steccn.oa.mapper.HrmresourceMapper;
import com.steccn.oa.mapper.KqGroupMapper;
import com.steccn.oa.mapper.KqGroupmemberMapper;
import com.steccn.oa.service.HrmClassInfoService;
import com.steccn.oa.service.OaService;
import com.steccn.oa.vo.OaClassVO;
import com.steccn.oa.vo.OaRosterRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 类说明
 *
 * @author AndrewLuo
 * @date 2023/07/26
 */
@Service
@RequiredArgsConstructor
public class HrmClassInfoServiceImpl implements HrmClassInfoService {
    private final HrmresourceMapper hrmresourceMapper;
    private final KqGroupmemberMapper kqGroupmemberMapper;
    private final KqGroupMapper kqGroupMapper;
    private final OaService oaService;

    @Override
    public OaClassVO getClassByUserId(TupleDTO<LocalDate, Integer> dto) {
        if(Objects.isNull(dto.getField1())||Objects.isNull(dto.getField2())){
            throw new BusinessException("必传值不能为空");
        }
        OaClassVO result = new OaClassVO();
        //先查询人相关id
        Hrmresource hrmresource = hrmresourceMapper.selectOne(new LambdaQueryWrapper<Hrmresource>()
                .select(Hrmresource::getLastname, Hrmresource::getJobtitle,Hrmresource::getWorkcode)
                .eq(Hrmresource::getId, dto.getField2()).last("LIMIT 1"));
        if(Objects.isNull(hrmresource)){
            throw new BusinessException("该人员不存在");
        }
        result.setUserId(dto.getField2());
        result.setUserName(hrmresource.getLastname());
        //班组信息
        KqGroupmember groupId = kqGroupmemberMapper.selectOne(new LambdaQueryWrapper<KqGroupmember>()
                .select(KqGroupmember::getGroupid)
                .isNull(KqGroupmember::getIsdelete)
                .eq(KqGroupmember::getTypevalue, dto.getField2())
                .last("LIMIT 1"));
        if(Objects.nonNull(groupId)){
            KqGroup kqGroup = kqGroupMapper.selectOne(new LambdaQueryWrapper<KqGroup>()
                    .select(KqGroup::getGroupname)
                    .eq(KqGroup::getId, groupId.getGroupid())
                    .last("LIMIT 1"));
            if(Objects.nonNull(kqGroup)){
                result.setClassGroup(kqGroup.getGroupname());
            }
        }
        if(Objects.nonNull(hrmresource.getJobtitle())){
            OaRosterRecordDTO recordDTO = new OaRosterRecordDTO();
            recordDTO.setStartdate(dto.getField1().toString());
            recordDTO.setEnddate(dto.getField2().toString());
            recordDTO.setJobtitle(hrmresource.getJobtitle().toString());
            List<OaRosterRecordVO> rosterRecord = oaService.getRosterRecord(recordDTO);
            OaRosterRecordVO oaRosterRecordVO = rosterRecord.stream().filter(v -> v.getWorkcode().equals(hrmresource.getWorkcode())).findFirst().orElse(null);
            if(Objects.nonNull(oaRosterRecordVO)){
                result.setClassGroup(oaRosterRecordVO.getSerial());
            }
        }
        return result;
    }
}
