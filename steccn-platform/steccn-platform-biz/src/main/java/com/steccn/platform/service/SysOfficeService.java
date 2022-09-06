package com.steccn.platform.service;

import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.domain.SysOffice;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author W10
* @description 针对表【sys_office(Mes组织机构表)】的数据库操作Service
* @createDate 2022-09-02 16:25:59
*/
public interface SysOfficeService extends IService<SysOffice> {

    List<SysOfficeVO> sysList(String officeType);

    List<SysOfficeVO> treeData();

    List<SysOfficeVO> data();
}
