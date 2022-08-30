package com.steccn.oa.dto;

import lombok.Data;

import java.util.List;

/**
 * oa系统菜单权限
 *
 * @author AndrewLuo
 * @date 2022/08/30
 */
@Data
public class OaMenuResult extends Object{
    private int id;
    private String name;
    private int pid;
    private String url;
    private Boolean status;
    private List<OaMenuResult> child;
}
