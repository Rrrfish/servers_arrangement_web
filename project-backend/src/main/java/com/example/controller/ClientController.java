package com.example.controller;

import com.example.entity.RestBean;
import com.example.service.ClientService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitor")
public class ClientController {
    @Resource
    private ClientService service;

    @GetMapping("/register")
    public RestBean<Void> registerClient(@RequestHeader("Authorization") String token) {
        return service.verifyAndRegister(token) ?
                RestBean.success() : RestBean.fail(401, "服务器注册失败");
    }
}
