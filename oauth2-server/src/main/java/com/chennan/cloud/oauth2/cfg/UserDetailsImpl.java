package com.chennan.cloud.oauth2.cfg;

import com.chennan.cloud.oauth2.bo.Permission;
import com.chennan.cloud.oauth2.bo.Role;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

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

    private List<Role> roleList;
    private List<Permission> permissionList;


    public UserDetailsImpl(){}

    public UserDetailsImpl(String userName, String password, Collection<? extends GrantedAuthority> authorities,
                           List<Role> roleList, List<Permission> permissionList,
                           boolean isAccountNonExpired, boolean isAccountNonLocked, boolean isCredentialsNonExpired, boolean isEnabled) {
        this.userName = userName;
        this.password = password;
        this.authorities = authorities;
        this.roleList = roleList;
        this.permissionList = permissionList;
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
