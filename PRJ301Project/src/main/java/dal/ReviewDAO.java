package dal;

import java.sql.PreparedStatement;

/**
 * DAO class for handling product reviews
 */
public class ReviewDAO extends DBContext {

    /**
     * Insert a new review for a product
     */
    public void addReview(int productId, String accountId,
            String message, int rating) {

        String sql = "INSERT INTO Review VALUES(?,?,?,?)";

        try {

            PreparedStatement ps = getConnection().prepareStatement(sql);

            ps.setInt(1, productId);
            ps.setString(2, accountId);
            ps.setString(3, message);
            ps.setInt(4, rating);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
