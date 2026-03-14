package service;

import dao.OrderDAO;
import java.util.List;
import model.Order;

/**
 * Basic service for order logic.
 */
public class OrderService {

    private final OrderDAO orderDAO;

    public OrderService() {
        orderDAO = new OrderDAO();
    }

    public int checkout(int accountId, String shippingAddress, String paymentMethod, String voucherCode) {
        return orderDAO.createOrderFromCart(accountId, shippingAddress, paymentMethod, voucherCode);
    }

    public List<Order> getOrdersByAccount(int accountId) {
        return orderDAO.getOrdersByAccountId(accountId);
    }

    public Order getOrderDetail(int orderId, int accountId) {
        return orderDAO.getOrderById(orderId, accountId);
    }
}