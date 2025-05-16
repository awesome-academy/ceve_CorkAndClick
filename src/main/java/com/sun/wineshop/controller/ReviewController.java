package com.sun.wineshop.controller;

import com.sun.wineshop.dto.request.ReviewRequest;
import com.sun.wineshop.dto.response.BaseApiResponse;
import com.sun.wineshop.service.ReviewService;
import com.sun.wineshop.utils.JwtUtil;
import com.sun.wineshop.utils.MessageUtil;
import com.sun.wineshop.utils.api.ProductApiPaths;
import com.sun.wineshop.utils.api.ReviewApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ReviewApiPaths.BASE)
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final MessageUtil messageUtil;

    @PostMapping(ReviewApiPaths.Endpoint.ADD)
    public ResponseEntity<BaseApiResponse<Void>> addReview(
            @RequestBody ReviewRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = JwtUtil.extractUserIdFromJwt(jwt);
        reviewService.addReview(userId, request);
        return ResponseEntity.ok(new BaseApiResponse<>(
                HttpStatus.OK.value(),
                messageUtil.getMessage("review.add.success")
        ));
    }
}
