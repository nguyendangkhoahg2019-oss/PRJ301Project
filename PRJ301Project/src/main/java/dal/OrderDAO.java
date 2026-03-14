package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import model.CartItem;
import model.Order;
import model.OrderDetail;

/**
 * DAO for order and order detail.
 */
public class OrderDAO extends DBContext {

    public List<Order> getOrdersByAccountId(int accountId) {
        List<Order> orders = new ArrayList<Order>();
        String sql = "SELECT o.OrderId, o.OrderCode, o.AccountId, o.CartId, o.OrderTime, o.SubTotal, "
                + "o.DiscountAmount, o.ShippingFee, o.TotalAmount, o.ShippingAddress, o.PaymentMethod, "
                + "o.PaymentStatus, o.StatusOrder, o.VoucherId, v.VoucherCode "
                + "FROM Orders o LEFT JOIN Vouchers v ON o.VoucherId = v.VoucherId "
                + "WHERE o.AccountId = ? ORDER BY o.OrderTime DESC, o.OrderId DESC";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                orders.add(readOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public Order getOrderById(int orderId, int accountId) {
        String sql = "SELECT o.OrderId, o.OrderCode, o.AccountId, o.CartId, o.OrderTime, o.SubTotal, "
                + "o.DiscountAmount, o.ShippingFee, o.TotalAmount, o.ShippingAddress, o.PaymentMethod, "
                + "o.PaymentStatus, o.StatusOrder, o.VoucherId, v.VoucherCode "
                + "FROM Orders o LEFT JOIN Vouchers v ON o.VoucherId = v.VoucherId "
                + "WHERE o.OrderId = ? AND o.AccountId = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Order order = readOrder(rs);
                order.setDetails(getOrderDetails(orderId, conn));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int createOrderFromCart(int accountId, String shippingAddress,
            String paymentMethod, String voucherCode) {

        CartDAO cartDAO = new CartDAO();
        CartItemDAO cartItemDAO = new CartItemDAO();
        int cartId = cartDAO.ensureActiveCart(accountId);
        if (cartId == 0) {
            return 0;
        }

        List<CartItem> items = cartItemDAO.getItemsByCartId(cartId);
        if (items.isEmpty()) {
            return 0;
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            for (int i = 0; i < items.size(); i++) {
                CartItem item = items.get(i);
                if (!hasEnoughStock(conn, item.getProductId(), item.getQuantity())) {
                    conn.rollback();
                    return -1;
                }
            }

            double subTotal = 0;
            for (int i = 0; i < items.size(); i++) {
                subTotal += items.get(i).getTotalPrice();
            }

            double shippingFee = subTotal >= 2000000 ? 0 : 30000;
            Integer voucherId = null;
            double discountAmount = 0;

            if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                VoucherData voucher = getValidVoucher(conn, voucherCode.trim(), subTotal);
                if (voucher != null) {
                    voucherId = voucher.voucherId;
                    if ("Amount".equalsIgnoreCase(voucher.discountType)) {
                        discountAmount = voucher.discountValue;
                    } else if ("Percent".equalsIgnoreCase(voucher.discountType)) {
                        discountAmount = subTotal * voucher.discountValue / 100.0;
                    }
                }
            }

            double totalAmount = subTotal + shippingFee - discountAmount;
            if (totalAmount < 0) {
                totalAmount = 0;
            }

            int orderId = insertOrder(conn, accountId, cartId, subTotal, discountAmount,
                    shippingFee, totalAmount, shippingAddress, paymentMethod, voucherId);
            if (orderId == 0) {
                conn.rollback();
                return 0;
            }

            updateOrderCode(conn, orderId);

            for (int i = 0; i < items.size(); i++) {
                CartItem item = items.get(i);
                insertOrderDetail(conn, orderId, item);
                updateStock(conn, item.getProductId(), item.getQuantity());
            }

            cartDAO.updateCartStatus(cartId, "CHECKOUT");
            deleteCartItems(conn, cartId);
            cartDAO.createActiveCart(accountId);

            if (voucherId != null) {
                decreaseVoucherQuantity(conn, voucherId.intValue());
            }

            conn.commit();
            return orderId;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    private Order readOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("OrderId"));
        order.setOrderCode(rs.getString("OrderCode"));
        order.setAccountId(rs.getInt("AccountId"));
        order.setCartId(rs.getInt("CartId"));
        order.setOrderTime(rs.getTimestamp("OrderTime"));
        order.setSubTotal(rs.getDouble("SubTotal"));
        order.setDiscountAmount(rs.getDouble("DiscountAmount"));
        order.setShippingFee(rs.getDouble("ShippingFee"));
        order.setTotalAmount(rs.getDouble("TotalAmount"));
        order.setShippingAddress(rs.getString("ShippingAddress"));
        order.setPaymentMethod(rs.getString("PaymentMethod"));
        order.setPaymentStatus(rs.getString("PaymentStatus"));
        order.setStatusOrder(rs.getString("StatusOrder"));
        int voucherId = rs.getInt("VoucherId");
        if (!rs.wasNull()) {
            order.setVoucherId(Integer.valueOf(voucherId));
        }
        order.setVoucherCode(rs.getString("VoucherCode"));
        return order;
    }

    private List<OrderDetail> getOrderDetails(int orderId, Connection conn) throws SQLException {
        List<OrderDetail> details = new ArrayList<OrderDetail>();
        String sql = "SELECT OrderDetailId, OrderId, ProductId, ProductName, UnitPrice, Quantity, LineTotal "
                + "FROM OrderDetails WHERE OrderId = ? ORDER BY OrderDetailId";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                OrderDetail detail = new OrderDetail();
                detail.setOrderDetailId(rs.getInt("OrderDetailId"));
                detail.setOrderId(rs.getInt("OrderId"));
                detail.setProductId(rs.getInt("ProductId"));
                detail.setProductName(rs.getString("ProductName"));
                detail.setUnitPrice(rs.getDouble("UnitPrice"));
                detail.setQuantity(rs.getInt("Quantity"));
                detail.setLineTotal(rs.getDouble("LineTotal"));
                details.add(detail);
            }
        }
        return details;
    }

