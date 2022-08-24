package com.steccn.usercenter.repository;

import com.steccn.usercenter.domain.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveCrudRepository<Role,Integer> {

    @Query("select * from sys_role where id=:id")
    Mono<Role> getRoleById(Integer id);

    @Query("select r.id,r.name from (select role_id from sys_user_role where user_id = :userId) t inner join sys_role r on t.role_id = r.id ")
    Flux<Role> getRolesByUserId(Long userId);

}
