package ecpay;

import android.os.Build;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * �@�Ψ禡���O
 *
 * @author mark.chiu
 */
public class EcpayFunction {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String httpPost(String url, String urlParameters, String encoding) {
        try {
            URL obj = new URL(url);
            HttpURLConnection connection = null;
            if (obj.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                connection = (HttpsURLConnection) obj.openConnection();
            } else {
                connection = (HttpURLConnection) obj.openConnection();
            }
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept-Language", encoding);
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            BufferedReader in = connection.getResponseCode() != 200 ? new BufferedReader(new InputStreamReader(connection.getErrorStream(), encoding)) : new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ���� Unix TimeStamp
     *
     * @return TimeStamp
     */
    public static long genUnixTimeStamp() {
        return System.currentTimeMillis() / 1000L;
    }

    public static Document xmlParser(String uri) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * https �B�z
     */
    private static void trustAllHosts() {

        X509TrustManager easyTrustManager = new X509TrustManager() {

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // Oh, I am easy!
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // Oh, I am easy!
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

        };

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{easyTrustManager};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String encodeValue(String value) {
        String code = null;
        try {
            code = URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    public static String decode(String value) {
        String code = null;
        try {
            code = URLDecoder.decode(value, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    public static String encrypt(String algorithm, String input, SecretKeySpec key, IvParameterSpec iv) {

        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] cipherText = cipher.doFinal(input.getBytes());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Base64.getEncoder().encodeToString(cipherText);
            } else {
                return android.util.Base64.encodeToString(cipherText, 0);
            }
        } catch (Exception e) {
            return null;
        }
    }


    public static String decrypt(String algorithm, String cipherText, SecretKeySpec key, IvParameterSpec iv) {

        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] plainText = new byte[0];
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
            } else {
                plainText = cipher.doFinal(android.util.Base64.decode(cipherText, 0));
            }
            return new String(plainText);
        } catch (Exception e) {
            return null;
        }
    }

    public static String ECPayEncrypt(String data, String algorithm, String key, String IV) {
        String URLEncode = encodeValue(data);
        return encrypt(algorithm, URLEncode, new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8)));
    }

    public static String ECPayDecrypt(String data, String algorithm, String key, String IV) {
        String aesDecrypt = decrypt(algorithm, data, new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES"), new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8)));
        return decode(aesDecrypt);
    }

    public static String getCurrentDateTime() {
        Date currentDate = new Date();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);

        return formatter.format(currentDate);
    }

