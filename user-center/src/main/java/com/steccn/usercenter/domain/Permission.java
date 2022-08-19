package com.steccn.usercenter.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/8
 */
@Table("sys_permission")
@Data
public class Permission {
    @Id
    private Long id;

    @Column("name")
    private String permissionName;

    @Column("uri")
    private String uri;
}
