package com.pural_ba3a.vulcanwash;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

public class NetworkMonitor {

    private final ConnectivityManager connectivityManager;
    private final ConnectivityManager.NetworkCallback networkCallback;
    private boolean isConnected = false;

    public NetworkMonitor(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                isConnected = true; // Network is available
            }

            @Override
            public void onLost(Network network) {
                isConnected = false; // Network is lost
            }
        };

        // Register network callback
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
    }

    public boolean isNetworkAvailable() {
        return isConnected;
    }

    // Clean up network callback when not needed
    public void unregisterCallback() {
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

}


