package com.xxl.job.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobUserDao;
import com.xxl.job.admin.service.LoginService;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xuxueli 2019-05-04 16:39:50
 */
@Controller
@RequestMapping("/user")
public class UserController {

    @Resource
    private XxlJobUserDao xxlJobUserDao;
    @Resource
    private XxlJobGroupDao xxlJobGroupDao;

    @RequestMapping
    @PermissionLimit(adminuser = true)
    public String index(Model model) {
        // 执行器列表
        List<XxlJobGroup> groupList = xxlJobGroupDao.selectList(new QueryWrapper<XxlJobGroup>().lambda().orderByAsc(XxlJobGroup::getOrder));
        model.addAttribute("groupList", groupList);
        return "user/user.index";
    }

    @RequestMapping("/pageList")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,@RequestParam(required = false, defaultValue = "10") int length, String username, int role) {
        // page list
        Page<XxlJobUser> page = new Page<>(start, length);
        page = (Page<XxlJobUser>) xxlJobUserDao.selectPage(page, new QueryWrapper<XxlJobUser>().lambda().eq(org.apache.commons.lang3.StringUtils.isNotBlank(username), XxlJobUser::getUsername, username).eq(role > -1 ,  XxlJobUser::getRole, role).orderByAsc(XxlJobUser::getUsername));
        // package result
        Map<String, Object> maps = new HashMap<>();
        maps.put("recordsTotal", page.getTotal());		// 总记录数
        maps.put("recordsFiltered", page.getTotal());	// 过滤后的总记录数
        maps.put("data", page.getRecords());  					// 分页列表
        return maps;
    }

    @RequestMapping("/add")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public ReturnT<String> add(XxlJobUser xxlJobUser) {
        // valid username
        if (!StringUtils.hasText(xxlJobUser.getUsername()))
            return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_please_input")+I18nUtil.getString("user_username") );
        xxlJobUser.setUsername(xxlJobUser.getUsername().trim());
        if (!(xxlJobUser.getUsername().length() >= 4 && xxlJobUser.getUsername().length() <= 20))
            return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit")+"[4-20]" );
        // valid password
        if (!StringUtils.hasText(xxlJobUser.getPassword()))
            return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_please_input")+I18nUtil.getString("user_password") );
        xxlJobUser.setPassword(xxlJobUser.getPassword().trim());
        if (!(xxlJobUser.getPassword().length() >= 4 && xxlJobUser.getPassword().length() <= 20))
            return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit" ) + "[4-20]" );
        // md5 password
        xxlJobUser.setPassword(DigestUtils.md5DigestAsHex(xxlJobUser.getPassword().getBytes()));
        // check repeat
        XxlJobUser existUser = xxlJobUserDao.selectOne(new QueryWrapper<XxlJobUser>().lambda().eq(XxlJobUser::getUsername, xxlJobUser.getUsername()));
        if (existUser != null)  return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("user_username_repeat") );
        // write
        xxlJobUserDao.insert(xxlJobUser);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/update")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public ReturnT<String> update(HttpServletRequest request, XxlJobUser xxlJobUser) {
        // avoid opt login seft
        XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
        if (loginUser.getUsername().equals(xxlJobUser.getUsername()))
            return new ReturnT<>(ReturnT.FAIL.getCode(), I18nUtil.getString("user_update_loginuser_limit"));
        // valid password
        if (StringUtils.hasText(xxlJobUser.getPassword())) {
            xxlJobUser.setPassword(xxlJobUser.getPassword().trim());
            if (!(xxlJobUser.getPassword().length() >= 4 && xxlJobUser.getPassword().length() <= 20))
                return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit")+"[4-20]" );
            // md5 password
            xxlJobUser.setPassword(DigestUtils.md5DigestAsHex(xxlJobUser.getPassword().getBytes()));
        } else xxlJobUser.setPassword(null);
        // write
        xxlJobUserDao.updateById(xxlJobUser);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/remove")
    @ResponseBody
    @PermissionLimit(adminuser = true)
    public ReturnT<String> remove(HttpServletRequest request, int id) {
        // avoid opt login seft
        XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
        if (loginUser.getId() == id) return new ReturnT<>(ReturnT.FAIL.getCode(), I18nUtil.getString("user_update_loginuser_limit"));
        xxlJobUserDao.deleteById(id);
        return ReturnT.SUCCESS;
    }

    @RequestMapping("/updatePwd")
    @ResponseBody
    public ReturnT<String> updatePwd(HttpServletRequest request, String password){
        // valid password
        if (password == null || password.trim().length() == 0) return new ReturnT<>(ReturnT.FAIL.getCode(), "密码不可为空");
        password = password.trim();
        if (!(password.length() >= 4 && password.length() <= 20)) return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit")+"[4-20]" );
        // md5 password
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        // update pwd
        XxlJobUser loginUser = (XxlJobUser) request.getAttribute(LoginService.LOGIN_IDENTITY_KEY);
        // do write
        XxlJobUser existUser = xxlJobUserDao.selectOne(new QueryWrapper<XxlJobUser>().lambda().eq(XxlJobUser::getUsername, loginUser.getUsername()));
        existUser.setPassword(md5Password);
        xxlJobUserDao.updateById(existUser);
        return ReturnT.SUCCESS;
    }
}
