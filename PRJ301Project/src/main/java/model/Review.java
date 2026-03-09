package model;

/**
 * Review model representing product review
 */
public class Review {

    private int id;
    private int productId;
    private String accountId;
    private String message;
    private int rating;

    public Review() {
    }

    public Review(int id, int productId, String accountId, String message, int rating) {
        this.id = id;
        this.productId = productId;
        this.accountId = accountId;
        this.message = message;
        this.rating = rating;
    }

}
