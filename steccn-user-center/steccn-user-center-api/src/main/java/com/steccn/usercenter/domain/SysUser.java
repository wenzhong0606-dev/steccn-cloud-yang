package com.steccn.usercenter.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
 * @date 2022/7/7
 */
@ApiModel("用户基本信息")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("sys_user")
public class SysUser {
    @ApiModelProperty("用户ID")
    @Id
    private Long id;

    @Column
    @ApiModelProperty("用户名")
    private String username;
    @Column
    @ApiModelProperty("用户密码")
    private String password;
    @Column
    @ApiModelProperty("移动电话")
    private String mobile;
    @Column
    @ApiModelProperty("用户性别")
    private Byte gender;

    public SysUser(SysUser user) {
        this.id =user.id;
        this.username = user.username;
        this.password = user.password;
        this.mobile = user.mobile;
        this.gender = user.gender;
    }
}