    private boolean hasEnoughStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "SELECT 1 FROM Products WHERE ProductId = ? AND IsDeleted = 0 AND StockQuantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, quantity);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    private VoucherData getValidVoucher(Connection conn, String voucherCode, double subTotal) throws SQLException {
        String sql = "SELECT VoucherId, DiscountType, DiscountValue, MinOrderValue "
                + "FROM Vouchers WHERE VoucherCode = ? AND IsDeleted = 0 AND Quantity > 0 AND ExpiryDate >= SYSDATETIME()";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, voucherCode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                double minOrderValue = rs.getDouble("MinOrderValue");
                if (subTotal >= minOrderValue) {
                    VoucherData data = new VoucherData();
                    data.voucherId = rs.getInt("VoucherId");
                    data.discountType = rs.getString("DiscountType");
                    data.discountValue = rs.getDouble("DiscountValue");
                    return data;
                }
            }
        }
        return null;
    }

    private int insertOrder(Connection conn, int accountId, int cartId, double subTotal,
            double discountAmount, double shippingFee, double totalAmount,
            String shippingAddress, String paymentMethod, Integer voucherId) throws SQLException {

        String sql = "INSERT INTO Orders (OrderCode, AccountId, CartId, OrderTime, SubTotal, DiscountAmount, "
                + "ShippingFee, TotalAmount, ShippingAddress, PaymentMethod, PaymentStatus, StatusOrder, VoucherId) "
                + "VALUES (?, ?, ?, SYSDATETIME(), ?, ?, ?, ?, ?, ?, 'Unpaid', 'Pending', ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "TMP");
            ps.setInt(2, accountId);
            ps.setInt(3, cartId);
            ps.setDouble(4, subTotal);
            ps.setDouble(5, discountAmount);
            ps.setDouble(6, shippingFee);
            ps.setDouble(7, totalAmount);
            ps.setString(8, shippingAddress);
            ps.setString(9, paymentMethod);
            if (voucherId == null) {
                ps.setNull(10, java.sql.Types.INTEGER);
            } else {
                ps.setInt(10, voucherId.intValue());
            }

            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private void updateOrderCode(Connection conn, int orderId) throws SQLException {
        String sql = "UPDATE Orders SET OrderCode = ? WHERE OrderId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String orderCode = String.format("ORD%06d", orderId);
            ps.setString(1, orderCode);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    private void insertOrderDetail(Connection conn, int orderId, CartItem item) throws SQLException {
        String productName = item.getProductName();
        if (productName == null || productName.trim().isEmpty()) {
            productName = getProductName(conn, item.getProductId());
        }

        String sql = "INSERT INTO OrderDetails (OrderId, ProductId, ProductName, UnitPrice, Quantity, LineTotal) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, item.getProductId());
            ps.setString(3, productName);
            ps.setDouble(4, item.getUnitPrice());
            ps.setInt(5, item.getQuantity());
            ps.setDouble(6, item.getTotalPrice());
            ps.executeUpdate();
        }
    }

    private String getProductName(Connection conn, int productId) throws SQLException {
        String sql = "SELECT ProductName FROM Products WHERE ProductId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("ProductName");
            }
        }
        return "Product #" + productId;
    }

    private void updateStock(Connection conn, int productId, int quantity) throws SQLException {
        String sql = "UPDATE Products SET StockQuantity = StockQuantity - ? WHERE ProductId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    private void deleteCartItems(Connection conn, int cartId) throws SQLException {
        String sql = "DELETE FROM CartItems WHERE CartId = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.executeUpdate();
        }
    }

    private void decreaseVoucherQuantity(Connection conn, int voucherId) throws SQLException {
        String sql = "UPDATE Vouchers SET Quantity = Quantity - 1 WHERE VoucherId = ? AND Quantity > 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, voucherId);
            ps.executeUpdate();
        }
    }

    private static class VoucherData {
        int voucherId;
        String discountType;
        double discountValue;
    }
}