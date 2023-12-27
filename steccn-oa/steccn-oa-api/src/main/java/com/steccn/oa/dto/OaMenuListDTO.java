package com.steccn.oa.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * oa菜单转换DTO
 *
 * @author AndrewLuo
 * @date 2023/12/27
 */
@Data
public class OaMenuListDTO implements Serializable {
    private String fname;
    private String id;
    private String mobxrouteurl;
    private List<OaMenuListDTO> child;
}
