package ecpay;
public class InvoicePrintJson {
    private String MerchantID;
    private String InvoiceNo;
    private String InvoiceDate;
    private int PrintStyle;

    public String getMerchantID() {
        return MerchantID;
    }

    public void setMerchantID(String merchantID) {
        MerchantID = merchantID;
    }

    public String getInvoiceNo() {
        return InvoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        InvoiceNo = invoiceNo;
    }

    public String getInvoiceDate() {
        return InvoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        InvoiceDate = invoiceDate;
    }

    public int getPrintStyle() {
        return PrintStyle;
    }

    public void setPrintStyle(int printStyle) {
        PrintStyle = printStyle;
    }
}
