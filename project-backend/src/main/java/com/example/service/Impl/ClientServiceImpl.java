package com.example.service.Impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.dto.Client;
import com.example.entity.dto.ClientDetail;
import com.example.entity.dto.ClientSsh;
import com.example.entity.vo.request.*;
import com.example.entity.vo.response.ClientDetailsVO;
import com.example.entity.vo.response.ClientPreviewVO;
import com.example.entity.vo.response.RuntimeHistoryVO;
import com.example.entity.vo.response.SshSettingsVO;
import com.example.mapper.ClientDetailMapper;
import com.example.mapper.ClientMapper;
import com.example.mapper.ClientSshMapper;
import com.example.service.ClientService;
import com.example.utils.InfluxDbUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClientServiceImpl extends ServiceImpl<ClientMapper, Client> implements ClientService {
    private String registerToken = generateToken();

    @Resource
    ClientDetailMapper detailMapper;

    @Resource
    InfluxDbUtils influx;

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
            Client client = new Client(id, "未命名主机", token, "cn", "为命名节点", new Date());
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
//        System.out.println(clientDetailVO);
        BeanUtils.copyProperties(clientDetailVO, detail);
        detail.setId(client.getId());
//        System.out.println(detail);
        if(detailMapper.selectById(client.getId()) != null) {
            detailMapper.updateById(detail);
        } else {
            detailMapper.insert(detail);
        }
    }

    private Map<Integer, RuntimeDetailVO> currentRuntime = new ConcurrentHashMap<>();
    @Override
    public void updateRuntimeDetail(RuntimeDetailVO runtimeDetailVO, Client client) {
        currentRuntime.put(client.getId(), runtimeDetailVO);
//        System.out.println(runtimeDetailVO);
        influx.writeRuntimeData(client.getId(), runtimeDetailVO);
    }

    private int generateRandomId() {
        return new Random().nextInt(90000000) + 10000000; //生成八位随机数
    }

    public static String generateToken()
    {
        String token = UUID.randomUUID().toString().replace("-", "");
//        System.out.println(token);
        return token;
    }

    @Override
    public List<ClientPreviewVO> listClients() {
        return clientIdCache.values().stream().map(client -> {
            ClientPreviewVO clientPreviewVO = client.asViewObject(ClientPreviewVO.class);
            BeanUtils.copyProperties(detailMapper.selectById(clientPreviewVO.getId()), clientPreviewVO);
            RuntimeDetailVO runtimeDetailVO = currentRuntime.get(clientPreviewVO.getId());
            if(isOnline(runtimeDetailVO)) {
                //一分钟内是否有上报动态数据，如果没有看作是断联了
                BeanUtils.copyProperties(runtimeDetailVO, clientPreviewVO);
                clientPreviewVO.setOnline(true);   //在线！
            }
            return clientPreviewVO;
        }).toList();
    }

    @Override
    public void renameClient(RenameClientVO vo) {
        this.update(Wrappers.<Client>update().eq("id", vo.getId()).set("name", vo.getName()));
        initCache(); //更新缓存
    }

    @Override
    public ClientDetailsVO clientDetails(int clientId) {
        ClientDetailsVO vo = clientIdCache.get(clientId).asViewObject(ClientDetailsVO.class);
        BeanUtils.copyProperties(detailMapper.selectById(clientId), vo);
        vo.setOnline(isOnline(currentRuntime.get(clientId)));
        return vo;
    }

    @Override
    public void renameNode(RenameNodeVO vo) {
        this.update(Wrappers.<Client>update().eq("id", vo.getId())
                .set("location", vo.getLocation()).set("node", vo.getNode()));
        initCache();
    }

    boolean isOnline(RuntimeDetailVO runtime) {
        return runtime != null && (System.currentTimeMillis() - runtime.getTimestamp()) < 60*1000;
    }

    @Override
    public RuntimeHistoryVO runtimeDetailsHistory(int clientId) {
        RuntimeHistoryVO history = influx.readRuntimeData(clientId);
        RuntimeDetailVO detail = currentRuntime.get(clientId);
        BeanUtils.copyProperties(history, detail);
        return history;
    }

    @Override
    public RuntimeDetailVO runtimeDetailNow(int clientId) {
        return currentRuntime.get(clientId);
    }

    @Override
    public void deleteClient(int clientId) {
        removeById(clientId);
        detailMapper.deleteById(clientId);
        clientTokenCache.remove(clientIdCache.get(clientId).getToken());
        clientIdCache.remove(clientId);
        currentRuntime.remove(clientId);
    }

    @Resource
    ClientSshMapper sshMapper;

    @Override
    public SshSettingsVO sshSettings(int clientId) {
        ClientDetail detail = detailMapper.selectById(clientId);
        ClientSsh clientSsh = sshMapper.selectById(clientId);
        SshSettingsVO vo = new SshSettingsVO();
        if(clientSsh != null) {
            vo = clientSsh.asViewObject(SshSettingsVO.class);
        } else {
            vo = new SshSettingsVO();
        }

        vo.setIp(detail.getIp());
        return  vo;
    }

    @Override
    public void saveClientSshConnection(SshConnectionVO vo) {
        Client client = clientIdCache.get(vo.getId());
        if(client == null) {
            return;
        }

        ClientSsh ssh = new ClientSsh();
        BeanUtils.copyProperties(vo, ssh);

        if(Objects.nonNull(sshMapper.selectById(client.getId()))) {
            sshMapper.updateById(ssh);
        } else {
            sshMapper.insert(ssh);
        }
    }
}
