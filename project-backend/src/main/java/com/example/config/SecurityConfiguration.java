package com.example.config;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.filter.JWTAuthorizeFilter;
import com.example.filter.MyCorsFilter;
import com.example.service.AccountService;
import com.example.service.Impl.AccountServiceImpl;
import com.example.utils.Config;
import com.example.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfiguration {
    @Resource
    JwtUtils utils;

    @Resource
    JWTAuthorizeFilter jwtAuthorizeFilter;
    //@Qualifier("enableGlobalAuthenticationAutowiredConfigurer")
    @Autowired
    private GlobalAuthenticationConfigurerAdapter enableGlobalAuthenticationAutowiredConfigurer;

    @Resource
    MyCorsFilter myCorsFilter;

    @Resource
    AccountService service = new AccountServiceImpl();


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(conf -> {

                    conf.requestMatchers("/api/auth/**", "/error").permitAll()
                            .requestMatchers("api/monitor/**").permitAll()  //给客户端放行
//                            .anyRequest().authenticated();
                            .anyRequest().hasAnyRole(Config.ROLE_ADMIN, Config.ROLE_NORMAL);
                })
                .formLogin(conf -> {
                    conf.loginProcessingUrl("/api/auth/login")
//                            .usernameParameter("username")
//                            .passwordParameter("password")
                            .successHandler(this::onAuthenticationSuccess)
                            .failureHandler(this::onAuthenticationFailure);
                })
                .logout(conf -> {
                    conf.logoutUrl("/api/auth/logout")
                            .logoutSuccessHandler(this::onLogoutSuccess);
                })
                .exceptionHandling(conf -> conf
                        .accessDeniedHandler(this::onAccessDenied) //Authority不足
                        .authenticationEntryPoint(this::onUnauthorized) //未登錄
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
        PrintWriter writer = response.getWriter();
        String authHeader = request.getHeader("Authorization");
        System.out.println("authHeader is " + authHeader + "!!!!!!!!!!");
//        if(utils.doInvalid(authHeader)) {
//            writer.write(RestBean.success().toJSONString());
//        } else {
//            writer.write(RestBean.fail(401, "退出失敗").toJSONString());
//        }
        /**
         * 这里就先不弄token的检验了，
         */
        writer.write(RestBean.success(authHeader).toJSONString());

    }
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        response.setHeader("Content-Type", "text/html;charset=UTF-8");
        response.getWriter().write(RestBean.fail(exception.getMessage()).toJSONString());
    }

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        response.setHeader("Content-Type", "text/html;charset=UTF-8");
        User principal = (User)authentication.getPrincipal();

        Account account = service.findAccountByUsernameOrEmail(principal.getUsername());
        String token = utils.generateToken(principal, account.getId(), account.getUsername());

        AuthorizeVO  vo = new AuthorizeVO();
        vo.setUsername(account.getUsername());
        vo.setRole(account.getRole());
        System.out.println("发出去的token" + token);
        vo.setToken(token);
        vo.setExpire(utils.expireTime());
        response.getWriter().write(RestBean.success(vo).toJSONString());
    }
}
