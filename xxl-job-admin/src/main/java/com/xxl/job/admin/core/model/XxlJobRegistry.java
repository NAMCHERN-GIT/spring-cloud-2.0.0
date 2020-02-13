package com.xxl.job.admin.core.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("XXL_JOB_REGISTRY")
public class XxlJobRegistry {

    @TableId
    private int id;
    private String registryGroup;
    private String registryKey;
    private String registryValue;
    private Date updateTime;
}
