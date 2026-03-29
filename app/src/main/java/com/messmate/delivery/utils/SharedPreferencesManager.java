package com.messmate.delivery.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPreferencesManager {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        Log.d("PREF_DEBUG", "✅ SharedPreferences Initialized");
    }

    // ================= TOKEN =================
    public void saveToken(String token) {
        Log.d("PREF_DEBUG", "💾 Saving Token: " + token);
        editor.putString(Constants.KEY_TOKEN, token).apply();
    }

    public String getToken() {
        String token = sharedPreferences.getString(Constants.KEY_TOKEN, null);
        Log.d("PREF_DEBUG", "📤 Fetch Token: " + token);
        return token;
    }

    // ================= AGENT ID =================
    public void saveAgentId(String agentId) {
        Log.d("PREF_DEBUG", "💾 Saving AgentId: " + agentId);
        editor.putString(Constants.KEY_AGENT_ID, agentId).apply();
    }

    public String getAgentId() {
        String id = sharedPreferences.getString(Constants.KEY_AGENT_ID, "");
        Log.d("PREF_DEBUG", "📤 Fetch AgentId: " + id);
        return id;
    }

    // 🔥 IMPORTANT (FIX FOR YOUR ERROR)
    public String getUserId() {
        String userId = getAgentId();
        Log.d("PREF_DEBUG", "📤 Fetch UserId (alias): " + userId);
        return userId;
    }

    // ================= AGENT NAME =================
    public void saveAgentName(String name) {
        Log.d("PREF_DEBUG", "💾 Saving AgentName: " + name);
        editor.putString(Constants.KEY_AGENT_NAME, name).apply();
    }

    public String getAgentName() {
        String name = sharedPreferences.getString(Constants.KEY_AGENT_NAME, "Agent");
        Log.d("PREF_DEBUG", "📤 Fetch AgentName: " + name);
        return name;
    }

    // ================= ROLE =================
    public void saveRole(String role) {
        Log.d("PREF_DEBUG", "💾 Saving Role: " + role);
        editor.putString("role", role).apply();
    }

    public String getRole() {
        String role = sharedPreferences.getString("role", "");
        Log.d("PREF_DEBUG", "📤 Fetch Role: " + role);
        return role;
    }

    // ================= LOGIN =================
    public boolean isLoggedIn() {
        String token = getToken();
        boolean isLoggedIn = token != null && !token.isEmpty();

        Log.d("PREF_DEBUG", "🔐 isLoggedIn: " + isLoggedIn);

        return isLoggedIn;
    }

    // ================= LOGOUT =================
    public void clearAll() {
        Log.d("PREF_DEBUG", "🧹 Clearing all preferences");
        editor.clear().apply();
    }
}