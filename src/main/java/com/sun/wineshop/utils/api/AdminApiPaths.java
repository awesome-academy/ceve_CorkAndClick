package com.sun.wineshop.utils.api;

public class AdminApiPaths {
    public static final String PREFIX = "/v1";
    public static final String BASE = PREFIX + "/admin";
    public static final String BASE_ALL = BASE + "/**";

    public static final class User {
        public static final String ADMIN_USER_CONTROLLER = "adminUserController";
        public static final String BASE = AdminApiPaths.BASE + "/users";
        public static final String BY_ID = "/{id}";
        public static final String SEARCH = "/search";
    }

    public static final class Product {
        public static final String ADMIN_PRODUCT_CONTROLLER = "adminProductController";
        public static final String BASE = AdminApiPaths.BASE + "/products";
        public static final String BY_ID = "/{id}";
        // ...
    }

    public static final class Category {
        public static final String ADMIN_CATEGORY_CONTROLLER = "adminCategoryController";
        public static final String BASE = AdminApiPaths.BASE + "/categories";
        public static final String BY_ID = "/{id}";
        // ...
    }

    public static final class Order {
        public static final String ADMIN_ORDER_CONTROLLER = "adminOrderController";
        public static final String BASE = AdminApiPaths.BASE + "/orders";
        public static final String UPDATE_STATUS = "/{orderId}/status";
        // ...
    }

    // Add more for Order, Comment, Category...

    private AdminApiPaths() {
        // Prevent instantiation
    }
}
