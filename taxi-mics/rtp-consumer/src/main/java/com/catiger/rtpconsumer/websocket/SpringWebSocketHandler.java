package com.catiger.rtpconsumer.websocket;

import com.catiger.rtpconsumer.inter.Pos;
import com.catiger.rtpconsumer.inter.PosdbClient;
import com.catiger.rtpconsumer.user.Driver;
import com.catiger.rtpconsumer.user.Passenger;
import com.catiger.rtpconsumer.user.User;
import org.apache.commons.lang.reflect.FieldUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.sockjs.transport.session.WebSocketServerSockJsSession;
import org.springframework.context.ApplicationContextAware;
import java.io.IOException;
import java.net.UnknownServiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SpringWebSocketHandler extends TextWebSocketHandler implements ApplicationContextAware {

    /**
     * 存储sessionId和webSocketSession
     * 需要注意的是，webSocketSession没有提供无参构造，不能进行序列化，也就不能通过redis存储
     * 在分布式系统中，要想别的办法实现webSocketSession共享
     */
    Logger logger = LoggerFactory.getLogger(SpringWebSocketHandler.class);
    public static Map<String, WebSocketSession> account2Session = new ConcurrentHashMap<>();
    public static Map<String, User> sessionID2User = new ConcurrentHashMap<>();
    public ApplicationContext applicationContext;
    public static int driverCounter;
    public static PosdbClient posdbClient;

    public static List<Driver> driverList = new ArrayList<>();
    /**
     * 获取sessionId
     */
    private String getSessionId(WebSocketSession session) {
        if (session instanceof WebSocketServerSockJsSession) {
            // sock js 连接
            try {
                return  ((WebSocketSession) FieldUtils.readField(session, "webSocketSession", true)).getId();
            } catch (IllegalAccessException e)
            {
                throw new RuntimeException("get sessionId error");
            }
        }
        return session.getId();
    }

    /**
     * webSocket连接创建后调用
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 获取参数
        String account = String.valueOf(session.getAttributes().get("account"));
        String lat = String.valueOf(session.getAttributes().get("lat"));
        String lon = String.valueOf(session.getAttributes().get("lon"));
        String license = String.valueOf(session.getAttributes().get("license"));
        double latd = Double.parseDouble(lat);
        double lond = Double.parseDouble(lon);
        User user = null;
        String append = "Passenger";
        if(!license.equals("null")) {
            user = new Driver(account, license, latd, lond);
            driverCounter++;
            driverList.add((Driver) user);
            append = "Driver";
        } else {
            user = new Passenger(account, latd, lond);
        }
        String sessionId = getSessionId(session);
        sessionID2User.put(sessionId, user);
        account2Session.put(account, session);
        logger.info("User " + user.getAccount() + " connected with session ID:" + sessionId + " Identity:" + append);
    }

    // 接收消息调用
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            if (message instanceof TextMessage) {
                String json = ((TextMessage)message).getPayload();
//                logger.info(session.getId() + ":" + json);
                JSONObject jsonObject = new JSONObject(json);
                int code = jsonObject.getInt("code");
                // 乘客、司机之间建立连接后相互转发位置消息。
                User user = sessionID2User.get(session.getId());
                if (code == 2) {
                    double lat = jsonObject.getDouble("lat");
                    double lon = jsonObject.getDouble("lon");
                    user.setLatitude(lat);
                    user.setLongitude(lon);
                    if (user.existBinding()) {
                        try {
                            String bindingAccount = user.getBindingAccount();
                            WebSocketSession socketSession = account2Session.get(bindingAccount);
                            socketSession.sendMessage(message);
                        }catch (NullPointerException e) {

                        }
                    }

                }
                // Observer the driver's location.
                if(user instanceof Driver) {
                    List<User> audience = ((Driver)user).getLooking();
                    List<Integer> indexs = ((Driver)user).getIndexList();
//                    logger.info("Audiences Number:" + audience.size() + "," + indexs.size());
                    for (int i=0; i < audience.size(); ++i) {
                        JSONObject watchJson = new JSONObject();
                        watchJson.put("code", 8);
                        watchJson.put("lat", jsonObject.getDouble("lat"));
                        watchJson.put("lon", jsonObject.getDouble("lon"));
                        watchJson.put("index", indexs.get(i));
                        WebSocketSession audienceSession = account2Session.get(audience.get(i).getAccount());
                        if (audienceSession == null) {
                            logger.error("Audience session is a null object");
                        }else
                            audienceSession.sendMessage(new TextMessage(watchJson.toString()));
                    }
                }
            } else if (message instanceof BinaryMessage) {

            } else if (message instanceof PongMessage) {

            } else {
                logger.error("Unexpected WebSocket message type: " + message);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 连接出错调用
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
//        sessionMap.remove(getSessionId(session));
    }

    // 连接关闭调用
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Map user = session.getAttributes();
        String account = (String)user.get("account");
        User u = sessionID2User.get(session.getId());
        String append = "Passenger";
        if(u instanceof Driver) {
            driverCounter--;
            driverList.remove(u);
            append = "Driver";
        }
        sessionID2User.remove(session.getId());
        account2Session.remove(account);
        logger.info("Connection closed. Account:" + account + " Identity:" + append + " Current Online Drivers:" + driverList.size() + "," + sessionID2User.size() +"," + account2Session.size());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // 后端发送消息示例，根据sessionId找到对应的WebSocketSession对象，调用该对象的sendMessage方法
    public void sendMessage(String user, String message) {
        /*try {
            String sessionId = userMap.get(user);
            WebSocketSession session = sessionMap.get(sessionId);
            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException e) {
            logger.error("User " + user + " didn't establish websocket link with server!" );
        }*/
    }

    @Override
    public void setApplicationContext(ApplicationContext app) throws BeansException {
        applicationContext = app;
    }

    @Autowired
    public void setPosdbClient(PosdbClient posdbClient) {
        SpringWebSocketHandler.posdbClient = posdbClient;
    }
}