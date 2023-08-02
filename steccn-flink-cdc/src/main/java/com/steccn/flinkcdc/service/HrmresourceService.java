package com.steccn.flinkcdc.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.authserver.util.FeignOptional;
import com.steccn.common.constant.FeignConstant;
import com.steccn.common.constant.LockConstant;
import com.steccn.common.vo.R;
import com.steccn.flinkcdc.constant.CdcConstant;
import com.steccn.flinkcdc.dto.DataChangeDTO;
import com.steccn.flinkcdc.dto.HrmresourceDTO;
import com.steccn.oa.feign.OaInfoService;
import com.steccn.oa.vo.OaDepartmentVO;
import com.steccn.oa.vo.OaJobTitleVO;
import com.steccn.platform.dto.MesOfficeDTO;
import com.steccn.platform.dto.MesUserDTO;
import com.steccn.platform.feign.RemoteMesFeignApi;
import com.steccn.platform.feign.RemoteOfficeFeign;
import com.steccn.platform.vo.SysOfficeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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

    private final RedissonClient redissonClient;

    /**
     * 处理人员信息
     * @param dataChangeInfo
     * @return
     */
    public Boolean handleData(Integer operatorType,DataChangeDTO dataChangeInfo){
        String data = dataChangeInfo.getData();
        HrmresourceDTO hrmresourceDTO = JSONObject.parseObject(data, HrmresourceDTO.class);
        RLock lock = redissonClient.getLock(LockConstant.CDC_USER_ADD);
        lock.lock();
        try {
            //新增
            if((operatorType.equals(CdcConstant.OperateType.UPDATE.getNum())
                    &&StrUtil.isNotBlank(hrmresourceDTO.getLoginid()))
                    ||operatorType.equals(CdcConstant.OperateType.INSERT.getNum())){
                MesUserDTO mesUserDTO = setUserDTO(hrmresourceDTO,true);
                String s = "";
                try {
                    s = remoteMesFeignApi.saveUser(mesUserDTO);
                } catch (Exception e) {
                    log.error("mes调用异常",e);
                }
                log.info("新增-mes返回:",s);
            }
            if(operatorType.equals(CdcConstant.OperateType.UPDATE.getNum())){
                //离职同步
                //修改前数据
                HrmresourceDTO beforeDTO = JSONObject.parseObject(dataChangeInfo.getBeforeData(), HrmresourceDTO.class);
                if(hrmresourceDTO.getStatus().equals(CdcConstant.NUM_5)){
                    //调用mes接口停用
                    String s = "";
                    try {
                        s = FeignOptional.of(() -> remoteMesFeignApi.disableUser(beforeDTO.getLoginid()), FeignConstant.STECCN_MES_SERVICE_NAME).get();
                    } catch (Exception e) {
                        log.error("mes调用异常",e);
                    }
                    log.info("离职-mes返回:",s);
                }else if(!beforeDTO.getDepartmentid().equals(hrmresourceDTO.getDepartmentid())||!beforeDTO.getJobtitle().equals(hrmresourceDTO.getJobtitle())){
                    //部门有所改动或者岗位有所改动
                    MesUserDTO mesUserDTO = setUserDTO(hrmresourceDTO,false);
                    String s = "";
                    try {
                        s = remoteMesFeignApi.saveUser(mesUserDTO);
                    } catch (Exception e) {
                        log.error("mes调用异常",e);
                    }
                    log.info("部门变动-mes返回:",s);
                }
            }

        }finally {
            lock.unlock();
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
        //获取oa岗位名字
        OaJobTitleVO oaJobTitleVO = oaInfoService.getJobInfoById(jobTitleId).getData();
        //officeCode作为岗位code
        mesOfficeDTO.setOfficeCode(oaJobTitleVO.getJobTitleName());
        //获取oa部门名字
        OaDepartmentVO departmentVO = oaInfoService.getDepartmentInfoById(departmentId).getData();
        if(Objects.isNull(departmentVO)){
            return mesOfficeDTO;
        }
        //传值为部门名称
        if(departmentVO.getDepartmentName().equals("深加工二车间")){
            departmentVO.setDepartmentName("深加工二车间（东线）");
        }
        if(departmentVO.getDepartmentName().equals("深加工三车间")){
            departmentVO.setDepartmentName("深加工三车间（西线）");
        }
        String parentName = "";
        if(!departmentVO.getSuperId().equals(0)){
            OaDepartmentVO parentDepartmentVO = oaInfoService.getDepartmentInfoById(departmentVO.getSuperId()).getData();
            if(parentDepartmentVO.getDepartmentName().equals("深加工二车间")){
                parentDepartmentVO.setDepartmentName("深加工二车间（东线）");
            }
            if(parentDepartmentVO.getDepartmentName().equals("深加工三车间")){
                parentDepartmentVO.setDepartmentName("深加工三车间（西线）");
            }
            parentName = parentDepartmentVO.getDepartmentName();
        }
        mesOfficeDTO.setOfficeName(parentName+"/"+departmentVO.getDepartmentName());
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
        mesUserDTO.setIsNewRecord(isNewRecord);
        mesUserDTO.setOfficeCode(mesOfficeDTO.getOfficeCode());
        mesUserDTO.setOfficeName(mesOfficeDTO.getOfficeName());
        mesUserDTO.setCompanyName("中国建材桐城新能源材料有限公司");
        mesUserDTO.setCompanyCode("CUCC");
        return mesUserDTO;
    }
}
