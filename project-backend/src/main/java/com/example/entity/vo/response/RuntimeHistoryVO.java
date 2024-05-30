package com.example.entity.vo.response;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class RuntimeHistoryVO {
    double disk; //总容量
    double memory;  //总容量
    List<JSONObject> list = new LinkedList<>();  //增删操作较多，为了优化性能
}
