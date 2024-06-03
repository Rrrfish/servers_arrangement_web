package com.example.config;

import com.alibaba.fastjson2.JSONObject;
import com.example.entity.ConnectionConfig;
import com.example.utils.MonitorUtils;
import com.example.utils.NetUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@Slf4j
@Configuration
public class ServerConfiguration implements ApplicationRunner {
    @Resource
    NetUtils net;

    @Resource
    MonitorUtils monitor;

    @Bean
    ConnectionConfig connectionConfig() {
        log.info("正在加载服务端配置...");
        ConnectionConfig config = readConnectionConfigFile();
        if(config == null) {
            config = registerToServer();
        }
        //System.out.println(monitor.monitorBaseDetail());
        return config;
    }

    private ConnectionConfig registerToServer() {
        Scanner in = new Scanner(System.in);
        String address, token;
        do {
            log.info("请输入服务器ip地址, 例如： http://127.0.0.1:8080:");
            address = in.nextLine();
            log.info("请输入服务端生成的Token秘钥:");
            token = in.nextLine();
        } while(!net.registerToServer(address, token));
        ConnectionConfig config = new ConnectionConfig(address, token);
        saveConfigurationToFile(config);
        return config;
    }

    private void saveConfigurationToFile(ConnectionConfig config) {
        File dir = new File("config");

        if(!dir.exists() && dir.mkdirs()) {
            log.info("服务器文件创建完毕");
        }
        File file = new File("config/server.json");
        try(FileWriter writer = new FileWriter(file)) {
            writer.write(JSONObject.toJSONString(config));
        } catch (IOException e) {
            log.error("保存服务器信息失败");
        }
        log.info("服务器信息保存成功");
    }


    private ConnectionConfig readConnectionConfigFile() {
        File connectionConfigFile = new File("config/server.json");
        if(connectionConfigFile.exists()) {
            System.out.println("存在配置文件");
            try(FileInputStream stream = new FileInputStream(connectionConfigFile)) {
                String raw = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                return JSONObject.parseObject(raw).to(ConnectionConfig.class); //反序列化为ConnectionConfig
            } catch (Exception e) {
                log.error("读取配置文件时出错" + e.getMessage());
            }
        }
        return null;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("正在更新服务器基本信息...");
        net.updateBaseDetails(monitor.monitorBaseDetail());
    }
}
