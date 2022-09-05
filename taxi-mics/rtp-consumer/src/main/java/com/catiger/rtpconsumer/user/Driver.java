package com.catiger.rtpconsumer.user;

import com.catiger.rtpconsumer.request.Order;
import org.aspectj.weaver.ast.Or;
import org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter;

import java.util.ArrayList;
import java.util.List;

public class Driver extends User {
    private String licence;
    private State state;
    private List<User> looking;
    private List<Integer> indexList;

    private Order currentOrder;
    public Driver(String account, String licence, double lat, double lon) {
        super(account, lat, lon);
        this.account = account;
        this.licence = licence;
        this.latitude = lat;
        this.longitude = lon;
        this.state = State.FREE;
        this.looking = new ArrayList<>();
        this.indexList = new ArrayList<>();
    }

    public void setCurrentOrder(Order currentOrder) {
        this.currentOrder = currentOrder;
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    public String getLicence() {
        return licence;
    }
    public void addLooking(User user, int index) {
        int r = find(user);
        // 去重
        if(r>=0) {
            this.looking.remove(r);
            this.indexList.remove(r);
            return;
        }
        this.looking.add(user);
        this.indexList.add(index);
    }

    public int find(User user) {
        for (int i=0; i < this.looking.size(); ++i) {
            User u = this.looking.get(i);
            if(u.equals(user))
                return i;
        }
        return -1;
    }

    public List<User> getLooking() {
        return looking;
    }

    public List<Integer> getIndexList() {
        return indexList;
    }
}
