package com.example.utils;

import com.alibaba.fastjson2.JSONObject;
import com.example.entity.BaseDetail;
import com.example.entity.ConnectionConfig;
import com.example.entity.Response;
import com.example.entity.RuntimeDetail;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Component
public class NetUtils {
    private final HttpClient client = HttpClient.newHttpClient();

    @Resource
    @Lazy      //防止循环引用，ConnectionConfig有NetUtils的字段
    ConnectionConfig config;

    public boolean registerToServer(String address, String token) {
      log.info("正在向服务端注册,请稍后...");
      Response response = doGet("/register", address, token);
      if(response.success()) {
          log.info("注册成功");
      } else {
          log.error("注册失败: {}", response.message());
      }
      return response.success();
    }

    public Response doGet(String url) {
        return this.doGet(url, config.getAddress(), config.getToken());
    }

    public Response doGet(String url, String address, String token) {
        try{
            System.out.println("准备doGet！");
            HttpRequest request = HttpRequest.newBuilder().GET()
                    .uri(new URI(address + "/api/monitor" + url))
                    .header("Authorization", token)
                    .build();
            log.info("Sending request to URL: " + address + "/api/monitor" + url);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Received response: " + response.body());
            // 发起Http请求，将响应体转化为字符串


            return JSONObject.parseObject(response.body()).to(Response.class);

            // 获取从服务器得到的响应体，将其转化为 response 类
        } catch(Exception e) {
            log.error("在向服务端发起请求时出现问题", e);
            return Response.errorResponse(e);
        }
    }

    private Response doPost(String url, Object data) {
        try {
            String rawData = JSONObject.from(data).toJSONString();
            HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(rawData))
                    .uri(new URI(config.getAddress() + "/api/monitor" + url))
                    .header("Authorization", config.getToken())
                    .header("Content-Type", "application/json")  //不加这个会报错
                    .build();
            log.info("Sending POST request to URL: " + config.getAddress() + "/api/monitor" + url + " with data: " + rawData);

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            if(response == null) System.out.println("response is null");
//            else log.info("Received POST response: " + response.body());
            Response response1 = JSONObject.parseObject(response.body()).to(Response.class);
//            System.out.println(response1);
            return response1;
        } catch (IOException | InterruptedException | URISyntaxException e) {
            log.error("请求失败", e);
            return Response.errorResponse(e);
        } catch (Exception e) {
            log.error("未预期的错误", e);
            return Response.errorResponse(e);
        }
    }

    public void updateRuntimeDetails(RuntimeDetail detail) {
        Response response = doPost("/runtime", detail);
        if(response.success()) {
            log.info("更新运行数据");
        } else{
            log.error("运行数据更新失败： {}", response.message());
        }
    }

    public void updateBaseDetails(BaseDetail detail) {
        Response response = doPost("/detail", detail);
        if(response.success()) {
            log.info("基本信息更新完毕");
        } else {
            log.error("基本信息更新失败: {}", response.message());
        }
    }
}
