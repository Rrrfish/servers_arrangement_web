package com.example.filter;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.entity.RestBean;
import com.example.entity.dto.Client;
import com.example.service.ClientService;
import com.example.service.Impl.ClientServiceImpl;
import com.example.utils.Config;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Controller
public class JWTAuthorizeFilter extends OncePerRequestFilter {
    @Resource
    JwtUtils utils;

    @Resource
    ClientService service;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        String uri = request.getRequestURI();

        if(uri.startsWith("/api/monitor")) {
            if(!uri.endsWith("/register")) {
                Client client = service.getClientByToken(authorization);
                if(client == null) {
                    response.setStatus(401);
                    response.getWriter().write(RestBean.fail(401, "未注册").toJSONString());
                    return;
                } else {
                    request.setAttribute(Config.ALTER_CLIENT, client);
                    //在这里塞入client，Controller就可以通过获取@RequestAttribute获取client了
                }
            }
        } else {
            DecodedJWT jwt = utils.decode(authorization);
            if( jwt != null ) {
                UserDetails user = utils.toUserDetails(jwt);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication); //表示已經認證過了
                //request.setAttribute("id", utils.toId(jwt));
            }
        }


        filterChain.doFilter(request, response);
    }

}
