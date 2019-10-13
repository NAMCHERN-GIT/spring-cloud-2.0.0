package com.chennan.cloud.oauth2.cfg;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码不加密实现
 * @author chen.nan
 */
public class NoEncryptPasswordEncoder implements PasswordEncoder {

    @Override
    public String encode(CharSequence charSequence) {
        return (String) charSequence;
    }

    @Override
    public boolean matches(CharSequence charSequence, String s) {
        return s.equals((String) charSequence);
    }
}
