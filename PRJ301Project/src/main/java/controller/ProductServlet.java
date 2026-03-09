package controller;

import dal.ProductDAO;
import dal.ReviewDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.util.ArrayList;
import model.Product;

/**
 * ProductServlet handles product related actions
 */
public class ProductServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        ProductDAO dao = new ProductDAO();

        /**
         * View product list with pagination
         */
        if (action.equals("view-list")) {

            int page = Integer.parseInt(request.getParameter("page"));

            ArrayList<Product> list = dao.getProducts(page);

            request.setAttribute("list", list);

            request.getRequestDispatcher("productList.jsp")
                    .forward(request, response);

        } /**
         * View product detail
         */
        else if (action.equals("view-detail")) {

            int id = Integer.parseInt(request.getParameter("productId"));

            Product p = dao.getProductById(id);

            request.setAttribute("product", p);

            request.getRequestDispatcher("productDetail.jsp")
                    .forward(request, response);

        } /**
         * Search product
         */
        else if (action.equals("view-search")) {

            String keyword = request.getParameter("keyword");

            ArrayList<Product> list = dao.searchProduct(keyword);

            request.setAttribute("list", list);

            request.getRequestDispatcher("productList.jsp")
                    .forward(request, response);

        } /**
         * Add product review (user must login)
         */
        else if (action.equals("view-review")) {

            HttpSession session = request.getSession();

            if (session.getAttribute("user") == null) {

                response.sendRedirect("login.jsp");
                return;

            }

            int productId = Integer.parseInt(request.getParameter("productId"));
            String message = request.getParameter("message");
            int rating = Integer.parseInt(request.getParameter("rating"));

            ReviewDAO rdao = new ReviewDAO();

            rdao.addReview(productId,
                    session.getAttribute("user").toString(),
                    message,
                    rating);

            response.sendRedirect("product?action=view-detail&productId=" + productId);
        }

    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        processRequest(request, response);
    }

}
