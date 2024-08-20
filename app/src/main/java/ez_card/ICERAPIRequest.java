package ez_card;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class ICERAPIRequest {
    private final int TXN_AMT_UNIT = 100; // amount accurate to the second decimal place without decimal mark, ex. 10.01 -> 1001(10.01*100)
    private String batchNo;

    public ICERAPIRequest(String batchNo) {
        this.batchNo = batchNo;
    }

    private static final Map<String, String> PROCESS_CODE = new HashMap<String, String>() {{
        put("signon", "881999");
        put("deduct", "606100");
        put("refund", "620061");
        put("query", "296000");
        put("settlement", "900099");
        put("cancel", "816100");
    }};

    private static final Map<String, String> MESSAGE_TYPE = new HashMap<String, String>() {{
        put("request", "0200");
        put("settlement", "0500");
        put("signon", "0800");
    }};

    /**
     * get the deduct's request xml string
     *
     * @param amount          transaction amount
     * @param hostSerialNum   HOST serialNum
     * @param transactionSeq  transactionSeq
     * @param icerTXNSequence icerTXNSequence
     * @return xml
     */
    public String deductRequest(int amount, String hostSerialNum, String transactionSeq, String icerTXNSequence) {
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("<T0400>").append(calAmount(amount)).append("</T0400>");
        requestBody.append("<T1100>").append(icerTXNSequence).append("</T1100>");
        requestBody.append("<T1101>").append(hostSerialNum).append("</T1101>");
        requestBody.append("<T3701>").append(transactionSeq).append("</T3701>");
        return buildRequestXml(MESSAGE_TYPE.get("request"), PROCESS_CODE.get("deduct"), requestBody.toString());
    }

    /**
     * get the refund's request xml string
     *
     * @param amount          transaction amount
     * @param hostSerialNum   HOST serialNum
     * @param transactionSeq  transactionSeq
     * @param icerTXNSequence icerTXNSequence
     * @return xml
     */
    public String refundRequest(int amount, String hostSerialNum, String transactionSeq, String icerTXNSequence) {
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("<T0400>").append(calAmount(amount)).append("</T0400>");
        requestBody.append("<T1100>").append(icerTXNSequence).append("</T1100>");
        requestBody.append("<T1101>").append(hostSerialNum).append("</T1101>");
        requestBody.append("<T3701>").append(transactionSeq).append("</T3701>");
        return buildRequestXml(MESSAGE_TYPE.get("request"), PROCESS_CODE.get("refund"), requestBody.toString());
    }

    /**
     * get the cancel's request xml string
     *
     * @param cancelAmount    cancel amount
     * @param hostSerialNum   HOST serialNum
     * @param transactionSeq  transactionSeq
     * @param icerTXNSequence icerTXNSequence
     * @param oriDeviceId     original device id
     * @param oriRRN          original reference number
     * @param oriTxnDate      original transaction date
     * @return xml
     */
    public String cancelRequest(int cancelAmount, String hostSerialNum, String transactionSeq, String icerTXNSequence, String oriDeviceId, String oriRRN, String oriTxnDate) {
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("<T0400>").append(calAmount(cancelAmount)).append("</T0400>");
        requestBody.append("<T1100>").append(icerTXNSequence).append("</T1100>");
        requestBody.append("<T1101>").append(hostSerialNum).append("</T1101>");
        requestBody.append("<T5581>").append(oriDeviceId).append("</T5581>");
        requestBody.append("<T5582>").append(oriRRN).append("</T5582>");
        requestBody.append("<T5583>").append(oriTxnDate).append("</T5583>");
        requestBody.append("<T3701>").append(transactionSeq).append("</T3701>");
        return buildRequestXml(MESSAGE_TYPE.get("request"), PROCESS_CODE.get("cancel"), requestBody.toString());
    }

    /**
     * get the query's request xml string
     *
     * @param hostSerialNum   HOST serialNum
     * @param icerTXNSequence icerTXNSequence
     * @return xml
     */
    public String queryRequest(String hostSerialNum, String icerTXNSequence) {
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("<T0400>000</T0400>");
        requestBody.append("<T1100>").append("100859").append("</T1100>");
        requestBody.append("<T1101>").append("100859").append("</T1101>");
        return buildRequestXml(MESSAGE_TYPE.get("request"), PROCESS_CODE.get("query"), requestBody.toString());
    }

    /**
     * get the settlement's request xml string
     *
     * @param hostSerialNum    HOST serialNum
     * @param transactionCount transaction count
     * @param transactionTotal transaction total
     * @param icerTXNSequence  icerTXNSequence
     * @return xml
     */
    public String settlementRequest(String hostSerialNum, int transactionCount, int transactionTotal, String icerTXNSequence) {
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("<T1100>").append(icerTXNSequence).append("</T1100>");
        requestBody.append("<T1101>").append(hostSerialNum).append("</T1101>");
        requestBody.append("<T5591>").append(transactionCount).append("</T5591>");
        requestBody.append("<T5592>");
        requestBody.append("<T559201>").append(transactionTotal).append("</T559201>");
        requestBody.append("</T5592>");
        return buildRequestXml(MESSAGE_TYPE.get("settlement"), PROCESS_CODE.get("settlement"), requestBody.toString());
    }

    /**
     * get the signon's request xml string
     *
     * @param hostSerialNum   HOST serialNum
     * @param icerTXNSequence icerTXNSequence
     * @return xml
     */
    public String signonRequest(String hostSerialNum, String icerTXNSequence) {
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("<T1100>").append(icerTXNSequence).append("</T1100>");
        requestBody.append("<T1101>").append(hostSerialNum).append("</T1101>");
        return buildRequestXml(MESSAGE_TYPE.get("signon"), PROCESS_CODE.get("signon"), requestBody.toString());
    }

    /**
     * calculate amount to icerapi amount
     *
     * @param amount transaction amount
     * @return calculated amount
     */
    private int calAmount(int amount) {
        double tmpAmount = amount * TXN_AMT_UNIT;
        return (int) Math.round(tmpAmount);
    }

    /**
     * build the request xml string
     *
     * @param messageType messageType
     * @param processCode processCode
     * @param requestBody requestBody
     * @return xml
     */
    private String buildRequestXml(String messageType, String processCode, String requestBody) {
        StringBuilder request = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><TransXML><TRANS>");
        request.append("<T0100>").append(messageType).append("</T0100>");
        request.append("<T0300>").append(processCode).append("</T0300>");
        if (requestBody != null && !requestBody.isEmpty()) {
            if (batchNo != null && !batchNo.isEmpty()) {
                requestBody += "<T5501>" + batchNo + "</T5501>";
            }
            request.append("<T1200>").append(new SimpleDateFormat("HHmmss").format(new Date())).append("</T1200>");
            request.append("<T1300>").append(new SimpleDateFormat("yyyyMMdd").format(new Date())).append("</T1300>");
            request.append(requestBody);
        }
        request.append("</TRANS></TransXML>");
        return request.toString();
    }
}
