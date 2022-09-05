package com.catiger.rtpconsumer.tasks;

import com.catiger.rtpconsumer.inter.PosdbClient;
import com.catiger.rtpconsumer.request.TimeOrder;
import com.catiger.rtpconsumer.websocket.SpringWebSocketHandler;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

public class Task implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        TimeOrder timeOrder = (TimeOrder)jobDataMap.getOrDefault("oid", null);
        WebSocketSession driverSession = getSessionByAccount("18832129036");
        JSONObject jsonMsg2Driver = new JSONObject();
        jsonMsg2Driver.put("code",4);
        jsonMsg2Driver.put("slat", timeOrder.getSlat());
        jsonMsg2Driver.put("slon", timeOrder.getSlon());
        jsonMsg2Driver.put("elat",timeOrder.getElat());
        jsonMsg2Driver.put("elon",timeOrder.getElon());
        jsonMsg2Driver.put("oid", timeOrder.getId());
        jsonMsg2Driver.put("time", timeOrder.getApptime());
        jsonMsg2Driver.put("splace",timeOrder.getSplace());
        jsonMsg2Driver.put("eplace",timeOrder.getEplace());
        jsonMsg2Driver.put("account",timeOrder.getAccount());
        try {
            driverSession.sendMessage(new TextMessage(jsonMsg2Driver.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }catch (NullPointerException e) {

        }

    }

    private WebSocketSession getSessionByAccount(String account) {
        WebSocketSession session = SpringWebSocketHandler.account2Session.get(account);
        return session;
    }
}
