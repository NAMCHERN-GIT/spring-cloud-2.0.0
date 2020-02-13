package com.xxl.job.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class XxlJobGroupService extends ServiceImpl<XxlJobGroupDao, XxlJobGroup> {

    public List<XxlJobGroup> findAll(){
        return list(new QueryWrapper<XxlJobGroup>().lambda().orderByAsc(XxlJobGroup::getOrder));
    }

    public List<XxlJobGroup> findByAddressType(int addressType){
        return list(new QueryWrapper<XxlJobGroup>().lambda().eq(XxlJobGroup::getAddressType, addressType).orderByAsc(XxlJobGroup::getOrder));
    }

}
