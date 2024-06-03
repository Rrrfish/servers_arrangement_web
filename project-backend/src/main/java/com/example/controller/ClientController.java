package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.dto.Client;
import com.example.entity.dto.ClientDetail;
import com.example.entity.vo.request.ClientDetailVO;
import com.example.entity.vo.request.RuntimeDetailVO;
import com.example.service.ClientService;
import com.example.utils.Config;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
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

    @PostMapping("/detail")
    public RestBean<Void> updateClientDetails(@RequestAttribute(Config.ALTER_CLIENT) Client client,
                                              @RequestBody @Valid ClientDetailVO clientDetailVO) {
//        System.out.println(clientDetailVO);
        service.updateClientDetail( clientDetailVO, client);
        return RestBean.success();
    }

    @PostMapping("/runtime")
    public RestBean<Void> updateRuntimeDetails(@RequestAttribute(Config.ALTER_CLIENT) Client client,
                                               @RequestBody @Valid RuntimeDetailVO runtimeDetailVO) {
        service.updateRuntimeDetail(runtimeDetailVO, client);
        return RestBean.success();
    }

}
