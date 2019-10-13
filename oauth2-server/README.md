# 微服务认证与授权
这里我使用 oauth2 和 Spring Security 来做spring cloud 微服务的认证与授权

## 1 什么是OAuth2
OAuth2 是一个授权框架，或称授权标准，它可以使第三方应用程序或客户端获得对HTTP服务上（例如 Google，GitHub ）用户帐户信息的有限访问权限。
OAuth 2 通过将用户身份验证委派给托管用户帐户的服务以及授权客户端访问用户帐户进行工作。

## 2 OAuth2能做什么
OAuth2 允许用户提供一个令牌，而不是用户名和密码来访问他们存放在特定服务提供者的数据。每一个令牌授权一个特定的网站（例如，视频编辑网站)在
特定的时段（例如，接下来的2小时内）内访问特定的资源（例如仅仅是某一相册中的视频）。这样，OAuth允许用户授权第三方网站访问他们存储在另外的服
务提供者上的信息，而不需要分享他们的访问许可或他们数据的所有内容。  
举个栗子：比如我们常用的微信公众号，当我们第一次打开公众号中网页的时候会弹出是否允许授权，当我们点击授权的时候，公众号网站就能获取到我们的头
像和昵称等信息。这个过程就是通过OAuth2 来实现的。

## 3 Spring Cloud集成OAuth2
### 3.1 创建并导入数据库文件
#### 3.1.1 oauth2所需的数据库。
经我整理修改过的sql脚本: [oauth2.sql](db/oauth2.sql)
#### 3.1.2 用户，角色，权限，以及两张关联表 sql脚本
经典的五张表：[user_db.sql](db/user_db.sql)

### 3.2 新建模块 oauth2-server
#### 3.2.1 [pom](./pom.xml) 引入依赖
```xml
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-oauth2</artifactId>
    </dependency>
```

#### 3.2.2 配置微服务名称
```yaml
spring:
  application:
    name: oauth2-server
```

#### 3.2.3 配置数据库连接信息
```yaml
spring:
  datasource:                                     # 数据源配置
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/oauth2?characterEncoding=UTF-8&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC
    username: chennan
    password: 123456
```

#### 3.2.4 配置redis连接信息
```yaml
spring:
  redis:
    host: 192.168.232.151
    port: 6379
    password:
    jedis:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

#### 3.2.5 UserDetails 接口实现
```java
package com.chennan.cloud.config;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * UserDetails 接口实现
 * @author chen.nan
 */
@Data
public class UserDetailsImpl implements UserDetails {

    private String userName;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    private boolean isEnabled;

    public UserDetailsImpl(){}

    public UserDetailsImpl(String userName, String password, Collection<? extends GrantedAuthority> authorities, boolean isAccountNonExpired, boolean isAccountNonLocked, boolean isCredentialsNonExpired, boolean isEnabled) {
        this.userName = userName;
        this.password = password;
        this.authorities = authorities;
        this.isAccountNonExpired = isAccountNonExpired;
        this.isAccountNonLocked = isAccountNonLocked;
        this.isCredentialsNonExpired = isCredentialsNonExpired;
        this.isEnabled = isEnabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }
}

```

#### 3.2.6 用户信息查询业务类实现
* 1.对应的bo类
* 2.Mapper接口
```java
package com.chennan.cloud.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chennan.cloud.bo.Permission;
import com.chennan.cloud.bo.Role;
import com.chennan.cloud.bo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Mapper接口类
 * <p>
 *     懒得写*Mapper.xml文件了，使用@Select注解实现SQL语句绑定
 * </p>
 * @author chen.nan 
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户username 查询角色列表
     * @param user 封装了username
     * @return List<Role>
     */
    @Select("SELECT r.* FROM SYS_USER_ROLE ur INNER JOIN SYS_ROLE r ON ur.ROLE_ID = r.ROLE_ID WHERE ur.USER_ID=#{userId}")
    List<Role> findRoleList(User user);

    /**
     * 根据角色ID查询权限列表
     * @param role 封装了roleId
     * @return List<Permission>
     */
    @Select("SELECT p.* FROM SYS_ROLE_PERMISSION rp INNER JOIN SYS_PERMISSION p ON RP.PERMISSION_ID = P.PERMISSION_ID WHERE RP.ROLE_ID = #{roleId}")
    List<Permission> findPermission(Role role);
}
```

* 3.UserService业务类
```java
package com.chennan.cloud.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chennan.cloud.bo.Role;
import com.chennan.cloud.bo.User;
import com.chennan.cloud.dao.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户角色权限查询业务类实现
 * @author chen.nan 
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    public User getByUserName(String userName){
        return getOne(new QueryWrapper<User>().eq("USER_NAME", userName), true);
    }

    public User getUserInfo(String userName){
        User userInfo = getByUserName(userName);
        List<Role> roleList = baseMapper.findRoleList(userInfo);
        roleList.forEach(role->role.setPermissionList(baseMapper.findPermission(role)));
        return userInfo.setRoleList(roleList);
    }

}

