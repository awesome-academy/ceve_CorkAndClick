package com.sun.wineshop.model.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN(1),
    USER(2),
    MANAGER(3);

    private final int value;

    UserRole(int value) {
        this.value = value;
    }
}
