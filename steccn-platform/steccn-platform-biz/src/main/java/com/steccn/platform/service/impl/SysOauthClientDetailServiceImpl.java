package com.steccn.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.MD5;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.steccn.common.constant.CacheConstant;
import com.steccn.common.constant.MpConstant;
import com.steccn.platform.domain.SysOauthClientDetail;
import com.steccn.platform.service.SysOauthClientDetailService;
import com.steccn.platform.mapper.SysOauthClientDetailMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * @author yequan
 * @description 针对表【sys_oauth_client_detail】的数据库操作Service实现
 * @createDate 2022-12-16 15:33:47
 */
@Service
public class SysOauthClientDetailServiceImpl extends ServiceImpl<SysOauthClientDetailMapper, SysOauthClientDetail>
        implements SysOauthClientDetailService {

    @Override
    @Cacheable(value = CacheConstant.SYS_CLIENT_ENCODE_PASSWORD)
    public List<String> clientEncodePassword() {
        MD5 md5 = SecureUtil.md5();
        List<String> resSet = Lists.newArrayList();
        List<SysOauthClientDetail> list = list();
        if (CollUtil.isNotEmpty(list)) {
            for (SysOauthClientDetail sysOauthClientDetail : list) {
                if (MpConstant.YES.equals(sysOauthClientDetail.getUseFlag())) {
                    String clientId = sysOauthClientDetail.getClientId();
                    String clientSecret = sysOauthClientDetail.getClientSecret();
                    String s = md5.digestHex(clientId + clientSecret, StandardCharsets.UTF_8);
                    resSet.add(s);
                }
            }
        }
        return resSet;
    }
}




