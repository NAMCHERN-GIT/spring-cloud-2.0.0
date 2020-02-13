package com.xxl.job.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxl.job.admin.core.model.XxlJobLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface XxlJobLogDao extends BaseMapper<XxlJobLog> {

	Map<String, Object> findLogReport(@Param("from") Date from, @Param("to") Date to);

	List<Long> findClearLogIds(@Param("jobGroup") int jobGroup, @Param("jobId") int jobId, @Param("clearBeforeTime") Date clearBeforeTime, @Param("clearBeforeNum") int clearBeforeNum, @Param("pagesize") int pagesize);

	List<Long> findFailJobLogIds(@Param("pagesize") int pagesize);
}
