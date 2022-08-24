package com.steccn.usercenter.repository;

import com.steccn.usercenter.domain.Role;
import com.steccn.usercenter.domain.SysUser;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface UserRepository extends ReactiveCrudRepository<SysUser,Integer> {

    @Query("select * from sys_user where id = :id")
    Mono<SysUser> getUserById(Long id);

    @Query("select * from sys_user where username = :username")
    Mono<SysUser> getUserByName(String name);

    @Query("select * from sys_user_role where user_id =:userId")
    Flux<Role> getUserRoles(Long userId);

}
