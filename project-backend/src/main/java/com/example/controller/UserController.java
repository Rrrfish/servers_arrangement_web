package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.request.ChangePasswordVO;
import com.example.service.AccountService;
import com.example.utils.Config;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    AccountService service;

    @PostMapping("/change-password")
    public RestBean<Void> changePassword(@RequestBody @Valid ChangePasswordVO vo
//                                         ,@RequestAttribute(Config.ALTER_USER_ID) int userId
    ) {
        int userId = 1;
        if(service.changePassword(vo, userId)) {
            return RestBean.success();
        } else {
            return RestBean.fail(401, "原密码输入错误");
        }
    }
}
