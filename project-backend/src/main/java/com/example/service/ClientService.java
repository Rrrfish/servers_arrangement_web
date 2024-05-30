package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.dto.Client;
import com.example.entity.vo.request.ClientDetailVO;
import com.example.entity.vo.request.RenameClientVO;
import com.example.entity.vo.request.RenameNodeVO;
import com.example.entity.vo.request.RuntimeDetailVO;
import com.example.entity.vo.response.ClientDetailsVO;
import com.example.entity.vo.response.ClientPreviewVO;
import com.example.entity.vo.response.RuntimeHistoryVO;

import java.util.List;

public interface ClientService extends IService<Client> {
    String registerToken();
    boolean verifyAndRegister(String token);
    Client getClientByToken(String token);
    void updateClientDetail(ClientDetailVO clientDetailVO, Client client);
    void updateRuntimeDetail(RuntimeDetailVO runtimeDetailVO, Client client);
    List<ClientPreviewVO> listClients();
    void renameClient(RenameClientVO vo);
    void renameNode(RenameNodeVO vo);
    ClientDetailsVO clientDetails(int clientId);
    RuntimeHistoryVO runtimeDetailsHistory(int clientId);
    RuntimeDetailVO runtimeDetailNow(int clientId);
}
