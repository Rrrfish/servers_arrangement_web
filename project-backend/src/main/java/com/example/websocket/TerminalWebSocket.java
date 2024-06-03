package com.example.websocket;


import com.example.entity.dto.ClientDetail;
import com.example.entity.dto.ClientSsh;
import com.example.mapper.ClientDetailMapper;
import com.example.mapper.ClientSshMapper;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import jakarta.annotation.Resource;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@ServerEndpoint("/terminal/{clientId}")
public class TerminalWebSocket {
    private static ClientDetailMapper detailMapper;

    @Resource
    public void setDetailMapper(ClientDetailMapper detailMapper) {
        TerminalWebSocket.detailMapper = detailMapper;
    }

    private static ClientSshMapper sshMapper;

    @Resource
    public void setSshMapper(ClientSshMapper sshMapper) {
        TerminalWebSocket.sshMapper = sshMapper;
    }

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private static final Map<Session, Shell> sessionMap = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("clientId") String clientId) throws Exception {
        ClientDetail detail = detailMapper.selectById(clientId);
        ClientSsh ssh = sshMapper.selectById(clientId);
        if(detail == null || ssh == null){
            session.close();
            log.error("无法识别此主机，id: {}", clientId);
            return;
        }
        if(createSshConnection(session, ssh, detail.getIp())) {
            log.info("主机{}的ssh连接已创建", detail.getIp());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
        Shell shell = sessionMap.get(session);
        OutputStream output =shell.out;
        output.write(message.getBytes(StandardCharsets.UTF_8));
        output.flush();
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        Shell shell = sessionMap.get(session);
        if(shell != null) {
            shell.close();
            sessionMap.remove(session);
            log.info("主机 {} 的SSH连接已断开", shell.jsSession.getHost());
        }
    }
    @OnError
    public void onError(Session session, Throwable error) throws IOException {
        log.error("用户WebSocket连接出现错误", error);
        session.close();
    }
    private boolean createSshConnection(Session session, ClientSsh clientSsh, String ip) throws IOException {
        try {
            JSch jsch = new JSch();
            com.jcraft.jsch.Session jsSession = jsch.getSession(clientSsh.getUsername(), ip, clientSsh.getPort());
            jsSession.setPassword(clientSsh.getPassword());
            jsSession.setConfig("StrictHostKeyChecking", "no"); //防止连接后断开连接
            jsSession.setTimeout(3000);  //三秒连不上就断开
            jsSession.connect();
            ChannelShell channel = (ChannelShell) jsSession.openChannel("shell");
            channel.setPtyType("xterm");  //使其有颜色，好看点
            channel.connect(1000);
            sessionMap.put(session, new Shell(session, jsSession, channel));
            return true;
        } catch(JSchException e) {
            String message = e.getMessage();
            if(message.equals("Auth fail")) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
                        "登录SSH失败，用户名或密码错误"));
                log.error("连接SSH失败，用户名或密码错误，登录失败");
            } else if(message.contains("Connection refused")) {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT,
                        "连接被拒绝，可能是没有启动SSH服务或是放开端口"));
                log.error("连接SSH失败，连接被拒绝，可能是没有启动SSH服务或是放开端口");
            } else {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, message));
                log.error("连接SSH时出现错误", e);
            }
        }
        return false;
    }

    private class Shell {
        private final Session session;
        private final com.jcraft.jsch.Session jsSession;
        private final ChannelShell channel;
        private final InputStream in;
        private final OutputStream out;

        public Shell(Session session, com.jcraft.jsch.Session jsSession ,ChannelShell channel) throws IOException {
            this.session = session;
            this.jsSession = jsSession;
            this.channel = channel;
            this.in = channel.getInputStream();
            this.out = channel.getOutputStream();
            service.submit(this::read);
        }

        private void read() {
            try {
                byte[] buffer = new byte[1024 * 1024];
                int i;
                while ((i = in.read(buffer)) != -1) {
                    String text = new String(Arrays.copyOfRange(buffer, 0, i), StandardCharsets.UTF_8);
                    session.getBasicRemote().sendText(text);
                }
            } catch (Exception e) {
                log.error("读取SSH输入流时出现问题", e);
            }
        }

        public void close() throws IOException {
            in.close();
            out.close();
            channel.disconnect();
            jsSession.disconnect();
            service.shutdown();
        }
    }
}
