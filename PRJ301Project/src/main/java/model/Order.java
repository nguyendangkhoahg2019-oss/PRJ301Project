package model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {

    private int orderId;
    private String orderCode;
    private int accountId;
    private int cartId;
    private Timestamp orderTime;
    private double subTotal;
    private double discountAmount;
    private double shippingFee;
    private double totalAmount;
    private String shippingAddress;
    private String paymentMethod;
    private String paymentStatus;
    private String statusOrder;
    private Integer voucherId;
    private String voucherCode;
    private List<OrderDetail> details;

    public Order() {
        details = new ArrayList<OrderDetail>();
    }

    public Order(int orderId, String orderCode, int accountId, int cartId, Timestamp orderTime,
            double subTotal, double discountAmount, double shippingFee, double totalAmount,
            String shippingAddress, String paymentMethod, String paymentStatus,
            String statusOrder, Integer voucherId, String voucherCode) {
        this.orderId = orderId;
        this.orderCode = orderCode;
        this.accountId = accountId;
        this.cartId = cartId;
        this.orderTime = orderTime;
        this.subTotal = subTotal;
        this.discountAmount = discountAmount;
        this.shippingFee = shippingFee;
        this.totalAmount = totalAmount;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.statusOrder = statusOrder;
        this.voucherId = voucherId;
        this.voucherCode = voucherCode;
        this.details = new ArrayList<OrderDetail>();
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public Timestamp getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Timestamp orderTime) {
        this.orderTime = orderTime;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(double subTotal) {
        this.subTotal = subTotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getStatusOrder() {
        return statusOrder;
    }

    public void setStatusOrder(String statusOrder) {
        this.statusOrder = statusOrder;
    }

    public Integer getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(Integer voucherId) {
        this.voucherId = voucherId;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public List<OrderDetail> getDetails() {
        return details;
    }

    public void setDetails(List<OrderDetail> details) {
        this.details = details;
    }
}