package ecpay;

public class MachineNumberData {
    private String MerchantID;
    private String InvoiceYear;
    private int InvoiceTerm;
    private int InvoiceStatus;
    private String MachineID;

    public MachineNumberData() {

    }

    public MachineNumberData(String merchantID, String invoiceYear, int invoiceTerm, int invoiceStatus, String machineID) {
        setMerchantID(merchantID);
        setInvoiceYear(invoiceYear);
        setInvoiceTerm(invoiceTerm);
        setInvoiceStatus(invoiceStatus);
        setMachineID(machineID);
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

    public int getInvoiceTerm() {
        return InvoiceTerm;
    }

    public void setInvoiceTerm(int invoiceTerm) {
        InvoiceTerm = invoiceTerm;
    }

    public int getInvoiceStatus() {
        return InvoiceStatus;
    }

    public void setInvoiceStatus(int invoiceStatus) {
        InvoiceStatus = invoiceStatus;
    }

    public String getMachineID() {
        return MachineID;
    }

    public void setMachineID(String machineID) {
        MachineID = machineID;
    }
}
