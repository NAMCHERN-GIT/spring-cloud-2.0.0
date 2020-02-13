package com.xxl.job.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface XxlJobRegistryDao extends BaseMapper<XxlJobRegistry> {

    @Select("SELECT t.id FROM xxl_job_registry AS t WHERE t.update_time <  DATE_ADD(#{nowTime},INTERVAL -#{timeout} SECOND)")
    List<Integer> findDead(@Param("timeout") int timeout, @Param("nowTime") Date nowTime);

    @Select("select * from xxl_job_registry WHERE update_time > DATE_ADD(#{nowTime},INTERVAL -#{timeout} SECOND)")
    List<XxlJobRegistry> findAll(@Param("timeout") int timeout, @Param("nowTime") Date nowTime);

}