```

* web层RESTful实现
```java
package com.chennan.cloud.web;

import com.chennan.cloud.base.common.vo.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * web restful层接口实现
 * @author chen.nan 
 */
@RequestMapping("/api")
@RestController
public class UserController {

    @Autowired private ConsumerTokenServices consumerTokenServices;

    /**
     * 用户认证信息查询
     */
    @GetMapping("/userInfo")
    public Principal user(Principal userInfo) {
        return userInfo;
    }

    /**
     * 退出，销毁access_token
     */
    @DeleteMapping(value = "/exit")
    public R revokeToken(String access_token) {
        if (!consumerTokenServices.revokeToken(access_token))
            return R.err("注销失败");
        return R.ok();
    }

}

```

#### 3.2.7 UserDetailsService 接口实现
```java
package com.chennan.cloud.config;

import com.chennan.cloud.bo.User;
import com.chennan.cloud.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * UserDetailsService 接口实现
 * @author chen.nan
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User userInfo = userService.getUserInfo(userName);
        if (userInfo == null) throw new UsernameNotFoundException(userName);
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        // 以下4个boolean类型的属性暂时写死

        // 可用性 :true:可用 false:不可用
        boolean isEnabled = true;
        // 过期性 :true:没过期 false:过期
        boolean isAccountNonExpired = true;
        // 有效性 :true:凭证有效 false:凭证无效
        boolean isCredentialsNonExpired = true;
        // 锁定性 :true:未锁定 false:已锁定
        boolean isAccountNonLocked = true;
        userInfo.getRoleList().forEach(role -> {
            //角色必须是ROLE_开头，可以在数据库中设置
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role.getRoleName());
            grantedAuthorities.add(grantedAuthority);
            role.getPermissionList().forEach(permission -> {
                GrantedAuthority authority = new SimpleGrantedAuthority(permission.getUri());
                grantedAuthorities.add(authority);
            });
        });

        return new UserDetailsImpl(userInfo.getUserName()
                , userInfo.getPassword()
                , grantedAuthorities
                , isAccountNonExpired
                , isAccountNonLocked
                , isCredentialsNonExpired
                , isEnabled);
    }
}

```

#### 3.2.8 配置web安全认证
```java
package com.chennan.cloud.oauth2.cfg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 配置web安全认证
 * @author chen.nan
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private UserDetailsService userDetailsService;

    @Autowired @Qualifier("userDetailsService")
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * 密码加密方式，这里没有做密码加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        //return new BCryptPasswordEncoder();
        return new NoEncryptPasswordEncoder();
    }
    /**
     * 不定义没有password grant_type
     */
    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 认证中心的请求放行
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requestMatchers().antMatchers("/oauth/**")
                .and()
                .authorizeRequests()
                .antMatchers("/oauth/**").authenticated()
                .and()
                .csrf().disable();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

}

```

#### 3.2.9 配置资源服务认证
```java
package com.chennan.cloud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

import javax.servlet.http.HttpServletResponse;

/**
 * 配置资源服务认证
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .and()
                .requestMatchers().antMatchers("/api/**")
                .and()
                .authorizeRequests()
                .antMatchers("/api/**").authenticated()
                .and()
                .httpBasic();
    }
}

```
#### 3.2.10 重新编写RedisTokenStore
原因是spring-data-redis 2.0版本中set(String,String)被弃用了，要使用RedisConnection.stringCommands().set(…)，
所有我自定义一个RedisTokenStore，代码和RedisTokenStore一样，只是把所有conn.set(…)都换成conn..stringCommands().set(…)，测试后方法可行
```java
package com.chennan.cloud.config;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.JdkSerializationStrategy;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStoreSerializationStrategy;

import java.util.*;

/**
 * 重新编写 RedisTokenStore 实现
 * <p>
 *     原因是spring-data-redis 2.0版本中set(String,String)被弃用了，要使用RedisConnection.stringCommands().set(…)，
 *     所有我自定义一个RedisTokenStore，代码和RedisTokenStore一样，只是把所有conn.set(…)都换成conn..stringCommands().set(…)，测试后方法可行。
 * </p>
 * @author chen.nan 
 */
