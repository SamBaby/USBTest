package ecpay;

public class DistributedNumberData {
    private String MerchantID;
    private String InvoiceYear;
    public DistributedNumberData() {
    }
    public DistributedNumberData(String merchantID, String invoiceYear) {
        setMerchantID(merchantID);
        setInvoiceYear(invoiceYear);
    }

    public String getMerchantID() {
        return MerchantID;
    }

    public void setMerchantID(String merchantID) {
        MerchantID = merchantID;
    }

    public String getInvoiceYear() {
        return InvoiceYear;
    }

    public void setInvoiceYear(String invoiceYear) {
        InvoiceYear = invoiceYear;
    }
}
