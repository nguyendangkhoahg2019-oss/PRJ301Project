package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import model.Product;

/**
 * DAO class handling product database operations
 */
public class ProductDAO extends DBContext {

    /**
     * Get product list with pagination
     */
    public ArrayList<Product> getProducts(int page) {

        ArrayList<Product> list = new ArrayList<>();

        int pageSize = 12;
        int start = (page - 1) * pageSize;

        String sql = "SELECT * FROM Product ORDER BY id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try {

            PreparedStatement ps = getConnection().prepareStatement(sql);
            ps.setInt(1, start);
            ps.setInt(2, pageSize);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Product p = new Product();

                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getDouble("price"));
                p.setDescription(rs.getString("description"));
                p.setCategory(rs.getString("category"));

                list.add(p);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Get product detail by id
     */
    public Product getProductById(int id) {

        String sql = "SELECT * FROM Product WHERE id=?";

        try {

            PreparedStatement ps = getConnection().prepareStatement(sql);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Product p = new Product();

                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getDouble("price"));
                p.setDescription(rs.getString("description"));
                p.setCategory(rs.getString("category"));

                return p;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Search product by name or category
     */
    public ArrayList<Product> searchProduct(String keyword) {

        ArrayList<Product> list = new ArrayList<>();

        String sql = "SELECT * FROM Product WHERE name LIKE ? OR category LIKE ?";

        try {

            PreparedStatement ps = getConnection().prepareStatement(sql);

            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Product p = new Product();

                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setPrice(rs.getDouble("price"));

                list.add(p);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

}
