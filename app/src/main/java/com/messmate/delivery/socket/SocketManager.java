package com.messmate.delivery.socket;

import android.util.Log;

import com.messmate.delivery.models.Order;
import com.messmate.delivery.utils.Constants;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.function.Consumer;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketManager {

    private static final String TAG = "SocketManager";
    private static SocketManager instance;
    private Socket socket;

    // ================= INIT =================
    private SocketManager() {
        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.reconnectionAttempts = 10;
            options.reconnectionDelay = 2000;

            socket = IO.socket(Constants.SOCKET_URL, options);

        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket Init Error", e);
        }
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public Socket getSocket() {
        return socket;
    }

    // ================= CONNECT =================
    public static void connect() {
        Socket s = getInstance().socket;

        if (s != null && !s.connected()) {
            s.connect();

            s.on(Socket.EVENT_CONNECT, args -> {
                Log.d(TAG, "✅ Socket Connected");
            });

            s.on(Socket.EVENT_DISCONNECT, args -> {
                Log.d(TAG, "❌ Socket Disconnected");
            });

            s.on(Socket.EVENT_CONNECT_ERROR, args -> {
                Log.e(TAG, "⚠️ Socket Error: " + args[0]);
            });
        }
    }

    public static void disconnect() {
        Socket s = getInstance().socket;

        if (s != null && s.connected()) {
            s.disconnect();
        }
    }

    // ================= ROOM =================
    public static void joinRoom(String room) {
        Socket s = getInstance().socket;

        if (s != null) {
            s.emit("join", room);
            Log.d(TAG, "Joined room: " + room);
        }
    }

    // ================= EVENTS =================

    // 🔥 NEW ORDER LISTENER
    public static void onNewOrder(Consumer<Order> callback) {
        Socket s = getInstance().socket;

        if (s != null) {
            s.on("new-order", args -> {
                try {
                    JSONObject data = (JSONObject) args[0];

                    Order order = parseOrder(data);

                    callback.accept(order);

                    Log.d(TAG, "📦 NEW ORDER received");

                } catch (Exception e) {
                    Log.e(TAG, "Parse error NEW_ORDER", e);
                }
            });
        }
    }

    // 🔥 ORDER TAKEN (BY OTHER AGENT)
    public static void onOrderTaken(Consumer<String> callback) {
        Socket s = getInstance().socket;

        if (s != null) {
            s.on("ORDER_TAKEN", args -> {
                try {
                    String orderId = (String) args[0];
                    callback.accept(orderId);

                    Log.d(TAG, "⚡ ORDER TAKEN: " + orderId);

                } catch (Exception e) {
                    Log.e(TAG, "Parse error ORDER_TAKEN", e);
                }
            });
        }
    }

    // ================= EMIT =================
    public static void emit(String event, Object data) {
        Socket s = getInstance().socket;

        if (s != null) {
            s.emit(event, data);
        }
    }

    // ================= PARSER =================
    private static Order parseOrder(JSONObject obj) {
        try {
            // 🔥 BEST: Direct JSON → Model mapping
            return new com.google.gson.Gson().fromJson(
                    obj.toString(),
                    Order.class
            );

        } catch (Exception e) {
            Log.e(TAG, "Order parse error", e);
            return null;
        }
    }
    // ================= CLEANUP =================
    public static void removeListeners() {
        Socket s = getInstance().socket;

        if (s != null) {
            s.off("NEW_ORDER");
            s.off("ORDER_TAKEN");
        }
    }
}