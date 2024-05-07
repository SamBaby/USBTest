package invoice_print_machine;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;

public class InvoiceUSBController {
    private final Context mApplicationContext;
    private final UsbManager mUsbManager;
    protected static final String ACTION_USB_PERMISSION = "com.dg.usbconn.USB";
    private final Handler mHandler;
    public static final int USB_CONNECTED = 0;
    public static final int USB_DISCONNECTED = 1;
    private UsbEndpoint ep = null;
    private UsbEndpoint ep_in = null;
    private UsbInterface usbIf = null;
    private UsbDeviceConnection conn = null;
    private final BroadcastReceiver mPermissionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            InvoiceUSBController.this.mApplicationContext.unregisterReceiver(this);
            if (intent.getAction().equals("com.dg.usbconn.USB") && intent.getBooleanExtra("permission", false)) {
                Log.d("usb调试", "已获取usb设备访问权限");
                UsbDevice dev = (UsbDevice)intent.getParcelableExtra("device");
                if (dev != null) {
                    Message msg = InvoiceUSBController.this.mHandler.obtainMessage(0);
                    InvoiceUSBController.this.mHandler.sendMessage(msg);
                }
            }

        }
    };

    public InvoiceUSBController(Activity parentActivity, Handler handler) {
        this.mApplicationContext = parentActivity;
        this.mUsbManager = (UsbManager)this.mApplicationContext.getSystemService(Context.USB_SERVICE);
        this.mHandler = handler;
    }

    public synchronized UsbDevice getDev(int vid, int pid) {
        UsbDevice dev = null;
        this.ep = null;
        this.ep_in = null;
        this.usbIf = null;
        this.conn = null;
        HashMap<String, UsbDevice> devlist = this.mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviter = devlist.values().iterator();

        while(deviter.hasNext()) {
            UsbDevice d = (UsbDevice)deviter.next();
            Log.d("usb device:", d.getDeviceName() + "  " + String.format("%04X:%04X", d.getVendorId(), d.getProductId()));
            if (d.getVendorId() == vid && d.getProductId() == pid) {
                dev = d;
                break;
            }
        }

        return dev;
    }

    public synchronized HashMap<String, UsbDevice> getUsbList() {
        return this.mUsbManager.getDeviceList();
    }

    public synchronized boolean isHasPermission(UsbDevice dev) {
        return this.mUsbManager.hasPermission(dev);
    }

    public synchronized void getPermission(UsbDevice dev) {
        if (dev != null) {
            if (!this.isHasPermission(dev)) {
                PendingIntent pi = PendingIntent.getBroadcast(this.mApplicationContext, 0, new Intent("com.dg.usbconn.USB"), 0);
                this.mApplicationContext.registerReceiver(this.mPermissionReceiver, new IntentFilter("com.dg.usbconn.USB"));
                this.mUsbManager.requestPermission(dev, pi);
            } else {
                Message msg = this.mHandler.obtainMessage(0);
                this.mHandler.sendMessage(msg);
            }

        }
    }

    public synchronized void sendMsg(String msg, String charset, UsbDevice dev) {
        if (msg.length() != 0) {
            byte[] send;
            try {
                send = msg.getBytes(charset);
            } catch (UnsupportedEncodingException var6) {
                send = msg.getBytes();
            }

            this.sendByte(send, dev);
            this.sendByte(new byte[]{13, 10, 0}, dev);
        }
    }

    public void sendByte(byte[] bits, UsbDevice dev) {
        if (bits != null) {
            if (this.ep != null && this.usbIf != null && this.conn != null) {
                this.conn.bulkTransfer(this.ep, bits, bits.length, 0);
            } else {
                if (this.conn == null) {
                    this.conn = this.mUsbManager.openDevice(dev);
                }

                if (dev.getInterfaceCount() == 0) {
                    return;
                }

                this.usbIf = dev.getInterface(0);
                if (this.usbIf.getEndpointCount() == 0) {
                    return;
                }

                for(int i = 0; i < this.usbIf.getEndpointCount(); ++i) {
                    if (this.usbIf.getEndpoint(i).getType() == 2 && this.usbIf.getEndpoint(i).getDirection() != 128) {
                        this.ep = this.usbIf.getEndpoint(i);
                    }
                }

                if (this.conn.claimInterface(this.usbIf, true)) {
                    this.conn.bulkTransfer(this.ep, bits, bits.length, 0);
                }
            }

        }
    }

    public byte revByte(UsbDevice dev) {
        byte[] bits = new byte[2];
        if (this.conn == null) {
            this.conn = this.mUsbManager.openDevice(dev);
        }

        if (this.ep_in == null) {
            for(int i = 0; i < this.usbIf.getEndpointCount(); ++i) {
                if (this.usbIf.getEndpoint(i).getType() == 2 && this.usbIf.getEndpoint(i).getDirection() == 128) {
                    this.ep_in = this.usbIf.getEndpoint(i);
                }
            }
        }

        if (this.conn.claimInterface(this.usbIf, true)) {
            this.conn.bulkTransfer(this.ep_in, bits, bits.length, 2000);
        }

        return bits[0];
    }

    public synchronized void close() {
        if (this.conn != null) {
            this.conn.close();
            this.ep = null;
            this.usbIf = null;
            this.conn = null;
        }

    }

    public synchronized void cutPaper(UsbDevice dev, int n) {
        byte[] bits = new byte[]{29, 86, 66, (byte)n};
        this.sendByte(bits, dev);
    }

    public synchronized void print_QRcode(String msg, String charset, UsbDevice dev) {
        byte[] cmd = new byte[5];
        if (msg.length() != 0) {
            byte[] send;
            try {
                send = msg.getBytes(charset);
            } catch (UnsupportedEncodingException var7) {
                send = msg.getBytes();
            }

            cmd[0] = 31;
            cmd[1] = 7;
            cmd[2] = 72;
            cmd[3] = 4;
            cmd[4] = 2;
            this.sendByte(cmd, dev);
            this.sendByte(send, dev);
            this.sendByte(new byte[1], dev);
            cmd[0] = 31;
            cmd[1] = 7;
            cmd[2] = 72;
            cmd[3] = 5;
            this.sendByte(cmd, dev);
        }
    }

    public synchronized void print_barcode(int m, int n, String msg, UsbDevice dev) {
        byte[] cmd = new byte[4];
        if (msg.length() != 0) {
            byte[] send = msg.getBytes();
            cmd[0] = 29;
            cmd[1] = 107;
            cmd[2] = (byte)m;
            if (m < 7) {
                this.sendByte(cmd, dev);
                this.sendByte(send, dev);
                this.sendByte(new byte[1], dev);
            } else if (m >= 65 && m <= 73) {
                cmd[3] = (byte)n;
                this.sendByte(cmd, dev);
                this.sendByte(send, dev);
            }

        }
    }

    public synchronized void catPaperByMode(UsbDevice dev, int mode) {
        byte[] bits = new byte[3];
        switch (mode) {
            case 0:
                bits[0] = 29;
                bits[1] = 86;
                bits[2] = 48;
                break;
            case 1:
                bits[0] = 29;
                bits[1] = 86;
                bits[2] = 49;
        }

        this.sendByte(bits, dev);
    }

    public synchronized void openCashBox(UsbDevice dev) {
        byte[] bits = new byte[]{27, 112, 0, 64, 80};
        this.sendByte(bits, dev);
    }

    public synchronized void defaultBuzzer(UsbDevice dev) {
        byte[] bits = new byte[]{27, 66, 4, 1};
        this.sendByte(bits, dev);
    }

    public synchronized void buzzer(UsbDevice dev, int n, int time) {
        byte[] bits = new byte[]{27, 66, (byte)n, (byte)time};
        this.sendByte(bits, dev);
    }

    public synchronized void setBuzzerMode(UsbDevice dev, int n, int time, int mode) {
        byte[] bits = new byte[]{27, 67, (byte)n, (byte)time, (byte)mode};
        this.sendByte(bits, dev);
    }
}
