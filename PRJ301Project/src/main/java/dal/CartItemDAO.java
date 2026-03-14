package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.CartItem;

/**
 * DAO for cart items.
 */
public class CartItemDAO extends DBContext {

    public List<CartItem> getItemsByCartId(int cartId) {
        List<CartItem> items = new ArrayList<CartItem>();
        String sql = "SELECT ci.CartItemId, ci.CartId, ci.ProductId, p.ProductName, p.ImageURL, "
                + "ci.UnitPrice, ci.Quantity, ci.TotalPrice, ci.AddedAt "
                + "FROM CartItems ci INNER JOIN Products p ON ci.ProductId = p.ProductId "
                + "WHERE ci.CartId = ? AND p.IsDeleted = 0 ORDER BY ci.CartItemId";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                CartItem item = new CartItem();
                item.setCartItemId(rs.getInt("CartItemId"));
                item.setCartId(rs.getInt("CartId"));
                item.setProductId(rs.getInt("ProductId"));
                item.setProductName(rs.getString("ProductName"));
                item.setImageURL(rs.getString("ImageURL"));
                item.setUnitPrice(rs.getDouble("UnitPrice"));
                item.setQuantity(rs.getInt("Quantity"));
                item.setTotalPrice(rs.getDouble("TotalPrice"));
                item.setAddedAt(rs.getTimestamp("AddedAt"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    public CartItem getCartItem(int cartId, int productId) {
        String sql = "SELECT CartItemId, CartId, ProductId, UnitPrice, Quantity, TotalPrice, AddedAt "
                + "FROM CartItems WHERE CartId = ? AND ProductId = ?";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                CartItem item = new CartItem();
                item.setCartItemId(rs.getInt("CartItemId"));
                item.setCartId(rs.getInt("CartId"));
                item.setProductId(rs.getInt("ProductId"));
                item.setUnitPrice(rs.getDouble("UnitPrice"));
                item.setQuantity(rs.getInt("Quantity"));
                item.setTotalPrice(rs.getDouble("TotalPrice"));
                item.setAddedAt(rs.getTimestamp("AddedAt"));
                return item;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean addItem(int cartId, int productId, double unitPrice, int quantity) {
        String sql = "INSERT INTO CartItems (CartId, ProductId, UnitPrice, Quantity, TotalPrice) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            ps.setDouble(3, unitPrice);
            ps.setInt(4, quantity);
            ps.setDouble(5, unitPrice * quantity);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateItemQuantity(int cartId, int productId, int quantity, double unitPrice) {
        String sql = "UPDATE CartItems SET Quantity = ?, UnitPrice = ?, TotalPrice = ?, AddedAt = SYSDATETIME() "
                + "WHERE CartId = ? AND ProductId = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setDouble(2, unitPrice);
            ps.setDouble(3, quantity * unitPrice);
            ps.setInt(4, cartId);
            ps.setInt(5, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeItem(int cartId, int productId) {
        String sql = "DELETE FROM CartItems WHERE CartId = ? AND ProductId = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public double getCartTotal(int cartId) {
        String sql = "SELECT ISNULL(SUM(TotalPrice), 0) AS Total FROM CartItems WHERE CartId = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("Total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getProductPrice(int productId) {
        String sql = "SELECT Price FROM Products WHERE ProductId = ? AND IsDeleted = 0";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("Price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean isProductAvailable(int productId, int quantity) {
        String sql = "SELECT 1 FROM Products WHERE ProductId = ? AND IsDeleted = 0 AND StockQuantity >= ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setInt(2, quantity);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}