package com.messmate.delivery.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        sharedPreferences.edit().putString(Constants.KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return sharedPreferences.getString(Constants.KEY_TOKEN, null);
    }
    
    public void saveAgentId(String agentId) {
        sharedPreferences.edit().putString(Constants.KEY_AGENT_ID, agentId).apply();
    }
    
    public String getAgentId() {
        return sharedPreferences.getString(Constants.KEY_AGENT_ID, "");
    }
    
    public void saveAgentName(String name) {
        sharedPreferences.edit().putString(Constants.KEY_AGENT_NAME, name).apply();
    }
    
    public String getAgentName() {
        return sharedPreferences.getString(Constants.KEY_AGENT_NAME, "Agent");
    }
    public void saveRole(String role) {
        sharedPreferences.edit().putString("role", role).apply();
    }
    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }
    public String getRole() {
        return sharedPreferences.getString("role", "");
    }
    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }
}