public class RedisTokenStore implements TokenStore {

    private static final String ACCESS = "access:";
    private static final String AUTH_TO_ACCESS = "auth_to_access:";
    private static final String AUTH = "auth:";
    private static final String REFRESH_AUTH = "refresh_auth:";
    private static final String ACCESS_TO_REFRESH = "access_to_refresh:";
    private static final String REFRESH = "refresh:";
    private static final String REFRESH_TO_ACCESS = "refresh_to_access:";
    private static final String CLIENT_ID_TO_ACCESS = "client_id_to_access:";
    private static final String UNAME_TO_ACCESS = "uname_to_access:";
    private final RedisConnectionFactory connectionFactory;
    private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();
    private RedisTokenStoreSerializationStrategy serializationStrategy = new JdkSerializationStrategy();
    private String prefix = "";

    public RedisTokenStore(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
        this.authenticationKeyGenerator = authenticationKeyGenerator;
    }

    public void setSerializationStrategy(RedisTokenStoreSerializationStrategy serializationStrategy) {
        this.serializationStrategy = serializationStrategy;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private RedisConnection getConnection() {
        return this.connectionFactory.getConnection();
    }

    private byte[] serialize(Object object) {
        return this.serializationStrategy.serialize(object);
    }

    private byte[] serializeKey(String object) {
        return this.serialize(this.prefix + object);
    }

    private OAuth2AccessToken deserializeAccessToken(byte[] bytes) {
        return (OAuth2AccessToken)this.serializationStrategy.deserialize(bytes, OAuth2AccessToken.class);
    }

    private OAuth2Authentication deserializeAuthentication(byte[] bytes) {
        return (OAuth2Authentication)this.serializationStrategy.deserialize(bytes, OAuth2Authentication.class);
    }

    private OAuth2RefreshToken deserializeRefreshToken(byte[] bytes) {
        return (OAuth2RefreshToken)this.serializationStrategy.deserialize(bytes, OAuth2RefreshToken.class);
    }

    private byte[] serialize(String string) {
        return this.serializationStrategy.serialize(string);
    }

    private String deserializeString(byte[] bytes) {
        return this.serializationStrategy.deserializeString(bytes);
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        String key = this.authenticationKeyGenerator.extractKey(authentication);
        byte[] serializedKey = this.serializeKey(AUTH_TO_ACCESS + key);
        byte[] bytes = null;
        RedisConnection conn = this.getConnection();
        try {
            bytes = conn.get(serializedKey);
        } finally {
            conn.close();
        }
        OAuth2AccessToken accessToken = this.deserializeAccessToken(bytes);
        if (accessToken != null) {
            OAuth2Authentication storedAuthentication = this.readAuthentication(accessToken.getValue());
            if (storedAuthentication == null || !key.equals(this.authenticationKeyGenerator.extractKey(storedAuthentication))) {
                this.storeAccessToken(accessToken, authentication);
            }
        }
        return accessToken;
    }

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return this.readAuthentication(token.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        byte[] bytes = null;
        RedisConnection conn = this.getConnection();
        try {
            bytes = conn.get(this.serializeKey("auth:" + token));
        } finally {
            conn.close();
        }
        OAuth2Authentication auth = this.deserializeAuthentication(bytes);
        return auth;
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return this.readAuthenticationForRefreshToken(token.getValue());
    }

    public OAuth2Authentication readAuthenticationForRefreshToken(String token) {
        RedisConnection conn = getConnection();
        try {
            byte[] bytes = conn.get(serializeKey(REFRESH_AUTH + token));
            OAuth2Authentication auth = deserializeAuthentication(bytes);
            return auth;
        } finally {
            conn.close();
        }
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        byte[] serializedAccessToken = serialize(token);
        byte[] serializedAuth = serialize(authentication);
        byte[] accessKey = serializeKey(ACCESS + token.getValue());
        byte[] authKey = serializeKey(AUTH + token.getValue());
        byte[] authToAccessKey = serializeKey(AUTH_TO_ACCESS + authenticationKeyGenerator.extractKey(authentication));
        byte[] approvalKey = serializeKey(UNAME_TO_ACCESS + getApprovalKey(authentication));
        byte[] clientId = serializeKey(CLIENT_ID_TO_ACCESS + authentication.getOAuth2Request().getClientId());

        RedisConnection conn = getConnection();
        try {
            conn.openPipeline();
            conn.stringCommands().set(accessKey, serializedAccessToken);
            conn.stringCommands().set(authKey, serializedAuth);
            conn.stringCommands().set(authToAccessKey, serializedAccessToken);
            if (!authentication.isClientOnly()) {
                conn.rPush(approvalKey, serializedAccessToken);
            }
            conn.rPush(clientId, serializedAccessToken);
            if (token.getExpiration() != null) {
                int seconds = token.getExpiresIn();
                conn.expire(accessKey, seconds);
                conn.expire(authKey, seconds);
                conn.expire(authToAccessKey, seconds);
                conn.expire(clientId, seconds);
                conn.expire(approvalKey, seconds);
            }
            OAuth2RefreshToken refreshToken = token.getRefreshToken();
            if (refreshToken != null && refreshToken.getValue() != null) {
                byte[] refresh = serialize(token.getRefreshToken().getValue());
                byte[] auth = serialize(token.getValue());
                byte[] refreshToAccessKey = serializeKey(REFRESH_TO_ACCESS + token.getRefreshToken().getValue());
                conn.stringCommands().set(refreshToAccessKey, auth);
                byte[] accessToRefreshKey = serializeKey(ACCESS_TO_REFRESH + token.getValue());
                conn.stringCommands().set(accessToRefreshKey, refresh);
                if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
                    ExpiringOAuth2RefreshToken expiringRefreshToken = (ExpiringOAuth2RefreshToken) refreshToken;
                    Date expiration = expiringRefreshToken.getExpiration();
                    if (expiration != null) {
                        int seconds = Long.valueOf((expiration.getTime() - System.currentTimeMillis()) / 1000L)
                                .intValue();
                        conn.expire(refreshToAccessKey, seconds);
                        conn.expire(accessToRefreshKey, seconds);
                    }
                }
            }
            conn.closePipeline();
        } finally {
            conn.close();
        }
    }

