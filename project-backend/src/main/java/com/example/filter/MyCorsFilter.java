package com.example.filter;

import com.example.utils.Config;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Config.ORDER_CORS)
public class MyCorsFilter extends HttpFilter {
    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        this.addCorsHeader(request, response);
        chain.doFilter(request, response);
    }

    private void addCorsHeader(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin")); //获取来源站点
//        String origin = request.getHeader("Origin");
//        if (origin != null && !origin.isEmpty()) {
//            response.setHeader("Access-Control-Allow-Origin", origin);
//        } else {
//            response.setHeader("Access-Control-Allow-Origin", "*"); // 仅用于开发，生产环境中不使用 *
//        }
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        //response.setHeader("Access-Control-Allow-Credentials", "true"); // 允许发送Cookie
    }
}
