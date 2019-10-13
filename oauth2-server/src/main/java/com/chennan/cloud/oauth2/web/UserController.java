package com.chennan.cloud.oauth2.web;

import com.chennan.cloud.base.vo.R;
import com.chennan.cloud.oauth2.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/")
@RestController
public class UserController {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("list")
    public R list(){
        return R.ok().addData(userService.list());
    }
}
