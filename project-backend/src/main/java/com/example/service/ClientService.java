package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Client;

public interface ClientService extends IService<Client> {
    boolean verifyAndRegister(String token);
    Client getClientByToken(String token);
}
