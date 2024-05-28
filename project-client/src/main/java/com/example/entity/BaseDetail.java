package com.example.entity;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

@Component
@Accessors(chain = true)
@Data
public class BaseDetail {
    String osArch;
    String osName;
    String osVersion;
    int osBit;
    String cpuName;
    int cpuCore;
    double memory;
    double disk;
    String ip;
}