    private static String getApprovalKey(OAuth2Authentication authentication) {
        String userName = authentication.getUserAuthentication() == null ? "": authentication.getUserAuthentication().getName();
        return getApprovalKey(authentication.getOAuth2Request().getClientId(), userName);
    }

    private static String getApprovalKey(String clientId, String userName) {
        return clientId + (userName == null ? "" : ":" + userName);
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken accessToken) {
        this.removeAccessToken(accessToken.getValue());
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        byte[] key = serializeKey(ACCESS + tokenValue);
        byte[] bytes = null;
        RedisConnection conn = getConnection();
        try {
            bytes = conn.get(key);
        } finally {
            conn.close();
        }
        OAuth2AccessToken accessToken = deserializeAccessToken(bytes);
        return accessToken;
    }

    public void removeAccessToken(String tokenValue) {
        byte[] accessKey = serializeKey(ACCESS + tokenValue);
        byte[] authKey = serializeKey(AUTH + tokenValue);
        byte[] accessToRefreshKey = serializeKey(ACCESS_TO_REFRESH + tokenValue);
        RedisConnection conn = getConnection();
        try {
            conn.openPipeline();
            conn.get(accessKey);
            conn.get(authKey);
            conn.del(accessKey);
            conn.del(accessToRefreshKey);
            // Don't remove the refresh token - it's up to the caller to do that
            conn.del(authKey);
            List<Object> results = conn.closePipeline();
            byte[] access = (byte[]) results.get(0);
            byte[] auth = (byte[]) results.get(1);

            OAuth2Authentication authentication = deserializeAuthentication(auth);
            if (authentication != null) {
                String key = authenticationKeyGenerator.extractKey(authentication);
                byte[] authToAccessKey = serializeKey(AUTH_TO_ACCESS + key);
                byte[] unameKey = serializeKey(UNAME_TO_ACCESS + getApprovalKey(authentication));
                byte[] clientId = serializeKey(CLIENT_ID_TO_ACCESS + authentication.getOAuth2Request().getClientId());
                conn.openPipeline();
                conn.del(authToAccessKey);
                conn.lRem(unameKey, 1, access);
                conn.lRem(clientId, 1, access);
                conn.del(serialize(ACCESS + key));
                conn.closePipeline();
            }
        } finally {
            conn.close();
        }
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        byte[] refreshKey = serializeKey(REFRESH + refreshToken.getValue());
        byte[] refreshAuthKey = serializeKey(REFRESH_AUTH + refreshToken.getValue());
        byte[] serializedRefreshToken = serialize(refreshToken);
        RedisConnection conn = getConnection();
        try {
            conn.openPipeline();
            conn.stringCommands().set(refreshKey, serializedRefreshToken);
            conn.stringCommands().set(refreshAuthKey, serialize(authentication));
            if (refreshToken instanceof ExpiringOAuth2RefreshToken) {
                ExpiringOAuth2RefreshToken expiringRefreshToken = (ExpiringOAuth2RefreshToken) refreshToken;
                Date expiration = expiringRefreshToken.getExpiration();
                if (expiration != null) {
                    int seconds = Long.valueOf((expiration.getTime() - System.currentTimeMillis()) / 1000L)
                            .intValue();
                    conn.expire(refreshKey, seconds);
                    conn.expire(refreshAuthKey, seconds);
                }
            }
            conn.closePipeline();
        } finally {
            conn.close();
        }
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        byte[] key = serializeKey(REFRESH + tokenValue);
        byte[] bytes = null;
        RedisConnection conn = getConnection();
        try {
            bytes = conn.get(key);
        } finally {
            conn.close();
        }
        OAuth2RefreshToken refreshToken = deserializeRefreshToken(bytes);
        return refreshToken;
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken refreshToken) {
        this.removeRefreshToken(refreshToken.getValue());
    }

