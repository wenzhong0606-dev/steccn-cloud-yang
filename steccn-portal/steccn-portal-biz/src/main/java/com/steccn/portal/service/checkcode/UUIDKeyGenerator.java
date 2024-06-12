package com.steccn.portal.service.checkcode;

import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @author yequan
 * @version 1.0
 * @desc uuid生成器
 */
@Component("UUIDKeyGenerator")
public class UUIDKeyGenerator implements CheckCodeService.KeyGenerator {
    @Override
    public String generate(String prefix) {
        String uuid = UUID.randomUUID().toString();
        return prefix + uuid.replaceAll("-", "");
    }
}
