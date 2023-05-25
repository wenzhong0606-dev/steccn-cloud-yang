package com.steccn.hikvision.entity;


import com.steccn.hikvision.vo.CameraVO;
import lombok.Data;

import java.util.List;

@Data
public class CameraData {
    private Integer total;
    private Integer pageNo;

    private Integer pageSize;

    private List<CameraVO> list;
}
