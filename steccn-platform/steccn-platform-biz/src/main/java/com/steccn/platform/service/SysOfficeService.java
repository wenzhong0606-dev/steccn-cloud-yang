package com.steccn.platform.service;

import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.vo.SysOfficeVO;
import com.steccn.platform.domain.SysOffice;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
* @author W10
* @description 针对表【sys_office(Mes组织机构表)】的数据库操作Service
* @createDate 2022-09-02 16:25:59
*/
public interface SysOfficeService extends IService<SysOffice> {

    List<SysOfficeVO> sysList(String officeType);

    List<SysOfficeVO> treeData();

    List<SysOfficeVO> data();

    /**
     * 返回数据类型下所有数据
     * @param officeType
     * @return
     */
    List<SysOfficeVO> flatData(String officeType);

    /**
     * 返回数据类型下所有数据
     * @param id officeId
     * @return
     */
    String findNameById(Integer id);

    /**
     * 返回类型和父级节点下数据
     * @param dto
     * @return
     */
    List<SysOfficeVO> findDataByPCodeAndType(SysOfficeParentDTO dto);

    /**
     * 根据主键id查询机构的信息
     * @param idList 主键id集合
     * @return map
     */
    Map<Integer, SysOfficeVO> listById(Set<Integer> idList);

    /**
     * 根据主键id查询机构的名称
     * @param idList id主键集合
     * @return map
     */
    Map<Integer, String> listByCh(Set<Integer> idList);

    /**
     * 根据code查询中文
     * @param codeSet code集合
     * @return 中文
     */
    Map<String, SysOfficeVO> listByCodeSet(Set<String> codeSet);

    /**
     * 仓库和货架
     * @return 集合
     */
    List<SysOfficeVO> warehouseAndShelf();

    /**
     * 查询货架的仓库id，如果无仓库，则返回null
     * @param id 货架id
     * @return
     */
    SysOfficeVO shelfWarehouse(Integer id);
}
