package model;

import java.sql.Timestamp;

public class Cart {

    private int cartId;
    private int accountId;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Cart() {
    }

    public Cart(int cartId, int accountId, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.cartId = cartId;
        this.accountId = accountId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}