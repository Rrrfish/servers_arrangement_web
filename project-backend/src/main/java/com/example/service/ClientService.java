package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Client;
import com.example.entity.dto.ClientDetail;
import com.example.entity.vo.request.ClientDetailVO;
import com.example.entity.vo.request.RuntimeDetailVO;
import com.example.entity.vo.response.ClientPreviewVO;

import java.util.List;

public interface ClientService extends IService<Client> {
    String registerToken();
    boolean verifyAndRegister(String token);
    Client getClientByToken(String token);
    void updateClientDetail(ClientDetailVO clientDetailVO, Client client);
    void updateRuntimeDetail(RuntimeDetailVO runtimeDetailVO, Client client);
    List<ClientPreviewVO> listClients();
}
