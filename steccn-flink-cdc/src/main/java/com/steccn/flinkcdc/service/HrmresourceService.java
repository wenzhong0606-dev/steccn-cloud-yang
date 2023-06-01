package com.steccn.flinkcdc.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
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
import com.steccn.platform.dto.MesCompanyDTO;
import com.steccn.platform.dto.MesEmployeeDTO;
import com.steccn.platform.dto.MesOfficeDTO;
import com.steccn.platform.dto.MesUserDTO;
import com.steccn.platform.feign.RemoteMesFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
            MesUserDTO mesUserDTO = setUserDTO(hrmresourceDTO,true);
            String s = FeignOptional.of(() -> remoteMesFeignApi.saveUser(mesUserDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
            log.info("mes返回:",s);
        }else if(operatorType.equals(CdcConstant.OperateType.UPDATE.getNum())){
            //离职同步
            //修改前数据
            HrmresourceDTO beforeDTO = JSONObject.parseObject(dataChangeInfo.getBeforeData(), HrmresourceDTO.class);
            if(!beforeDTO.getStatus().equals(hrmresourceDTO.getStatus())&&hrmresourceDTO.getStatus().equals(CdcConstant.NUM_5)){
                //调用mes接口停用
                String s = FeignOptional.of(() -> remoteMesFeignApi.disableUser(hrmresourceDTO.getLoginid()), FeignConstant.STECCN_MES_SERVICE_NAME).get();
                log.info("mes返回:",s);
            }else if(!beforeDTO.getDepartmentid().equals(hrmresourceDTO.getDepartmentid())||!beforeDTO.getJobtitle().equals(hrmresourceDTO.getJobtitle())){
                //部门有所改动或者岗位有所改动
                MesUserDTO mesUserDTO = setUserDTO(hrmresourceDTO,false);
                String s = FeignOptional.of(() -> remoteMesFeignApi.saveUser(mesUserDTO), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
                log.info("mes返回:",s);
            }
        }
        return true;
    }

    /**
     * 找到mes对应的部门code
     * @param departmentId
     * @return
     */
    private MesOfficeDTO findMesOfficeCode(Integer departmentId,Integer jobTitleId){
        //默认部门是未分配
        MesOfficeDTO mesOfficeDTO = new MesOfficeDTO();
        mesOfficeDTO.setOfficeCode(CdcConstant.UNALLOCATED_DEPARTMENT_CODE);
        mesOfficeDTO.setOfficeName(CdcConstant.UNALLOCATED_DEPARTMENT_NAME);
        //获取oa部门名字
        OaDepartmentVO departmentVO = oaInfoService.getDepartmentInfoById(departmentId).getData();
        if(Objects.isNull(departmentVO)){
            return mesOfficeDTO;
        }
        //查看是否为2级节点
        while(!departmentVO.getTLevel().equals(CdcConstant.NUM_2)){
            departmentVO = oaInfoService.getDepartmentInfoById(departmentVO.getSuperId()).getData();
            if(Objects.isNull(departmentVO)){
                return mesOfficeDTO;
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
            return mesOfficeDTO;
        }
        String finalMesNameByOaName = mesNameByOaName;
        Map<String, SysOfficeVO> stringSysOfficeVOMap = FeignOptional.of(() -> remoteOfficeFeign.listByName(Sets.newHashSet(finalMesNameByOaName)), FeignConstant.STECCN_PLATFORM_SERVICE_NAME).get();
        SysOfficeVO sysOfficeVO= new SysOfficeVO();
        sysOfficeVO.setOfficeCode(CdcConstant.UNALLOCATED_DEPARTMENT_CODE);
        String departmentCode = stringSysOfficeVOMap.getOrDefault(mesNameByOaName,sysOfficeVO).getOfficeCode();
        mesOfficeDTO.setOfficeCode(departmentCode);
        mesOfficeDTO.setOfficeName(finalMesNameByOaName);
        return mesOfficeDTO;
    }

    /**
     * 设置userDTO
     * @param hrmresourceDTO
     * @param isNewRecord
     * @return
     */
    private MesUserDTO setUserDTO(HrmresourceDTO hrmresourceDTO,Boolean isNewRecord){
        MesOfficeDTO mesOfficeDTO = findMesOfficeCode(hrmresourceDTO.getDepartmentid(),hrmresourceDTO.getJobtitle());
        MesUserDTO mesUserDTO = new MesUserDTO();
        mesUserDTO.setLoginCode(hrmresourceDTO.getLoginid());
        mesUserDTO.setUserName(hrmresourceDTO.getLastname());
        mesUserDTO.setUserRoleString("");
        mesUserDTO.setUserType("employee");
        mesUserDTO.setIsNewRecord(isNewRecord);
        MesEmployeeDTO mesEmployeeDTO = new MesEmployeeDTO();
        mesEmployeeDTO.setOffice(mesOfficeDTO);
        mesEmployeeDTO.setCompany(new MesCompanyDTO("CUCC","中国建材桐城新能源材料有限公司"));
        mesEmployeeDTO.setEmployeePosts("");
        mesEmployeeDTO.setEmployeeOfficeList(Lists.newArrayList());
        mesUserDTO.setMesEmployeeDTO(mesEmployeeDTO);
        return mesUserDTO;
    }
}
