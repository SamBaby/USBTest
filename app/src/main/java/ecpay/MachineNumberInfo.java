package ecpay;

public class MachineNumberInfo {
    private String InvoiceHeader;
    private String InvoiceStart;
    private String InvoiceEnd;
    private int Times;

    public String getInvoiceHeader() {
        return InvoiceHeader;
    }

    public void setInvoiceHeader(String invoiceHeader) {
        InvoiceHeader = invoiceHeader;
    }

    public String getInvoiceStart() {
        return InvoiceStart;
    }

    public void setInvoiceStart(String invoiceStart) {
        InvoiceStart = invoiceStart;
    }

    public String getInvoiceEnd() {
        return InvoiceEnd;
    }

    public void setInvoiceEnd(String invoiceEnd) {
        InvoiceEnd = invoiceEnd;
    }

    public int getTimes() {
        return Times;
    }

    public void setTimes(int times) {
        Times = times;
    }
}
