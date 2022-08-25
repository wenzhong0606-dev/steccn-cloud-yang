package com.steccn.portal.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * @desc 菜单权限表
 */
@Table("permission_info")
@Data
public class PermissionInfo {
    @Id
    private Integer id;

    @Column("code")
    private String code;

    @Column("name")
    private String name;

    @Column("icon")
    private String icon;

    @Column("uri")
    private String uri;

    @Column("host")
    private String host;

    @Column("app_name")
    private String appName;
}
