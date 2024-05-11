package ecpay;

public class DistributedNumberInfo {
    private int InvoiceTerm;
    private String InvType;
    private String InvoiceHeader;
    private String InvoiceStart;
    private String InvoiceEnd;
    private int Number;

    public DistributedNumberInfo(int invoiceTerm, String invType, String invoiceHeader, String invoiceStart, String invoiceEnd, int number) {
        setInvoiceTerm(invoiceTerm);
        setInvType(invType);
        setInvoiceHeader(invoiceHeader);
        setInvoiceStart(invoiceStart);
        setInvoiceEnd(invoiceEnd);
        setNumber(number);
    }

    public int getInvoiceTerm() {
        return InvoiceTerm;
    }

    public void setInvoiceTerm(int invoiceTerm) {
        InvoiceTerm = invoiceTerm;
    }

    public String getInvType() {
        return InvType;
    }

    public void setInvType(String invType) {
        InvType = invType;
    }

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

    public int getNumber() {
        return Number;
    }

    public void setNumber(int number) {
        Number = number;
    }
}
