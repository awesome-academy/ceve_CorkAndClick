package com.sun.wineshop.utils.api;

public class UserApiPaths {
    public static final String PREFIX = "/api/v1";
    public static final String BASE = PREFIX + "/users";

    public static class Endpoint {
        public static final String REGISTER = "/register";
        public static final String INFO = "/info";
        public static final String FULL_REGISTER = BASE + REGISTER;
    }
}
