package com.steccn.pms.mapper;

import com.steccn.pms.domain.PmProcess;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectKey;

/**
 * @author yequan
 * @description 针对表【pm_process(生产工序表)】的数据库操作Mapper
 * @createDate 2022-09-02 17:01:20
 * @Entity com.steccn.pms.domain.PmProcess
 */
@Mapper
public interface PmProcessMapper extends BaseMapper<PmProcess> {

    @Insert("insert  into pm_process(code,type,name,status,file_id,create_by,create_name, update_by,update_name) values(#{code},#{type},#{name},#{status},#{fileId},#{createBy},#{createName},#{updateBy},#{updateName})")
    @Options(useGeneratedKeys = true,keyProperty = "id",keyColumn = "id")
    Integer save(PmProcess model);
}




