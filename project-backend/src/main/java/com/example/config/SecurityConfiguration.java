package com.example.config;

import com.example.entity.RestBean;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.filter.JWTAuthorizeFilter;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
public class SecurityConfiguration {
    @Resource
    JwtUtils utils;

    @Resource
    JWTAuthorizeFilter jwtAuthorizeFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf -> {

                    conf.requestMatchers("/api/auth/**").permitAll()
                            .anyRequest().authenticated();
                })
                .formLogin(conf -> {
                    conf.loginProcessingUrl("/api/auth/login")
                            .usernameParameter("username")
                            .passwordParameter("password")
                            .successHandler(this::onAuthenticationSuccess)
                            .failureHandler(this::onAuthenticationFailure);
                })
                .logout(conf -> {
                    conf.logoutUrl("/api/auth/logout")
                            .logoutSuccessHandler(this::onLogoutSuccess);
                })
                .exceptionHandling(conf -> conf
                        .authenticationEntryPoint(this::onUnauthorized) //未登錄
                        .accessDeniedHandler(this::onAccessDenied) //Authority不足
                )
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(conf -> {
                    conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class) //在它之前
                .build();
    }

    public void onAccessDenied(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 403 forbidden!
        response.setHeader("Content-Type", "text/html;charset=UTF-8");
        response.getWriter().write(RestBean.fail( 403, accessDeniedException.getMessage()).toJSONString());
    }

    public void onUnauthorized(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        response.setHeader("Content-Type", "text/html;charset=UTF-8");
        response.getWriter().write(RestBean.fail( 401, exception.getMessage()).toJSONString());

    }


    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        response.setHeader("Content-Type", "text/html;charset=UTF-8");

        response.getWriter().write("退出成功！");
    }
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        response.setHeader("Content-Type", "text/html;charset=UTF-8");
        response.getWriter().write(RestBean.fail(exception.getMessage()).toJSONString());
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        response.setHeader("Content-Type", "text/html;charset=UTF-8");
        User principal = (User)authentication.getPrincipal();

        String token = utils.generateToken(principal, 1, "goodUser");
        AuthorizeVO  vo = new AuthorizeVO();
        vo.setUsername(principal.getUsername());
        vo.setRole("ROLE_ADMIN");
        vo.setToken(token);
        vo.setExpire(utils.expireTime());
        response.getWriter().write(RestBean.success(vo).toJSONString());
    }
}
