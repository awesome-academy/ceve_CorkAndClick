package com.sun.wineshop.utils.api;

public class AdminApiPaths {
    public static final String BASE = "/admin";
    public static final String BASE_ALL = BASE + "/**";

    public static final class User {
        public static final String BASE = "/users";
        public static final String BY_ID = BASE + "/{id}";
        public static final String SEARCH = BASE + "/search";
    }

    public static final class Product {
        public static final String BASE = "/products";
        public static final String BY_ID = BASE + "/{id}";
        // ...
    }

    // Add more for Order, Comment, Category...

    private AdminApiPaths() {
        // Prevent instantiation
    }
}
