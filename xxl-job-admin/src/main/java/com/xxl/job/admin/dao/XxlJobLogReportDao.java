package com.xxl.job.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxl.job.admin.core.model.XxlJobLogReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface XxlJobLogReportDao extends BaseMapper<XxlJobLogReport> {

	@Select("SELECT SUM(running_count) AS runningCount, SUM(suc_count) AS sucCount, SUM(fail_count) AS failCount FROM xxl_job_log_report AS t")
	XxlJobLogReport queryLogReportTotal();

}
