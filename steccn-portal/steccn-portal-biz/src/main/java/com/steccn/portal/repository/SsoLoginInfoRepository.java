package com.steccn.portal.repository;

import com.steccn.portal.domain.SsoLoginInfo;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SsoLoginInfoRepository extends ReactiveCrudRepository<SsoLoginInfo, Integer> {

}
