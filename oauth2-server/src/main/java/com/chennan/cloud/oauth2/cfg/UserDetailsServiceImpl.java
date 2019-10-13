package com.chennan.cloud.oauth2.cfg;

import com.chennan.cloud.oauth2.bo.Permission;
import com.chennan.cloud.oauth2.bo.User;
import com.chennan.cloud.oauth2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * UserDetailsService 接口实现
 * @author chen.nan
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User userInfo = userService.getUserInfo(userName);
        if (userInfo == null) throw new UsernameNotFoundException(userName);
        List<Permission> permissionList = userService.findPermissionByUserId(userInfo);
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();

        /*
        // 以下4个boolean类型的属性暂时写死
        // 可用性 :true:可用 false:不可用
        boolean isEnabled = true;
        // 过期性 :true:没过期 false:过期
        boolean isAccountNonExpired = true;
        // 有效性 :true:凭证有效 false:凭证无效
        boolean isCredentialsNonExpired = true;
        // 锁定性 :true:未锁定 false:已锁定
        boolean isAccountNonLocked = true;
        */

        userInfo.getRoleList().forEach(role -> {
            //角色必须是ROLE_开头，可以在数据库中设置
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role.getRoleName());
            grantedAuthorities.add(grantedAuthority);
            permissionList.forEach(permission -> {
                GrantedAuthority authority = new SimpleGrantedAuthority(permission.getUri());
                grantedAuthorities.add(authority);
            });
        });

        return new UserDetailsImpl(userInfo.getUserName()
                , userInfo.getPassword()
                , grantedAuthorities
                , userInfo.getRoleList()
                , permissionList
                , /*isAccountNonExpired*/true
                , /*isAccountNonLocked*/true
                , /*isCredentialsNonExpired*/true
                , /*isEnabled*/true);
    }
}
