package com.chennan.cloud.oauth2.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chennan.cloud.oauth2.bo.Permission;
import com.chennan.cloud.oauth2.bo.Role;
import com.chennan.cloud.oauth2.bo.User;
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
    @Select("SELECT r.* FROM SYS_USER_ROLE ur INNER JOIN SYS_ROLE r ON ur.ROLE_ID = r.ROLE_ID WHERE ur.USER_ID = #{userId}")
    List<Role> findRoleList(User user);

    /**
     * 根据角色ID查询权限列表
     * @param role 封装了roleId
     * @return List<Permission>
     */
    @Select("SELECT p.* FROM SYS_ROLE_PERMISSION rp INNER JOIN SYS_PERMISSION p ON RP.PERMISSION_ID = P.PERMISSION_ID WHERE RP.ROLE_ID = #{roleId}")
    List<Permission> findPermission(Role role);

    @Select("SELECT DISTINCT p.* FROM SYS_PERMISSION p INNER JOIN SYS_ROLE_PERMISSION rp ON RP.PERMISSION_ID = P.PERMISSION_ID INNER JOIN SYS_USER_ROLE ur ON ur.ROLE_ID = rp.ROLE_ID WHERE ur.USER_ID = #{userId}")
    List<Permission> findPermissionByUserId(User user);
}
