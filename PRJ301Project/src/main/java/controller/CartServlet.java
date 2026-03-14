package controller;

import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.Cart;
import model.CartItem;
import service.CartService;

/**
 * Cart controller for Tuan Anh task.
 */
@WebServlet(name = "CartServlet", urlPatterns = {"/cart"})
public class CartServlet extends HttpServlet {

    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty() || "view".equalsIgnoreCase(action)) {
            showCart(request, response);
            return;
        }

        if ("remove".equalsIgnoreCase(action)) {
            handleRemove(request, response);
            return;
        }

        if ("update".equalsIgnoreCase(action)) {
            handleUpdate(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/cart?action=view");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("add".equalsIgnoreCase(action)) {
            handleAdd(request, response);
            return;
        }

        if ("remove".equalsIgnoreCase(action)) {
            handleRemove(request, response);
            return;
        }

        if ("update".equalsIgnoreCase(action)) {
            handleUpdate(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/cart?action=view");
    }

    private void showCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Account user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        Cart cart = cartService.getActiveCart(user.getAccountId());
        List<CartItem> items = cartService.getCartItems(user.getAccountId());
        request.setAttribute("cart", cart);
        request.setAttribute("items", items);
        request.setAttribute("total", cartService.getCartTotal(user.getAccountId()));
        request.getRequestDispatcher("/WEB-INF/view/cart.jsp").forward(request, response);
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Account user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int productId = getIntParameter(request, "ProductId", "productId");
        int quantity = getIntParameter(request, "Quantity", "quantity");

        if (productId <= 0 || quantity <= 0) {
            request.getSession().setAttribute("cartError", "ProductId hoặc Quantity không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/cart?action=view");
            return;
        }

        boolean success = cartService.addToCart(user.getAccountId(), productId, quantity);
        if (success) {
            request.getSession().setAttribute("cartSuccess", "Đã thêm sản phẩm vào giỏ hàng.");
        } else {
            request.getSession().setAttribute("cartError", "Không thể thêm sản phẩm. Hãy kiểm tra tồn kho.");
        }
        response.sendRedirect(request.getContextPath() + "/cart?action=view");
    }

    private void handleRemove(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Account user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int productId = getIntParameter(request, "ProductId", "productId");
        if (productId <= 0) {
            request.getSession().setAttribute("cartError", "ProductId không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/cart?action=view");
            return;
        }

        boolean success = cartService.removeItem(user.getAccountId(), productId);
        if (success) {
            request.getSession().setAttribute("cartSuccess", "Đã xóa sản phẩm khỏi giỏ hàng.");
        } else {
            request.getSession().setAttribute("cartError", "Không thể xóa sản phẩm khỏi giỏ hàng.");
        }
        response.sendRedirect(request.getContextPath() + "/cart?action=view");
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Account user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int productId = getIntParameter(request, "ProductId", "productId");
        int quantity = getIntParameter(request, "Quantity", "quantity");

        if (productId <= 0) {
            request.getSession().setAttribute("cartError", "ProductId không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/cart?action=view");
            return;
        }

        boolean success = cartService.updateQuantity(user.getAccountId(), productId, quantity);
        if (success) {
            request.getSession().setAttribute("cartSuccess", "Đã cập nhật giỏ hàng.");
        } else {
            request.getSession().setAttribute("cartError", "Không thể cập nhật số lượng. Hãy kiểm tra tồn kho.");
        }
        response.sendRedirect(request.getContextPath() + "/cart?action=view");
    }

    private Account getLoggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("user");
        if (user instanceof Account) {
            return (Account) user;
        }
        return null;
    }

    private int getIntParameter(HttpServletRequest request, String primaryName, String secondaryName) {
        String raw = request.getParameter(primaryName);
        if (raw == null || raw.trim().isEmpty()) {
            raw = request.getParameter(secondaryName);
        }
        try {
            return Integer.parseInt(raw);
        } catch (Exception e) {
            return 0;
        }
    }
}