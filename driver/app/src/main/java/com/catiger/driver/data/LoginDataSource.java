package com.catiger.driver.data;

import com.catiger.driver.data.model.LoggedInUser;
import com.catiger.driver.util.HttpUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {
    private static String logonURL = "http://192.168.42.61:8081/user/driverLogon";

    public Result<LoggedInUser> login(String username, String password) {

        try {
            // TODO: handle loggedInUser authentication
            AtomicReference<String> result = new AtomicReference<>();
            Thread thread = new Thread(() -> {
                HttpUtil httpUtil = HttpUtil.getHttpUtil();
                result.set(httpUtil.postSyn2(logonURL, "account", username, "password", password));
            });
            thread.start();
            thread.join();
            JSONObject jsonObject = new JSONObject(result.toString());
            if (jsonObject.getInt("code") == 200) {
                LoggedInUser fakeUser =
                        new LoggedInUser(java.util.UUID.randomUUID().toString(), username);
                return new Result.Success<>(fakeUser);
            }
            return new Result.Error(new Exception("用户名密码错误"));
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}