package io.eddie.step3.dto;

public final class Transaction {

    /**
     * 가맹점
     */
    private final String merchant;

    /**
     * 금액(원화(KRW)
     */
    private final int amount;

    /**
     * 분류(예> 식비, 교통, 쇼핑, 기타)
     */
    private final String category;


    public Transaction(String merchant, int amount, String category) {
        this.merchant = merchant;
        this.amount = amount;
        this.category = category;
    }

    public String getMerchant() {
        return merchant;
    }

    public int getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

}
