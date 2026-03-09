package controller;

import dal.AccountDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import model.Account;

/**
 * AuthServlet Handle login logout register
 */
public class AuthServlet extends HttpServlet {

    /**
     * Handle GET request Used for logout
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action != null && action.equals("logout")) {

            HttpSession session = request.getSession();

            // destroy session
            session.invalidate();

            response.sendRedirect("login.jsp");
        }

    }

    /**
     * Handle POST request Used for login and register
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        AccountDAO dao = new AccountDAO();

        // LOGIN
        if (action.equals("login")) {

            String username = request.getParameter("username");
            String password = request.getParameter("password");

            Account acc = dao.login(username, password);

            if (acc != null) {

                HttpSession session = request.getSession();

                // store user in session
                session.setAttribute("user", acc);

                response.sendRedirect("index.jsp");

            } else {

                request.setAttribute("error", "Invalid username or password");

                request.getRequestDispatcher("login.jsp")
                        .forward(request, response);
            }
        }

        // REGISTER
        if (action.equals("register")) {

            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String fullname = request.getParameter("fullname");
            String email = request.getParameter("email");
            String phone = request.getParameter("phone");

            boolean exist = dao.checkUserExist(username);

            if (exist) {

                request.setAttribute("error", "Username already exists");

                request.getRequestDispatcher("register.jsp")
                        .forward(request, response);

            } else {

                dao.register(username, password, fullname, email, phone);

                response.sendRedirect("login.jsp");
            }

        }

    }

}
