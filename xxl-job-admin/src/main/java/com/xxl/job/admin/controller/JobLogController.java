package com.xxl.job.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxl.job.admin.core.scheduler.XxlJobScheduler;
import com.xxl.job.admin.core.exception.XxlJobException;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobLogDao;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.model.LogResult;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
@RequestMapping("/joblog")
public class JobLogController {
	private static Logger logger = LoggerFactory.getLogger(JobLogController.class);

	@Resource
	private XxlJobGroupDao xxlJobGroupDao;
	@Resource
	public XxlJobInfoDao xxlJobInfoDao;
	@Resource
	public XxlJobLogDao xxlJobLogDao;

	@RequestMapping
	public String index(HttpServletRequest request, Model model, @RequestParam(required = false, defaultValue = "0") Integer jobId) {
		// 执行器列表
		List<XxlJobGroup> jobGroupList_all =  xxlJobGroupDao.selectList(new QueryWrapper<XxlJobGroup>().lambda().orderByAsc(XxlJobGroup::getOrder));
		// filter group
		List<XxlJobGroup> jobGroupList = JobInfoController.filterJobGroupByRole(request, jobGroupList_all);
		if (jobGroupList==null || jobGroupList.size()==0) throw new XxlJobException(I18nUtil.getString("jobgroup_empty"));
		model.addAttribute("JobGroupList", jobGroupList);
		// 任务
		if (jobId > 0) {
			XxlJobInfo jobInfo = xxlJobInfoDao.selectById(jobId);
			if (jobInfo == null) throw new RuntimeException(I18nUtil.getString("jobinfo_field_id") + I18nUtil.getString("system_unvalid"));
			model.addAttribute("jobInfo", jobInfo);
			// valid permission
			JobInfoController.validPermission(request, jobInfo.getJobGroup());
		}
		return "joblog/joblog.index";
	}

