package com.getpebble.example;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends Activity
{
    private static String TAG = "rfcomm-leak-example";
    private static final UUID TEST_UUID = UUID.randomUUID();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void doBtServerSocketTest(View view) {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < 64; ++i) {
                    Log.d(TAG, String.format("Opening server socket #%d", i));

                    BtTestThread t = new BtTestThread(TEST_UUID);
                    t.start();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        // Swallow
                    }
                    t.cancel();
                }
            }
        };

        new Thread(r).start();
    }

    private class BtTestThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public BtTestThread(UUID uuid) {
            BluetoothServerSocket tmp = null;
            try {
                tmp = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord("MyServer", uuid);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            while (true) {
                try {
                    Log.d(TAG, "bluetooth server socket waiting for connection");
                    if (mmServerSocket == null) {
                        Log.d(TAG, "bluetooth server socket was not created");
                        break;
                    }
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    break;
                }

                if (socket != null) {
                    Log.d(TAG, "remote client connected");
                    try {
                        mmServerSocket.close();
                        socket.close();
                    } catch (IOException e) {
                        Log.w(TAG, "Failed to close client socket");
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                if (mmServerSocket != null) {
                    mmServerSocket.close();
                    Log.i(TAG, "Closed bluetooth server socket");
                } else {
                    Log.w(TAG, "server socket was null");
                }
            } catch (IOException e) { }
        }
    }

}
