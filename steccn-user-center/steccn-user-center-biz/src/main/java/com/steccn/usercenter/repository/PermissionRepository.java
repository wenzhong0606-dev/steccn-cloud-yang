package com.steccn.usercenter.repository;

import com.steccn.usercenter.domain.Permission;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PermissionRepository extends ReactiveCrudRepository<Permission,Integer> {

    @Query("select a.id,a.name,a.uri from (select permission_id from sys_role_permission where role_id = :roleId) t left join sys_permission a on t.permission_id = a.id ")
    Flux<Permission> getPermissionsByRoleId(Integer roleId);

    @Query("select id,name,uri from sys_permission where id in " +
            "(select rp.permission_id from (select role_id from sys_user_role where user_id =:userId) ur right join sys_role_permission rp where ur.role_id = rp.role_id)")
    Flux<Permission> getPermissionsByUserId(Long userId);

}
