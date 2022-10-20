package com.steccn.platform.mapper;

import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.domain.SysEqpSpare;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.platform.vo.EqpSpareVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * @author W10
 * @description 针对表【sys_eqp_spare(Mes备品备件表)】的数据库操作Mapper
 * @createDate 2022-09-02 16:22:51
 * @Entity com.steccn.platform.domain.SysEqpSpare
 */
@Mapper
public interface SysEqpSpareMapper extends BaseMapper<SysEqpSpare> {

    /**
     * 根据备件名称查询备件内容
     * @param name
     * @return
     */
    List<EqpSpareVO> listSpares(@Param("name") String name);

    /**
     * 根据备件编码查询备件信息
     * @param code
     * @return
     */
    EqpSpareVO getByCode(@Param("code") String code);

    /**
     * 根据备件编码批量查询
     * @param codeList
     * @return
     */
    Collection<SysEqpSpare> listByCode(@Param("codeList") List<String> codeList);
}




