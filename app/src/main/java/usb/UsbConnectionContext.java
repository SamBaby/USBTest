package usb;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

/**
 * Created by sunneo on 2018/7/31.
 */

public class UsbConnectionContext {
    public boolean valid;
    public int uid;
    public int vid;
    public int pid;
    public int tag;
    public UsbManager manager; //USB管理器
    public UsbDevice mUsbDevice; //找到的USB設備
    public UsbInterface mInterface;
    public UsbDeviceConnection mDeviceConnection;
    public UsbEndpoint epOut;
    public UsbEndpoint epIn;
    public volatile boolean mConnected;
    public volatile boolean waitPermission;
}
