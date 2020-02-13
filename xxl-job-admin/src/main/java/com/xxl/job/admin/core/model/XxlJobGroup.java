package com.xxl.job.admin.core.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xuxueli on 16/9/30.
 */
@Data
@Accessors(chain = true)
@TableName("XXL_JOB_GROUP")
public class XxlJobGroup {

    @TableId
    private int id;
    private String appName;
    private String title;
    @TableField("`order`")
    private int order;
    private int addressType;        // 执行器地址类型：0=自动注册、1=手动录入
    private String addressList;     // 执行器地址列表，多地址逗号分隔(手动录入)

    // registry list
    @TableField(exist = false)
    private List<String> registryList;  // 执行器地址列表(系统注册)
}
