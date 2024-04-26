package com.example.usbtest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import usb.UsbConnectionContext;
import usb.UsbConnector;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "USBTest";

    /* USB system service */
    private static final String ACTION_USB_PERMISSION = "com.example.USB_PERMISSION";
    private UsbManager usbManager;
    private UsbDevice device;

    private PendingIntent permissionIntent;

    private Button btnConnect;
    private Button btnDisconnect;
    private Button btnSend;
    private TextView txtConnect;
    private TextView txtInput;
    private EditText txtOutput;
    private EditText txtVID;
    private EditText txtPID;
    private TextView txtMachine;
    private boolean reading = false;
    UsbConnector connector;
    UsbConnectionContext cxt;
    private int vid = 1240;
    private int pid = 63;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = (Button) findViewById(R.id.connect_button);
        btnDisconnect = (Button) findViewById(R.id.disconnect_button);
        btnSend = (Button) findViewById(R.id.send_button);
        txtConnect = (TextView) findViewById(R.id.connectText);
        txtInput = (TextView) findViewById(R.id.input);
        txtOutput = (EditText) findViewById(R.id.output_editText);
        txtVID = (EditText) findViewById(R.id.vid_editText);
        txtPID = (EditText) findViewById(R.id.pid_editText);
        txtMachine = (TextView) findViewById(R.id.machine_textView);

        txtVID.setText(String.valueOf(vid));
        txtPID.setText(String.valueOf(pid));

        connector = new UsbConnector(this);
        btnConnect.setOnClickListener(v -> {
            if (!txtVID.getText().toString().isEmpty() && !txtPID.getText().toString().isEmpty()) {
                vid = Integer.parseInt(txtVID.getText().toString());
                pid = Integer.parseInt(txtPID.getText().toString());
            } else {
                runOnUiThread(() -> {
                    txtConnect.setText("Please enter your VID or PID.");
                });
                return;
            }

            cxt = connector.ConnectUsb(0, vid, pid, 0);
            if (cxt != null) {
                txtConnect.setText("Connected");
                txtConnect.setBackgroundColor(Color.GREEN);
                reading = true;
                new Thread(() -> {
                    while (reading) {
                        try {
                            byte[] bytes = connector.ReadBytes(cxt, 64, 10000);
                            if (bytes != null && bytes.length > 0) {
                                String s = byteArrayToHexStr(bytes);
                                runOnUiThread(() -> txtInput.setText("Input from USB: " + s));
                            }
                        }catch (Exception e){
                            runOnUiThread(()->{
                                txtMachine.setText("Reading: " + e.toString());
                            });
                        }
                    }
                }).start();
            }
        });
        btnDisconnect.setOnClickListener(v -> {
            reading = false;
            connector.Disconnect(vid, pid);
            runOnUiThread(() -> {
                txtConnect.setText("Disconnected");
                txtConnect.setBackgroundColor(Color.RED);
            });
        });
        btnSend.setOnClickListener(v -> {
            String txt = txtOutput.getText().toString();
            if (cxt != null && !txt.isEmpty()) {
                try {
                    byte[] bytes = hexToByte(txt);
                    connector.WriteBytes(cxt, bytes, 10000);
                } catch (Exception e) {
                    runOnUiThread(()->{
                        txtMachine.setText("Sending: " + e.toString());
                    });
                }
            }
        });
    }

    private void findAndOpenDevice() {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        StringBuilder builder = new StringBuilder();
        for (UsbDevice device : deviceList.values()) {
            // Check if device matches your criteria
            builder.append("VID:");
            builder.append(device.getVendorId());
            builder.append(",PID:");
            builder.append(device.getProductId());
            builder.append("\n");
//            if (device.getVendorId() == 1659 && device.getProductId() == 8963) {
//                this.device = device;
//                // Request permission to access the device
//                usbManager.requestPermission(device, permissionIntent);
//                break;
//            }
        }
        if (!builder.toString().isEmpty()) {
            txtMachine.setText(builder.toString());
        }
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // Permission granted, you can now communicate with the device
                            // Open a connection and send/receive data
//                            txtConnect.setText("Connected");
//                            txtConnect.setBackgroundColor(Color.GREEN);
//                            reading = true;
                            UsbDeviceConnection deviceConnection = usbManager.openDevice(device);
                            for (int i = 0; i < device.getInterfaceCount(); i++) {
                                UsbInterface usbInterface = device.getInterface(i);
                                Log.d(TAG, "onReceive: ");
                            }
//                            new Thread(() -> {
//                                while(true){
//                                    UsbDeviceConnection deviceConnection = usbManager.openDevice(device);
//                                    deviceConnection.claimInterface(usbInterface, true);
//                                    byte[] maxLun = new byte[1];
//                                    deviceConnection.controlTransfer(requestType,request, value, index, bytes, bytes.length, TIMEOUT);
//                                    Log.d("","");
//                                }
//
//                            }).start();
                        }
                    } else {
                        // Permission denied
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reading = false;
        connector.Disconnect(vid, pid);
    }

    /**
     * 將ByteArray轉成字串可顯示的ASCII
     */
    private String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        StringBuilder hex = new StringBuilder(byteArray.length * 2);
        for (byte aData : byteArray) {
            hex.append(String.format("%02X ", aData));
        }
        String gethex = hex.toString();
        return gethex;
    }

    private byte[] hexToByte(String str) {
        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(str.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

}

