package ecpay;

public class InvoiceDataOffline {
    private String MerchantID = "";
    private String MachineID = "";
    private String InvoiceNo = "";
    private String InvoiceDate = "";
    private String RelateNumber = "";
    private String CustomerID = "";
    private String CustomerIdentifier = "";
    private String CustomerName = "";
    private String CustomerAddr = "";
    private String CustomerPhone = "";
    private String CustomerEmail = "";
    private String ClearanceMark = "";
    private String LoveCode = "";
    private String Print;
    private String Donation;
    private String CarrierType;
    private String CarrierNum;
    private String TaxType;
    private int SalesAmount;
    private String InvType;
    private String vat;
    private String InvoiceRemark;
    private String SpecialTaxType;
    private String RandomNumber;
    private EnvoiceItem[] Items;

    public void setMerchantID(String merchantID) {
        this.MerchantID = merchantID;
    }

    public String getMerchantID() {
        return this.MerchantID;
    }

    public void setRelateNumber(String relateNumber) {
        this.RelateNumber = relateNumber;
    }

    public String getRelateNumber() {
        return RelateNumber;
    }

    public String getCustomerIdentifier() {
        return CustomerIdentifier;
    }

    public void setCustomerIdentifier(String customerIdentifier) {
        CustomerIdentifier = customerIdentifier;
    }

    public String getPrint() {
        return Print;
    }

    public void setPrint(String print) {
        this.Print = print;
    }

    public String getDonation() {
        return Donation;
    }

    public void setDonation(String donation) {
        Donation = donation;
    }

    public String getCarrierType() {
        return CarrierType;
    }

    public void setCarrierType(String carrierType) {
        CarrierType = carrierType;
    }

    public String getCarrierNum() {
        return CarrierNum;
    }

    public void setCarrierNum(String carrierNum) {
        CarrierNum = carrierNum;
    }

    public String getTaxType() {
        return TaxType;
    }

    public void setTaxType(String taxType) {
        TaxType = taxType;
    }

    public int getSalesAmount() {
        return SalesAmount;
    }

    public void setSalesAmount(int salesAmount) {
        SalesAmount = salesAmount;
    }

    public String getInvType() {
        return InvType;
    }

    public void setInvType(String invType) {
        InvType = invType;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public String getInvoiceRemark() {
        return InvoiceRemark;
    }

    public void setInvoiceRemark(String invoiceRemark) {
        InvoiceRemark = invoiceRemark;
    }

    public String getCustomerID() {
        return CustomerID;
    }

    public void setCustomerID(String customerID) {
        CustomerID = customerID;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public void setCustomerName(String customerName) {
        CustomerName = customerName;
    }

    public String getCustomerAddr() {
        return CustomerAddr;
    }

    public void setCustomerAddr(String customerAddr) {
        CustomerAddr = customerAddr;
    }

    public String getCustomerPhone() {
        return CustomerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        CustomerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return CustomerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        CustomerEmail = customerEmail;
    }

    public String getClearanceMark() {
        return ClearanceMark;
    }

    public void setClearanceMark(String clearanceMark) {
        ClearanceMark = clearanceMark;
    }

    public String getLoveCode() {
        return LoveCode;
    }

    public void setLoveCode(String loveCode) {
        LoveCode = loveCode;
    }

    public String getSpecialTaxType() {
        return SpecialTaxType;
    }

    public void setSpecialTaxType(String specialTaxType) {
        SpecialTaxType = specialTaxType;
    }

    public EnvoiceItem[] getItems() {
        return Items;
    }

    public void setItems(EnvoiceItem[] items) {
        Items = items;
    }

    public String getMachineID() {
        return MachineID;
    }

    public void setMachineID(String machineID) {
        MachineID = machineID;
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

    public String getRandomNumber() {
        return RandomNumber;
    }

    public void setRandomNumber(String randomNumber) {
        RandomNumber = randomNumber;
    }
}
