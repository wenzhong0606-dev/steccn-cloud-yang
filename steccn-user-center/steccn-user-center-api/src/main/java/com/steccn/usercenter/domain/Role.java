package com.steccn.usercenter.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("sys_role")
public class Role {
    @Id()
    private Integer id;
    @Column("name")
    private String name;

    public Role(Role role){
        this.id = role.id;
        this.name = role.name;
    }
}
