package com.steccn.platform.service;

import com.steccn.platform.dto.SysOfficeDTO;
import com.steccn.platform.dto.SysOfficeParentIdsDTO;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.dto.SysOfficeTypeKeyDTO;
import com.steccn.platform.vo.StationLineVO;
import com.steccn.platform.vo.SysOfficeParentVO;
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
     *
     * @param officeType
     * @return
     */
    List<SysOfficeVO> flatData(String officeType);

    /**
     * 返回数据类型下所有数据
     *
     * @param id officeId
     * @return
     */
    String findNameById(Integer id);

    /**
     * 返回类型和父级节点下数据
     *
     * @param dto
     * @return
     */
    List<SysOfficeVO> findDataByPCodeAndType(SysOfficeParentDTO dto);

    /**
     * 根据主键id查询机构的信息
     *
     * @param idList 主键id集合
     * @return map
     */
    Map<Integer, SysOfficeVO> listById(Set<Integer> idList);

    /**
     * 根据主键id查询机构的名称
     *
     * @param idList id主键集合
     * @return map
     */
    Map<Integer, String> listByCh(Set<Integer> idList);

    /**
     * 根据code查询中文
     *
     * @param codeSet code集合
     * @return 中文
     */
    Map<String, SysOfficeVO> listByCodeSet(Set<String> codeSet);

    /**
     * 仓库和货架
     *
     * @param typeList 类型集合
     * @return 集合
     */
    List<SysOfficeVO> warehouseAndShelf(List<String> typeList);

    /**
     * 查询货架的仓库id，如果无仓库，则返回null
     *
     * @param id 货架id
     * @return
     */
    SysOfficeVO shelfWarehouse(Integer id);

    /**
     * 根据孩子id获取父亲节点数据
     *
     * @param childId 货架id
     * @return
     */
    SysOfficeVO parentOffice(Integer childId);

    /**
     * 扫描工位二维码
     *
     * @param id 工位id
     * @return
     */
    StationLineVO stationScan(Integer id);

    /**
     * 根据父节点id获取下级数据
     *
     * @param dto
     * @return
     */
    List<SysOfficeVO> listByParentId(SysOfficeParentDTO dto);

    /**
     * 根据车间编码列表选择产线
     *
     * @param dto
     * @return
     */
    List<SysOfficeParentVO> getListSysOfficeList(SysOfficeParentIdsDTO dto);

    /**
     * 根据孩子id获取父亲节点数据
     *
     * @param childId 货架id
     * @return
     */
    Map<Integer, SysOfficeVO> parentOfficeMap(List<Integer> childId);

    /**
     * 根据机构名字和类型查找
     *
     * @return
     */
    List<SysOfficeVO> findByTypeAndName(SysOfficeDTO dto);

    /**
     * 根据机构类型以及关键字返回SysOfficeVO集合信息
     *
     * @param dto
     * @return
     */
    List<SysOfficeVO> getListByTypeAndKey(SysOfficeTypeKeyDTO dto);


    /**
     * 根据子节点id 获取父级信息
     *
     * @param childIdList
     * @return
     */
    Map<Integer, SysOfficeVO> parentInfo(Set<Integer> childIdList);

    String getByIdSysOfficeInfo( Long id);


    /**
     *
     * @param id  根据主键获取仓库或者货架信息
     * @return
     */
    SysOfficeVO shelfWarehouseInfo(Integer id);

    /**
     * 查询指定机构类型，及机构类型的孩子节点，返回平铺数据
     * @param officeType
     * @return
     */
    Set<Integer> typeAndChildrenFlat(String officeType);
}
