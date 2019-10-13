package com.chennan.cloud.oauth2.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chennan.cloud.oauth2.bo.Permission;
import com.chennan.cloud.oauth2.bo.Role;
import com.chennan.cloud.oauth2.bo.User;
import com.chennan.cloud.oauth2.dao.UserMapper;
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
        return userInfo.setRoleList(roleList);
    }

    public List<Permission> findPermissionByUserId(User user){
        return baseMapper.findPermissionByUserId(user);
    }

}
