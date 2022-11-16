package com.steccn.platform.mapper;

import com.steccn.platform.domain.SysOffice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.platform.dto.SysOfficeParentDTO;
import com.steccn.platform.vo.SysOfficeVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author W10
 * @description 针对表【sys_office(Mes组织机构表)】的数据库操作Mapper
 * @createDate 2022-09-02 16:25:59
 * @Entity com.steccn.platform.domain.SysOffice
 */
@Mapper
public interface SysOfficeMapper extends BaseMapper<SysOffice> {

    /**
     * 根据id和类型查询下级数据
     *
     * @param id
     * @param officeType
     * @return
     */
    List<SysOfficeVO> listByParentId(@Param("id") Integer id, @Param("officeType") String officeType);


    List<SysOfficeVO> listByParentIds(@Param("ids") List<Integer> ids, @Param("officeType") String officeType);


    /**
     * 根据子id查询父id
     *
     * @param childIdList 子id集合
     * @return
     */
    List<SysOfficeVO> listByChildIds(@Param("childIdList") List<Integer> childIdList);

    /**
     * 获取父节点信息
     * @param childIdList
     * @return
     */
    List<SysOfficeVO> listParentInfo(@Param("childIdList") Set<Integer> childIdList);
}



