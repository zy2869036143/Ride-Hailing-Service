package com.catiger.rtpconsumer.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * websocket 握手处理器
 * @author fufer
 */
@Component
public class WebSocketInterceptor extends HttpSessionHandshakeInterceptor
{

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        log.info("Received hand shake!");
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpSession session = servletRequest.getServletRequest().getSession();
            if (session == null) {
                return false;
            }
            // 使用userName区分WebSocketHandler，以便定向发送消息
            String userName = servletRequest.getServletRequest().getParameter("account");
            if (StringUtils.isEmpty(userName)) {
                log.error("未传入account参数");
                return false;
            }
            String lat = servletRequest.getServletRequest().getParameter("lat");
            if (StringUtils.isEmpty(lat)) {
                log.error("未传入latitude参数");
                return false;
            }
            String lon = servletRequest.getServletRequest().getParameter("lon");
            if (StringUtils.isEmpty(lon)) {
                log.error("未传入longitude参数");
                return false;
            }
            String licence = servletRequest.getServletRequest().getParameter("license");
            if (!StringUtils.isEmpty(licence)) {
                attributes.put("license", licence);
            }
            log.info("Websocket receive account:{}, latitude:{}, longitude:{}", userName, lat, lon);
            attributes.put("account", userName);
            attributes.put("lat", lat);
            attributes.put("lon", lon);
        }
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception ex) {
        log.info("Established hand shake with a user。");
        super.afterHandshake(request, response, wsHandler, ex);
    }

}