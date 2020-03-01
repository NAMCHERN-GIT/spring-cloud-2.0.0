package com.chennan.cloud.oauth2.bo;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@TableName("SYS_ROLE")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long roleId;
    private String roleName;
    private String status;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    @TableField(exist = false)
    private List<Permission> permissionList;
}
