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
import model.Order;
import service.CartService;
import service.OrderService;

/**
 * Order controller for Tuan Anh task.
 */
@WebServlet(name = "OrderServlet", urlPatterns = {"/order"})
public class OrderServlet extends HttpServlet {

    private final CartService cartService = new CartService();
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String view = request.getParameter("view");
        if (view == null || view.trim().isEmpty()) {
            view = "list";
        }

        if ("checkout".equalsIgnoreCase(view)) {
            showCheckout(request, response);
            return;
        }

        if ("detail".equalsIgnoreCase(view)) {
            showOrderDetail(request, response);
            return;
        }

        showOrderList(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if ("checkout".equalsIgnoreCase(action)) {
            handleCheckout(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/order?view=list");
    }

    private void showCheckout(HttpServletRequest request, HttpServletResponse response)
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
        request.getRequestDispatcher("/WEB-INF/view/checkout.jsp").forward(request, response);
    }

    private void showOrderList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Account user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        List<Order> orders = orderService.getOrdersByAccount(user.getAccountId());
        request.setAttribute("orders", orders);
        request.getRequestDispatcher("/WEB-INF/view/orderList.jsp").forward(request, response);
    }

    private void showOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Account user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        int orderId = getIntParameter(request, "OrderId", "orderId");
        if (orderId <= 0) {
            request.getSession().setAttribute("orderError", "OrderId không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/order?view=list");
            return;
        }

        Order order = orderService.getOrderDetail(orderId, user.getAccountId());
        if (order == null) {
            request.getSession().setAttribute("orderError", "Không tìm thấy đơn hàng.");
            response.sendRedirect(request.getContextPath() + "/order?view=list");
            return;
        }

        request.setAttribute("order", order);
        request.getRequestDispatcher("/WEB-INF/view/order.jsp").forward(request, response);
    }

    private void handleCheckout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Account user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String shippingAddress = trim(request.getParameter("shippingAddress"));
        String paymentMethod = trim(request.getParameter("paymentMethod"));
        String voucherCode = trim(request.getParameter("voucherCode"));

        if (shippingAddress.isEmpty()) {
            request.getSession().setAttribute("orderError", "Vui lòng nhập địa chỉ giao hàng.");
            response.sendRedirect(request.getContextPath() + "/order?view=checkout");
            return;
        }

        if (paymentMethod.isEmpty()) {
            request.getSession().setAttribute("orderError", "Vui lòng chọn phương thức thanh toán.");
            response.sendRedirect(request.getContextPath() + "/order?view=checkout");
            return;
        }

        int orderId = orderService.checkout(user.getAccountId(), shippingAddress, paymentMethod, voucherCode);
        if (orderId > 0) {
            request.getSession().setAttribute("orderSuccess", "Đặt hàng thành công.");
            response.sendRedirect(request.getContextPath() + "/order?view=detail&OrderId=" + orderId);
            return;
        }

        if (orderId == -1) {
            request.getSession().setAttribute("orderError", "Tồn kho không đủ để checkout.");
        } else {
            request.getSession().setAttribute("orderError", "Không thể tạo đơn hàng. Giỏ hàng có thể đang trống.");
        }
        response.sendRedirect(request.getContextPath() + "/order?view=checkout");
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

    private String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}