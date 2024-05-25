package com.example.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Account;
import com.example.mapper.AccountMapper;
import com.example.service.AccountService;
import com.example.utils.Config;
import com.example.utils.FlowUtils;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.example.utils.Config.VERIFY_EMAIL_LIMIT;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService{
    @Resource
    AmqpTemplate amqpTemplate;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    FlowUtils flowUtils;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByUsernameOrEmail(username);
        if (account == null) {
            System.out.println("User not found: " + username);
            throw new UsernameNotFoundException("用戶不存在");
        }
        System.out.println("????????????????/");
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }

    public Account findAccountByUsernameOrEmail(String context){
        return this.query().eq("username", context).or().eq("email", context).one();

    }

    @Override
    public String registerEmailVarifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {
            if (!verifyLimit(ip)) return "请求频繁，请稍后再试";
            Random random = new Random();
            int code = random.nextInt(900000) + 99999; //保证验证码位数
            Map<String, Object> map = Map.of("type", type, "email", email, "code", code);
            amqpTemplate.convertAndSend("mail", map);
            stringRedisTemplate.opsForValue()
                    .set(Config.VERIFY_EMAIL_DATA + "email", email, 3, TimeUnit.MINUTES);

            return null;
        }
    }

    private boolean verifyLimit(String ip) {
        String key = VERIFY_EMAIL_LIMIT + ip;
        return flowUtils.limitOnceCheck(key, 60);
    }
}
