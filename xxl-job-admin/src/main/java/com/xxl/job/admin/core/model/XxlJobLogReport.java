package com.xxl.job.admin.core.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@TableName("XXL_JOB_LOG_REPORT")
public class XxlJobLogReport {

    @TableId
    private int id;
    private Date triggerDay;
    private int runningCount;
    private int sucCount;
    private int failCount;
}
