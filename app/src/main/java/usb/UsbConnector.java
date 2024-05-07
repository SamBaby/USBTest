package usb;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usbtest.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created by sunneo on 2018/7/30.
 */

public class UsbConnector {


    public UsbConnector(Activity Parent){
        this.ParentActivity = Parent;
        receiver = new UsbRequestBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbRequestBroadcastReceiver.USB_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ParentActivity.registerReceiver(receiver,filter,Context.RECEIVER_NOT_EXPORTED);
        }

    }
    Activity ParentActivity;
    public static final boolean DEBUG=false;


    public volatile boolean waitPermission;
    PendingIntent mPermissionIntent;
    public Hashtable<String, UsbConnectionContext> UsbConnections = new Hashtable<>();
    public void ToastShow(String txt){
        if(DEBUG) {
            final String _txt = txt;
            ParentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ParentActivity, _txt, Toast.LENGTH_SHORT).show();
                }
            });
            Log.d("UsbConnector", txt);
        }

    }
    private UsbConnectionContext OnUsbDevicePermissionGranted(UsbConnectionContext _ctx) {
        final UsbConnectionContext ctx = _ctx;
        try {
            if (ctx.mUsbDevice.getInterfaceCount() > 0) {
                UsbInterface intf = ctx.mUsbDevice.getInterface(0);
                ctx.mInterface = intf;
            } else {
                ToastShow("MainActivity:No Interface");
            }
            if (ctx.mInterface != null) {
                UsbDeviceConnection connection = null;
                if (ctx.manager.hasPermission(ctx.mUsbDevice)) {
                    ToastShow("MainActivity:OnUsbDevicePermissionGranted:Has Permission->OpenDevice");
                    connection = ctx.manager.openDevice(ctx.mUsbDevice);
                    if (connection == null) {
                        ToastShow("connect is null");
                        return null;
                    }
                    ToastShow("MainActivity:OnUsbDevicePermissionGranted:Has Permission->claimInterface");
                    if (connection.claimInterface(ctx.mInterface, true)) {
                        ctx.mDeviceConnection = connection;
                        if (ctx.mInterface.getEndpointCount() > 1 && ctx.mInterface.getEndpoint(1) != null) {
                            ctx.epOut = ctx.mInterface.getEndpoint(1);
                        }
                        if (ctx.mInterface.getEndpointCount() > 0 && ctx.mInterface.getEndpoint(0) != null) {
                            ctx.epIn = ctx.mInterface.getEndpoint(0);
                        }
                        if (true) {
                            ToastShow("wait Permission = true");
                            ctx.mConnected = true;
                            waitPermission = false;
                            ParentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastShow("MainActivity:OnConnected");
                                    String key = CreateDeviceKey(ctx.mUsbDevice);
                                    if (!UsbConnections.containsKey(key))
                                        UsbConnections.put(key, ctx);
                                }
                            });
                        } else {
                            ToastShow("wait Permission = false");
                        }
                        ctx.valid=true;
                        return ctx;
                    } else {
                        connection.close();

                        return null;
                    }
                } else {//appendDebugMsg("3沒權限\n");

                    return null;
                }
            }
        }catch(Exception ee){
            StringWriter stringWriter = new StringWriter();
            ee.printStackTrace(new PrintWriter(stringWriter));
            ToastShow(stringWriter.getBuffer().toString());
        }
        return null;
    }
    public UsbConnectionContext ConnectUsb(int uid,int vendorId,int productId,int tag) {
        UsbConnectionContext ctx = new UsbConnectionContext();
        ctx.uid = uid;
        ctx.vid = vendorId;
        ctx.pid = productId;
        ctx.tag = tag;
        // 1.獲取USB設備*****
        ctx.manager = (UsbManager) ParentActivity.getSystemService(Context.USB_SERVICE);        //---------UsbManager USB管理器
        if (ctx.manager == null) {
            ToastShow(String.format("ConnectUsb Exited because ctx.manager is null"));
            return null;
        }

        HashMap<String, UsbDevice> deviceList = ctx.manager.getDeviceList();     //..............getDeviceList 枚舉USB設備
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();  //????
        ctx.mUsbDevice = null;
        ctx.mInterface = null;
        StringBuilder builder = new StringBuilder();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            builder.append("VID:");
            builder.append(device.getVendorId());
            builder.append(",PID:");
            builder.append(device.getProductId());
            builder.append("\n");
            if (device.getVendorId() == vendorId && device.getProductId() == productId) {
                ctx.mUsbDevice = device;
                if (ctx.mUsbDevice == null) {
                    ToastShow(String.format("ConnectUsb Exited because ctx.mUsbDevice is null"));
                    return null;
                }
                break;
            }
        }
        if(!builder.toString().isEmpty() && ParentActivity.findViewById(R.id.machine_textView) != null){
            runOnUiThread(()->{
                ((TextView)ParentActivity.findViewById(R.id.machine_textView)).setText(builder.toString());
            });
        }
        if (ctx.mUsbDevice != null) {
            ctx.waitPermission = true;
            if (ctx.manager.hasPermission(ctx.mUsbDevice)) {
                ToastShow(String.format("ConnectUsb has Permission"));
                ctx.waitPermission = true;
                ctx.valid=true;
                return OnUsbDevicePermissionGranted(ctx);
            } else {
                ToastShow(String.format("ConnectUsb has NO Permission"));
                ctx.waitPermission = true;
                PendingIntent permissionIntent = PendingIntent.getBroadcast(ParentActivity, 0, new Intent("com.example.USB_PERMISSION"), 0);
                ctx.manager.requestPermission(ctx.mUsbDevice, permissionIntent);
                ctx.valid=false;
            }
            return ctx;
        }
        ToastShow(String.format("ConnectUsb return null"));
        return null;

    }


    public void runOnUiThread(Runnable r){
        this.ParentActivity.runOnUiThread(r);
    }

    public boolean WriteBytes(UsbConnectionContext ctx,byte[] bytes,int timeout) {
        if(ctx.mUsbDevice == null ||ctx.mDeviceConnection == null || ctx.epOut == null) return false;
        return mWriteBytes(ctx,bytes,timeout);
    }
    private synchronized boolean mWriteBytes(UsbConnectionContext ctx,byte[] bytes,int timeout) {
        if(ctx.mUsbDevice == null || ctx.mDeviceConnection == null || ctx.epOut == null) return false;
        try {
            for(int i=0; i<5; ++i) {
                int re0 = ctx.mDeviceConnection.bulkTransfer(ctx.epOut, bytes, bytes.length, timeout);
                if (re0 == bytes.length) {
                    return true;
                }
            }

        } catch(Exception ee) {
            //form.dispatchErrorOccurredEvent(this, "WriteBytes",  ErrorMessages.ERROR_EXTENSION_ERROR,ee);
            return false;
        }
        return false;
    }
    public synchronized byte[] ReadBytesToBuffer(UsbConnectionContext ctx,int length,int timeout,byte[] bytes) {
        if(!ctx.mConnected) {
            return null;
        }
        if(ctx == null || ctx.mUsbDevice == null){
            return new byte[length];
        }
        int ret = ctx.mDeviceConnection.bulkTransfer(ctx.epIn, bytes, length, timeout);
        if(ret!=length) return null;
        return bytes;
    }
    public byte[] ReadBytes(UsbConnectionContext ctx,int length,int timeout) {
        if(!ctx.mConnected) {
            return null;
        }
        if(ctx == null || ctx.mUsbDevice == null){
            return new byte[length];
        }
        byte[] bytes = new byte[length];
        int ret = ctx.mDeviceConnection.bulkTransfer(ctx.epIn, bytes, length, timeout);
        if(ret!=bytes.length) return null;
        return bytes;
    }
    public byte[] WriteReadBytesToBuffer(UsbConnectionContext ctx,byte[] Data,int writeTimeout,int readLength,int readTimeout,byte[] readBuffer){
        if(ctx == null || ctx.mUsbDevice == null){
            return readBuffer;
        }
        String key = CreateDeviceKey(ctx.mUsbDevice);
        //ToastShow("HandleWriteReadBytes: key="+key);
        if(UsbConnections.containsKey(key)){
            //ToastShow("HandleWriteReadBytes: Found key="+key);
            if(writeTimeout < 0)
                writeTimeout = 10000;
            if(readLength<0 && readBuffer != null) {
                readLength =  readBuffer.length;
            }
            if(readTimeout<0)
                readTimeout = 1000;

            int uid = ctx.uid;
            int tag = ctx.tag;
            try{
                //ToastShow("HandleWriteReadBytes: Write "+Data);
                WriteBytes(ctx,Data,writeTimeout);
                //ToastShow("HandleWriteReadBytes: Read");
                ReadBytesToBuffer(ctx,readLength,readTimeout,readBuffer);
                //ToastShow("HandleWriteReadBytes: OnWriteReadFinished");
                return readBuffer;
            }catch(Exception ee){
                ee.printStackTrace();
            }

        }
        return readBuffer;
    }
    public byte[] WriteReadBytes(UsbConnectionContext ctx,byte[] Data,int writeTimeout,int readLength,int readTimeout){
        if(ctx == null || ctx.mUsbDevice == null){
            return new byte[readLength];
        }
        String key = CreateDeviceKey(ctx.mUsbDevice);
        ToastShow("HandleWriteReadBytes: key="+key);
        if(UsbConnections.containsKey(key)){
            ToastShow("HandleWriteReadBytes: Found key="+key);
            if(writeTimeout < 0)
                writeTimeout = 10000;
            if(readLength<0) {
                readLength =  60;
            }
            if(readTimeout<0)
               readTimeout = 1000;

            int uid = ctx.uid;
            int tag = ctx.tag;
            try{
                ToastShow("HandleWriteReadBytes: Write "+Data);
                WriteBytes(ctx,Data,writeTimeout);
                ToastShow("HandleWriteReadBytes: Read");
                Data = ReadBytes(ctx,readLength,readTimeout);
                ToastShow("HandleWriteReadBytes: OnWriteReadFinished");
                return Data;
            }catch(Exception ee){
                StringWriter stringWriter = new StringWriter();
                ee.printStackTrace(new PrintWriter(stringWriter));
                ToastShow(stringWriter.getBuffer().toString());
            }

        }
        return null;
    }

    class UsbRequestBroadcastReceiver extends BroadcastReceiver {

        UsbConnector Parent;
        public static final String USB_DELEGATOR_CONNECT_REQUEST = "USB_DELEGATOR_CONNECT_REQUEST";
        public static final String USB_DELEGATOR_DISCONNECT_REQUEST = "USB_DELEGATOR_DISCONNECT_REQUEST";
        public static final String USB_DELEGATOR_WRITEBYTES_REQUEST = "USB_DELEGATOR_WRITEBYTES_REQUEST";
        public static final String USB_DELEGATOR_READBYTES_REQUEST = "USB_DELEGATOR_READBYTES_REQUEST";
        public static final String USB_DELEGATOR_WRITEREADBYTES_REQUEST = "USB_DELEGATOR_WRITEREADBYTES_REQUEST";

        public static final String USB_PERMISSION = "com.android.example.USB_PERMISSION";
        public UsbRequestBroadcastReceiver(UsbConnector Parent) {
            this.Parent = Parent;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(USB_PERMISSION)) {
                HandlePermission(intent);
            }
        }

        private void HandlePermission(Intent intent){
            if(Parent != null) {
                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && Parent!=null) {
                    String key = CreateDeviceKey(device);
                    if(Parent.UsbConnections.containsKey(key)){
                        UsbConnectionContext ctx = Parent.UsbConnections.get(key);
                        if(ctx.manager.hasPermission(ctx.mUsbDevice)) {
                            Parent.OnUsbDevicePermissionGranted(ctx);
                        }
                    }
                }
            }
        }
        private void HandleConnect(Intent intent){
            int uid = intent.getIntExtra("uid", -1);
            int vid = intent.getIntExtra("VendorId", -1);
            int pid = intent.getIntExtra("ProductId", -1);
            int tag = intent.getIntExtra("Tag", -1);
            String key = CreateDeviceKey(vid,pid);
            ToastShow(String.format("HandleConnect...uid=%d,vid=%d,pid=%d,tag=%d",uid,vid,pid,tag));
            if(Parent.UsbConnections.containsKey(key)){
                // already connected
                ToastShow(String.format("HandleConnect.->OnConnected( uid=%d,vid=%d,pid=%d,tag=%d)",uid,vid,pid,tag));
            }
            else{
                ToastShow(String.format("HandleConnect.->.ConnectUsb uid=%d,vid=%d,pid=%d,tag=%d",uid,vid,pid,tag));
                ConnectUsb(uid,vid,pid,tag);
            }
        }
        private void HandleDisconnect(Intent intent){
            int uid = intent.getIntExtra("uid", -1);
            int vid = intent.getIntExtra("VendorId", -1);
            int pid = intent.getIntExtra("ProductId", -1);
            int tag = intent.getIntExtra("Tag", -1);

        }
    }
    public void Disconnect(int vid,int pid){
        String key = CreateDeviceKey(vid,pid);
        if(UsbConnections.containsKey(key)){
            UsbConnectionContext ctx = UsbConnections.get(key);
            try{
                UsbConnections.remove(key);
                ctx.mDeviceConnection.close();
            }catch(Exception ee){
                ee.printStackTrace();
            }
        }
    }
    private static String CreateDeviceKey(Intent intent){
        int vid = intent.getIntExtra("VendorId", -1);
        int pid = intent.getIntExtra("ProductId", -1);
        return CreateDeviceKey(vid,pid);
    }
    public static String CreateDeviceKey(int vid,int pid){
        return String.format("%d:%d", vid, pid);
    }
    public static String CreateDeviceKey(UsbDevice device){
        int vid = device.getVendorId();
        int pid = device.getProductId();
        return CreateDeviceKey(vid,pid);
    }
    UsbRequestBroadcastReceiver receiver;
}