    public void removeRefreshToken(String tokenValue) {
        byte[] refreshKey = serializeKey(REFRESH + tokenValue);
        byte[] refreshAuthKey = serializeKey(REFRESH_AUTH + tokenValue);
        byte[] refresh2AccessKey = serializeKey(REFRESH_TO_ACCESS + tokenValue);
        byte[] access2RefreshKey = serializeKey(ACCESS_TO_REFRESH + tokenValue);
        RedisConnection conn = getConnection();
        try {
            conn.openPipeline();
            conn.del(refreshKey);
            conn.del(refreshAuthKey);
            conn.del(refresh2AccessKey);
            conn.del(access2RefreshKey);
            conn.closePipeline();
        } finally {
            conn.close();
        }
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        this.removeAccessTokenUsingRefreshToken(refreshToken.getValue());
    }

    private void removeAccessTokenUsingRefreshToken(String refreshToken) {
        byte[] key = serializeKey(REFRESH_TO_ACCESS + refreshToken);
        List<Object> results = null;
        RedisConnection conn = getConnection();
        try {
            conn.openPipeline();
            conn.get(key);
            conn.del(key);
            results = conn.closePipeline();
        } finally {
            conn.close();
        }
        if (results == null) {
            return;
        }
        byte[] bytes = (byte[]) results.get(0);
        String accessToken = deserializeString(bytes);
        if (accessToken != null) {
            removeAccessToken(accessToken);
        }
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        byte[] approvalKey = serializeKey(UNAME_TO_ACCESS + getApprovalKey(clientId, userName));
        List<byte[]> byteList = null;
        RedisConnection conn = getConnection();
        try {
            byteList = conn.lRange(approvalKey, 0, -1);
        } finally {
            conn.close();
        }
        if (byteList == null || byteList.size() == 0) {
            return Collections.<OAuth2AccessToken> emptySet();
        }
        List<OAuth2AccessToken> accessTokens = new ArrayList<OAuth2AccessToken>(byteList.size());
        for (byte[] bytes : byteList) {
            OAuth2AccessToken accessToken = deserializeAccessToken(bytes);
            accessTokens.add(accessToken);
        }
        return Collections.<OAuth2AccessToken> unmodifiableCollection(accessTokens);
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        byte[] key = serializeKey(CLIENT_ID_TO_ACCESS + clientId);
        List<byte[]> byteList = null;
        RedisConnection conn = getConnection();
        try {
            byteList = conn.lRange(key, 0, -1);
        } finally {
            conn.close();
        }
        if (byteList == null || byteList.size() == 0) {
            return Collections.<OAuth2AccessToken> emptySet();
        }
        List<OAuth2AccessToken> accessTokens = new ArrayList<OAuth2AccessToken>(byteList.size());
        for (byte[] bytes : byteList) {
            OAuth2AccessToken accessToken = deserializeAccessToken(bytes);
            accessTokens.add(accessToken);
        }
        return Collections.<OAuth2AccessToken> unmodifiableCollection(accessTokens);
    }
}

