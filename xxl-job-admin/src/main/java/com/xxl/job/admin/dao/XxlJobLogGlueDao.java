package com.xxl.job.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxl.job.admin.core.model.XxlJobLogGlue;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface XxlJobLogGlueDao extends BaseMapper<XxlJobLogGlue> {

	@Delete("DELETE FROM XXL_JOB_LOGGLUE WHERE id NOT IN( SELECT id FROM XXL_JOB_LOGGLUE WHERE `job_id` = #{jobId} ORDER BY update_time desc LIMIT 0, #{limit} ) AND `job_id` = #{jobId}")
	void removeOld(@Param("jobId") int jobId, @Param("limit") int limit);
}