    public static Vector<DistributedNumberInfo> getDistributedNumbers(String year, String merchantID, String algorithm, String key, String IV) {
        RqHeader header = new RqHeader();
        header.setTimestamp(genUnixTimeStamp());
        DistributedNumberData data = new DistributedNumberData();
        data.setMerchantID(merchantID);
        data.setInvoiceYear(year);
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        String dataString = gson.toJson(data);

        EnvoiceJson json = new EnvoiceJson();
        json.MerchantID = merchantID;
        json.RqHeader = header;
        json.Data = EcpayFunction.ECPayEncrypt(dataString, algorithm, key, IV);

        String jsonText = gson.toJson(json);
        String res = EcpayFunction.httpPost("https://einvoice-stage.ecpay.com.tw/B2CInvoice/GetGovInvoiceWordSetting", jsonText, "UTF-8");
        if (res != null) {
            try {
                JSONObject ret = new JSONObject(res);
                String reply = ret.getString("Data");
                if (reply.isEmpty()) {
                    return null;
                }
                JSONObject dataJson = new JSONObject(EcpayFunction.ECPayDecrypt(reply, algorithm, key, IV));
                if (dataJson.getInt("RtnCode") == 1) {
                    JSONArray array = dataJson.getJSONArray("InvoiceInfo");
                    Vector<DistributedNumberInfo> infoList = new Vector<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject info = array.getJSONObject(i);
                        infoList.add(gson.fromJson(info.toString(), DistributedNumberInfo.class));
                    }
                    return infoList;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static MachineNumberInfo getMachineNumbers(String merchantID, String year, int term, String machineID, String algorithm, String key, String IV) {
        RqHeader header = new RqHeader();
        header.setTimestamp(genUnixTimeStamp());
        MachineNumberData data = new MachineNumberData();
        data.setMerchantID(merchantID);
        data.setInvoiceYear(year);
        data.setInvoiceTerm(term);
        data.setInvoiceStatus(1);
        data.setMachineID(machineID);
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        String dataString = gson.toJson(data);

        EnvoiceJson json = new EnvoiceJson();
        json.MerchantID = merchantID;
        json.RqHeader = header;
        json.Data = EcpayFunction.ECPayEncrypt(dataString, algorithm, key, IV);

        String jsonText = gson.toJson(json);
        String res = EcpayFunction.httpPost("https://einvoice-stage.ecpay.com.tw/B2CInvoice/GetOfflineInvoiceWordSetting", jsonText, "UTF-8");
        if (res != null) {
            try {
                JSONObject ret = new JSONObject(res);
                String reply = ret.getString("Data");
                if (reply.isEmpty()) {
                    return null;
                }
                JSONObject dataJson = new JSONObject(EcpayFunction.ECPayDecrypt(reply, algorithm, key, IV));
                if (dataJson.getInt("RtnCode") == 1) {
                    MachineNumberInfo info = new MachineNumberInfo();
                    info.setInvoiceHeader(dataJson.getString("InvoiceHeader"));
                    info.setInvoiceStart(dataJson.getString("InvoiceStart"));
                    info.setInvoiceEnd(dataJson.getString("InvoiceEnd"));
                    info.setTimes(dataJson.getInt("Times"));
                    return info;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        return String.valueOf(year - 1911);
    }

    public static int getCurrentTerm() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;
        int term = month / 2;
        if (month % 2 == 1) {
            term++;
        }
        return term;
    }

    public static String getMachineInvoiceNumber(String merchantID, String machineID, String algorithm, String key, String IV) {
        RqHeader header = new RqHeader();
        header.setTimestamp(genUnixTimeStamp());
        MachineNumberData data = new MachineNumberData();

        data.setMerchantID(merchantID);
        data.setInvoiceYear(getCurrentYear());
        data.setInvoiceTerm(getCurrentTerm());
        data.setInvoiceStatus(1);
        data.setMachineID(machineID);
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        String dataString = gson.toJson(data);

        EnvoiceJson json = new EnvoiceJson();
        json.MerchantID = merchantID;
        json.RqHeader = header;
        json.Data = EcpayFunction.ECPayEncrypt(dataString, algorithm, key, IV);

        String jsonText = gson.toJson(json);
        String res = EcpayFunction.httpPost("https://einvoice-stage.ecpay.com.tw/B2CInvoice/GetOfflineInvoiceWordSetting", jsonText, "UTF-8");
        if (res != null) {
            try {
                JSONObject ret = new JSONObject(res);
                String reply = ret.getString("Data");
                if (reply.isEmpty()) {
                    return null;
                }
                JSONObject dataJson = new JSONObject(EcpayFunction.ECPayDecrypt(reply, algorithm, key, IV));
                if (dataJson.getInt("RtnCode") == 1) {
                    MachineNumberInfo info = new MachineNumberInfo();
                    info.setInvoiceHeader(dataJson.getString("InvoiceHeader"));
                    info.setInvoiceStart(dataJson.getString("InvoiceStart"));
                    info.setInvoiceEnd(dataJson.getString("InvoiceEnd"));
                    info.setTimes(dataJson.getInt("Times"));
                    long start = Long.parseLong(info.getInvoiceStart());
                    long end = Long.parseLong(info.getInvoiceEnd());
                    long times = info.getTimes() - 1;
                    long current = start + times;
                    if (current > end) {
                        return null;
                    } else {
                        return info.getInvoiceHeader() + current;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean setMachineNumbers(String merchantID, String year, int term, String machineID,
                                            String invoiceHeader, String start, String end,
                                            String algorithm, String key, String IV) {
        RqHeader header = new RqHeader();
        header.setTimestamp(genUnixTimeStamp());
        NumberDistributionData data = new NumberDistributionData();
        data.setMerchantID(merchantID);
        data.setInvoiceYear(year);
        data.setInvoiceTerm(term);
        data.setMachineID(machineID);
        data.setInvoiceHeader(invoiceHeader);
        data.setInvoiceStart(start);
        data.setInvoiceEnd(end);
        data.setInvType("07");
        data.setInvoiceCategory("4");
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
        String dataString = gson.toJson(data);

        EnvoiceJson json = new EnvoiceJson();
        json.MerchantID = merchantID;
        json.RqHeader = header;
        json.Data = EcpayFunction.ECPayEncrypt(dataString, algorithm, key, IV);

        String jsonText = gson.toJson(json);
        String res = EcpayFunction.httpPost("https://einvoice-stage.ecpay.com.tw/B2CInvoice/AddInvoiceWordSetting", jsonText, "UTF-8");
        if (res != null) {
            try {
                JSONObject ret = new JSONObject(res);
                String reply = ret.getString("Data");
                if (reply.isEmpty()) {
                    return false;
                }
                String fff = "";
                fff.getBytes("GBK");
                JSONObject dataJson = new JSONObject(EcpayFunction.ECPayDecrypt(reply, algorithm, key, IV));
                if (dataJson.getInt("RtnCode") == 1) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
