package com.chennan.cloud.oauth2.web;

import com.chennan.cloud.base.vo.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * web restful层接口实现
 * @author chen.nan
 */
@RequestMapping("/api/")
@RestController
public class Oauth2Controller {

    private ConsumerTokenServices consumerTokenServices;

    @Autowired
    public void setConsumerTokenServices(ConsumerTokenServices consumerTokenServices) {
        this.consumerTokenServices = consumerTokenServices;
    }

    /**
     * 用户认证登录
     */
    @GetMapping("userInfo")
    public Principal user(Principal userInfo) {
        return userInfo;
    }

    /**
     * 退出，销毁accessToken
     */
    @PostMapping(value = "exit")
    public R revokeToken(String access_token) {
        if (!consumerTokenServices.revokeToken(access_token))return R.err("注销失败");
        return R.ok();
    }

}
