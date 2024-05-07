package ecpay;

public class TaxIDCheckJson {
    private String MerchantID;
    private String UnifiedBusinessNo;

    public String getMerchantID() {
        return MerchantID;
    }

    public void setMerchantID(String merchantID) {
        MerchantID = merchantID;
    }

    public String getUnifiedBusinessNo() {
        return UnifiedBusinessNo;
    }

    public void setUnifiedBusinessNo(String unifiedBusinessNo) {
        UnifiedBusinessNo = unifiedBusinessNo;
    }
}
