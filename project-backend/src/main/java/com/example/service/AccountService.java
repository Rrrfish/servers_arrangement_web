package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Account;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public interface AccountService extends IService<Account>, UserDetailsService {
    public Account findAccountByUsernameOrEmail(String context);

    /// type区分是什么类型的邮件, 利用ip来限制邮箱验证码的发送频率
    String registerEmailVarifyCode(String type, String email, String ip);
}
