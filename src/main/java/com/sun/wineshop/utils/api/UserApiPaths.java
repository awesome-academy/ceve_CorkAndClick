package com.sun.wineshop.utils.api;

public class UserApiPaths {
    public static final String PREFIX = "/api/v1";
    public static final String BASE = PREFIX + "/users";

    public static class Endpoint {
        public static final String REGISTER = "/register";
        public static final String INFO = "/info";
        public static final String FULL_INFO = BASE +  INFO;
        public static final String FULL_REGISTER = BASE + REGISTER;
    }

    public static class Chat {
        public static final String BASE = UserApiPaths.BASE + "/chat";
        public static final String HISTORY = "/history";
        public static final String WEBSOCKET_ENDPOINT = "/ws/chat";
        public static final String WEBSOCKET_BROKER = "/topic/messages/";
        public static final String SEND = "/chat.send.user";
    }
}
