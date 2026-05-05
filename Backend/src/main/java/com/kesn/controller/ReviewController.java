package com.kesn.controller;

import com.kesn.entity.Review;
import com.kesn.repository.ReviewRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
public class ReviewController {

    private final ReviewRepository reviewRepository;

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /** Lấy tất cả review của sản phẩm (mới nhất lên đầu) */
    @GetMapping
    public ResponseEntity<List<Review>> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    /** Gửi review mới */
    @PostMapping
    public ResponseEntity<?> addReview(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body
    ) {
        String userName = (String) body.get("userName");
        Object ratingObj = body.get("rating");
        String comment = (String) body.get("comment");

        if (userName == null || userName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Tên người dùng không được trống"));
        }
        if (ratingObj == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Điểm đánh giá không được trống"));
        }
        if (comment == null || comment.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Bình luận không được trống"));
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingObj.toString());
            if (rating < 1 || rating > 5) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Điểm đánh giá phải từ 1 đến 5"));
        }

        Review review = new Review();
        review.setProductId(productId);
        review.setUserName(userName.trim());
        review.setRating(rating);
        review.setComment(comment.trim());
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        return ResponseEntity.ok(saved);
    }
}
