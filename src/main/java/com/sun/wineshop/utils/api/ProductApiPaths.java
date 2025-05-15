package com.sun.wineshop.utils.api;

public class ProductApiPaths {
    public static final String BASE = "/api/v1/products";
    public static final String BASE_ALL = BASE + "/**";

    public static class Endpoint {
        public static final String SEARCH = "/search";
        public static final String REVIEW = "/{productId}/reviews";
    }
}
