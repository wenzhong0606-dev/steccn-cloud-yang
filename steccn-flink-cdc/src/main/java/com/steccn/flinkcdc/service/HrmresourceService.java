package com.steccn.flinkcdc.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.vo.R;
import com.steccn.flinkcdc.constant.CdcConstant;
import com.steccn.flinkcdc.dto.DataChangeDTO;
import com.steccn.flinkcdc.dto.HrmresourceDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.oa.vo.OaDepartmentVO;
import com.steccn.oa.vo.OaJobTitleVO;
import com.steccn.platform.feign.RemoteMesFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * 处理泛微hrmresoruce
 *
 * @author AndrewLuo
 * @date 2023/05/26
 */
@Service
@RequiredArgsConstructor
public class HrmresourceService {
    private final OaInfoService oaInfoService;

    private final RemoteOfficeFeign remoteOfficeFeign;

    private final RemoteMesFeignApi remoteMesFeignApi;

    /**
     * 处理人员信息
     * @param dataChangeInfo
     * @return
     */
    public Boolean handleData(Integer operatorType,DataChangeDTO dataChangeInfo){
        String data = dataChangeInfo.getData();
        HrmresourceDTO hrmresourceDTO = JSONObject.parseObject(data, HrmresourceDTO.class);
        //新增
        if(operatorType.equals(CdcConstant.OperateType.INSERT.getNum())){
            String mesOfficeCode = findMesOfficeCode(hrmresourceDTO.getDepartmentid(),hrmresourceDTO.getJobtitle());
        }else if(operatorType.equals(CdcConstant.OperateType.UPDATE.getNum())){

        }
        return true;
    }

    /**
     * 找到mes对应的部门code
     * @param departmentId
     * @return
     */
    private String findMesOfficeCode(Integer departmentId,Integer jobTitleId){
        //默认部门是未分配
        String departmentCode = CdcConstant.UNALLOCATED_DEPARTMENT_CODE;
        OaDepartmentVO departmentVO = oaInfoService.getDepartmentInfoById(departmentId).getData();
        if(Objects.isNull(departmentVO)){
            return departmentCode;
        }
        //查看是否为2级节点
        while(!departmentVO.getTLevel().equals(CdcConstant.NUM_2)){
            departmentVO = oaInfoService.getDepartmentInfoById(departmentVO.getSuperId()).getData();
            if(Objects.isNull(departmentVO)){
                return departmentCode;
            }
        }
        //获取mes中的名字
        String mesNameByOaName = CdcConstant.OaMesDepartmentMapping.getMesNameByOaName(departmentVO.getDepartmentName());
        //如果是基片二车间则判断岗位
        if(departmentVO.getDepartmentName().equals(CdcConstant.OaMesDepartmentMapping.A.getOaName())){
            OaJobTitleVO jobInfoById = oaInfoService.getJobInfoById(jobTitleId).getData();
            if(Objects.nonNull(jobInfoById)){
                if(jobInfoById.getJobTitleName().equals(CdcConstant.JOB_TITLE_RONGHUA)){
                    mesNameByOaName = mesNameByOaName + "-"+jobInfoById.getJobTitleName();
                }
            }
        }

        //如果为空则返回
        if(StrUtil.isBlank(mesNameByOaName)){
            return departmentCode;
        }
        String finalMesNameByOaName = mesNameByOaName;
        Map<String, SysOfficeVO> stringSysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listByName(Sets.newHashSet(finalMesNameByOaName)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysOfficeVO sysOfficeVO= new SysOfficeVO();
        sysOfficeVO.setOfficeCode(departmentCode);
        departmentCode = stringSysOfficeVOMap.getOrDefault(mesNameByOaName,sysOfficeVO).getOfficeCode();
        return departmentCode;
    }
}
