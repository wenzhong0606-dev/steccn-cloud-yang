package com.steccn.usercenter.dto;

import com.steccn.usercenter.domain.SysUser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


/**
 * 类说明
 *
 * @author Jinglihui
 * @date 2022/7/8
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(value = "用户信息")
public class UserDTO extends SysUser {
    @ApiModelProperty("用户角色列表")
    private List<RoleDTO> roles = new ArrayList<>();

    public UserDTO(SysUser user, List<RoleDTO> roles){
        super(user);
        this.roles =roles;
    }

    public void addRoles(RoleDTO roleDto){
        roles.add(roleDto);
    }
    public UserDTO(SysUser user){
        super(user);
    }
}
