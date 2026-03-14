package dao;

import db.DBContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.Cart;

/**
 * DAO for cart table.
 */
public class CartDAO extends DBContext {

    public Cart getActiveCartByAccountId(int accountId) {
        String sql = "SELECT TOP 1 CartId, AccountId, Status, CreatedAt, UpdatedAt "
                + "FROM Carts WHERE AccountId = ? AND Status = 'ACTIVE' ORDER BY CartId DESC";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Cart cart = new Cart();
                cart.setCartId(rs.getInt("CartId"));
                cart.setAccountId(rs.getInt("AccountId"));
                cart.setStatus(rs.getString("Status"));
                cart.setCreatedAt(rs.getTimestamp("CreatedAt"));
                cart.setUpdatedAt(rs.getTimestamp("UpdatedAt"));
                return cart;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int createActiveCart(int accountId) {
        String sql = "INSERT INTO Carts (AccountId, Status) VALUES (?, 'ACTIVE')";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, accountId);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int ensureActiveCart(int accountId) {
        Cart cart = getActiveCartByAccountId(accountId);
        if (cart != null) {
            return cart.getCartId();
        }
        return createActiveCart(accountId);
    }

    public boolean updateCartStatus(int cartId, String status) {
        String sql = "UPDATE Carts SET Status = ?, UpdatedAt = SYSDATETIME() WHERE CartId = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, cartId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}