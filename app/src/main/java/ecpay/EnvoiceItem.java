package ecpay;
public class EnvoiceItem {
    private int ItemSeq;
    private String ItemName;
    private int ItemCount;
    private String ItemWord;
    private int ItemPrice;
    private String ItemTaxType;
    private int ItemAmount;
    private String ItemRemark;

    public int getItemSeq() {
        return ItemSeq;
    }

    public void setItemSeq(int itemSeq) {
        ItemSeq = itemSeq;
    }

    public String getItemName() {
        return ItemName;
    }

    public void setItemName(String itemName) {
        ItemName = itemName;
    }

    public int getItemCount() {
        return ItemCount;
    }

    public void setItemCount(int itemCount) {
        ItemCount = itemCount;
    }

    public String getItemWord() {
        return ItemWord;
    }

    public void setItemWord(String itemWord) {
        ItemWord = itemWord;
    }

    public int getItemPrice() {
        return ItemPrice;
    }

    public void setItemPrice(int itemPrice) {
        ItemPrice = itemPrice;
    }

    public String getItemTaxType() {
        return ItemTaxType;
    }

    public void setItemTaxType(String itemTaxType) {
        ItemTaxType = itemTaxType;
    }

    public int getItemAmount() {
        return ItemAmount;
    }

    public void setItemAmount(int itemAmount) {
        ItemAmount = itemAmount;
    }

    public String getItemRemark() {
        return ItemRemark;
    }

    public void setItemRemark(String itemRemark) {
        ItemRemark = itemRemark;
    }


}