```
#### 3.2.11 密码编码器
本案例不加密，若要使用加密，请修改内部加密算法或者请实现接口 PasswordEncoder 
```java
package com.chennan.cloud.config;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码不加密实现
 * @author chen.nan 
 */
public class NoEncryptPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence charSequence) {
        return (String) charSequence;
    }

    @Override
    public boolean matches(CharSequence charSequence, String s) {
        return s.equals((String) charSequence);
    }
}

```

#### 3.2.12 配置认证服务器
```java
package com.chennan.cloud.oauth2.cfg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * 配置认证服务器
 * {@link @EnableAuthorizationServer}开启认证服务
 * @author chen.nan
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private AuthenticationManager authenticationManager;
    private DataSource dataSource;
    private UserDetailsService userDetailsService;

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Autowired
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * 认证信息存入数据库中
     * @return TokenStore
     */
    @Primary
    @Bean
    public TokenStore jdbcTokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    @Bean
    public ClientDetailsService clientDetails() {
        return new JdbcClientDetailsService(dataSource);
    }

    @Primary
    @Bean
    public DefaultTokenServices defaultTokenServices(){
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(jdbcTokenStore());
        tokenServices.setSupportRefreshToken(true);
        // token有效期自定义设置，默认12小时
        tokenServices.setAccessTokenValiditySeconds(60*60*12);
        // refresh_token默认30天
        tokenServices.setRefreshTokenValiditySeconds(60 * 60 * 24 * 7);
        return tokenServices;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.allowFormAuthenticationForClients()
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()");
    }

    /**
     * 客户端信息配置
     * @param clients 客户端
     * @throws Exception 异常
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(clientDetails());
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints){
        endpoints.tokenStore(jdbcTokenStore());
        endpoints.userDetailsService(userDetailsService);
        endpoints.authenticationManager(authenticationManager);
        endpoints.tokenServices(defaultTokenServices());
        // 默认只允许 post 请求进行登录，追加get请求
        // endpoints.allowedTokenEndpointRequestMethods(HttpMethod.POST, HttpMethod.GET);
    }
}

```

### 3.3 普通模块接入微服务认证中心
#### 3.3.1 zuul路由网关微服务配置
##### 3.3.1.1 application.yml核心配置
```yaml
# -----------------服务网关代理Zuul------------------------------------
zuul:
  # 踩坑：prefix配置项前必须有'/',否则GG
  # 访问地址：http://127.0.0.1:11000/chennan/myProvider/dept/get?deptNo=1
  # prefix: /chennan
  ignored-services: "*"
  routes:
    myProvider:
      serviceId: msc-provider
      path: /myProvider/**
    user:
      path: /user/**
      serviceId: msc-user
      sensitiveHeaders: "*"
    # 认证中心配置
    auth:
      path: /auth/**
      serviceId: oauth2-server
      sensitiveHeaders: "*"
  add-proxy-headers: true

#---------------------OAuth2---------------------
security:
  oauth2:
    client:
      access-token-uri: http://localhost:${server.port}/auth/oauth/token
      user-authorization-uri: http://localhost:${server.port}/auth/oauth/authorize
      client-id: web
    resource:
      user-info-uri:  http://localhost:${server.port}/auth/api/userInfo
      prefer-token-info: false

```
##### 3.3.1.2 添加SecurityConfig,CSRF防止跨站攻击
```java
package com.chennan.cloud.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * CSRF 防止跨站攻击
 * @author chen.nan 
 */
