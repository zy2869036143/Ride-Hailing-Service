package com.catiger.driver.data.model;

import com.catiger.driver.FullscreenActivity;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String displayName;
    private FullscreenActivity activity;
    public LoggedInUser(String userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public FullscreenActivity getActivity() {
        return activity;
    }

    public void setActivity(FullscreenActivity activity) {
        this.activity = activity;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}