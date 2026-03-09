package dal;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.Account;


/**
 * Data Access Object for Account Handle all database operations related to
 * Account.
 */
public class AccountDAO extends DBContext {

    /**
     * Authenticate user using username and password
     */
    public Account login(String username, String password) {

        String sql = "SELECT * FROM Account WHERE username=? AND password=?";

        try {

            PreparedStatement ps = getConnection().prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Account acc = new Account();

                acc.setUsername(rs.getString("username"));
                acc.setFullname(rs.getString("fullname"));
                acc.setEmail(rs.getString("email"));
                acc.setPhone(rs.getString("phone"));

                return acc;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Check if username already exists in database
     */
    public boolean checkUserExist(String username) {

        String sql = "SELECT * FROM Account WHERE username=?";

        try {

            PreparedStatement ps = getConnection().prepareStatement(sql);
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Insert new account into database
     */
    public void register(String username, String password,
            String fullname, String email, String phone) {

        String sql = "INSERT INTO Account VALUES (?,?,?,?,?)";

        try {

            PreparedStatement ps = getConnection().prepareStatement(sql);

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullname);
            ps.setString(4, email);
            ps.setString(5, phone);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
