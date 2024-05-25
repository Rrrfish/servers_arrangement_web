package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.request.EmailRegisterVO;
import com.example.service.AccountService;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.HttpRequestHandlerServlet;

@RestController
@Validated
@RequestMapping("/api/auth")
public class AuthorizeController {
    @Resource
    AccountService accountService;

    @GetMapping("ask-code")
    public RestBean<Void> askVerifyCode(@RequestParam @Email String email,
                                        @RequestParam @Pattern(regexp = "(register|reset)")
                                        String type,
                                        HttpServletRequest request) {
        String message = accountService.registerEmailVarifyCode(type, email,
                request.getRemoteAddr());
        if(message == null) {
            return RestBean.success();
        } else {
            return RestBean.fail(400, message);
        }
    }

    @PostMapping("/register")
    public RestBean<Void> registerAccount(@Valid @RequestBody EmailRegisterVO vo) {
        String message = accountService.registerEmailAccount(vo);
        if(message == null) {
            return RestBean.success();
        } else {
            return RestBean.fail(400, message);
        }
    }
}
