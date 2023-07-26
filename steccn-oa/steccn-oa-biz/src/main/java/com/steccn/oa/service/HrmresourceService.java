package com.steccn.oa.service;

import com.steccn.common.constant.ResultCode;
import com.steccn.common.dto.TupleDTO;
import com.steccn.oa.domain.Hrmresource;
import com.baomidou.mybatisplus.extension.service.IService;
import com.steccn.oa.dto.OaUserIdAndName;
import com.steccn.oa.vo.OaClassVO;
import com.steccn.oa.vo.OaMesUserVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
* @author yequan
* @description 针对表【hrmresource】的数据库操作Service
* @createDate 2022-09-01 18:37:02
*/
public interface HrmresourceService extends IService<Hrmresource> {
    /**
     * 通过工号查询id
     * @param workCode
     * @return
     */
    Long getIdByWorkCode(String workCode);

    /**
     * 通过登录名查询名字
     * @param loginId
     * @return
     */
    OaUserIdAndName getNameByLoginId(String loginId);

    /**
     * 根据名称模糊匹配人员信息
     * @param userName
     * @return
     */
    List<OaUserIdAndName> getUserByName(String userName);

    /**
     * 根据部门和岗位查询人
     * @param departmentId
     * @param jobTitle
     * @return
     */
    List<OaUserIdAndName> getUserByDepartmentIdAndJobTitle(Integer departmentId,Integer jobTitle);

    /**
     * 根据id获取用户的中文名称
     * @param idList
     * @return
     */
    Map<Long, String> listByIdCh(List<Long> idList);

    /**
     * 根据用户名列表查询id
     * @param userNames
     * @return
     */
    List<Integer> getIdByUserName(List<String> userNames);

    /**
     * 根据岗位code查询oa数据
     * @param workCodes
     * @return
     */
    List<OaUserIdAndName> getIdByWorkCode(List<String> workCodes);

    /**
     * 根据id查询用户名
     * @param id
     * @return
     */
    String getLoginIdById(Integer id);

    /**
     * 获取mes用户信息
     * @return
     */
    List<OaMesUserVO> getMesUser();

    /**
     * 同步用户
     * @return
     */
    String synchronizeUser();

}