	@RequestMapping("/getJobsByGroup")
	@ResponseBody
	public ReturnT<List<XxlJobInfo>> getJobsByGroup(int jobGroup){
		return new ReturnT<>(xxlJobInfoDao.selectList(new QueryWrapper<XxlJobInfo>().lambda().eq(XxlJobInfo::getJobGroup, jobGroup)));
	}
	
	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") int start, @RequestParam(required = false, defaultValue = "10") int length, int jobGroup, int jobId, int logStatus, String filterTime) {
		// valid permission
		JobInfoController.validPermission(request, jobGroup);	// 仅管理员支持查询全部；普通用户仅支持查询有权限的 jobGroup
		// parse param
		Date triggerTimeStart = null;
		Date triggerTimeEnd = null;
		if (filterTime!=null && filterTime.trim().length()>0) {
			String[] temp = filterTime.split(" - ");
			if (temp.length == 2) {
				triggerTimeStart = DateUtil.parseDateTime(temp[0]);
				triggerTimeEnd = DateUtil.parseDateTime(temp[1]);
			}
		}
		// page query
		Page<XxlJobLog> page = new Page<>(start, length);
		page = (Page<XxlJobLog>) xxlJobLogDao.selectPage(page, new QueryWrapper<XxlJobLog>().lambda()
				.eq(jobId == 0 && jobGroup > 0 , XxlJobLog::getJobGroup, jobGroup)
				.eq(jobId > 0 , XxlJobLog::getJobId, jobId)
				.ge(triggerTimeStart != null, XxlJobLog::getTriggerTime, triggerTimeStart)
				.le(triggerTimeEnd != null, XxlJobLog::getTriggerTime, triggerTimeEnd)
				.and(logStatus == 1, wrapper -> wrapper.eq(XxlJobLog::getHandleCode, 200))
				// AND ( t.trigger_code NOT IN (0, 200) OR t.handle_code NOT IN (0, 200) )
				.and(logStatus == 2, wrapper -> wrapper.notIn(XxlJobLog::getTriggerCode, Arrays.asList(0, 200)).or().notIn(XxlJobLog::getHandleCode, Arrays.asList(0, 200)))
				.and(logStatus == 3, wrapper -> wrapper.eq(XxlJobLog::getTriggerCode, 200).eq(XxlJobLog::getHandleCode, 0))
				.orderByDesc(XxlJobLog::getTriggerTime)
		);
		// package result
		Map<String, Object> maps = new HashMap<>();
	    maps.put("recordsTotal", page.getTotal());		// 总记录数
	    maps.put("recordsFiltered", page.getTotal());	// 过滤后的总记录数
	    maps.put("data", page.getRecords());  					// 分页列表
		return maps;
	}

	@RequestMapping("/logDetailPage")
	public String logDetailPage(int id, Model model){
		// base check
		// ReturnT<String> logStatue = ReturnT.SUCCESS;
		XxlJobLog jobLog = xxlJobLogDao.selectById(id);
		if (jobLog == null) throw new RuntimeException(I18nUtil.getString("joblog_logid_unvalid"));
        model.addAttribute("triggerCode", jobLog.getTriggerCode());
        model.addAttribute("handleCode", jobLog.getHandleCode());
        model.addAttribute("executorAddress", jobLog.getExecutorAddress());
        model.addAttribute("triggerTime", jobLog.getTriggerTime().getTime());
        model.addAttribute("logId", jobLog.getId());
		return "joblog/joblog.detail";
	}

	@RequestMapping("/logDetailCat")
	@ResponseBody
	public ReturnT<LogResult> logDetailCat(String executorAddress, long triggerTime, long logId, int fromLineNum){
		try {
			ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(executorAddress);
			ReturnT<LogResult> logResult = executorBiz.log(triggerTime, logId, fromLineNum);
			// is end
            if (logResult.getContent()!=null && logResult.getContent().getFromLineNum() > logResult.getContent().getToLineNum()) {
                XxlJobLog jobLog = xxlJobLogDao.selectById(logId);
                if (jobLog.getHandleCode() > 0) logResult.getContent().setEnd(true);
            }
			return logResult;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ReturnT<>(ReturnT.FAIL_CODE, e.getMessage());
		}
	}

	@RequestMapping("/logKill")
	@ResponseBody
	public ReturnT<String> logKill(int id){
		// base check
		XxlJobLog log = xxlJobLogDao.selectById(id);
		XxlJobInfo jobInfo = xxlJobInfoDao.selectById(log.getJobId());
		if (jobInfo==null) return new ReturnT<>(500, I18nUtil.getString("jobinfo_glue_jobid_unvalid"));
		if (ReturnT.SUCCESS_CODE != log.getTriggerCode()) return new ReturnT<>(500, I18nUtil.getString("joblog_kill_log_limit"));
		// request of kill
		ReturnT<String> runResult;
		try {
			ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(log.getExecutorAddress());
			runResult = executorBiz.kill(jobInfo.getId());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			runResult = new ReturnT<>(500, e.getMessage());
		}
		if (ReturnT.SUCCESS_CODE == runResult.getCode()) {
			log.setHandleCode(ReturnT.FAIL_CODE);
			log.setHandleMsg( I18nUtil.getString("joblog_kill_log_byman")+":" + (runResult.getMsg()!=null?runResult.getMsg():""));
			log.setHandleTime(new Date());
			xxlJobLogDao.updateById(log);
			return new ReturnT<>(runResult.getMsg());
		} else return new ReturnT<>(500, runResult.getMsg());
	}

	@RequestMapping("/clearLog")
	@ResponseBody
	public ReturnT<String> clearLog(int jobGroup, int jobId, int type){
		Date clearBeforeTime = null;
		int clearBeforeNum = 0;
		switch (type){
			case 1: clearBeforeTime = DateUtil.addMonths(new Date(), -1);	break;	// 清理一个月之前日志数据
			case 2: clearBeforeTime = DateUtil.addMonths(new Date(), -3); 	break;	// 清理三个月之前日志数据
			case 3: clearBeforeTime = DateUtil.addMonths(new Date(), -6); 	break;	// 清理六个月之前日志数据
			case 4: clearBeforeTime = DateUtil.addYears(new Date(), -1); 	break;	// 清理一年之前日志数据
			case 5: clearBeforeNum = 1000; 											break;	// 清理一千条以前日志数据
			case 6: clearBeforeNum = 10000;											break;	// 清理一万条以前日志数据
			case 7: clearBeforeNum = 30000;	 										break;	// 清理三万条以前日志数据
			case 8: clearBeforeNum = 100000; 										break;	// 清理十万条以前日志数据
			case 9: clearBeforeNum = 0;	 											break;	// 清理所有日志数据
			default: return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("joblog_clean_type_unvalid"));
		}
		List<Long> logIds;
		do {
			logIds = xxlJobLogDao.findClearLogIds(jobGroup, jobId, clearBeforeTime, clearBeforeNum, 1000);
			if (logIds != null && logIds.size() > 0) xxlJobLogDao.deleteBatchIds(logIds);
		} while (logIds != null && logIds.size() > 0);
		return ReturnT.SUCCESS;
	}

}