@Configuration
@EnableWebSecurity
@Order(99)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
    }
}
```

#### 3.3.2 普通微服务配置
参考 [msc-user](../msc-user/README.md)

#### 3.3.3 验证
##### 3.3.3.1 没有获取令牌前的访问接口
```shell script
curl -XGET http://localhost:11000/user/api/current
```
响应401未认证
##### 3.3.3.2 获取令牌
```shell script
curl -POST http://localhost:11000/auth/oauth/token -d {
"grant_type":"password",
"username":"zhang.san",
"password":"123456",
"client_id":"android",
"client_secret":"android"
} 
```
响应结果
```json
{
    "access_token": "a38fca4e-6294-4fae-b764-0f5e629cb784",
    "token_type": "bearer",
    "refresh_token": "3e3a75cd-1a6e-45e8-9ff3-4ea6e1a53ca0",
    "expires_in": 41236,
    "scope": "read"
}
```

##### 3.3.3.3 带token访问接口
```shell script
curl -XGET http://localhost:11000/user/api/current?access_token=a38fca4e-6294-4fae-b764-0f5e629cb784
```
响应结果：
```json
{
	"authenticated":true,
	"authorities":[
		{
			"authority":"exit"
		},
		{
			"authority":"current"
		},
		{
			"authority":"query"
		},
		{
			"authority":"ROLE_ADMIN"
		},
		{
			"authority":"user"
		}
	],
	"clientOnly":false,
	"credentials":"",
	"details":{
		"decodedDetails":null,
		"remoteAddress":"192.168.188.1",
		"sessionId":"",
		"tokenType":"Bearer",
		"tokenValue":"901df61f-db17-4351-98b9-2b9771cb2256"
	},
	"name":"zhang.san",
	"oAuth2Request":{
		"approved":true,
		"authorities":[],
		"clientId":"",
		"extensions":{},
		"grantType":"",
		"redirectUri":"",
		"refresh":false,
		"refreshTokenRequest":null,
		"requestParameters":{},
		"resourceIds":[],
		"responseTypes":[],
		"scope":[]
	},
	"principal":"zhang.san",
	"userAuthentication":{
		"authenticated":true,
		"authorities":[
			{"$ref":"$.authorities[0]"},
			{"$ref":"$.authorities[1]"},
			{"$ref":"$.authorities[2]"},
			{"$ref":"$.authorities[3]"},
			{"$ref":"$.authorities[4]"}
		],
		"credentials":"N/A",
		"details":{
			"authenticated":true,
			"authorities":[
				{
					"authority":"exit"
				},
				{
					"authority":"current"
				},
				{
					"authority":"query"
				},
				{
					"authority":"ROLE_ADMIN"
				},
				{
					"authority":"user"
				}
			],
			"clientOnly":false,
			"credentials":"",
			"details":{
				"decodedDetails":null,
				"remoteAddress":"192.168.188.1",
				"sessionId":"",
				"tokenType":"Bearer",
				"tokenValue":"901df61f-db17-4351-98b9-2b9771cb2256"
			},
			"name":"zhang.san",
			"oAuth2Request":{
				"approved":true,
				"authorities":[],
				"clientId":"android",
				"extensions":{},
				"grantType":"password",
				"redirectUri":"",
				"refresh":false,
				"refreshTokenRequest":null,
				"requestParameters":{
					"grant_type":"password",
					"username":"zhang.san"
				},
				"resourceIds":[],
				"responseTypes":[],
				"scope":[
					"read"
				]
			},
			"principal":{
				"accountNonExpired":true,
				"accountNonLocked":true,
				"authorities":[
					{
						"$ref":"$.authorities[0]"
					},
					{
						"$ref":"$.authorities[1]"
					},
					{
						"$ref":"$.authorities[3]"
					},
					{
						"$ref":"$.authorities[4]"
					},
					{
						"$ref":"$.authorities[2]"
					}
				],
				"credentialsNonExpired":true,
				"enabled":true,
				"password":"123456",
				"username":"zhang.san"
			},
			"userAuthentication":{
				"authenticated":true,
				"authorities":[
					{
						"$ref":"$.authorities[0]"
					},
					{
						"$ref":"$.authorities[1]"
					},
					{
						"$ref":"$.authorities[2]"
					},
					{
						"$ref":"$.authorities[3]"
					},
					{
						"$ref":"$.authorities[4]"
					}
				],
				"credentials":null,
				"details":{
					"grant_type":"password",
					"username":"zhang.san"
				},
				"name":"zhang.san",
				"principal":{
					"$ref":"$.principal"
				}
			}
		},
		"name":"zhang.san",
		"principal":"zhang.san"
	}
}
```
