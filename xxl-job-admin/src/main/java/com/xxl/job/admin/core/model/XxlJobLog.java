package com.xxl.job.admin.core.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * xxl-job log, used to track trigger process
 * @author xuxueli  2015-12-19 23:19:09
 */
@Data
@Accessors(chain = true)
@TableName("XXL_JOB_LOG")
public class XxlJobLog {

	@TableId
	private long id;
	
	// job info
	private int jobGroup;
	private int jobId;

	// execute info
	private String executorAddress;
	private String executorHandler;
	private String executorParam;
	private String executorShardingParam;
	private int executorFailRetryCount;
	
	// trigger info
	private Date triggerTime;
	private int triggerCode;
	private String triggerMsg;
	
	// handle info
	private Date handleTime;
	private int handleCode;
	private String handleMsg;

	// alarm info
	private int alarmStatus;
}
