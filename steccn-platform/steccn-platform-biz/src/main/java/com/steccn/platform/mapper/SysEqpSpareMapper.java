package com.steccn.platform.mapper;

import com.steccn.platform.dto.SpareDTO;
import com.steccn.platform.domain.SysEqpSpare;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steccn.platform.vo.EqpSpareVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author W10
 * @description 针对表【sys_eqp_spare(Mes备品备件表)】的数据库操作Mapper
 * @createDate 2022-09-02 16:22:51
 * @Entity com.steccn.platform.domain.SysEqpSpare
 */
@Mapper
public interface SysEqpSpareMapper extends BaseMapper<SysEqpSpare> {

    @Select("select id,code,name from sys_eqp_spare where name like concat(concat('%',#{name}),'%')")
    List<EqpSpareVO> listSpares(String name);

    @Select("select id,code,name from sys_eqp_spare where code = #{code}")
    EqpSpareVO getByCode(String code);
}




