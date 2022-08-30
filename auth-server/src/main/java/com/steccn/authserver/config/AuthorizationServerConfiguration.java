package com.steccn.authserver.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.steccn.authserver.handler.MyAuthenticationSuccessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * 类说明
 * 认证服务器配置类
 *
 * @author Jinglihui
 * @date 2022/6/28
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfiguration {

    @Autowired
    private MyAuthenticationSuccessHandler successHandler;

    @Autowired
    private Environment env;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity httpSecurity)
            throws Exception {

        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(httpSecurity);
        return httpSecurity.formLogin(v -> v.successHandler(successHandler)).build();
    }


    /**
     * 方法说明
     * 注册客户端
     *
     * @return
     * @author Jinglihui
     * @date 2022/7/20 14:19
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate,PasswordEncoder encoder) {
        log.info("#### registeredClientRepository {}", jdbcTemplate.toString());
        JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
        RegisteredClient registeredClient = registeredClientRepository.findByClientId("steccn-gateway");
        if (registeredClient == null) {
            log.info("#### Client {} 不存在，执行注册", "steccn-gateway");
            //配置Token
            TokenSettings tokenSettings = TokenSettings.builder()
                    //令牌存活时间
                    .accessTokenTimeToLive(Duration.ofDays(30))
                    //令牌可以刷新
                    .reuseRefreshTokens(true)
                    //刷新时间为30天。30天内令牌过期时，可以用刷新令牌重新申请令牌，不需要再确认
                    .refreshTokenTimeToLive(Duration.ofDays(30))
                    .build();
            //客户端相关配置
//            ClientSettings clientSettings = ClientSettings.builder()
//                    //是否需要用户授权确认：不需要
//                    .requireAuthorizationConsent(false)
//                    .build();
            // BCrypt 生成的密码会带有前缘`{bcrypt]`如果不截取掉，无法通过自己的合法校验（搞笑吧）
            // BCrypt 生成的加密串是60个字符长，如果加了前缘就是68个长。
            String pwd = encoder.encode("steccn-gateway");
            registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                    .clientId("steccn-gateway")
                    .clientSecret(pwd)
                    .clientName("steccn-gateway")
                    //授权方法
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    //授权码模式
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    //刷新令牌
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    //客户端模式
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    //回调地址：授权服务器向当前客户端响应时调用下面地址；
                    //不在此列的地址将被拒绝；只能使用IP或域名，不能使用localhost
                    //messaging-client-oidc是客户端的名字
                    .redirectUri("http://127.0.0.1:9000/login/oauth2/code/messaging-client-oidc")
                    //用户成功通过授权服务器身份验证，authorization code将被附加到这个uri
                    //这个URI是OAuth Client的地址：gateway
                    .redirectUri("http://127.0.0.1:9000/authorized")
                    .redirectUri("http://127.0.0.1:9000/aas/token")
                    .scope(OidcScopes.OPENID)
                    //当前客户授权范围
                    .scope("read")
                    .scope("write")
                    .tokenSettings(tokenSettings)
                    //.clientSettings(clientSettings)
                    .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                    .build();
            //存入数据库
            registeredClientRepository.save(registeredClient);
        }
        return registeredClientRepository;
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        log.info("#### jwtDecoder called at {}", LocalDateTime.now().toString());
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() throws NoSuchAlgorithmException {
        RSAKey rsaKey = generateRsaKey();
        JWKSet jwkSet = new JWKSet(rsaKey);

        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    private static RSAKey generateRsaKey() throws NoSuchAlgorithmException {
        KeyPair keyPair = generateRsaKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        return new RSAKey.Builder(publicKey).privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    private static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);

        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 方法说明
     * 向JWT中加角色权限信息
     *
     * @return
     * @author Jinglihui
     * @date 2022/7/28 16:34
     */
    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {
                //todo 携带额外信息 存放类
//                Authentication principal = context.getPrincipal();
//                Set<String> authorities = principal.getAuthorities().stream()
//                        .map(GrantedAuthority::getAuthority)
//                        .collect(Collectors.toSet());
//                log.info("#### jwtCustomizer authorities {} call at {}", authorities, LocalDateTime.now().toString());
                //角色信息的集合
//                context.getClaims().claim("roles", authorities);
//                context.getClaims().claim("userName", principal.getName());
            }
        };
    }

    /**
     * 方法说明
     * ProviderSettings配置 Spring Authorization Server的实例
     *
     * @return
     * @author Jinglihui
     * @date 2022/7/20 16:18
     */
    @Bean
    public ProviderSettings providerSettings() {
        return ProviderSettings.builder().issuer("http://localhost:9002").build();
    }

    @Bean
    public DataSource getDatasource() {
        DriverManagerDataSource datasource = new DriverManagerDataSource();
        datasource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        datasource.setUrl(env.getProperty("spring.datasource.url"));
        datasource.setUsername(env.getProperty("spring.datasource.username"));
        datasource.setPassword(env.getProperty("spring.datasource.password"));
        return datasource;
    }

    @Bean
    public JdbcTemplate getJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
