package com.steccn.platform.dto;


import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("字典类型DTO")
public class SysDictTypeDTO {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 类型code
     */
    private String type;

    /**
     * 类型名称
     */
    private String name;

}
