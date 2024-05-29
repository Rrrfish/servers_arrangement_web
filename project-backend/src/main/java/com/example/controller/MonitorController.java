package com.example.controller;

import com.example.entity.RestBean;
import com.example.entity.vo.request.RenameClientVO;
import com.example.entity.vo.response.ClientPreviewVO;
import com.example.service.ClientService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {
    @Resource
    ClientService service;

    @GetMapping("/list")
    public RestBean<List<ClientPreviewVO>> listClients() {
        return RestBean.success(service.listClients());
    }

    @PostMapping("/rename")
    public RestBean<Void> renameClient(@RequestBody RenameClientVO vo) {
        service.renameClient(vo);
        return RestBean.success();
    }
}
