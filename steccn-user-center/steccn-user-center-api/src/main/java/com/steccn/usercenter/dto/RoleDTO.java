package com.steccn.usercenter.dto;

import com.steccn.usercenter.domain.Permission;
import com.steccn.usercenter.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO extends Role {

    private List<Permission> permissions = new  ArrayList<>();

    public RoleDTO(Role r,List<Permission> permissionList){
        super(r);
        this.permissions = permissionList;
    }
    public RoleDTO(Role role){
        super(role);
    }
}
