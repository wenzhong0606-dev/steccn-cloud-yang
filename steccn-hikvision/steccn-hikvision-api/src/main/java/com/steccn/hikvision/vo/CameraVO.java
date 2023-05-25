package com.steccn.hikvision.vo;


import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.lang.ref.PhantomReference;
import java.util.ArrayList;
import java.util.List;

@ApiModel("监控")
@Data
public class CameraVO {


    /**
     * 唯一编码
     */
    private String indexCode;


    /**
     * 区域路径名称，"/"分隔
     */
    private String regionPathName;

    /**
     * 所属区域路径
     */
    private String regionName;


    /**
     * 父级资源编号
     */
    private String parentIndexCode;

    private List<CameraVO> children;
    public CameraVO() {
        children = new ArrayList<>();
    }
}
