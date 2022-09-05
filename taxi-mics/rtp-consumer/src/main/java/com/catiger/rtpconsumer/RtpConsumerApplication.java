package com.catiger.rtpconsumer;

import com.catiger.rtpconsumer.inter.HttpUtil;
import com.catiger.rtpconsumer.user.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
@EnableWebSocket
@EnableFeignClients
@EnableDiscoveryClient
public class RtpConsumerApplication {

    public static void main(String[] args) {
        /*double inf = (1<<20)-1;
        int driverNum = 3;
        int orderNum = 4;
        double[][] matrix = new double[driverNum+1][orderNum+1];
        for (int row = 1; row <= driverNum; row++) {
            for (int col=1; col <= orderNum; col++) {
                Random r = new Random();
                int p = r.nextInt(99) + 1;
                matrix[row][col] = p; // km
            }
        }
        double[] maxDriver = new double[driverNum+1];
        double[] maxOrder = new double[orderNum+1];
        // links[i]=x : order x is assigned to driver i
        int[] links = new int[1000];
        for(int i=1; i<=driverNum; ++i) {
            maxDriver[i] = Arrays.stream(matrix[i]).max().getAsDouble();
        }
        for(int i=1; i<=driverNum; ++i) {
            while (true) {
                double d = inf;
                boolean[] useDriver = new boolean[driverNum+1];
                boolean[] userOrder= new boolean[orderNum+1];
                if(dfs(i, maxDriver, maxOrder, matrix, useDriver, userOrder, links))
                    break;
                for (int j=1; j <= driverNum; ++j) {
                    if(useDriver[j]) {
                        for (int k=1;k <= orderNum; ++k) {
                            if(!userOrder[k]) {
                                double minus = maxDriver[j] + maxOrder[k] -matrix[j][k];
                                d = d<minus?d:minus;
                            }
                        }
                    }
                }
                if(d==inf) return;
                for (int j=1; j<=driverNum; ++j) {
                    if (useDriver[j])
                        maxDriver[j] -= d;
                }
                for (int j=1; j<=driverNum; ++j) {
                    if(userOrder[j])
                        maxOrder[j] += d;
                }
            }
        }*/
        SpringApplication.run(RtpConsumerApplication.class, args);
    }
    /*private static boolean dfs(int u, double[] cx, double[] cy, double[][] weight, boolean[] usex, boolean[] usey, int[] linking){
        usex[u] = true;
        for (int i=1; i<=cy.length-1; i++) {
            if(!usey[i]&&cx[u]+cy[i]==weight[u][i]) {
                usey[i] = true;
                if(linking[i]==0||dfs(linking[i],cx,cy,weight,usex,usey,linking)) {
                    linking[i] = u;
                    return true;
                }
            }
        }
        return false;
    }*/
//    @Bean
    public Map<String, WebSocketSession> account2Session() {
        Map<String, WebSocketSession> account2Session = new ConcurrentHashMap<>();
        return account2Session;
    }
    @Bean
    Map<String,User>  sessionID2User() {
        Map<String, User> sessionID2User = new ConcurrentHashMap<>();
        return sessionID2User;
    }
}
