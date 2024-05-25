package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.HttpRequestHandlerServlet;

@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {
    @Resource
    AccountService accountService;

    @GetMapping("ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam String email,
                                        @RequestParam String type,
                                        HttpServletRequest request) {
        String message = accountService.registerEmailVarifyCode(type, email,
                request.getRemoteAddr());
        if(message == null) {
            return RestBean.success();
        } else {
            return RestBean.fail(400, message);
        }
    }
}
