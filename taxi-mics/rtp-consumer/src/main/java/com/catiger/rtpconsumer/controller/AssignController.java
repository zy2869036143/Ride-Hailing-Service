package com.catiger.rtpconsumer.controller;

import com.catiger.rtpconsumer.inter.*;
import com.catiger.rtpconsumer.request.*;
import com.catiger.rtpconsumer.tasks.Task;
import com.catiger.rtpconsumer.user.*;
import com.catiger.rtpconsumer.websocket.SpringWebSocketHandler;
import org.aspectj.weaver.ast.Or;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.*;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class AssignController {
    @Autowired
    private SpringWebSocketHandler springWebSocketHandler;
    @Autowired
    private PosdbClient posdbClient;
    private static  Scheduler scheduler;

    static {
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @Autowired
    private HttpUtil httpUtil;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public static Map<String, Driver> servingDrivers = new ConcurrentHashMap();
    List<Order> orderList = new ArrayList<>();

    @PostMapping("/assign")
    public ResponseEntity<?> assignOrder(@RequestBody @Validated Order order) {
        // Save order to db no matter whether the order will be canceled or not in the future.
        String orderJson = posdbClient.postOrder(order);
        orderList.add(order);
        JSONObject jsonObject = new JSONObject(orderJson);
        long oid = jsonObject.getLong("content");
        order.setId(oid);
        Response response = new Response(200,"订单已收到", oid);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelOrder(@RequestParam("oid") long orderId, @RequestParam("driver") String account) {
        if(account.equals("null")) {
            for(int i=0; i < orderList.size(); ++i) {
                Order order = orderList.get(i);
                if(order.getId() == orderId)
                    orderList.remove(i);
            }
        }else {
            try {
                Driver driver = servingDrivers.get(account);
                driver.setCurrentOrder(null);
                driver.setBinding(null);
                WebSocketSession driverSession = getSessionByAccount(driver.getAccount());
                JSONObject cancelOrderMsg = new JSONObject();
                cancelOrderMsg.put("code",8);
                driverSession.sendMessage(new TextMessage(cancelOrderMsg.toString()));
                servingDrivers.remove(account);
                SpringWebSocketHandler.driverList.add(driver);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        Response response = new Response(200, "订单已取消");
        logger.info("Order with id:" + orderId + " canceled");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refuse")
    public ResponseEntity<?> refuseOrder(@RequestParam("account") String account) {
        try {
            Driver driver = servingDrivers.get(account);
            Order order = driver.getCurrentOrder();
            orderList.add(order);
            servingDrivers.remove(account);
            SpringWebSocketHandler.driverList.add(driver);
            logger.info("Order " + account + " refused.");
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(new Response(200, "已拒绝"));
    }

    @PostMapping("/accept")
    public ResponseEntity<?> acceptOrder(@RequestParam("account") String account) {
        try {
            Driver driver = servingDrivers.get(account);
            Order order = driver.getCurrentOrder();
            driver.setBinding(getUserByAccount(order.getAccount()));
            JSONObject jsonMsg2Passenger = new JSONObject();
            jsonMsg2Passenger.put("code",1);
            jsonMsg2Passenger.put("lat", driver.getLatitude());
            jsonMsg2Passenger.put("lon", driver.getLongitude());
            jsonMsg2Passenger.put("account", driver.getAccount());
            jsonMsg2Passenger.put("license", driver.getLicence());
            WebSocketSession session = getSessionByAccount(order.getAccount());
            session.sendMessage(new TextMessage(jsonMsg2Passenger.toString()));
            logger.info("Order " + account + " accepted.");
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(new Response(200, "已接受"));
    }

    @PostMapping("/arrivePick")
    public ResponseEntity<?> arrivedPickUp(@RequestParam("account") String account) {
        try {
            Driver driver = servingDrivers.get(account);
            Order order = driver.getCurrentOrder();
            order.setStart(LocalDateTime.now());
            JSONObject jsonMsg2Passenger = new JSONObject();
            jsonMsg2Passenger.put("code", 3);
            WebSocketSession session = getSessionByAccount(order.getAccount());
            session.sendMessage(new TextMessage(jsonMsg2Passenger.toString()));
            logger.info("Order " + account + " driver arrived pick up zone.");
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(new Response(200, "已接受"));
    }

    @PostMapping("/finish")
    public ResponseEntity<?> finishTrip(@RequestParam("account")  String driverAct) {
        try {
            Driver driver = servingDrivers.get(driverAct);
            Order order = driver.getCurrentOrder();
            order.setEnd(LocalDateTime.now());
            Duration duration = Duration.between(order.getStart(),order.getEnd());
            long days = duration.toDays(); //相差的天数
            long hours = duration.toHours();//相差的小时数
            long minutes = duration.toMinutes();//相差的分钟数
            long totalMin = days*24*60+hours*60+minutes;
            double dprice = PriceStrategy.driverPrice(totalMin, order.getKm());
            double pprice = PriceStrategy.passengerPrice(totalMin, order.getKm(), order.getStart(), order.getEnd());
            logger.info("km: "+order.getKm() + "minute:" + totalMin +" driver price:" + dprice + " passenger price:" + pprice);
            driver.setBinding(null);
            driver.setCurrentOrder(null);
            servingDrivers.remove(driverAct);
            SpringWebSocketHandler.driverList.add(driver);
            FinishOrder fo = new FinishOrder();
            fo.setOid(order.getId());
            fo.setLicense(driver.getLicence());
            fo.setDaccount(driver.getAccount());
            fo.setStartTime(order.getStart());
            fo.setEndTime(order.getEnd());
            fo.setDprice(dprice);
            fo.setPprice(pprice);
            logger.info("Oid: " + fo.getOid());
            posdbClient.finishOrder(fo);
            JSONObject jsonMsg2Passenger = new JSONObject();
            jsonMsg2Passenger.put("code", 88);
            jsonMsg2Passenger.put("price", pprice);
            WebSocketSession session = getSessionByAccount(order.getAccount());
            session.sendMessage(new TextMessage(jsonMsg2Passenger.toString()));
            return ResponseEntity.ok(new Response(200, "请求成功", dprice));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(new Response(400, "请求失败"));
    }

    @PostMapping("/server")
    public ResponseEntity<?> startServe(@RequestParam("oid") long oid, @RequestParam("account") String account) {
        TimeOrder timeOrder = posdbClient.getOrder(oid);
        Order order = new Order();
        order.setId(timeOrder.getId());
        order.setAccount(timeOrder.getAccount());
        order.setSlat(timeOrder.getSlat());
        order.setSlon(timeOrder.getSlon());
        order.setElat(timeOrder.getElat());
        order.setElon(timeOrder.getElon());
        order.setSplace(timeOrder.getSplace());
        order.setEplace(timeOrder.getEplace());
        order.setKm(timeOrder.getKm());
        Driver driver =  (Driver)getUserByAccount(account);
        SpringWebSocketHandler.driverList.remove(driver);
        servingDrivers.put(driver.getAccount(), driver);
        driver.setBinding(new User(timeOrder.getAccount(), 0,0));
        driver.setCurrentOrder(order);
        return ResponseEntity.ok(new Response(200,"ok"));
    }

    @GetMapping("/center")
    public ResponseEntity<?> refuseOrder(
            @RequestParam("account") String account,
            @RequestParam("lat") double lat,
            @RequestParam("lon") double lon) {
        int index = 0;
        JSONArray resultArray = new JSONArray();
        for (Driver driver: SpringWebSocketHandler.driverList) {
            try {
                double[] dis = httpUtil.getDistanceAnd(driver.getLongitude(), driver.getLatitude(), lon, lat);
                if (dis[1] <= 3) {
                    JSONObject item = new JSONObject();
                    User user = getUserByAccount(account);
                    driver.addLooking(user, index);
                    item.put("index", index);
                    item.put("lat", driver.getLatitude());
                    item.put("lon", driver.getLongitude());
                    item.put("min", dis[2]);
                    item.put("km", dis[1]);
                    resultArray.put(item);
                    index++;
                }
            } catch (NullPointerException | JSONException e) {
                logger.error("The origin and dest don't exist path");
            }
        }
        return ResponseEntity.ok(new Response(200, "成功获得", resultArray.toString()));
    }

    // Update driver's location.
    @PostMapping("/driverPos")
    public ResponseEntity<?> updateDriverPos(@RequestBody PosUpdate posUpdate) {
        try {
            logger.info("Update driver " + posUpdate.getAccount() + " position");
            User user = getUserByAccount(posUpdate.getAccount());
            user.setLatitude(posUpdate.getLat());
            user.setLongitude(posUpdate.getLon());
        }catch (NullPointerException e) {
            logger.error("/driverPos account:" + posUpdate.getAccount() + " null");
        }
        return ResponseEntity.ok(new Response(200 ,"更新成功"));
    }

    private static int counter = 1;
    @PostMapping("/task")
    public ResponseEntity<?> timeTask(@RequestBody TimeOrder timeOrder) {
        logger.info(timeOrder.getApptime());
        String msg = posdbClient.appOrder(timeOrder);
        JSONObject jsonObject = new JSONObject(msg);
        int oid = jsonObject.getInt("content");
        timeOrder.setId(oid);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("oid", timeOrder);
        JobDetail jobDetail= JobBuilder.newJob(Task.class).withIdentity("task"+counter,"group"+counter).usingJobData(dataMap).build();
        SimpleTrigger simple=TriggerBuilder.newTrigger()
                .withIdentity("task"+counter,"group"+counter)
                .startAt(DateBuilder.futureDate(20, DateBuilder.IntervalUnit.SECOND))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                .withIntervalInMinutes(5)
                                .withRepeatCount(3))
                .build();
        counter++;
        try {
            scheduler.scheduleJob(jobDetail,simple);
            scheduler.start();
            return ResponseEntity.ok(new Response(200,"ok"));
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    // unit: s
    private static int slot = 3;
    @PostConstruct
    private void giveOrder() {
        // order-driver matching algorithm
        Thread thread = new Thread(()-> {
            while (true) {
                try {
                    int driverNum = SpringWebSocketHandler.driverList.size();
                    int orderNum = orderList.size();
                    int max = driverNum;
                    if(orderNum > driverNum)
                        max = orderNum;
                    double[][] matrix = new double[driverNum+1][orderNum+1];
                    // links[i]=x : order x is assigned to driver i
                    int[] links = new int[max + 1];
                    for (int row = 1; row <= driverNum; row++) {
                        Driver driver = SpringWebSocketHandler.driverList.get(row-1);
                        for (int col=1; col <= orderNum; col++) {
                            Order order = orderList.get(col-1);
                            double[] dis = httpUtil.getDistanceAnd(driver.getLongitude(), driver.getLatitude(), order.getSlon(), order.getSlat());
                            matrix[row][col] = minusExp(dis[1]); // km
                        }
                    }
                    boolean result = km(matrix, links, driverNum, orderNum);
                    logger.info("Avaliable Drivers:"+ driverNum +" Order:" + orderNum + " Result:" + result + " Serving Drivers:" + servingDrivers.size());
                    if(result) {
                        // Assign order and passenger
                        for(int i = 1; i<= max; ++i) {
                            try {
                                Driver driver = SpringWebSocketHandler.driverList.get(i - 1);
                                Order order = orderList.get(links[i] - 1);
                                WebSocketSession driverSession = getSessionByAccount(driver.getAccount());
                                WebSocketSession passengerSession = getSessionByAccount(order.getAccount());
                                if(driverSession==null || passengerSession==null) {
                                    logger.error("While assigning order, driver session or passenger session is a null object");
                                    break;
                                }
                                driver.setCurrentOrder(order);
                                orderList.remove(links[i] - 1);
                                SpringWebSocketHandler.driverList.remove(i - 1);
                                servingDrivers.put(driver.getAccount(), driver);
                                JSONObject jsonMsg2Driver = new JSONObject();
                                jsonMsg2Driver.put("code",3);
                                jsonMsg2Driver.put("slat", order.getSlat());
                                jsonMsg2Driver.put("slon", order.getSlon());
                                jsonMsg2Driver.put("elat",order.getElat());
                                jsonMsg2Driver.put("elon",order.getElon());
                                jsonMsg2Driver.put("oid", order.getId());
                                jsonMsg2Driver.put("splace",order.getSplace());
                                jsonMsg2Driver.put("eplace",order.getEplace());
                                jsonMsg2Driver.put("account",order.getAccount());
                                driverSession.sendMessage(new TextMessage(jsonMsg2Driver.toString()));
                            }catch (NullPointerException nullPointerException) {
                                nullPointerException.printStackTrace();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }catch (Exception E) {
                                E.printStackTrace();
                            }
                        }
                    }
                    Thread.sleep(1000*slot);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
        saveServingDB();
    }

    private static final int SAMPLE = 30;
//    @PostConstruct
    private void saveServingDB() {
        Thread saveServingPosThread = new Thread(()->{
            while (true) {
                try {
                    for (Map.Entry<String, Driver> entry : servingDrivers.entrySet()) {
                        Driver driver = entry.getValue();
                        Order order = driver.getCurrentOrder();
                        if(order.getStart()==null)
                            continue;
                        Pos pos = new Pos(order.getId(), driver.getAccount(),driver.getLatitude(), driver.getLongitude());
                        posdbClient.postTest(pos);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    try {
                        Thread.sleep(1000*SAMPLE);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        saveServingPosThread.start();
    }

    private static final int MAX_PICK_UP_DISTANCE = 10;
    private double minusExp(double km) {
        double bias = Math.exp(MAX_PICK_UP_DISTANCE);
        return bias - Math.exp(km);
    }

    private boolean km(double[][] matrix, int[] links, int driverNum, int orderNum) {
        if(driverNum==0 || orderNum==0)
            return false;
        double inf = (1<<20)-1;
        double[] maxDriver = new double[driverNum+1];
        double[] maxOrder = new double[orderNum+1];
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
                for (int j=1; j < driverNum; ++j) {
                    if(useDriver[j]) {
                        for (int k=1;k < orderNum; ++k) {
                            if(!userOrder[k]){
                                double minus = maxDriver[j] + maxOrder[k] -matrix[j][k];
                                d = d<minus?d:minus;
                            }
                        }
                    }
                }
                if(d==inf) return false;
                for (int j=1; j<driverNum; ++j) {
                    if (useDriver[j])
                        maxDriver[j] -= d;
                }
                for (int j=1; j<driverNum; ++j) {
                    if(userOrder[j])
                        maxOrder[j] += d;
                }
            }
        }
        return true;
    }

    private boolean dfs(int u, double[] cx, double[] cy, double[][] weight, boolean[] usex, boolean[] usey, int[] linking){
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
    }



    private User getUserByAccount(String account) {
        WebSocketSession session = SpringWebSocketHandler.account2Session.get(account);
        if(session == null)
            return null;
        return SpringWebSocketHandler.sessionID2User.get(session.getId());
    }

    private WebSocketSession getSessionByAccount(String account) {
        WebSocketSession session = SpringWebSocketHandler.account2Session.get(account);
        return session;
    }
}
