package com.byteshaft.doctor.gettersetter;

/**
 * Created by s9iper1 on 4/10/17.
 */

public class Review {

    private int reviewId;
    private String reviewText;
    private float reviewStars;

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public float getReviewStars() {
        return reviewStars;
    }

    public void setReviewStars(float reviewStars) {
        this.reviewStars = reviewStars;
    }
}
