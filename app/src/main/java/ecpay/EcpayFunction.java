package ecpay;

import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

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
    public final static String httpPost(String url, String urlParameters, String encoding) {
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
            BufferedReader in = connection.getResponseCode()!=200 ?new BufferedReader(new InputStreamReader(connection.getErrorStream(), encoding)):new BufferedReader(new InputStreamReader(connection.getInputStream(), encoding));
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
    public final static String genUnixTimeStamp() {
        Date date = new Date();
        Integer timeStamp = (int) (date.getTime() / 1000);
        return timeStamp.toString();
    }

    public final static Document xmlParser(String uri) {
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

            public void checkClientTrusted(
                    X509Certificate[] chain,
                    String authType) throws CertificateException {
                // Oh, I am easy!
            }

            public void checkServerTrusted(
                    X509Certificate[] chain,
                    String authType) throws CertificateException {
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

}
