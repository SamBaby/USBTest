package com.example.usbtest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ecpay.BarcodeCheckJson;
import ecpay.EcpayFunction;
import ecpay.EnvoiceData;
import ecpay.EnvoiceItem;
import ecpay.EnvoiceJson;
import ecpay.InvoicePrintJson;
import ecpay.RqHeader;
import ecpay.TaxIDCheckJson;
import usb.UsbConnectionContext;
import usb.UsbConnector;

public class MainActivity extends AppCompatActivity {
    private static final String key = "ejCk326UnaZWKisg";
    private static final String IV = "q9jcZX8Ib9LM8wYk";
    private static final String algorithm = "AES/CBC/PKCS7Padding";
    private static final String merchantID = "3085340";
    private static final String TAG = "USBTest";

    /* USB system service */
    private static final String ACTION_USB_PERMISSION = "com.example.USB_PERMISSION";
    private UsbManager usbManager;
    private UsbDevice device;

    private PendingIntent permissionIntent;

    private Button btnConnect;
    private Button btnDisconnect;
    private Button btnSend;
    private Button btnInvoiceIssue;
    private Button btnInvoicePrint;
    private Button btnQRPrint;
    private Button btnStatus;
    private TextView txtConnect;
    private TextView txtInput;
    private EditText txtOutput;
    private EditText txtVID;
    private EditText txtPID;
    private TextView txtMachine;
    private TextView txtInvoice;
    private boolean reading = false;
    UsbConnector connector;
    UsbConnectionContext cxt;
    private int vid = 1137;
    private int pid = 85;
    private String invoiceNo;
    private String invoiceDate;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = (Button) findViewById(R.id.connect_button);
        btnDisconnect = (Button) findViewById(R.id.disconnect_button);
        btnSend = (Button) findViewById(R.id.send_button);
        btnInvoiceIssue = findViewById(R.id.invoice_issue_button);
        btnInvoicePrint = findViewById(R.id.invoice_print_button);
        btnQRPrint = findViewById(R.id.QR_code_button);
        btnStatus = findViewById(R.id.machine_status_button);
        txtConnect = (TextView) findViewById(R.id.connectText);
        txtInput = (TextView) findViewById(R.id.input);
        txtOutput = (EditText) findViewById(R.id.output_editText);
        txtVID = (EditText) findViewById(R.id.vid_editText);
        txtPID = (EditText) findViewById(R.id.pid_editText);
        txtMachine = (TextView) findViewById(R.id.machine_textView);
        txtInvoice = findViewById(R.id.invoice_textView);

        txtVID.setText(String.valueOf(vid));
        txtPID.setText(String.valueOf(pid));

        connector = new UsbConnector(this);
        btnConnect.setOnClickListener(v -> {
            try {
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
                            } catch (Exception e) {
                                runOnUiThread(() -> {
                                    txtMachine.setText("Reading: " + e.toString());
                                });
                            }
                        }
                    }).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                    runOnUiThread(() -> {
                        txtMachine.setText("Sending: " + e.toString());
                    });
                }
            }
        });
        btnInvoiceIssue.setOnClickListener(v -> {
            invoiceIssue();
        });
        btnInvoicePrint.setOnClickListener(v -> {
            invoicePrint(merchantID, key, IV, invoiceNo, invoiceDate);
        });
        btnQRPrint.setOnClickListener(v -> {
            qrPrint("http://www.google.com");
        });
        btnStatus.setOnClickListener(v -> {
            printMachineStatus();
        });
