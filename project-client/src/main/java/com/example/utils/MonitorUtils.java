package com.example.utils;


import com.example.entity.BaseDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;


import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Properties;

@Slf4j
@Component
public class MonitorUtils {
    private final SystemInfo info = new SystemInfo();
    private final Properties properties = System.getProperties();

    public BaseDetail monitorBaseDetail() {
        OperatingSystem os = info.getOperatingSystem();
        HardwareAbstractionLayer hardware = info.getHardware();
        double memory = hardware.getMemory().getTotal() / 1024.0 / 1024 / 1024; //转换为G为单位
        double diskSize = Arrays.stream(File.listRoots())
                .mapToLong(File::getTotalSpace).sum()  / 1024.0 / 1024 / 1024;
        //String ip = Objects.requireNonNull(findNetworkInterface(hardware)).getIPv4addr()[0];
        String ip = "未知";
        try {
            ip = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            log.error("获取服务器ip地址失败");
        }
        ip = ip.split("/")[1];
        System.out.println("ip: " + ip);
        return new BaseDetail()
                .setOsArch(properties.getProperty("os.arch"))
                .setOsName(os.getFamily())
                .setOsVersion(properties.getProperty("os.version"))
                .setOsBit(os.getBitness())
                .setCpuName(hardware.getProcessor().getProcessorIdentifier().getName())
                .setCpuCore(hardware.getProcessor().getPhysicalProcessorCount())
                .setMemory(memory)
                .setIp(ip)
                .setDisk(diskSize);
    }

}
