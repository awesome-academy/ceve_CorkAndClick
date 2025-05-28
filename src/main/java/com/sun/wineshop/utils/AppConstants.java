package com.sun.wineshop.utils;

public class AppConstants {
    public static final int BCRYPT_STRENGTH = 10;
    public static final String ADMIN = "ADMIN";
    public static final String USER = "USER";
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    // JWT keys
    public static final String JWT_USER_ID = "userId";

    //Pageable
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";

    // Product
    public static  final String EXPORT_PRODUCT_FILE_NAME="attachment; filename=products.xlsx";
    public static  final String IMPORT_TYPE="file";
}
