package service;

import dao.CartDAO;
import dao.CartItemDAO;
import java.util.List;
import model.Cart;
import model.CartItem;

/**
 * Basic service for cart logic.
 */
public class CartService {

    private final CartDAO cartDAO;
    private final CartItemDAO cartItemDAO;

    public CartService() {
        cartDAO = new CartDAO();
        cartItemDAO = new CartItemDAO();
    }

    public Cart getActiveCart(int accountId) {
        return cartDAO.getActiveCartByAccountId(accountId);
    }

    public List<CartItem> getCartItems(int accountId) {
        Cart cart = cartDAO.getActiveCartByAccountId(accountId);
        if (cart == null) {
            return new java.util.ArrayList<CartItem>();
        }
        return cartItemDAO.getItemsByCartId(cart.getCartId());
    }

    public boolean addToCart(int accountId, int productId, int quantity) {
        if (quantity <= 0 || !cartItemDAO.isProductAvailable(productId, quantity)) {
            return false;
        }

        int cartId = cartDAO.ensureActiveCart(accountId);
        if (cartId == 0) {
            return false;
        }

        double price = cartItemDAO.getProductPrice(productId);
        if (price < 0) {
            return false;
        }

        CartItem existingItem = cartItemDAO.getCartItem(cartId, productId);
        if (existingItem == null) {
            return cartItemDAO.addItem(cartId, productId, price, quantity);
        }

        int newQuantity = existingItem.getQuantity() + quantity;
        if (!cartItemDAO.isProductAvailable(productId, newQuantity)) {
            return false;
        }
        return cartItemDAO.updateItemQuantity(cartId, productId, newQuantity, price);
    }

    public boolean updateQuantity(int accountId, int productId, int quantity) {
        Cart cart = cartDAO.getActiveCartByAccountId(accountId);
        if (cart == null) {
            return false;
        }
        if (quantity <= 0) {
            return cartItemDAO.removeItem(cart.getCartId(), productId);
        }
        if (!cartItemDAO.isProductAvailable(productId, quantity)) {
            return false;
        }
        double price = cartItemDAO.getProductPrice(productId);
        return cartItemDAO.updateItemQuantity(cart.getCartId(), productId, quantity, price);
    }

    public boolean removeItem(int accountId, int productId) {
        Cart cart = cartDAO.getActiveCartByAccountId(accountId);
        if (cart == null) {
            return false;
        }
        return cartItemDAO.removeItem(cart.getCartId(), productId);
    }

    public double getCartTotal(int accountId) {
        Cart cart = cartDAO.getActiveCartByAccountId(accountId);
        if (cart == null) {
            return 0;
        }
        return cartItemDAO.getCartTotal(cart.getCartId());
    }
}