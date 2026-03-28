package com.messmate.delivery.socket;

import android.util.Log;
import com.messmate.delivery.utils.Constants;
import io.socket.client.IO;
import io.socket.client.Socket;
import java.net.URISyntaxException;

public class SocketManager {
    private static final String TAG = "SocketManager";
    private static SocketManager instance;
    private Socket mSocket;

    private SocketManager() {
        try {
            mSocket = IO.socket(Constants.SOCKET_URL);
        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket Initialization Error: ", e);
        }
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public Socket getSocket() {
        return mSocket;
    }

    public void connect() {
        if (mSocket != null && !mSocket.connected()) {
            mSocket.connect();
            Log.d(TAG, "Connecting to Socket...");
        }
    }

    public void disconnect() {
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            Log.d(TAG, "Disconnected from Socket");
        }
    }
}
