package com.steccn.platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.steccn.platform.constant.OaDataConstant;
import com.steccn.platform.domain.OaResponse;
import com.steccn.platform.domain.SysEqpSpare;
import com.steccn.platform.dto.OaMaterialDTO;
import com.steccn.platform.dto.OaMessagePushDTO;
import com.steccn.platform.dto.OaProcessNotifyDTO;
import com.steccn.platform.dto.OaProcessPushDTO;
import com.steccn.platform.feign.RemoteMesFeignApi;
import com.steccn.platform.service.OaResponseService;
import com.steccn.platform.mapper.OaResponseMapper;
import com.steccn.platform.service.SysEqpSpareService;
import com.steccn.platform.vo.EqpSpareVO;
import com.steccn.platform.vo.MesClassificationVO;
import com.steccn.platform.vo.OaMaterialVO;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author Admin
* @description 针对表【oa_response】的数据库操作Service实现
* @createDate 2022-12-26 13:10:08
*/
@Service
@RequiredArgsConstructor
public class OaResponseServiceImpl extends ServiceImpl<OaResponseMapper, OaResponse>
    implements OaResponseService{

    private final SysEqpSpareService sysEqpSpareService;

    private final RemoteMesFeignApi remoteMesFeignApi;

    @Override
    public Boolean addProcessPush(OaProcessPushDTO dto) {
        OaResponse model = new OaResponse();
        model.setUniqueValue(dto.getUniqueValue());
        model.setType(OaDataConstant.Oa_Data_Type.PROCESS.getCode());
        JSONObject jsonObject = new JSONObject(dto);
        model.setContentJson(jsonObject.toString());
        return save(model);
    }

    @Override
    public Boolean addMessagePush(OaMessagePushDTO dto) {
        OaResponse model = new OaResponse();
        model.setUniqueValue(dto.getUniqueValue());
        model.setType(OaDataConstant.Oa_Data_Type.MESSAGE.getCode());
        JSONObject jsonObject = new JSONObject(dto);
        model.setContentJson(jsonObject.toString());
        return save(model);
    }

    @Override
    public Boolean addProcessNotify(OaProcessNotifyDTO dto) {
        OaResponse model = new OaResponse();
        model.setUniqueValue(dto.getUniqueValue());
        model.setType(OaDataConstant.Oa_Data_Type.NOTIFY.getCode());
        if(dto.getCreationTime()!=null){
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            dto.setCreationTimeStr(dto.getCreationTime().format(fmt));
        }
        JSONObject jsonObject = new JSONObject(dto);
        model.setContentJson(jsonObject.toString());
        return save(model);
    }

    @Override
    public Page<OaMaterialVO> listPage(OaMaterialDTO dto) {
        //1.自定义查询条件
        LambdaQueryWrapper<SysEqpSpare> condition = new LambdaQueryWrapper<SysEqpSpare>()
                .like(StrUtil.isNotBlank(dto.getKey()), SysEqpSpare::getName, dto.getKey())
                //如果传了值则是该时间之后的值
                .ge(dto.getUpdateTime()!=null,SysEqpSpare::getUpdateDate,dto.getUpdateTime());
        //2.定义分页对象
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        //3.查询分页数据
        IPage<SysEqpSpare> resPage = sysEqpSpareService.page(page, condition);
        //4.拷贝page信息
        BeanUtil.copyProperties(resPage, page);
        List<SysEqpSpare> records = resPage.getRecords();
        //5.处理list的数据
        List<OaMaterialVO> eqpSpareVOList = Lists.newArrayList();
        if(CollUtil.isNotEmpty(records)){
            //搜集分类id的数据
            Set<String> classIds = new HashSet<>();
            records.forEach(tmp -> {
                classIds.add(tmp.getType());
            });
            Map<String, String> classInfoById = remoteMesFeignApi.getClassInfoById(classIds);
            //再次遍历加入数据
            records.forEach(tmp -> {
                OaMaterialVO resultVO = new OaMaterialVO();
                resultVO.setMaterialCode(tmp.getCode());
                resultVO.setMaterialName(tmp.getName());
                resultVO.setUpdateTime(tmp.getUpdateDate());
                if(classInfoById.containsKey(tmp.getType())){
                    resultVO.setCategoryName(classInfoById.get(tmp.getType()));
                }else {
                    resultVO.setCategoryName("");
                }
                eqpSpareVOList.add(resultVO);
            });
        }
        return page.setRecords(eqpSpareVOList);
    }
}




