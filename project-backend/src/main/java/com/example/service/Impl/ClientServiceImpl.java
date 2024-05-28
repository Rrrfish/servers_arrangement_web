package com.example.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Client;
import com.example.entity.dto.ClientDetail;
import com.example.entity.vo.request.ClientDetailVO;
import com.example.mapper.ClientDetailMapper;
import com.example.mapper.ClientMapper;
import com.example.service.ClientService;
import com.fasterxml.jackson.databind.util.BeanUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClientServiceImpl extends ServiceImpl<ClientMapper, Client> implements ClientService {
    private String registerToken = generateToken();

    @Resource
    ClientDetailMapper detailMapper;

    @Override
    public String registerToken() {
        return registerToken;
    }

    private final Map<Integer, Client> clientIdCache = new ConcurrentHashMap<>();
    private final Map<String, Client> clientTokenCache = new ConcurrentHashMap<>();

    private void addClientCache(Client client) {
        clientIdCache.put(client.getId(), client);
        clientTokenCache.put(client.getToken(), client);
    }

    @PostConstruct
    public void initCache() {            //在刚初始化的时候就将所有客户端加入缓存，便于访问，反正服务器也不多
        this.list().forEach(this::addClientCache);
    }

    @Override
    public boolean verifyAndRegister(String token) {
        if(token.equals(registerToken)) {
            int id = generateRandomId();
            Client client = new Client(id, "未命名主机", token, new Date());
            registerToken = generateToken();
            if (!this.save(client)) {   //如果生成的token已生成过
                client.setToken(registerToken);
                registerToken = generateToken();
            }
            addClientCache(client);     //记得放入缓存！！！
            return true;
//            if (this.save(client)) {
//                registerToken = generateToken();
//                this.addClientCache(client);
//                return true;
            }

        return false;
    }

    @Override
    public Client getClientByToken(String token) {
        return clientTokenCache.get(token);
    }

    @Override
    public void updateClientDetail(ClientDetailVO clientDetailVO, Client client) {
        ClientDetail detail = new ClientDetail();
        System.out.println(clientDetailVO);
        BeanUtils.copyProperties(clientDetailVO, detail);
        detail.setId(client.getId());
        System.out.println(detail);
        if(detailMapper.selectById(client.getId()) != null) {
            detailMapper.updateById(detail);
        } else {
            detailMapper.insert(detail);
        }
    }

    private int generateRandomId() {
        return new Random().nextInt(90000000) + 10000000; //生成八位随机数
    }

    public static String generateToken()
    {
        String token = UUID.randomUUID().toString().replace("-", "");
        System.out.println(token);
        return token;
    }


}
