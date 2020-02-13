package com.xxl.job.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.admin.core.thread.JobTriggerPoolHelper;
import com.xxl.job.admin.core.trigger.TriggerTypeEnum;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobLogDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

/**
 * @author xuxueli 2017-07-27 21:54:20
 */
@Slf4j
@Service
public class AdminBizImpl implements AdminBiz {
    @Resource
    public XxlJobLogDao xxlJobLogDao;
    @Resource
    private XxlJobInfoDao xxlJobInfoDao;
    @Resource
    private XxlJobRegistryDao xxlJobRegistryDao;

    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        for (HandleCallbackParam handleCallbackParam: callbackParamList) {
            ReturnT<String> callbackResult = callback(handleCallbackParam);
            log.debug(">>>>>>>>> JobApiController.callback {}, handleCallbackParam={}, callbackResult={}", (callbackResult.getCode() == IJobHandler.SUCCESS.getCode() ? "success" : "fail"), handleCallbackParam, callbackResult );
        }
        return ReturnT.SUCCESS;
    }

    private ReturnT<String> callback(HandleCallbackParam handleCallbackParam) {
        // valid log item
        XxlJobLog log = xxlJobLogDao.selectById(handleCallbackParam.getLogId());
        if (log == null) return new ReturnT<>(ReturnT.FAIL_CODE, "log item not found.");
        if (log.getHandleCode() > 0) return new ReturnT<>(ReturnT.FAIL_CODE, "log repeate callback.");     // avoid repeat callback, trigger child job etc
        // trigger success, to trigger child job
        StringBuilder callbackMsg = null;
        if (IJobHandler.SUCCESS.getCode() == handleCallbackParam.getExecuteResult().getCode()) {
            XxlJobInfo xxlJobInfo = xxlJobInfoDao.selectById(log.getJobId());
            if (xxlJobInfo != null && xxlJobInfo.getChildJobId() != null && xxlJobInfo.getChildJobId().trim().length() > 0) {
                callbackMsg = new StringBuilder("<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>" + I18nUtil.getString("jobconf_trigger_child_run") + "<<<<<<<<<<< </span><br>");
                String[] childJobIds = xxlJobInfo.getChildJobId().split(",");
                for (int i = 0; i < childJobIds.length; i++) {
                    int childJobId = (childJobIds[i]!=null && childJobIds[i].trim().length()>0 && isNumeric(childJobIds[i]))?Integer.parseInt(childJobIds[i]):-1;
                    if (childJobId > 0) {
                        JobTriggerPoolHelper.trigger(childJobId, TriggerTypeEnum.PARENT, -1, null, null);
                        ReturnT<String> triggerChildResult = ReturnT.SUCCESS;
                        // add msg
                        callbackMsg.append(MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg1"), (i + 1), childJobIds.length, childJobIds[i], (triggerChildResult.getCode() == ReturnT.SUCCESS_CODE ? I18nUtil.getString("system_success") : I18nUtil.getString("system_fail")), triggerChildResult.getMsg()));
                    } else callbackMsg.append(MessageFormat.format(I18nUtil.getString("jobconf_callback_child_msg2"), (i + 1), childJobIds.length, childJobIds[i]));
                }
            }
        }

        // handle msg
        StringBuilder handleMsg = new StringBuilder();
        if (log.getHandleMsg()!=null) handleMsg.append(log.getHandleMsg()).append("<br>");
        if (handleCallbackParam.getExecuteResult().getMsg() != null) handleMsg.append(handleCallbackParam.getExecuteResult().getMsg());
        if (callbackMsg != null) handleMsg.append(callbackMsg);
        // success, save log
        log.setHandleTime(new Date());
        log.setHandleCode(handleCallbackParam.getExecuteResult().getCode());
        log.setHandleMsg(handleMsg.toString());
        xxlJobLogDao.updateById(log);
        return ReturnT.SUCCESS;
    }

    private boolean isNumeric(String str){
        try {
            Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        // valid
        if (!StringUtils.hasText(registryParam.getRegistryGroup()) || !StringUtils.hasText(registryParam.getRegistryKey()) || !StringUtils.hasText(registryParam.getRegistryValue()))
            return new ReturnT<>(ReturnT.FAIL_CODE, "Illegal Argument.");
        int ret = xxlJobRegistryDao.update(new XxlJobRegistry().setUpdateTime(new Date()), new UpdateWrapper<XxlJobRegistry>().lambda().eq(XxlJobRegistry::getRegistryGroup, registryParam.getRegistryGroup()).eq(XxlJobRegistry::getRegistryKey, registryParam.getRegistryKey()).eq(XxlJobRegistry::getRegistryValue, registryParam.getRegistryValue()));
        if (ret < 1) {
            xxlJobRegistryDao.insert(new XxlJobRegistry().setRegistryGroup(registryParam.getRegistryGroup()).setRegistryKey(registryParam.getRegistryKey()).setRegistryValue(registryParam.getRegistryValue()).setUpdateTime(new Date()));
            // fresh
            freshGroupRegistryInfo(registryParam);
        }
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        // valid
        if (!StringUtils.hasText(registryParam.getRegistryGroup()) || !StringUtils.hasText(registryParam.getRegistryKey()) || !StringUtils.hasText(registryParam.getRegistryValue()))
            return new ReturnT<>(ReturnT.FAIL_CODE, "Illegal Argument.");
        int ret = xxlJobRegistryDao.delete(new UpdateWrapper<XxlJobRegistry>().lambda().eq(XxlJobRegistry::getRegistryGroup, registryParam.getRegistryGroup()).eq(XxlJobRegistry::getRegistryKey, registryParam.getRegistryKey()).eq(XxlJobRegistry::getRegistryValue, registryParam.getRegistryValue()));
        if (ret > 0) freshGroupRegistryInfo(registryParam); // fresh
        return ReturnT.SUCCESS;
    }
    private void freshGroupRegistryInfo(RegistryParam registryParam){
        // Under consideration, prevent affecting core tables
        log.debug("registryParam's value is 【{}】", registryParam.toString());
    }
}