//        TextView image = findViewById(R.id.image);
//        image.setOnClickListener(v -> {
//            String text = image.getText().toString();
//            copyToClipboard(text);
//            Toast.makeText(MainActivity.this, "已經複製到剪貼簿", Toast.LENGTH_SHORT).show();
//        });
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

    public void invoiceIssue() {
        new Thread(() -> {
            Date currentTime = Calendar.getInstance().getTime();
//            long unixTime = System.currentTimeMillis() / 1000L;
            long unixTime = currentTime.getTime();
            EnvoiceData data = new EnvoiceData();
            data.setMerchantID("2000132");
            data.setRelateNumber("Samuel" + String.valueOf(unixTime));
            data.setCustomerID("97025978");
            data.setCustomerName("Samuel");
            data.setCustomerAddr("SamuelAddr");
            data.setCustomerPhone("");
            data.setCustomerEmail("sameul@samuel.com");
            data.setClearanceMark("");
            data.setPrint("1");
            data.setDonation("0");
            data.setLoveCode("");
            data.setCarrierType("");
            data.setCarrierNum("");
            data.setTaxType(1);
            data.setSpecialTaxType(0);
            data.setSalesAmount(100);
            data.setInvType("07");
            data.setVat("1");
            data.setInvoiceRemark("Samuel1" + String.valueOf(unixTime));
            data.setItems(new EnvoiceItem[]{new EnvoiceItem()});
            data.getItems()[0] = new EnvoiceItem();
            data.getItems()[0].setItemSeq(1);
            data.getItems()[0].setItemName("park");
            data.getItems()[0].setItemCount(1);
            data.getItems()[0].setItemWord("次");
            data.getItems()[0].setItemPrice(100);
            data.getItems()[0].setItemTaxType("");
            data.getItems()[0].setItemAmount(100);
            data.getItems()[0].setItemRemark("one hour");
            EnvoiceJson json = new EnvoiceJson();
            RqHeader header = new RqHeader();
            header.setTimestamp(unixTime);

            json.MerchantID = "2000132";
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            json.RqHeader = header;
            String dataString = gson.toJson(data);
            json.Data = ECPayEncrypt(dataString);
            String jsonText = gson.toJson(json);
            System.out.println(jsonText);

            try {
                String res = EcpayFunction.httpPost("https://einvoice-stage.ecpay.com.tw/B2CInvoice/Issue", jsonText, "UTF-8");
                if (res != null) {
                    JSONObject ret = new JSONObject(res);
                    JSONObject returnData = new JSONObject(ECPayDecrypt(ret.getString("Data")));
                    invoiceNo = returnData.getString("InvoiceNo");
                    invoiceDate = returnData.getString("InvoiceDate").split(" ")[0];
                    if (invoiceNo.isEmpty() || invoiceDate.isEmpty()) {
                        runOnUiThread(() -> {
                            txtInvoice.setText("發票開立失敗");
                        });
                    } else {
                        runOnUiThread(() -> {
                            txtInvoice.setText("發票號碼:" + invoiceNo);
                        });
                    }
                } else {
                    runOnUiThread(() -> {
                        txtInvoice.setText("發票開立失敗");
                    });
                }
            } catch (Exception ee) {
                ee.printStackTrace();
                runOnUiThread(() -> {
                    txtInvoice.setText("發票開立失敗");
                });
            }
        }).start();
    }

    public void invoicePrint(String merchantID, String key, String IV, String invoiceNumber, String date) {
        if (invoiceNumber != null && date != null && !invoiceNumber.isEmpty() && !date.isEmpty()) {
            new Thread(() -> {
                long unixTime = System.currentTimeMillis() / 1000L;
                EnvoiceJson json = new EnvoiceJson();
                RqHeader header = new RqHeader();
                header.setTimestamp(unixTime);
                InvoicePrintJson data = new InvoicePrintJson();
                data.setMerchantID(merchantID);
                data.setInvoiceNo(invoiceNumber);
                data.setInvoiceDate(date);
                data.setPrintStyle(3);
                json.MerchantID = "2000132";
                Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
                json.RqHeader = header;
                String dataString = gson.toJson(data);
                json.Data = ECPayEncrypt(dataString);
                String jsonText = gson.toJson(json);
                System.out.println(jsonText);

                try {
                    String res = EcpayFunction.httpPost("https://einvoice-stage.ecpay.com.tw/B2CInvoice/InvoicePrint", jsonText, "UTF-8");
                    if (res != null) {
                        JSONObject ret = new JSONObject(res);
                        JSONObject returnData = new JSONObject(ECPayDecrypt(ret.getString("Data")));
                        String url = returnData.getString("InvoiceHtml");
                        if (!url.isEmpty()) {
                            runOnUiThread(() -> {
                                WebView view = new WebView(this);
                                view.setPictureListener(new WebView.PictureListener() {
                                    boolean print = true;

                                    @Override
                                    public void onNewPicture(WebView view, @Nullable Picture picture) {
                                        if (print && view.getHeight() > 0 && view.getWidth() >= 100) {
                                            view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                                                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                                            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
                                            view.setDrawingCacheEnabled(true);
                                            view.buildDrawingCache();
                                            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                                            Canvas canvas = new Canvas(bitmap);
                                            view.draw(canvas);
                                            invoiceMachinePrint(bitmap);
                                            view.setVisibility(View.GONE);
                                            print = false;
                                            view.destroy();
                                        } else if (print) {
                                            view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                                                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                                            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
                                        }

                                    }
                                });
                                // 启用 JavaScript
                                WebSettings webSettings = view.getSettings();
                                webSettings.setJavaScriptEnabled(true);

                                // 加载 HTML 内容
                                view.loadUrl(url);
                            });
                        } else {
                            runOnUiThread(() -> {
                                txtInvoice.setText("發票網址取得失敗");
                            });
                        }

                    } else {
                        runOnUiThread(() -> {
                            txtInvoice.setText("發票網址取得失敗");
                        });
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                    runOnUiThread(() -> {
                        txtInvoice.setText("發票網址取得失敗");
                    });
                }
            }).start();
        } else {
            txtInvoice.setText("發票網址取得失敗");
        }

    }

    public void barcodeCheck(String merchantID, String key, String IV, String barcode) {
        new Thread(() -> {
            long unixTime = System.currentTimeMillis() / 1000L;
            EnvoiceJson json = new EnvoiceJson();
            RqHeader header = new RqHeader();
            header.setTimestamp(unixTime);
            BarcodeCheckJson data = new BarcodeCheckJson();
            data.setMerchantID(merchantID);
            data.setBarCode(barcode);
            json.MerchantID = "2000132";
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            json.RqHeader = header;
            String dataString = gson.toJson(data);
            json.Data = ECPayEncrypt(dataString);
            String jsonText = gson.toJson(json);
            System.out.println(jsonText);

            try {
                String res = EcpayFunction.httpPost("https://einvoice-stage.ecpay.com.tw/B2CInvoice/CheckBarcode", jsonText, "UTF-8");
                if (res != null) {
                    JSONObject ret = new JSONObject(res);
                    JSONObject returnData = new JSONObject(ECPayDecrypt(ret.getString("Data")));
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }).start();
    }

    public void taxIDCheck(String merchantID, String key, String IV, String taxID) {
        new Thread(() -> {
            long unixTime = System.currentTimeMillis() / 1000L;
            EnvoiceJson json = new EnvoiceJson();
            RqHeader header = new RqHeader();
            header.setTimestamp(unixTime);
            TaxIDCheckJson data = new TaxIDCheckJson();
            data.setMerchantID(merchantID);
            data.setUnifiedBusinessNo(taxID);
            json.MerchantID = "2000132";
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
            json.RqHeader = header;
            String dataString = gson.toJson(data);
            json.Data = ECPayEncrypt(dataString);
            String jsonText = gson.toJson(json);
            System.out.println(jsonText);

            try {
                String res = EcpayFunction.httpPost("https://einvoice-stage.ecpay.com.tw/B2CInvoice/GetCompanyNameByTaxID", jsonText, "UTF-8");
                if (res != null) {
                    JSONObject ret = new JSONObject(res);
                    JSONObject returnData = new JSONObject(ECPayDecrypt(ret.getString("Data")));
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }).start();
    }

    public void printMachineStatus() {
        if (cxt != null) {
            byte[] status = new byte[]{0x10, 0x04, 0x04};
            connector.WriteBytes(cxt, status, 0);
        }
    }

    public void qrPrint(String text) {
        if (cxt != null) {
            byte[] size = new byte[]{0x1D, 0x01, 0x03, 0x0A};
            byte[] faultLevel = new byte[]{0x1D, 0x01, 0x04, 0x32};
            byte[] length = new byte[]{0x1D, 0x01, 0x01, (byte) text.length(), 0x00};
            byte[] print = new byte[]{0x1D, 0x01, 0x02};
            byte[] line = new byte[]{0x0A};
            byte[] content = new byte[text.length()];
            for (int i = 0; i < text.length(); i++) {
                content[i] = (byte) text.charAt(i);
            }
            byte[] reset = new byte[2];
            reset[0] = 0x1B;
            reset[1] = 0x40;
            byte[] position = new byte[4];
            position[0] = 0x1B;
            position[1] = 0x24;
            position[2] = 0x50;
            position[3] = 0x00;
            byte[] cut = new byte[2];
            cut[0] = 0x1B;
            cut[1] = 0x6D;
            byte[] blank = new byte[3];
            blank[0] = 0x1B;
            blank[1] = 0x4A;
            blank[2] = (byte) 0xA0;
            byte[] rollForward = new byte[3];
            rollForward[0] = 0x1B;
            rollForward[1] = 0x4A;
            rollForward[2] = 0x05;
            connector.WriteBytes(cxt, rollForward, 0);
            connector.WriteBytes(cxt, reset, 0);

            connector.WriteBytes(cxt, size, 0);
            connector.WriteBytes(cxt, faultLevel, 0);
            connector.WriteBytes(cxt, length, 0);
            connector.WriteBytes(cxt, content, 0);
            connector.WriteBytes(cxt, position, 0);
            connector.WriteBytes(cxt, print, 0);
            connector.WriteBytes(cxt, line, 0);

            //print 50 blank lines
            connector.WriteBytes(cxt, blank, 0);
            //cut paper
            connector.WriteBytes(cxt, cut, 0);
            //roll back paper 60 pixels
            byte[] rollback = new byte[3];
            rollback[0] = 0x1B;
            rollback[1] = 0x6A;
            rollback[2] = 0x60;
            connector.WriteBytes(cxt, rollback, 0);
            connector.WriteBytes(cxt, reset, 0);
        }
    }

    public void invoiceMachinePrint(Bitmap invoicePic) {
        // 指定目标宽度和高度
        int targetWidth = 456;
        int targetHeight = 720;

        // 缩放 Bitmap
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(invoicePic, targetWidth, targetHeight, false);
        ImageView view = findViewById(R.id.image);
        view.setImageBitmap(scaledBitmap);
        if (cxt != null) {
            try {
                runOnUiThread(() -> {
                    int s = 0, index = 0;
                    byte[] sendData = printDraw(scaledBitmap);
                    byte[] temp = new byte[8 + (targetWidth / 8)];
                    byte[] reset = new byte[2];
                    reset[0] = 0x1B;
                    reset[1] = 0x40;
                    byte[] position = new byte[4];
                    position[0] = 0x1B;
                    position[1] = 0x24;
                    position[2] = 0x40;
                    position[3] = 0x00;
                    byte[] cut = new byte[2];
                    cut[0] = 0x1B;
                    cut[1] = 0x6D;
                    byte[] blank = new byte[3];
                    blank[0] = 0x1B;
                    blank[1] = 0x4A;
                    blank[2] = 0x50;
                    byte[] rollForward = new byte[3];
                    rollForward[0] = 0x1B;
                    rollForward[1] = 0x4A;
                    rollForward[2] = 0x05;
                    connector.WriteBytes(cxt, rollForward, 0);
                    for (int i = 0; i < targetHeight; i++) {
                        //clear register
                        if (i % 240 == 0) {
                            connector.WriteBytes(cxt, reset, 0);
                        }
                        index = 0;
                        temp[index++] = 0x1D;
                        temp[index++] = 0x76;
                        temp[index++] = 0x30;
                        temp[index++] = 0x00;
                        temp[index++] = (byte) (targetWidth / 8);
                        temp[index++] = 0x00;
                        temp[index++] = (byte) 0x01;
                        temp[index++] = 0x00;
                        for (int j = 0; j < (targetWidth / 8); j++) {
                            temp[index++] = sendData[s++];
                        }
                        //reset position and print line
                        connector.WriteBytes(cxt, position, 0);
                        connector.WriteBytes(cxt, temp, 0);
                    }
                    //print 50 blank lines
                    connector.WriteBytes(cxt, blank, 0);
                    //cut paper
                    connector.WriteBytes(cxt, cut, 0);
                    //roll back paper 50 pixels
                    byte[] rollback = new byte[3];
                    rollback[0] = 0x1B;
                    rollback[1] = 0x6A;
                    rollback[2] = 0x60;
                    connector.WriteBytes(cxt, rollback, 0);
                    connector.WriteBytes(cxt, reset, 0);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            runOnUiThread(() -> {
                txtInvoice.setText("can't find device");
            });
        }
    }

    private String encodeValue(String value) {
        String code = null;
        try {
            code = URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    private String decode(String value) {
        String code = null;
        try {
            code = URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    public static String encrypt(String algorithm, String input, SecretKeySpec key,
                                 IvParameterSpec iv) {

        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] cipherText = cipher.doFinal(input.getBytes());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder()
                        .encodeToString(cipherText);
            } else {
                return android.util.Base64.encodeToString(cipherText, 0);
            }
        } catch (Exception e) {
            return null;
        }
    }


    public static String decrypt(String algorithm, String cipherText, SecretKeySpec key,
                                 IvParameterSpec iv) {

        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] plainText = new byte[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                plainText = cipher.doFinal(Base64.getDecoder()
                        .decode(cipherText));
            } else {
                plainText = cipher.doFinal(android.util.Base64.decode(cipherText, 0));
            }
            return new String(plainText);
        } catch (Exception e) {
            return null;
        }
    }

    public String ECPayEncrypt(String data) {
        String URLEncode = encodeValue(data);
        return encrypt(algorithm, URLEncode, new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8)));
    }

    public String ECPayDecrypt(String data) {
        String aesDecrypt = decrypt(algorithm, data, new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8)));
        return decode(aesDecrypt);
    }

    public byte[] printDraw(Bitmap nbm) {
        if (nbm.getHeight() == 0) {
            return null;
        } else {
            byte[] imgbuf = new byte[nbm.getWidth() / 8 * nbm.getHeight()];
            int s = 0;
            byte[] bitbuf = new byte[nbm.getWidth() / 8];
            try {
                for (int i = 0; i < nbm.getHeight(); ++i) {
                    int k;
                    for (k = 0; k < nbm.getWidth() / 8; ++k) {
                        int c0 = nbm.getPixel(k * 8 + 0, i);
                        byte p0;
                        if (c0 == -1) {
                            p0 = 0;
                        } else {
                            p0 = 1;
                        }

                        int c1 = nbm.getPixel(k * 8 + 1, i);
                        byte p1;
                        if (c1 == -1) {
                            p1 = 0;
                        } else {
                            p1 = 1;
                        }

                        int c2 = nbm.getPixel(k * 8 + 2, i);
                        byte p2;
                        if (c2 == -1) {
                            p2 = 0;
                        } else {
                            p2 = 1;
                        }

                        int c3 = nbm.getPixel(k * 8 + 3, i);
                        byte p3;
                        if (c3 == -1) {
                            p3 = 0;
                        } else {
                            p3 = 1;
                        }

                        int c4 = nbm.getPixel(k * 8 + 4, i);
                        byte p4;
                        if (c4 == -1) {
                            p4 = 0;
                        } else {
                            p4 = 1;
                        }

                        int c5 = nbm.getPixel(k * 8 + 5, i);
                        byte p5;
                        if (c5 == -1) {
                            p5 = 0;
                        } else {
                            p5 = 1;
                        }

                        int c6 = nbm.getPixel(k * 8 + 6, i);
                        byte p6;
                        if (c6 == -1) {
                            p6 = 0;
                        } else {
                            p6 = 1;
                        }

                        int c7 = nbm.getPixel(k * 8 + 7, i);
                        byte p7;
                        if (c7 == -1) {
                            p7 = 0;
                        } else {
                            p7 = 1;
                        }

                        int value = p0 * 128 + p1 * 64 + p2 * 32 + p3 * 16 + p4 * 8 + p5 * 4 + p6 * 2 + p7;
                        bitbuf[k] = (byte) value;
                    }
                    for (k = 0; k < nbm.getWidth() / 8; ++k) {
                        imgbuf[s] = bitbuf[k];
                        ++s;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return imgbuf;
        }
    }

    public Bitmap hexToBitmap(byte[] bytes, int w, int h) {
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        int s = 0;
        try {
            for (int i = 0; i < bitmap.getHeight(); ++i) {
                for (int k = 0; k < bitmap.getWidth() / 8; ++k) {
                    byte b = bytes[s];
                    s++;
                    String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
                    char[] chars = s1.toCharArray();
                    for (int j = 0; j < chars.length; j++) {
                        char c = chars[j];
                        if (c == '1') {
                            bitmap.setPixel(k * 8 + j, i, 0xFF000000);
                        } else {
                            bitmap.setPixel(k * 8 + j, i, 0xFFFFFFFF);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private void copyToClipboard(String text) {
        // 获取剪贴板管理器
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // 创建 ClipData 对象
        ClipData clipData = ClipData.newPlainText("text", text);

        // 将 ClipData 对象放入剪贴板
        clipboardManager.setPrimaryClip(clipData);
    }
}

